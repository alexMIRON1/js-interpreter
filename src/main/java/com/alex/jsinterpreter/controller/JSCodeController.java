package com.alex.jsinterpreter.controller;

import com.alex.jsinterpreter.logic.service.JSCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class {@code JSCodeController } represents endpoints of API which allow us doing operation with
 * javascript code.
 *
 * @author Oleksandr Myronenko
 */
@RestController("api/v1/js-codes")
public record JSCodeController(JSCodeService jsCodeService) {
    @PostMapping
    public ResponseEntity<Void> executeJSCode(@RequestBody String jsCode,
                                              @RequestParam
                                              Optional<Instant> scheduledTime) {
        jsCodeService.executeJSCode(jsCode, scheduledTime);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public List<Object> getJSCodes() {
        return Collections.emptyList();
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
    public Object getJSCodeById(Long id) {
        return null;
    }

    @PutMapping
    public ResponseEntity<Void> stopExecutionJSCode() {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInactiveJSCode(Long id) {
        return null;
    }
}
