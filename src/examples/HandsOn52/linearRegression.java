package HandsOn5;

public class linearRegression {
    protected double nuevosVx = 0;
    protected int i = 0;

    public double[] calcularLinearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        double b1 = ((n * sumXY) - (sumX * sumY)) / ((n * sumX2) - (sumX * sumX));
        double b0 = (sumY - (b1 * sumX)) / n;
        double results[] = { b0, b1 };

        return results;
    }

    public void prediccionesS(double[] b, double[] nuevosx) {
        for (double x : nuevosx) {
            nuevosVx = i;
            double res = (b[0] + (b[1] * x));
            System.out.println("y = " + b[0] + " + " + b[1] + " * " + x + " = " + res);
        }
    }

}
