package com.alex.jsinterpreter.domain.dto;

import com.alex.jsinterpreter.document.JSCodeStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JSCodeCommonResponse {
    private Long jsCodeId;
    private JSCodeStatus jsCodeStatus;
    private Instant scheduledTime;
    private LocalTime executionTime;
}
