package com.alex.jsinterpreter.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalTime;

/**
 * The document stores JavaScript code and all needed information about it.
 *
 * @author Oleksandr Myronenko
 */
@Document(collection = "js_interpreter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JSCode {
    @Id
    private Long jsCodeId;
    private JSCodeStatus statusCode;
    private String scriptBody;
    private String scriptResult;
    private Instant scheduledTime;
    private LocalTime executionTime;
}
