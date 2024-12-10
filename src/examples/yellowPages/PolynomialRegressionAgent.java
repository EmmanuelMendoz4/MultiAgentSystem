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

public class PolynomialRegressionAgent extends Agent {

    protected void setup() {
        System.out.println("Agente " + getLocalName() + " iniciado.");

        // Registrar el servicio de regresión polinomial
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        // Establece el tipo del servicio
        sd.setType("polynomial-regression-service");
        // Asigna un nombre al servicio
        sd.setName("polynomial-regression-service");
        dfd.addServices(sd);
        try {
            // registra el agente
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReceiveDatasetBehaviour());
    }

    // convierte un JSONArray (arreglo en formato JSON) a un arreglo de double en
    // Java
    public double[] parseJsonArray(JSONArray jsonArray) {
        double[] array = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getDouble(i);
        }
        return array;
    }

    private String performPolynomialRegression(double[] x, double[] y) {
        DiscreteMathematics discreteMath = new DiscreteMathematics();
        LinearAlgebra algebra = new LinearAlgebra();

        double sumX = discreteMath.sum(x);
        double sumY = discreteMath.sum(y);
        double sumX2 = discreteMath.sumOfSquares(x);
        double sumX3 = discreteMath.sumOfCubes(x);
        double sumX4 = discreteMath.sumOfFourthPowers(x);
        double sumXY = discreteMath.sumOfProducts(x, y);
        double sumX2Y = discreteMath.sumOfSquaredProducts(x, y);

        double[][] matrix = {
                { x.length, sumX, sumX2 },
                { sumX, sumX2, sumX3 },
                { sumX2, sumX3, sumX4 }
        };

        double[][] inverseMatrix = algebra.calculateInverse(matrix);

        double[] b = { sumY, sumXY, sumX2Y };
        double[] coefficients = algebra.matrixProduct(inverseMatrix, b);

        // Crear un JSON con los resultados de la regresión
        JSONObject regressionResult = new JSONObject();
        regressionResult.put("Coefficients", coefficients);

        return regressionResult.toString();
    }

    public class LinearAlgebra {
        public double[][] calculateInverse(double[][] matrix) {
            double det = matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
                    - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
                    + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);

            double[][] inverse = new double[3][3];

            inverse[0][0] = (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) / det;
            inverse[0][1] = (matrix[0][2] * matrix[2][1] - matrix[0][1] * matrix[2][2]) / det;
            inverse[0][2] = (matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1]) / det;
            inverse[1][0] = (matrix[1][2] * matrix[2][0] - matrix[1][0] * matrix[2][2]) / det;
            inverse[1][1] = (matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0]) / det;
            inverse[1][2] = (matrix[0][2] * matrix[1][0] - matrix[0][0] * matrix[1][2]) / det;
            inverse[2][0] = (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]) / det;
            inverse[2][1] = (matrix[0][1] * matrix[2][0] - matrix[0][0] * matrix[2][1]) / det;
            inverse[2][2] = (matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]) / det;
            return inverse;
        }

        public double[] matrixProduct(double[][] matrix, double[] a) {
            double[] result = new double[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                double sum = 0.0;
                for (int j = 0; j < matrix[0].length; j++) {
                    sum += matrix[i][j] * a[j];
                }
                result[i] = sum;
            }
            return result;
        }
    }

    public class DiscreteMathematics {
        public double sum(double[] a) {
            double r = 0;
            for (double v : a) {
                r += v;
            }
            return r;
        }

        public double sumOfSquares(double[] a) {
            double r = 0;
            for (double num : a) {
                r += Math.pow(num, 2);
            }
            return r;
        }

        public double sumOfCubes(double[] a) {
            double r = 0;
            for (double num : a) {
                r += Math.pow(num, 3);
            }
            return r;
        }

        public double sumOfFourthPowers(double[] a) {
            double r = 0;
            for (double num : a) {
                r += Math.pow(num, 4);
            }
            return r;
        }

        public double sumOfProducts(double[] a, double[] b) {
            double r = 0;
            for (int i = 0; i < a.length; i++) {
                r += a[i] * b[i];
            }
            return r;
        }

        public double sumOfSquaredProducts(double[] a, double[] b) {
            double r = 0;
            for (int i = 0; i < a.length; i++) {
                r += Math.pow(a[i], 2) * b[i];
            }
            return r;
        }
    }

    private class ReceiveDatasetBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            // método que intenta recibir un mensaje.
            ACLMessage msg = receive();
            // Asegura que efectivamente se ha recibido un mensaje
            // Y que es parte de una conversación de análisis de regresión
            if (msg != null && msg.getConversationId().equals("regression-analysis")) {
                JSONObject json = new JSONObject(msg.getContent());
                JSONArray xArray = json.getJSONArray("x1");
                JSONArray yArray = json.getJSONArray("y");

                double[] xValues = parseJsonArray(xArray);
                double[] yValues = parseJsonArray(yArray);

                // Realizar regresión y obtener los resultados
                String regressionResult = performPolynomialRegression(xValues, yValues);

                // Enviar los resultados al agente solicitante
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(regressionResult);
                send(reply);
                System.out.println("Resultados de regresión polinomial enviados al agente solicitante.");
            } else {
                block();
            }
        }
    }
}
