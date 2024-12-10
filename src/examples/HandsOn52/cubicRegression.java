package HandsOn5;

public class cubicRegression {
    protected double nuevosVx = 0;
    protected int i = 0;

    public double[] calcularCubicRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumX2 = 0, sumX3 = 0, sumX4 = 0, sumX5 = 0, sumX6 = 0;
        double sumY = 0, sumXY = 0, sumX2Y = 0, sumX3Y = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumX2 += x[i] * x[i];
            sumX3 += x[i] * x[i] * x[i];
            sumX4 += x[i] * x[i] * x[i] * x[i];
            sumX5 += x[i] * x[i] * x[i] * x[i] * x[i];
            sumX6 += x[i] * x[i] * x[i] * x[i] * x[i] * x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2Y += x[i] * x[i] * y[i];
            sumX3Y += x[i] * x[i] * x[i] * y[i];
        }

        double[][] matrix = {
                { n, sumX, sumX2, sumX3 },
                { sumX, sumX2, sumX3, sumX4 },
                { sumX2, sumX3, sumX4, sumX5 },
                { sumX3, sumX4, sumX5, sumX6 }
        };

        double[] results = { sumY, sumXY, sumX2Y, sumX3Y };

        linearAlgebra algebra = new linearAlgebra();
        double[] solution = algebra.gauss(matrix, results);
        return solution;
    }

    public void prediccionesC(double[] b, double[] nuevosx) {
        for (double x : nuevosx) {
            nuevosVx = i;
            double res = (b[0] * Math.pow(x, 3) + b[1] * Math.pow(x, 2) + b[2] * x + b[3]);
            System.out.println("y = " + b[0] + " * " + x + "^3 + " + b[1] + " * " + x + "^2 + " + b[2] + " * " + x
                    + " + " + b[3] + " = " + res);
        }
    }

}
