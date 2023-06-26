package com.alex.jsinterpreter.logic;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import com.alex.jsinterpreter.repository.JSCodeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * class responsible for testing business logic application
 *
 * @author Oleksandr Myronenko
 */
@SpringBootTest
class JSCodeServiceTest {
    @Autowired
    private JSCodeService jsCodeService;
    @MockBean
    private JSCodeRepository jsCodeRepository;

    @Test
    void executeJSCodeTest() {
        JSCode completedJSCode = new JSCode(JSCodeStatus.COMPLETED, JSCodeScript.JS_CODE_RIGHT_SCRIPT
                , JSCodeResult.SUCCESSFUL_RESULT, Instant.now(), 345L);
        Mockito.when(jsCodeRepository.save(completedJSCode)).thenReturn(completedJSCode);
        Mockito.when(jsCodeRepository.findById(null)).thenReturn(Optional.of(completedJSCode));

        List<String> scriptActualResults = jsCodeService.executeJSCode(JSCodeScript.JS_CODE_RIGHT_SCRIPT,
                null, false);
        List<String> scriptExpectedResults = JSCodeResult.SUCCESSFUL_RESULT;
        for (int i = 0; i < scriptActualResults.size(); i++) {
            assertEquals(scriptExpectedResults.get(i), scriptActualResults.get(i));
        }
    }

    @Test
    void executeInfinityJSCodeTest() {
        JSCode failedJSCode = new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_CODE_INFINITY_SCRIPT
                , JSCodeResult.INFINITY_RESULT, Instant.now(), 145L);
        Mockito.when(jsCodeRepository.save(failedJSCode)).thenReturn(failedJSCode);
        Mockito.when(jsCodeRepository.findById(null)).thenReturn(Optional.of(failedJSCode));

