package com.alex.jsinterpreter.logic;

public class JSCodeScript {
    public static final String JS_CODE_RIGHT_SCRIPT="console.log('Script execution started');\n" +
            "for (var i = 1; i <= 5; i++) {\n" +
            "   console.log('Result ' + i);\n" +
            "}\n" +
            "console.log('Script execution completed');";
    public static final String JS_CODE_INFINITY_SCRIPT="console.log(10/0)";
    public static final String JS_NOT_DEFINED_ERROR_SCRIPT= "con.log(11)";
}
