package com.alex.jsinterpreter.domain.dto;

import com.alex.jsinterpreter.document.JSCodeStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JSCodeDetailedResponse {
    private Long jsCodeId;
    private JSCodeStatus jsCodeStatus;
    private String scriptBody;
    private String scriptResult;
    private Instant scheduledTime;
    private LocalTime executionTime;
}
