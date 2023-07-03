package com.alex.jsinterpreter.web.controller;

import com.alex.jsinterpreter.domain.dto.JSCodeCommonResponse;
import com.alex.jsinterpreter.domain.dto.JSCodeDetailedResponse;
import com.alex.jsinterpreter.logic.service.JSCodeManager;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * This class {@code JSCodeController } represents endpoints of API which allow us doing operation with
 * javascript code.
 *
 * @author Oleksandr Myronenko
 */
@RestController
@RequestMapping("/api/v1/js-codes")
public record JSCodeController(JSCodeService jsCodeService, JSCodeManager jsCodeManager) {
    @PostMapping
    public ResponseEntity<List<String>> executeJSCode(HttpServletResponse response, @RequestBody String jsCode,
                                                      @RequestParam(required = false) String scheduledTime,
                                                      @RequestParam(required = false) boolean showResults) {
        List<String> scriptResults = jsCodeManager.executeJSCode(jsCode, scheduledTime, showResults);
        if (showResults) {
            return new ResponseEntity<>(scriptResults, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping
    public List<JSCodeCommonResponse> getJSCodes(@RequestParam Optional<String> status,
                                                 @RequestParam Optional<String> sortBy) {
        return jsCodeService.getListJSCodes(status, sortBy);
    }

    @GetMapping("/{id}")
    public JSCodeDetailedResponse getJSCodeById(@PathVariable("id") String id) {
        return jsCodeService.getDetailedJSCodeById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> stopExecutionJSCode(@PathVariable("id") String id) {
        jsCodeManager.stopJSCode(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInactiveJSCode(@PathVariable("id") String id) {
        jsCodeService.deleteInactiveJSCode(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
