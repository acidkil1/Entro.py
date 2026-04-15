package com.entro.calculator;

/**
 * Утилита для вычисления энтропии Шеннона
 * Порт логики из Python версии entro_gui.py
 */
public class EntropyCalculator {

    /**
     * Вычисление энтропии Шеннона для вектора вероятностей
     * H = -Σ p * log2(p)
     */
    public static double calculateEntropy(double[] probVector) {
        double h = 0.0;
        for (double p : probVector) {
            if (p > 1e-9) {
                h -= p * (Math.log(p) / Math.log(2));
            }
        }
        return h;
    }

    /**
     * Нормализация вектора (сумма = 1)
     */
    public static double[] normalizeVector(double[] vec) {
        double sum = 0;
        for (double v : vec) {
            sum += v;
        }
        if (sum == 0) {
            double[] uniform = new double[vec.length];
            for (int i = 0; i < vec.length; i++) {
                uniform[i] = 1.0 / vec.length;
            }
            return uniform;
        }
        double[] normalized = new double[vec.length];
        for (int i = 0; i < vec.length; i++) {
            normalized[i] = vec[i] / sum;
        }
        return normalized;
    }

    /**
     * Нормализация строк матрицы (каждая строка суммируется в 1)
     */
    public static double[][] normalizeMatrixRows(double[][] matrix) {
        int n = matrix.length;
        double[][] normalized = new double[n][];
        for (int i = 0; i < n; i++) {
            normalized[i] = normalizeVector(matrix[i]);
        }
        return normalized;
    }

    /**
     * Нормализация столбцов матрицы (каждый столбец суммируется в 1)
     */
    public static double[][] normalizeMatrixCols(double[][] matrix) {
        int n = matrix.length;
        if (n == 0) return new double[0][];
        int m = matrix[0].length;
        double[][] normalized = new double[n][m];
        
        for (int j = 0; j < m; j++) {
            double colSum = 0;
            for (int i = 0; i < n; i++) {
                colSum += matrix[i][j];
            }
            if (colSum > 0) {
                for (int i = 0; i < n; i++) {
                    normalized[i][j] = matrix[i][j] / colSum;
                }
            } else {
                for (int i = 0; i < n; i++) {
                    normalized[i][j] = 1.0 / n;
                }
            }
        }
        return normalized;
    }

    /**
     * Основной метод расчета энтропии из совместной матрицы
     * Порт метода calculate_from_joint из Python
     */
    public static EntropyResult calculateFromJoint(double[][] jointMatrix) {
        int n = jointMatrix.length;
        if (n == 0) return null;

        EntropyResult result = new EntropyResult();
        
        // Вычисление маргинальных вероятностей P(A) и P(B)
        result.pa = new double[n];
        result.pb = new double[n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result.pa[i] += jointMatrix[i][j];
                result.pb[j] += jointMatrix[i][j];
            }
        }

        result.hA = calculateEntropy(result.pa);
        result.hB = calculateEntropy(result.pb);

        // H(A,B) - совместная энтропия
        result.hAB = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double pAB = jointMatrix[i][j];
                if (pAB > 1e-9) {
                    result.hAB -= pAB * (Math.log(pAB) / Math.log(2));
                }
            }
        }

        // H(B|A) и H(Ai) для каждого i
        result.hAiList = new double[n];
        double hBGivenAWeightedSum = 0.0;
        for (int i = 0; i < n; i++) {
            if (result.pa[i] > 1e-9) {
                double[] rowDist = new double[n];
                for (int j = 0; j < n; j++) {
                    rowDist[j] = jointMatrix[i][j] / result.pa[i];
                }
                double hCond = calculateEntropy(rowDist);
                result.hAiList[i] = hCond;
                hBGivenAWeightedSum += result.pa[i] * hCond;
            } else {
                result.hAiList[i] = 0.0;
            }
        }
        result.hBGivenA = hBGivenAWeightedSum;

        // H(A|B) и H(Bj) для каждого j
        result.hBjList = new double[n];
        double hAGivenBWeightedSum = 0.0;
        for (int j = 0; j < n; j++) {
            if (result.pb[j] > 1e-9) {
                double[] colDist = new double[n];
                for (int i = 0; i < n; i++) {
                    colDist[i] = jointMatrix[i][j] / result.pb[j];
                }
                double hCond = calculateEntropy(colDist);
                result.hBjList[j] = hCond;
                hAGivenBWeightedSum += result.pb[j] * hCond;
            } else {
                result.hBjList[j] = 0.0;
            }
        }
        result.hAGivenB = hAGivenBWeightedSum;

        return result;
    }

    /**
     * Расчет условной энтропии при равномерном распределении
     * Порт метода calculate_uniform_conditional_entropy из Python
     */
    public static double calculateUniformConditionalEntropy(double[][] matrix, String channelType) {
        int n = matrix.length;
        if (n == 0) return 0;
        double uniformP = 1.0 / n;
        
        double hConditionalUniform = 0.0;
        
        if (channelType.equals("b|a")) {
            for (int i = 0; i < n; i++) {
                double hBiGivenAi = calculateEntropy(matrix[i]);
                hConditionalUniform += uniformP * hBiGivenAi;
            }
        } else if (channelType.equals("a|b")) {
            for (int j = 0; j < n; j++) {
                double[] colDist = new double[n];
                for (int i = 0; i < n; i++) {
                    colDist[i] = matrix[i][j];
                }
                double hAjGivenBj = calculateEntropy(colDist);
                hConditionalUniform += uniformP * hAjGivenBj;
            }
        } else if (channelType.equals("a,b")) {
            double[] paOrig = new double[n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    paOrig[i] += matrix[i][j];
                }
            }
            for (int i = 0; i < n; i++) {
                if (paOrig[i] > 1e-9) {
                    double[] rowDist = new double[n];
                    for (int j = 0; j < n; j++) {
                        rowDist[j] = matrix[i][j] / paOrig[i];
                    }
                    double hBiGivenAi = calculateEntropy(rowDist);
                    hConditionalUniform += uniformP * hBiGivenAi;
                }
            }
        }

        return hConditionalUniform;
    }
}
