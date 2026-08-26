package com.example.demo.exception;

public class MinStayViolationException extends RuntimeException {
    private final int requiredMinStay;

    public MinStayViolationException(String message, int requiredMinStay) {
        super(message);
        this.requiredMinStay = requiredMinStay;
    }

    public int getRequiredMinStay() {
        return requiredMinStay;
    }
}
