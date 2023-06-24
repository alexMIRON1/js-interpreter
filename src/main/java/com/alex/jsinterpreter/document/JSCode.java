package com.alex.jsinterpreter.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * The document stores JavaScript code and all needed information about it.
 *
 * @author Oleksandr Myronenko
 */
@Document(collection = "js_interpreter")
@Getter
@Setter
@NoArgsConstructor
public class JSCode {
    @Id
    private String jsCodeId;
    private JSCodeStatus statusCode;
    private String scriptBody;
    private List<String> scriptResults;
    private Instant scheduledTime;
    private Long executionTime;

    public JSCode(JSCodeStatus statusCode, String scriptBody, List<String> scriptResults,
                  Instant scheduledTime, Long executionTime) {
        this.statusCode = statusCode;
        this.scriptBody = scriptBody;
        this.scriptResults = scriptResults;
        this.scheduledTime = scheduledTime;
        this.executionTime = executionTime;
    }
}
