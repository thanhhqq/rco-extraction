package com.saigonbpo.entity.config;

public enum Position {
	LEFT(0),
    TOP(-1),
    RIGHT(2),
    BOTTOM(1);

    private final int value;

    private Position(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
