package HandsOn5;

public class linearAlgebra {


    public double[] gauss(double[][] matrix, double[] results) {
        int n = matrix.length;

        // Ampliar la matriz con los resultados (Augmented Matrix)
        double[][] augmentedMatrix = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = matrix[i][j];
            }
            augmentedMatrix[i][n] = results[i];
        }

        // Aplicar eliminación de Gauss
        for (int i = 0; i < n; i++) { 
            // Buscar el máximo en la columna i
            double maxEl = Math.abs(augmentedMatrix[i][i]); 
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmentedMatrix[k][i]) > maxEl) {
                    maxEl = Math.abs(augmentedMatrix[k][i]);
                    maxRow = k;
                }
            }

            // Intercambiar la fila actual con la fila del máximo
            for (int k = i; k < n + 1; k++) {
                double tmp = augmentedMatrix[maxRow][k];
                augmentedMatrix[maxRow][k] = augmentedMatrix[i][k];
                augmentedMatrix[i][k] = tmp;
            }

            // Hacer ceros en la columna i
            for (int k = i + 1; k < n; k++) {
                double c = -augmentedMatrix[k][i] / augmentedMatrix[i][i];
                for (int j = i; j < n + 1; j++) {
                    if (i == j) {
                        augmentedMatrix[k][j] = 0;
                    } else {
                        augmentedMatrix[k][j] += c * augmentedMatrix[i][j];
                    }
                }
            }
        }

        // Crear un arreglo para las soluciones
        double[] solution = new double[n];

        // Solucionar la matriz
        for (int i = n - 1; i >= 0; i--) {
            solution[i] = augmentedMatrix[i][n] / augmentedMatrix[i][i];
            for (int j = i + 1; j < n; j++) {
                solution[i] -= augmentedMatrix[i][j] * solution[j] / augmentedMatrix[i][i];



            }
        }
        for (int i = 0; i < solution.length / 2; i++) {
            double temp = solution[i];
            solution[i] = solution[solution.length - 1 - i];
            solution[solution.length - 1 - i] = temp;
        }

        return solution;
    }

    public double[][] calcularInversa(double[][] matriz) {
        double det = matriz[0][0] * (matriz[1][1] * matriz[2][2] - matriz[1][2] * matriz[2][1])
                - matriz[0][1] * (matriz[1][0] * matriz[2][2] - matriz[1][2] * matriz[2][0])
                + matriz[0][2] * (matriz[1][0] * matriz[2][1] - matriz[1][1] * matriz[2][0]);

        double[][] inversa = new double[3][3];

        inversa[0][0] = (matriz[1][1] * matriz[2][2] - matriz[1][2] * matriz[2][1]) / det;
        inversa[0][1] = (matriz[0][2] * matriz[2][1] - matriz[0][1] * matriz[2][2]) / det;
        inversa[0][2] = (matriz[0][1] * matriz[1][2] - matriz[0][2] * matriz[1][1]) / det;
        inversa[1][0] = (matriz[1][2] * matriz[2][0] - matriz[1][0] * matriz[2][2]) / det;
        inversa[1][1] = (matriz[0][0] * matriz[2][2] - matriz[0][2] * matriz[2][0]) / det;
        inversa[1][2] = (matriz[0][2] * matriz[1][0] - matriz[0][0] * matriz[1][2]) / det;
        inversa[2][0] = (matriz[1][0] * matriz[2][1] - matriz[1][1] * matriz[2][0]) / det;
        inversa[2][1] = (matriz[0][1] * matriz[2][0] - matriz[0][0] * matriz[2][1]) / det;
        inversa[2][2] = (matriz[0][0] * matriz[1][1] - matriz[0][1] * matriz[1][0]) / det;

        return inversa;
    }

    public double[] productoMatrizA(double[][] matriz, double[] a) {
        double[] resultado = new double[ matriz.length];

        for (int i = 0; i <  matriz.length; i++) {
            double suma = 0.0;
            for (int j = 0; j < matriz[0].length; j++) {
                suma += matriz[i][j] * a[j];
            }
            resultado[i] = suma;
        }

        for (int i = 0; i < resultado.length / 2; i++) {
            double temp = resultado[i];
            resultado[i] = resultado[resultado.length - 1 - i];
            resultado[resultado.length - 1 - i] = temp;
        }
        return resultado;
    }

}











