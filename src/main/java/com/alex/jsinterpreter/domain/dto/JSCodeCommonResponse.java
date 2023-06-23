package com.alex.jsinterpreter.domain.dto;

import com.alex.jsinterpreter.document.JSCodeStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JSCodeCommonResponse {
    private String jsCodeId;
    private JSCodeStatus statusCode;
    private Instant scheduledTime;
    private Long executionTime;
}
