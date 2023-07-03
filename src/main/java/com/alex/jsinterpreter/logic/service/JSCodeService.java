package com.alex.jsinterpreter.logic.service;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import com.alex.jsinterpreter.domain.mapper.JSCodeMapper;
import com.alex.jsinterpreter.repository.JSCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public record JSCodeService(JSCodeRepository jsCodeRepository, JSCodeMapper jsCodeMapper) {
    private static final String JS_CODE_SORTED_PARAM_ID = "_id";
    private static final String JS_CODE_SORTED_PARAM_SCHEDULED_TIME = "scheduledTime";

    /**
     * using for updating js code status
     *
     * @param jsCode       js code  for updating
     * @param jsCodeStatus new js code status
     */
    public void updateStatus(JSCode jsCode, JSCodeStatus jsCodeStatus) {
        jsCode.setStatusCode(jsCodeStatus);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for updating js code script result
     *
     * @param jsCode       js code  for updating
     * @param scriptResult new js code script result
     */
    public void updateScriptResult(JSCode jsCode, List<String> scriptResult) {
        jsCode.setScriptResults(scriptResult);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for updating js code execution time
     *
     * @param jsCode        js code  for updating
     * @param executionTime new js code execution time
     */
    public void updateExecutionTime(JSCode jsCode, Long executionTime) {
        jsCode.setExecutionTime(executionTime);
        jsCodeRepository.save(jsCode);
    }

    /**
     * using for getting by id js code
     *
     * @param jsCodeId js code id
     * @return {@link JSCodeDetailedResponse}
     */
    public JSCode getById(String jsCodeId) {
        Optional<JSCode> jsCode = jsCodeRepository.findById(jsCodeId);
        if (jsCode.isEmpty()) {
            log.warn("wrong js code id -> {}", jsCodeId);
            throw new NoSuchElementException("JS code with this id was not found " + jsCodeId);
        }
        return jsCode.get();
    }

    /**
     * using for getting by id detailed js code
     *
     * @param jsCodeId js code id
     * @return detailed js code
     */
    public JSCodeDetailedResponse getDetailedJSCodeById(String jsCodeId) {
        return jsCodeMapper.documentMapToDetailedResponse(getById(jsCodeId));
    }

    /**
     * using for get list js codes with different optional params as sorting or/and status js code
     *
     * @param statusJSCode optional value of status js code
     * @param sortBy       optional value of sorting param
     * @return list of {@link JSCodeCommonResponse}
     */
    public List<JSCodeCommonResponse> getListJSCodes(Optional<String> statusJSCode, Optional<String> sortBy) {
        if (statusJSCode.isEmpty() && sortBy.isPresent()) {
            log.info("get list js codes sorted by -> {}", sortBy);
            return getListJSCodesSortedBy(sortBy.get());
        }
        if (statusJSCode.isPresent() && sortBy.isEmpty()) {
            log.info("get list js codes by stats");
            return getListJSCodesByStatus(statusJSCode.get());
        }
        if (statusJSCode.isPresent()) {
            log.info("get list js codes by status and sorted by -> {}", sortBy);
            return getListJSCodesByStatusAndSortedBy(statusJSCode.get(), sortBy.get());
        }
        log.info("get usual js code list");
        return getListJSCodes();
    }

    /**
     * using for deletion inactive js code
     *
     * @param jsCodeId js code id for deletion
     */
    @Transactional
    public void deleteInactiveJSCode(String jsCodeId) {
        JSCode jsCode = getById(jsCodeId);
        if (!checkStatusForDeletionJSCode(jsCode)) {
            log.warn("js code status is active -> {}", jsCode.getStatusCode());
            throw new IllegalArgumentException("Js code status is active");
        }
        jsCodeRepository.delete(jsCode);
        log.info("js code was deleted by id -> {}", jsCodeId);
    }

    private List<JSCodeCommonResponse> getListJSCodes() {
        return jsCodeRepository.findAll().stream().map(jsCodeMapper::documentMapToCommonResponse).toList();
    }

    private List<JSCodeCommonResponse> getListJSCodesByStatusAndSortedBy(String statusJSCode, String sortBy) {
        return jsCodeRepository.findByStatusCode(JSCodeStatus.valueOf(statusJSCode.toUpperCase()), Sort
                        .by(Sort.Direction.DESC, sortBy))
                .stream().map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    private List<JSCodeCommonResponse> getListJSCodesByStatus(String statusJSCode) {
        return jsCodeRepository.findByStatusCode(JSCodeStatus.valueOf(statusJSCode.toUpperCase()))
                .stream()
                .map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    private List<JSCodeCommonResponse> getListJSCodesSortedBy(String sortBy) {
        checkSortByParam(sortBy);
        return jsCodeRepository.findAll(Sort.by(Sort.Direction.DESC, sortBy))
                .stream()
                .map(jsCodeMapper::documentMapToCommonResponse)
                .toList();
    }

    private void checkSortByParam(String sortBy) {
        if (!sortBy.equalsIgnoreCase(JS_CODE_SORTED_PARAM_ID) && !sortBy.equalsIgnoreCase(JS_CODE_SORTED_PARAM_SCHEDULED_TIME)) {

            throw new UnsupportedOperationException("Sorting by this param does not supported yet" + sortBy);
        }
    }

    private boolean checkStatusForDeletionJSCode(JSCode jsCode) {
        JSCodeStatus currentStatus = jsCode.getStatusCode();
        return currentStatus.equals(JSCodeStatus.COMPLETED) || currentStatus.equals(JSCodeStatus.FAILED) ||
                currentStatus.equals(JSCodeStatus.STOPPED);
    }
}