        List<String> scriptActualResults = jsCodeService.executeJSCode(JSCodeScript.JS_CODE_INFINITY_SCRIPT,
                null, false);
        List<String> scriptExpectedResults = JSCodeResult.INFINITY_RESULT;
        for (int i = 0; i < scriptActualResults.size(); i++) {
            assertEquals(scriptExpectedResults.get(i), scriptActualResults.get(i));
        }
    }

    @Test
    void executeNotDefinedJSCodeTest() {
        JSCode failedJSCode = new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT
                , JSCodeResult.NOT_DEFINED_RESULT, Instant.now(), 145L);
        Mockito.when(jsCodeRepository.save(failedJSCode)).thenReturn(failedJSCode);
        Mockito.when(jsCodeRepository.findById(null)).thenReturn(Optional.of(failedJSCode));

        List<String> scriptActualResults = jsCodeService.executeJSCode(JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT,
                null, false);
        List<String> scriptExpectedResults = JSCodeResult.NOT_DEFINED_RESULT;
        for (int i = 0; i < scriptActualResults.size(); i++) {
            assertEquals(scriptExpectedResults.get(i), scriptActualResults.get(i));
        }
    }

    @Test
    void executeScheduledCodeWithShowResults() {
        assertThrows(UnsupportedOperationException.class, () -> jsCodeService.executeJSCode(JSCodeScript.JS_CODE_RIGHT_SCRIPT,
                "2023-06-23T22:29", true));
    }

    @Test
    void getByIdTest() {
        String jsCodeId = "649970f8429f5f1e8e8e7a40";
        Instant time = Instant.now();
        long executionTime = 121L;
        JSCode failedJSCode = new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT
                , JSCodeResult.NOT_DEFINED_RESULT, time, executionTime);
        failedJSCode.setJsCodeId(jsCodeId);
        Mockito.when(jsCodeRepository.findById(jsCodeId)).thenReturn(Optional.of(failedJSCode));
        JSCodeDetailedResponse expectedResult = jsCodeService.getById(jsCodeId);
        JSCodeDetailedResponse actualResult = new JSCodeDetailedResponse(jsCodeId, JSCodeStatus.FAILED,
                JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT, JSCodeResult.NOT_DEFINED_RESULT, time, executionTime);
        assertEquals(expectedResult.getJsCodeId(), actualResult.getJsCodeId());
        assertEquals(expectedResult.getScriptResults(), actualResult.getScriptResults());
        assertEquals(expectedResult.getStatusCode(), actualResult.getStatusCode());
        assertEquals(expectedResult.getScriptBody(), actualResult.getScriptBody());
        assertEquals(expectedResult.getScheduledTime(), actualResult.getScheduledTime());
        assertEquals(expectedResult.getExecutionTime(), actualResult.getExecutionTime());
    }

    @Test
    void getByWrongIdTest() {
        String jsCodeId = "649970f8429f5f1e8e8e7a40";
        Mockito.when(jsCodeRepository.findById(jsCodeId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> jsCodeService.getById(jsCodeId));
    }

    @Test
    void getListJSCodesTest() {
        Mockito.when(jsCodeRepository.findAll()).thenReturn(getJSCodes());
        List<JSCodeCommonResponse> expectedResult = getJSCodeCommonResponses();
        List<JSCodeCommonResponse> actualResult = jsCodeService.getListJSCodes();
        testEqualsJSCodeCommonResponseList(expectedResult, actualResult);

    }

    @Test
    void getListJSCodesByStatus() {
        String jsCodeStatus = "Completed";
        Mockito.when(jsCodeRepository.findByStatusCode(JSCodeStatus.valueOf(jsCodeStatus.toUpperCase())))
                .thenReturn(getJSCodes()
                        .stream()
                        .filter(jsCode -> jsCode.getStatusCode()
                                .equals(JSCodeStatus.valueOf(jsCodeStatus.toUpperCase())))
                        .toList());
        List<JSCodeCommonResponse> expectedResult = getJSCodeCommonResponses()
                .stream()
                .filter(jsCodeCommonResponse -> jsCodeCommonResponse.getStatusCode()
                        .equals(JSCodeStatus.valueOf(jsCodeStatus.toUpperCase())))
                .toList();
        List<JSCodeCommonResponse> actualResult = jsCodeService.getListJSCodesByStatus(jsCodeStatus);
        testEqualsJSCodeCommonResponseList(expectedResult, actualResult);
    }

    @Test
    void getListJSCodesSortedByScheduledTime() {
        Mockito.when(jsCodeRepository.findAll(Sort.by(Sort.Direction.DESC, "scheduledTime")))
                .thenReturn(getJSCodes()
                        .stream().sorted(Comparator.comparing(JSCode::getScheduledTime).reversed())
                        .toList());
        List<JSCodeCommonResponse> expectedResult = getJSCodeCommonResponses()
                .stream()
                .sorted(Comparator.comparing(JSCodeCommonResponse::getScheduledTime).reversed())
                .toList();
        List<JSCodeCommonResponse> actualResult = jsCodeService.getListJSCodesSortedByScheduledTime();
        testEqualsJSCodeCommonResponseList(expectedResult, actualResult);
    }

    @Test
    void deleteWrongInactiveJSCodeTest() {
        String jsCodeId = "64970745def9b62d84fa423f";
        JSCode plannedJSCode = new JSCode(JSCodeStatus.PLANNED, JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT
                , JSCodeResult.NOT_DEFINED_RESULT, Instant.now(), 132L);
        Mockito.when(jsCodeRepository.findById(jsCodeId)).thenReturn(Optional.of(plannedJSCode));
        assertThrows(IllegalArgumentException.class, () -> jsCodeService.deleteInactiveJSCode(jsCodeId));
    }

    private List<JSCode> getJSCodes() {
        List<JSCode> jsCodes = new ArrayList<>();
        jsCodes.add(new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT, JSCodeResult.NOT_DEFINED_RESULT,
                Instant.parse("2023-06-24T15:11:00Z"), 2445L));
        jsCodes.add(new JSCode(JSCodeStatus.COMPLETED, JSCodeScript.JS_CODE_RIGHT_SCRIPT, JSCodeResult.SUCCESSFUL_RESULT,
                Instant.parse("2023-06-24T15:25:55.213Z"), 1584L));
        jsCodes.add(new JSCode(JSCodeStatus.COMPLETED, JSCodeScript.JS_CODE_RIGHT_SCRIPT, JSCodeResult.SUCCESSFUL_RESULT,
                Instant.parse("2023-06-24T15:27:00Z"), 52L));
        jsCodes.add(new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_CODE_INFINITY_SCRIPT, JSCodeResult.INFINITY_RESULT,
                Instant.parse("2023-06-24T15:27:01.571Z"), 65L));
        jsCodes.add(new JSCode(JSCodeStatus.FAILED, JSCodeScript.JS_NOT_DEFINED_ERROR_SCRIPT, JSCodeResult.NOT_DEFINED_RESULT,
                Instant.parse("2023-06-24T15:28:34.958Z"), 1423L));
        return jsCodes;

    }

    private List<JSCodeCommonResponse> getJSCodeCommonResponses() {
        List<JSCodeCommonResponse> jsCodeCommonResponses = new ArrayList<>();
        jsCodeCommonResponses.add(new JSCodeCommonResponse("64970745def9b62d84fa423f", JSCodeStatus.FAILED,
                Instant.parse("2023-06-24T15:11:00Z"), 2445L));
        jsCodeCommonResponses.add(new JSCodeCommonResponse("64970b03e79aca6c9ceb1ab2", JSCodeStatus.COMPLETED,
                Instant.parse("2023-06-24T15:25:55.213Z"), 1584L));
        jsCodeCommonResponses.add(new JSCodeCommonResponse("64970b20e79aca6c9ceb1ab3", JSCodeStatus.COMPLETED,
                Instant.parse("2023-06-24T15:27:00Z"), 52L));
        jsCodeCommonResponses.add(new JSCodeCommonResponse("64970b45e79aca6c9ceb1ab4", JSCodeStatus.FAILED,
                Instant.parse("2023-06-24T15:27:01.571Z"), 65L));
        jsCodeCommonResponses.add(new JSCodeCommonResponse("64970ba3ccc19d661903a9e6", JSCodeStatus.FAILED,
                Instant.parse("2023-06-24T15:28:34.958Z"), 1423L));
        return jsCodeCommonResponses;

    }

    private void testEqualsJSCodeCommonResponseList(List<JSCodeCommonResponse> expectedResult,
                                                    List<JSCodeCommonResponse> actualResult) {
        for (int i = 0; i < actualResult.size(); i++) {
            assertEquals(expectedResult.get(i).getStatusCode(), actualResult.get(i).getStatusCode());
            assertEquals(expectedResult.get(i).getExecutionTime(), actualResult.get(i).getExecutionTime());
            assertEquals(expectedResult.get(i).getScheduledTime(), actualResult.get(i).getScheduledTime());
        }
    }
}
