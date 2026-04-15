package com.entro.calculator;

/**
 * Класс для хранения результатов вычисления энтропии
 */
public class EntropyResult {
    public double[] pa;
    public double[] pb;
    public double hA;
    public double hB;
    public double hAB;
    public double hBGivenA;
    public double hAGivenB;
    public double[] hAiList;
    public double[] hBjList;

    public EntropyResult() {
    }
}
