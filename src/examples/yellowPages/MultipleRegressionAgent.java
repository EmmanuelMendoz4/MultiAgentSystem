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

public class MultipleRegressionAgent extends Agent {

    protected void setup() {
        System.out.println("Agente " + getLocalName() + " iniciado.");

        // Registrar el servicio de regresión múltiple
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        // Establece el tipo del servicio
        sd.setType("multiple-regression-service");
        // Asigna un nombre al servicio
        sd.setName("multiple-regression-service");
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

    private String performMultipleRegression(double[] x1, double[] x2, double[] y) {
        DiscreteMathematics discreteMath = new DiscreteMathematics();
        LinearAlgebra algebra = new LinearAlgebra();

        double sumX1 = discreteMath.sum(x1);
        double sumX2 = discreteMath.sum(x2);
        double sumY = discreteMath.sum(y);
        double sumX1Squared = discreteMath.sumOfSquares(x1);
        double sumX2Squared = discreteMath.sumOfSquares(x2);
        double sumX1X2 = discreteMath.sumOfProducts(x1, x2);
        double sumX1Y = discreteMath.sumOfProducts(x1, y);
        double sumX2Y = discreteMath.sumOfProducts(x2, y);

        double[][] matrix = {
                { x1.length, sumX1, sumX2 },
                { sumX1, sumX1Squared, sumX1X2 },
                { sumX2, sumX1X2, sumX2Squared }
        };

        double[][] inverseMatrix = algebra.calculateInverse(matrix);

        double[] b = { sumY, sumX1Y, sumX2Y };
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

        public double sumOfProducts(double[] a, double[] b) {
            double r = 0;
            for (int i = 0; i < a.length; i++) {
                r += a[i] * b[i];
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
                JSONArray x1Array = json.getJSONArray("x1");
                JSONArray x2Array = json.getJSONArray("x2");
                JSONArray yArray = json.getJSONArray("y");

                double[] x1Values = parseJsonArray(x1Array);
                double[] x2Values = parseJsonArray(x2Array);
                double[] yValues = parseJsonArray(yArray);

                // Realizar regresión y obtener los resultados
                String regressionResult = performMultipleRegression(x1Values, x2Values, yValues);

                // Enviar los resultados al agente solicitante
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(regressionResult);
                send(reply);
                System.out.println("Resultados de regresión múltiple enviados al agente solicitante.");
            } else {
                block();
            }
        }
    }
}
