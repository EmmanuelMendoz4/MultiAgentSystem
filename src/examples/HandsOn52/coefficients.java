package HandsOn5;

public class coefficients {

    public double determinationCoefficient(double correlation) {
        return Math.pow(correlation, 2) * 100;
    }

    public double correlationCoefficient(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        return (n * sumXY - sumX * sumY) /
                Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
    }

    public double determinationCoefficientC(double explainedVariance , double totalVariance) {
        return (explainedVariance /totalVariance) * 100;
    }

    public double correlationCoefficient2(double determinationCoefficient) {
        // Calcula la raíz cuadrada de R^2 para obtener el coeficiente de correlación
        return Math.sqrt(determinationCoefficient / 100);
    }

}

