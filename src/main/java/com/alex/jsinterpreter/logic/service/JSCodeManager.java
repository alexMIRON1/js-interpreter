package com.alex.jsinterpreter.logic.service;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.mapper.JSCodeMapper;
import com.alex.jsinterpreter.logic.job.ExecutorJSCodeJob;
import com.alex.jsinterpreter.repository.JSCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
@Slf4j
@Service
public record JSCodeManager(JSCodeRepository jsCodeRepository, JSCodeMapper jsCodeMapper,
                            ExecutorJSCodeJob executorJSCodeJob) {
    /**
     * using for executing js code
     *
     * @param jsCodeScript        js code script for execution
     * @param scheduledTime scheduled time execution
     * @param showResults   boolean value for showing results of execution
     * @return list of script results
     */
    @Transactional
    public List<String> executeJSCode(String jsCodeScript, String scheduledTime, boolean showResults) {
        checkScheduledCodeWithShowingResults(scheduledTime, showResults);
        Instant instantScheduledTime;
        JSCode jsCode;
        if (scheduledTime == null) {
            instantScheduledTime = Instant.now();
            jsCode = createJSCodeDocument(jsCodeScript, instantScheduledTime);
            executorJSCodeJob.executeJSCode(jsCode);
        } else {
            jsCode = createJSCodeDocument(jsCodeScript, ZonedDateTime.of(LocalDateTime
                    .parse(scheduledTime), ZoneId.systemDefault()).toInstant());
            executorJSCodeJob.scheduleJSCodeJobById(jsCode);
        }
        return jsCode.getScriptResults();
    }
    /**
     * using for stopping js code
     *
     * @param jsCodeId js code id for stop
     */
    public void stopJSCode(String jsCodeId) {
        executorJSCodeJob.stopJSCodeJobById(jsCodeId);
    }

    private JSCode createJSCodeDocument(String jsCode, Instant scheduledTime) {
        JSCode jsCodeDocument = new JSCode();
        jsCodeDocument.setScriptBody(jsCode);
        jsCodeDocument.setScheduledTime(scheduledTime);
        jsCodeDocument.setStatusCode(JSCodeStatus.PLANNED);
        jsCodeRepository.save(jsCodeDocument);
        log.info("JSCode was saved to database");
        return jsCodeDocument;
    }
    private void checkScheduledCodeWithShowingResults(String scheduledTime, boolean showResults) {
        if (showResults && scheduledTime != null) {
            log.warn("scheduled time is not null -> {} and show results is true -> {}", scheduledTime, true);
            throw new UnsupportedOperationException("It is impossible scheduling an show output and the same time");
        }
    }
}
