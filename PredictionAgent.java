package SystemGeneticAlgorithm;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PredictionAgent extends Agent {
    @Override
    protected void setup() {
        doWait(5000);
        System.out.println("AgentePredicciones iniciado: " + getLocalName());
        addBehaviour(new PredictionBehaviour());
    }

    private class PredictionBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive();
            if (message != null) {
                processMessage(message);
            } else {
                block();
            }
        }

        private void processMessage(ACLMessage message) {
            if ("predictions".equals(message.getConversationId())) {
                handleContent(message.getContent());
            } else {
                System.out.println("Mensaje no reconocido. ID de conversación: " + message.getConversationId());
            }
        }

        private void handleContent(String content) {
            String[] parts = content.split(",");
            double b0 = parseDouble(parts[0]);
            double b1 = parseDouble(parts[1]);
            double rSquared = parseDouble(parts[2]);

            System.out.println("B0 = " + b0 + ", B1 = " + b1 + ", R^2 = " + rSquared);

            generatePredictions(b0, b1, rSquared);
        }

        private double parseDouble(String value) {
            return Double.parseDouble(value);
        }

        private void generatePredictions(double intercept, double slope, double rSquared) {
            double[] xValues = getXValues();
            for (double x : xValues) {
                double y = calculateY(intercept, slope, x);
                System.out.println("y = " + intercept + " + " + slope + " * " + x + " = " + y);
            }
            System.out.println("R^2 = " + rSquared);
        }

        private double[] getXValues() {
            return new double[]{1, 2, 3, 4, 5};
        }

        private double calculateY(double intercept, double slope, double x) {
            return intercept + slope * x;
        }
    }
}
