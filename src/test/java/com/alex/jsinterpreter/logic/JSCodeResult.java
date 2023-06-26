package com.alex.jsinterpreter.logic;

import java.util.List;

public class JSCodeResult {
    public static final List<String> SUCCESSFUL_RESULT = List.of("Script execution started", "1", "2", "3",
            "4", "5", "Script execution completed");
    public static final List<String> INFINITY_RESULT = List.of("Infinity");
    public static final List<String> NOT_DEFINED_RESULT = List.of("ReferenceError: con is not defined");
}
