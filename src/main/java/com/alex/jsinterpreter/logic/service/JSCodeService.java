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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
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
    @Transactional
    public void executeJSCode(String jsCode, String scheduledTime) {
        String jsCodeId = createJSCodeDocument(jsCode, ZonedDateTime.of(LocalDateTime
                .parse(scheduledTime), ZoneId.systemDefault()).toInstant());
        executorJSCodeJob.scheduleJSCodeJobById(jsCodeId);
    }

    @Transactional
    public void updateStatus(String jsCodeId, JSCodeStatus jsCodeStatus) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setStatusCode(jsCodeStatus);
        jsCodeRepository.save(jsCode);
    }

    @Transactional
    public void updateScriptResult(String jsCodeId, List<String> scriptResult) {
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setScriptResults(scriptResult);
        jsCodeRepository.save(jsCode);
    }
    @Transactional
    public void updateExecutionTime(String jsCodeId, Long executionTime){
        JSCode jsCode = jsCodeMapper.detailedResponseMapToDocument(getById(jsCodeId));
        jsCode.setExecutionTime(executionTime);
        jsCodeRepository.save(jsCode);
    }

    public JSCodeDetailedResponse getById(String jsCodeId) {
        Optional<JSCode> jsCode = jsCodeRepository.findById(jsCodeId);
        if (jsCode.isEmpty()) {
            log.warn("wrong js code id -> {}", jsCodeId);
            throw new NoSuchElementException("JS code with this id was not found" + jsCodeId);
        }
        return jsCode.map(jsCodeMapper::documentMapToDetailedResponse).orElseThrow();
    }

    public List<JSCodeCommonResponse> getListJSCodes() {
        return jsCodeRepository.findAll().stream().map(jsCodeMapper::documentMapToIncompleteResponse).toList();
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
}
