package com.alex.jsinterpreter.logic.handler;

import com.alex.jsinterpreter.document.JSCode;
import com.alex.jsinterpreter.document.JSCodeStatus;
import com.alex.jsinterpreter.logic.JSMember;
import com.alex.jsinterpreter.logic.collector.JSCodeCollectionCollector;
import com.alex.jsinterpreter.logic.service.JSCodeService;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * class responsible for handling js code results
 *
 * @author Oleksandr Myronenko
 */
@Component
@Slf4j
public class JSCodeResultHandler {
    private final JSCodeService jsCodeService;
    private final List<String> scriptResults;

    public JSCodeResultHandler(JSCodeService jsCodeService, List<String> scriptResults) {
        this.jsCodeService = jsCodeService;
        this.scriptResults = scriptResults;
    }

    /**
     * using for handling output executing js code and update it
     *
     * @param context context of js code graalvm
     * @param jsCode  {@link JSCode}
     */
    public void handleOutputExecutingAndUpdateJSCode(Context context, JSCode jsCode) {
        jsCodeService.updateStatus(jsCode, JSCodeStatus.EXECUTING);
        // collecting all output to collection
        context.getBindings(JSMember.JS.getValue()).getMember(JSMember.CONSOLE.getValue())
                .putMember(JSMember.LOG.getValue(), new JSCodeCollectionCollector(scriptResults));
        // execute js code
        context.eval(JSMember.JS.getValue(), jsCode.getScriptBody());
        jsCodeService.updateScriptResult(jsCode, scriptResults);
        if (checkScriptResults(scriptResults)) {
            jsCodeService.updateStatus(jsCode, JSCodeStatus.COMPLETED);
        } else {
            jsCodeService.updateStatus(jsCode, JSCodeStatus.FAILED);
        }
    }

    /**
     * using for handling exception to results and update js code
     *
     * @param exception exception for handling
     * @param jsCode    {@link JSCode}
     */
    public void handleExceptionsAndUpdateJSCode(PolyglotException exception, JSCode jsCode) {
        String exceptionMessage = exception.getMessage();
        scriptResults.add(exceptionMessage);
        jsCodeService.updateScriptResult(jsCode, scriptResults);
        log.warn("java script code produce error -> {}", exceptionMessage);
        jsCodeService.updateStatus(jsCode, JSCodeStatus.FAILED);
    }

    /**
     * using for clearing all results
     */
    public void clearAllResults() {
        scriptResults.clear();
    }

    private boolean checkScriptResults(List<String> scriptResults) {
        return !scriptResults.contains("Infinity");
    }
}
