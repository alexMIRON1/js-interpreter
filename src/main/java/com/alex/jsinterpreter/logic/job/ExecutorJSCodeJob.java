package com.alex.jsinterpreter.logic.job;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.logic.collector.JSCodeCollectionCollector;
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
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final ScheduledExecutorService threadPoolExecutor;

    public ExecutorJSCodeJob(JSCodeService jsCodeService) {
        this.jsCodeService = jsCodeService;
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }


    /**
     * using for scheduling js code job
     *
     * @param jsCode js code for scheduling
     */
    public void scheduleJSCodeJobById(JSCode jsCode) {
        ScheduledFuture<?> scheduledJob = threadPoolExecutor.schedule(() -> executeJSCode(jsCode), Duration
                .between(Instant.now(), jsCode.getScheduledTime()).toSeconds(), TimeUnit.SECONDS);
        scheduledJobs.put(jsCode.getJsCodeId(), scheduledJob);
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
            log.warn("scheduled job does not exist by this id -> {}", jsCodeId);
            throw new NoSuchElementException("This job does not exist by this id " + jsCodeId);
        }
        scheduledJob.cancel(true);
        scheduledJobs.remove(jsCodeId);
        jsCodeService.updateStatus(jsCodeService.getById(jsCodeId), JSCodeStatus.STOPPED);
        log.info("Scheduled job was stopped");
    }

    /**
     * method using for execution js code
     *
     * @param jsCode js code for execution
     */
    public void executeJSCode(JSCode jsCode) {
        String script = jsCode.getScriptBody();
        String js = "js";
        String consoleMember = "console";
        String logMember = "log";
        long startExecution = 0L;
        List<String> scriptResults = new ArrayList<>();
        try (Context context = Context.newBuilder(js).build()) {
            jsCodeService.updateStatus(jsCode, JSCodeStatus.EXECUTING);
            // collecting all output to collection
            context.getBindings(js).getMember(consoleMember).putMember(logMember,
                    new JSCodeCollectionCollector(scriptResults));
            // executing js script
            startExecution = System.currentTimeMillis();
            context.eval(js, script);
            // setting execution time
            jsCodeService.updateExecutionTime(jsCode,
                    System.currentTimeMillis() - startExecution);
            jsCodeService.updateScriptResult(jsCode, scriptResults);
            if (checkScriptResults(scriptResults)) {
                jsCodeService.updateStatus(jsCode, JSCodeStatus.COMPLETED);
            } else {
                jsCodeService.updateStatus(jsCode, JSCodeStatus.FAILED);
            }
            log.info("JavaScriptCode was executed, get result -> {} ", scriptResults);
        } catch (PolyglotException e) {
            jsCodeService.updateExecutionTime(jsCode,
                    System.currentTimeMillis() - startExecution);
            scriptResults.add(e.getMessage());
            jsCodeService.updateScriptResult(jsCode, scriptResults);
            log.warn("java script code produce error -> {}", e.getMessage());
            jsCodeService.updateStatus(jsCode, JSCodeStatus.FAILED);
        }
    }

    private boolean checkScriptResults(List<String> scriptResults) {
        return !scriptResults.contains("Infinity");
    }
}
