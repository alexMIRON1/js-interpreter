package com.alex.jsinterpreter.logic.job;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.mapper.JSCodeMapper;
import com.alex.jsinterpreter.logic.collector.JSCodeCollector;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ExecutorJSCodeJob {
    private final JSCodeService jsCodeService;
    private final JSCodeMapper jsCodeMapper;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private JSCode jsCode;

    public ExecutorJSCodeJob(JSCodeService jsCodeService, JSCodeMapper jsCodeMapper) {
        this.jsCodeService = jsCodeService;
        this.jsCodeMapper = jsCodeMapper;
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public void doJob() {
        String jsCodeId = jsCode.getJsCodeId();
        long startExecution = System.currentTimeMillis();
        executeJSCodeById(jsCodeId);
        jsCodeService.updateExecutionTime(jsCodeId, System.currentTimeMillis() - startExecution);
    }

    public void scheduleJSCodeJobById(String jsCodeId) {
        jsCode = jsCodeMapper.detailedResponseMapToDocument(jsCodeService.getById(jsCodeId));
        if (jsCode.getScheduledTime() == null) {
            doJob();
            log.info("js code going to be executed");
        } else {
            ScheduledExecutorService threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime()
                    .availableProcessors());
            ScheduledFuture<?> scheduledJob = threadPoolExecutor.schedule(this::doJob, Duration
                    .between(Instant.now(), jsCode.getScheduledTime()).toSeconds(), TimeUnit.SECONDS);
            scheduledJobs.put(jsCodeId, scheduledJob);
            log.info("Js code was planned");
        }
    }

    public void stopJSCodeJobById(String jsCodeId) {
        ScheduledFuture<?> scheduledJob = scheduledJobs.get(jsCodeId);
        if (scheduledJob == null) {
            log.warn("scheduled job is null");
            throw new NoSuchElementException("This job does not exist by this id " + jsCodeId);
        }
        scheduledJob.cancel(true);
        scheduledJobs.remove(jsCodeId);
        jsCodeService.updateStatus(jsCodeId, JSCodeStatus.STOPPED);
        log.info("Scheduled job was stopped");
    }

    private void executeJSCodeById(String jsCodeId) {
        String script = jsCode.getScriptBody();
        String js = "js";
        String consoleMember = "console";
        String logMember = "log";
        List<String> scriptResults = new ArrayList<>();
        try (Context context = Context.newBuilder(js).build()) {
            jsCodeService.updateStatus(jsCodeId, JSCodeStatus.EXECUTING);
            context.getBindings(js).getMember(consoleMember).putMember(logMember,
                    new JSCodeCollector(scriptResults));
            context.eval(js, script);
            jsCodeService.updateScriptResult(jsCodeId, scriptResults);
            if (checkScriptResults(scriptResults)) {
                jsCodeService.updateStatus(jsCodeId, JSCodeStatus.COMPLETED);
            } else {
                jsCodeService.updateStatus(jsCodeId, JSCodeStatus.FAILED);
            }
            log.info("JavaScriptCode was executed, get result -> {} ", scriptResults);
        } catch (PolyglotException e) {
            scriptResults.add(e.getMessage());
            jsCodeService.updateScriptResult(jsCodeId, scriptResults);
            log.warn("java script code produce error -> {}", e.getMessage());
            jsCodeService.updateStatus(jsCodeId, JSCodeStatus.FAILED);
        }
    }

    private boolean checkScriptResults(List<String> scriptResults) {
        return !scriptResults.contains("Infinity");
    }
}
