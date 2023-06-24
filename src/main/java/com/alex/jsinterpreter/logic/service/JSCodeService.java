package com.alex.jsinterpreter.logic.service;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import com.alex.jsinterpreter.domain.mapper.JSCodeMapper;
import com.alex.jsinterpreter.logic.job.ExecutorJSCodeJob;
import com.alex.jsinterpreter.repository.JSCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * this class responsible for managing JS code
 *
 * @author Oleksandr Myronenko
 */
@Service
@Slf4j
public record JSCodeService(JSCodeRepository jsCodeRepository, JSCodeMapper jsCodeMapper,
                            @Lazy ExecutorJSCodeJob executorJSCodeJob) {
    /**
     * using for executing js code
     *
     * @param jsCode        js code script for execution
     * @param scheduledTime scheduled time execution
     */
    @Transactional
    public void executeJSCode(String jsCode, String scheduledTime) {
        Instant instantScheduledTime;
        String jsCodeId;
        if (scheduledTime == null) {
            instantScheduledTime = Instant.now();
            jsCodeId = createJSCodeDocument(jsCode, instantScheduledTime);
        } else {
            jsCodeId = createJSCodeDocument(jsCode, ZonedDateTime.of(LocalDateTime
                    .parse(scheduledTime), ZoneId.systemDefault()).toInstant());
        }
        executorJSCodeJob.scheduleJSCodeJobById(jsCodeId);
    }

    /**
     * using for updating js code status
     *
     * @param jsCodeId     js code id for updating
     * @param jsCodeStatus new js code status
     */
    @Transactional
    public void updateStatus(String jsCodeId, JSCodeStatus jsCodeStatus) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setStatusCode(jsCodeStatus);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for updating js code script result
     *
     * @param jsCodeId     js code id for updating
     * @param scriptResult new js code script result
     */
    @Transactional
    public void updateScriptResult(String jsCodeId, List<String> scriptResult) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setScriptResults(scriptResult);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for updating js code execution time
     *
     * @param jsCodeId      js code id for updating
     * @param executionTime new js code execution time
     */
    @Transactional
    public void updateExecutionTime(String jsCodeId, Long executionTime) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setExecutionTime(executionTime);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for getting by id js code
     *
     * @param jsCodeId js code id
     * @return {@link JSCodeDetailedResponse}
     */
    public JSCodeDetailedResponse getById(String jsCodeId) {
        Optional<JSCode> jsCode = jsCodeRepository.findById(jsCodeId);
        if (jsCode.isEmpty()) {
            log.warn("wrong js code id -> {}", jsCodeId);
            throw new NoSuchElementException("JS code with this id was not found " + jsCodeId);
        }
        return jsCode.map(jsCodeMapper::documentMapToDetailedResponse).orElseThrow();
    }

    /**
     * using for getting list js codes
     *
     * @return list of {@link JSCodeCommonResponse}
     */
    public List<JSCodeCommonResponse> getListJSCodes() {
        return jsCodeRepository.findAll().stream().map(jsCodeMapper::documentMapToCommonResponse).toList();
    }

    /**
     * using for getting list js codes by status
     *
     * @param statusJSCode js code status
     * @return list of {@link JSCodeCommonResponse}
     */
    public List<JSCodeCommonResponse> getListJSCodesByStatus(String statusJSCode) {
        return jsCodeRepository.findByStatusCode(JSCodeStatus.valueOf(statusJSCode.toUpperCase()))
                .stream()
                .map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    /**
     * using for getting list js codes sorted by id
     *
     * @return list of {@link JSCodeCommonResponse}
     */
    public List<JSCodeCommonResponse> getListJSCodesSortedById() {
        return jsCodeRepository.findAll(Sort.by(Sort.Direction.DESC, "_id"))
                .stream()
                .map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    /**
     * using for getting list js codes sorted by scheduled time
     *
     * @return list of {@link JSCodeCommonResponse}
     */
    public List<JSCodeCommonResponse> getListJSCodesSortedByScheduledTime() {
        return jsCodeRepository.findAll(Sort.by(Sort.Direction.DESC, "scheduledTime"))
                .stream()
                .map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    /**
     * using for stopping js code
     *
     * @param jsCodeId js code id for stop
     */
    public void stopJSCode(String jsCodeId) {
        executorJSCodeJob.stopJSCodeJobById(jsCodeId);
    }

    /**
     * using for deletion inactive js code
     *
     * @param jsCodeId js code id for deletion
     */
    @Transactional
    public void deleteInactiveJSCode(String jsCodeId) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        if (!checkStatusForDeletionJSCode(jsCode)) {
            log.warn("js code status is active -> {}", jsCode.getStatusCode());
            throw new IllegalArgumentException("Js code status is active");
        }
        jsCodeRepository.delete(jsCode);
        log.info("js code was deleted by id -> {}", jsCodeId);
    }

    private String createJSCodeDocument(String jsCode, Instant scheduledTime) {
        JSCode jsCodeDocument = new JSCode();
        jsCodeDocument.setScriptBody(jsCode);
        jsCodeDocument.setScheduledTime(scheduledTime);
        jsCodeDocument.setStatusCode(JSCodeStatus.PLANNED);
        jsCodeRepository.save(jsCodeDocument);
        log.info("JSCode was saved to database");
        return jsCodeDocument.getJsCodeId();
    }

    private boolean checkStatusForDeletionJSCode(JSCode jsCode) {
        JSCodeStatus currentStatus = jsCode.getStatusCode();
        return currentStatus.equals(JSCodeStatus.COMPLETED) || currentStatus.equals(JSCodeStatus.FAILED) ||
                currentStatus.equals(JSCodeStatus.STOPPED);
    }
}
