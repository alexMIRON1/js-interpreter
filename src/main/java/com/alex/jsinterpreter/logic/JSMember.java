package com.alex.jsinterpreter.logic;

public enum JSMember {
    JS("js"), CONSOLE("console"), LOG("log");
    private final String value;
    JSMember(String value) {
        this.value= value;
    }

    public String getValue() {
        return value;
    }
}
