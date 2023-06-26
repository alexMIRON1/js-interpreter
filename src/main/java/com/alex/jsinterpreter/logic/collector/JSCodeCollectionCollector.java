package com.alex.jsinterpreter.logic.collector;


import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.List;

/**
 * class using for collecting results js code
 *
 * @author Oleksandr Myronenko
 */
@Slf4j
public class JSCodeCollectionCollector implements ProxyExecutable {
    private final List<String> scriptResults;

    public JSCodeCollectionCollector(List<String> scriptResults) {
        this.scriptResults = scriptResults;
    }

    @Override
    public Object execute(Value... arguments) {
        for (Value arg : arguments) {
            scriptResults.add(String.valueOf(arg.as(Object.class)));
        }
        log.info("arguments was added to collection");
        return null;
    }
}
