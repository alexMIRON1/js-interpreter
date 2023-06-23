package com.alex.jsinterpreter.logic.job;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.mapper.JSCodeMapper;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionJSCodeJob {
    private final JSCodeService jsCodeService;
    private final JSCodeMapper jsCodeMapper;
    private Map<Long, ScheduledFuture<?>> scheduledJobs;
    private JSCode jsCode;

    public void doJob() {
        String script = jsCode.getScriptBody();
        String js = "js";
        Long jsCodeId = jsCode.getJsCodeId();
        try (Context context = Context.newBuilder(js).build()) {
            jsCodeService.updateStatus(jsCodeId, JSCodeStatus.EXECUTING);
            Value result = context.eval(js, script);
            jsCodeService.updateScriptResult(jsCodeId, result.asString());
            log.info("JavaScriptCode was executed, get result -> {} ", result.asString());
        } catch (PolyglotException e) {
            jsCodeService.updateScriptResult(jsCodeId, e.getMessage());
            log.warn("java script code produce error -> {}", e.getMessage());
            jsCodeService.updateStatus(jsCodeId, JSCodeStatus.FAILED);
        }
        jsCodeService.updateStatus(jsCodeId, JSCodeStatus.COMPLETED);
    }

    public void scheduleJSCodeJobById(Long jsCodeId) {
        scheduledJobs = new ConcurrentHashMap<>();
        try (ScheduledExecutorService threadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime()
                .availableProcessors())) {
            jsCode = jsCodeMapper.detailedResponseMapToDocument(jsCodeService.getById(jsCodeId));
            ScheduledFuture<?> scheduledJob = threadPoolExecutor.schedule(this::doJob, Duration
                    .between(Instant.now(), jsCode.getScheduledTime()).toMillis(), TimeUnit.MILLISECONDS);
            scheduledJobs.put(jsCodeId, scheduledJob);
            log.info("Js code was planned");
        }
    }

    public void stopJSCodeJobById(Long jsCodeId) {
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
}
