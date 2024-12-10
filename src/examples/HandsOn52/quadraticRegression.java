package HandsOn5;

public class quadraticRegression {
    private double nuevosVx = 0;    
    private int i = 0;

    public double[] calcularQuadraticRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumX2 = 0, sumX3 = 0, sumX4 = 0;
        double sumY = 0, sumXY = 0, sumX2Y = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumX2 += x[i] * x[i];
            sumX3 += x[i] * x[i] * x[i];
            sumX4 += x[i] * x[i] * x[i] * x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2Y += (x[i] * x[i]) * y[i];
        }

        double[][] matrix = {
                { n, sumX, sumX2 },
                { sumX, sumX2, sumX3 },
                { sumX2, sumX3, sumX4 }
        };

        double[] results = { sumY, sumXY, sumX2Y };
        linearAlgebra algebra = new linearAlgebra();
        double matrizQ[][] = algebra.calcularInversa(matrix);
        double resultQ[] = algebra.productoMatrizA(matrizQ, results);

        return resultQ;
    }

    public void prediccionesQ(double[] b, double[] nuevosx) {
        for (double x : nuevosx) {
            nuevosVx = x;
            double res = ((b[0] * Math.pow(x, 2)) + (b[1] * x) + b[2]);
            System.out.println("y = " + b[0] + " * " + x + "^2 +" + b[1] + " * " + x + " + " + b[2] + " = " + res);
        }
    }

}
