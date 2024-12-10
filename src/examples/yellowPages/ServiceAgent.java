package examples.yellowPages;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Arrays;

public class ServiceAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("\nAgente " + getLocalName() + " iniciado.");

        // Registrar el servicio de clasificación
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("classification-service");
        sd.setName("classification-service");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Añadir comportamiento cíclico para recibir solicitudes
        addBehaviour(new OfferClassificationBehaviour());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\n\nEl agente " + getAID().getName() + " ya no ofrece sus servicios.");
    }

    private class OfferClassificationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getConversationId().equals("classification-analysis")) {
                JSONObject json = new JSONObject(msg.getContent());
                JSONArray yArray = json.getJSONArray("y");
                double[] y = parseJsonArray(yArray);

                double[] x1 = null;
                double[] x2 = null;

                if (json.has("x1") && json.has("x2") && json.getJSONArray("x2").length() > 0) {
                    x1 = parseJsonArray(json.getJSONArray("x1"));
                    x2 = parseJsonArray(json.getJSONArray("x2"));
                } else if (json.has("x1")) {
                    x1 = parseJsonArray(json.getJSONArray("x1"));
                }

                String regressionType = classifyRegression(x1, x2, y);

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(regressionType);
                send(reply);
                System.out.println("Análisis recomendado enviado: " + regressionType);
            } else {
                block();
            }
        }

        private double[] parseJsonArray(JSONArray jsonArray) {
            double[] array = new double[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = jsonArray.getDouble(i);
            }
            return array;
        }
    }

    private String classifyRegression(double[] x1, double[] x2, double[] y) {
        if (x2 != null && x2.length > 0) {
            double correlationX1 = calculatePearsonCorrelation(x1, y);
            double correlationX2 = calculatePearsonCorrelation(x2, y);

            System.out.println("Correlaciones (Múltiple): X1=" + correlationX1 + ", X2=" + correlationX2);

            if (Math.abs(correlationX1) > 0.7 && Math.abs(correlationX2) > 0.7) {
                return "Multiple Linear Regression";
            }
        } else if (x1 != null) {
            double r2Linear = calculateR2(x1, y, 1);
            double r2Polynomial = calculateR2(x1, y, 2);

            double mseLinear = calculateMSE(x1, y, 1);
            double msePolynomial = calculateMSE(x1, y, 2);

            System.out.println("R² Lineal: " + r2Linear);
            System.out.println("R² Polinomial: " + r2Polynomial);

            if ((r2Polynomial - r2Linear > 0.02) && (msePolynomial < mseLinear)) {
                return "Polynomial Regression";
            } else if (r2Linear > 0.85) {
                return "Simple Linear Regression";
            }
        }

        return "Regresion no encontrada";
    }

    private double calculatePearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return denominator == 0 ? 0 : numerator / denominator;
    }

    private double calculateR2(double[] x, double[] y, int degree) {
        double[] coefficients = fitRegression(x, y, degree);
        double meanY = Arrays.stream(y).average().orElse(0);
        double totalSS = 0.0;
        double residualSS = 0.0;

        for (int i = 0; i < x.length; i++) {
            double prediction = coefficients[0];
            for (int j = 1; j <= degree; j++) {
                prediction += coefficients[j] * Math.pow(x[i], j);
            }
            totalSS += Math.pow(y[i] - meanY, 2);
            residualSS += Math.pow(y[i] - prediction, 2);
        }

        return 1 - (residualSS / totalSS);
    }

    private double calculateMSE(double[] x, double[] y, int degree) {
        double[] coefficients = fitRegression(x, y, degree);
        double mse = 0.0;

        for (int i = 0; i < x.length; i++) {
            double prediction = coefficients[0];
            for (int j = 1; j <= degree; j++) {
                prediction += coefficients[j] * Math.pow(x[i], j);
            }
            mse += Math.pow(y[i] - prediction, 2);
        }

        return mse / x.length;
    }

    private double[] fitRegression(double[] x, double[] y, int degree) {
        int n = x.length;
        double[][] matrix = new double[n][degree + 1];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= degree; j++) {
                matrix[i][j] = Math.pow(x[i], j);
            }
        }

        double[][] xtx = multiplyMatrices(transposeMatrix(matrix), matrix);
        double[] xty = multiplyMatrixVector(transposeMatrix(matrix), y);
        double[][] xtxInverse = invertMatrix(xtx);

        return multiplyMatrixVector(xtxInverse, xty);
    }

    private double[][] transposeMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    private double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private double[] multiplyMatrixVector(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    private double[][] invertMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] identity = new double[n][n];
        for (int i = 0; i < n; i++) {
            identity[i][i] = 1;
        }

        for (int i = 0; i < n; i++) {
            int max = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(matrix[k][i]) > Math.abs(matrix[max][i])) {
                    max = k;
                }
            }

            double[] temp = matrix[i];
            matrix[i] = matrix[max];
            matrix[max] = temp;

            temp = identity[i];
            identity[i] = identity[max];
            identity[max] = temp;

            // Verificar si la matriz es singular
            if (Math.abs(matrix[i][i]) <= 1e-10) {
                throw new ArithmeticException("La matriz es singular."); // No se puede invertir
            }

            double factor = matrix[i][i]; 
            for (int j = 0; j < n; j++) { 
                matrix[i][j] /= factor;
                identity[i][j] /= factor;
            }

            for (int k = 0; k < n; k++) { 
                if (k != i) {
                    factor = matrix[k][i];
                    for (int j = 0; j < n; j++) {
                        matrix[k][j] -= factor * matrix[i][j];
                        identity[k][j] -= factor * identity[i][j];
                    }
                }
            }
        }

        return identity;
    }
}
