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

/**
 * class using responsible for managing js code jobs
 *
 * @author Oleksandr Myronenko
 */
@Slf4j
@Component
public class ExecutorJSCodeJob {
    private final JSCodeService jsCodeService;
    private final JSCodeMapper jsCodeMapper;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final ScheduledExecutorService threadPoolExecutor;

    public ExecutorJSCodeJob(JSCodeService jsCodeService, JSCodeMapper jsCodeMapper) {
        this.jsCodeService = jsCodeService;
        this.jsCodeMapper = jsCodeMapper;
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }


    /**
     * using for scheduling js code job
     *
     * @param jsCodeId js code id for scheduling
     */
    public void scheduleJSCodeJobById(String jsCodeId) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(jsCodeService.getById(jsCodeId));
        ScheduledFuture<?> scheduledJob = threadPoolExecutor.schedule(() -> doJob(jsCode), Duration
                .between(Instant.now(), jsCode.getScheduledTime()).toSeconds(), TimeUnit.SECONDS);
        scheduledJobs.put(jsCodeId, scheduledJob);
        log.info("Js code was planned");

    }

    /**
     * using for stopping js code job by id
     *
     * @param jsCodeId js code id
     */
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

    private void doJob(JSCode jsCode) {
        long startExecution = System.currentTimeMillis();
        executeJSCode(jsCode);
        jsCodeService.updateExecutionTime(jsCode.getJsCodeId(), System.currentTimeMillis() - startExecution);
    }

    private void executeJSCode(JSCode jsCode) {
        String script = jsCode.getScriptBody();
        String jsCodeId = jsCode.getJsCodeId();
        String js = "js";
        String consoleMember = "console";
        String logMember = "log";
        List<String> scriptResults = new ArrayList<>();
        try (Context context = Context.newBuilder(js).build()) {
            jsCodeService.updateStatus(jsCodeId, JSCodeStatus.EXECUTING);
            // collecting all output to collection
            context.getBindings(js).getMember(consoleMember).putMember(logMember,
                    new JSCodeCollector(scriptResults));
            // executing js script
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
