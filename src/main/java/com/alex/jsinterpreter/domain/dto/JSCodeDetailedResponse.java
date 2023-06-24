package com.alex.jsinterpreter.domain.dto;

import com.alex.jsinterpreter.document.JSCodeStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * dto class for js code in detailed response
 *
 * @author Oleksandr Myronenko
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JSCodeDetailedResponse {
    private String jsCodeId;
    private JSCodeStatus statusCode;
    private String scriptBody;
    private List<String> scriptResults;
    private Instant scheduledTime;
    private Long executionTime;
}
