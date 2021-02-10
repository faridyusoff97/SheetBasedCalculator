package com.mmu.fyp.sheetbasedcalculator.calculator;

/**
 * Created by User on 11/13/2016.
 */
public class CalculationException extends Throwable {
    private String message;

    public CalculationException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}