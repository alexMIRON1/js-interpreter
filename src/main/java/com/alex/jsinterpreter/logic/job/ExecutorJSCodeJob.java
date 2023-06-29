package com.alex.jsinterpreter.logic.job;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.logic.JSMember;
import com.alex.jsinterpreter.logic.handler.JSCodeResultHandler;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
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
    private final JSCodeResultHandler jsCodeResultHandler;

    public ExecutorJSCodeJob(JSCodeService jsCodeService, JSCodeResultHandler jsCodeResultHandler) {
        this.jsCodeService = jsCodeService;
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        this.jsCodeResultHandler = jsCodeResultHandler;
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
        // clear all previous script results
        jsCodeResultHandler.clearAllResults();
        long startExecution = 0L;
        try (Context context = Context.newBuilder(JSMember.JS.getValue()).build()) {
            startExecution = System.currentTimeMillis();
            jsCodeResultHandler.handleOutputExecutingAndUpdateJSCode(context,jsCode);
            jsCodeService.updateExecutionTime(jsCode, System.currentTimeMillis() - startExecution);
            log.info("JavaScriptCode was executed, get result -> {} ", jsCode.getScriptResults());
        } catch (PolyglotException e) {
            jsCodeService.updateExecutionTime(jsCode,
                    System.currentTimeMillis() - startExecution);
            jsCodeResultHandler.handleExceptionsAndUpdateJSCode(e,jsCode);
        }
    }
}
