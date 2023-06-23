package com.alex.jsinterpreter.controller;

import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * This class {@code JSCodeController } represents endpoints of API which allow us doing operation with
 * javascript code.
 *
 * @author Oleksandr Myronenko
 */
@RestController
@RequestMapping("/api/v1/js-codes")
public record JSCodeController(JSCodeService jsCodeService) {
    @PostMapping
    public ResponseEntity<Void> executeJSCode(@RequestBody String jsCode,
                                              @RequestParam(required = false) String scheduledTime) {
        jsCodeService.executeJSCode(jsCode, scheduledTime);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping
    public List<JSCodeCommonResponse> getJSCodes() {
        return jsCodeService.getListJSCodes();
    }

    @GetMapping("/{status}")
    public List<Object> getJSCodesByStatus(String status) {
        return Collections.emptyList();
    }

    @GetMapping("/sortById")
    public List<Object> getJSCodesSortedById(@RequestParam String sortOrder) {
        return Collections.emptyList();
    }

    @GetMapping("/sortByScheduledTime")
    public List<Object> getJSCodesSortedByScheduledTime(@RequestParam String sortOrder) {
        return Collections.emptyList();
    }

    @GetMapping("/{id}")
    public JSCodeDetailedResponse getJSCodeById(String id) {
        return jsCodeService.getById(id);
    }

    @PutMapping
    public ResponseEntity<Void> stopExecutionJSCode() {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInactiveJSCode(String id) {
        return null;
    }
}
