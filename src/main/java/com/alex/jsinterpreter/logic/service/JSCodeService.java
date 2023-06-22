package com.alex.jsinterpreter.logic.service;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.logic.job.ExecutionJSCodeJob;
import com.alex.jsinterpreter.repository.JSCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * this class responsible for managing JS code
 *
 * @author Oleksandr Myronenko
 */
@Service
@Slf4j
public record JSCodeService(JSCodeRepository jsCodeRepository,
                            ExecutionJSCodeJob executionJSCodeJob) {

    public void executeJSCode(String jsCode, Optional<Instant> optionalScheduledTime) {
        if (optionalScheduledTime.isEmpty()) {
            optionalScheduledTime = Optional.of(Instant.now());
        }
        Instant scheduledTime = optionalScheduledTime.get();
        Long jsCodeId  = createJSCodeEntity(jsCode, scheduledTime);
        executionJSCodeJob.scheduleJSCodeJobById(jsCodeId);
    }

    @Transactional
    public void updateStatus(Long jsCodeId, JSCodeStatus jsCodeStatus) {
        JSCode jsCode = getById(jsCodeId);
        jsCode.setStatusCode(jsCodeStatus);
        jsCodeRepository.save(jsCode);
    }

    @Transactional
    public void updateScriptResult(Long jsCodeId, String scriptResult) {
        JSCode jsCode = getById(jsCodeId);
        jsCode.setScriptResult(scriptResult);
        jsCodeRepository.save(jsCode);
    }

    public JSCode getById(Long jsCodeId) {
        Optional<JSCode> jsCode = jsCodeRepository.findById(jsCodeId);
        if (jsCode.isEmpty()) {
            log.warn("wrong js code id -> {}", jsCodeId);
            throw new NoSuchElementException("JS code with this id was not found" + jsCodeId);
        }
        return jsCode.get();
    }

    private Long createJSCodeEntity(String jsCode, Instant scheduledTime) {
        JSCode jsCodeDocument = new JSCode();
        jsCodeDocument.setScriptBody(jsCode);
        jsCodeDocument.setScheduledTime(scheduledTime);
        jsCodeDocument.setStatusCode(JSCodeStatus.PLANNED);
        jsCodeRepository.save(jsCodeDocument);
        log.info("JSCode was saved to database");
        return jsCodeDocument.getJsCodeId();
    }
}
