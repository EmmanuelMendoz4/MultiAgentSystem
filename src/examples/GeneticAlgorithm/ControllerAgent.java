package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.json.JSONObject;

import java.util.Arrays;

public class ControllerAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("ControllerAgent iniciado.");

        // Datos iniciales
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};

        // Crear mensaje para el GeneticAlgorithmAgent
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(getAID("GeneticAlgorithmAgent"));
        JSONObject json = new JSONObject();
        json.put("x", Arrays.toString(x));
        json.put("y", Arrays.toString(y));
        message.setContent(json.toString());
        send(message);

        // Comportamiento para recibir la respuesta del GeneticAlgorithmAgent
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage reply = receive();
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        String content = reply.getContent();
                        System.out.println("\nCoeficientes óptimos recibidos: " + content);

                        // Obtener los coeficientes del mensaje recibido
                        JSONObject json = new JSONObject(content);
                        JSONArray coefficientsArray = json.getJSONArray("Coefficients");
                        double coef1 = coefficientsArray.getDouble(0); // beta0
                        double coef2 = coefficientsArray.getDouble(1); // beta1

                        // Realizar las predicciones
                        double[] testX = {10.5, 20.3, 30.7, 40.2, 50.8}; // Valores de prueba
                        System.out.println("\nPredicciones basadas en los coeficientes:");
                        for (double testValue : testX) {
                            double prediction = coef1 + coef2 * testValue;
                            System.out.printf("y = %.2f + %.2f * (%.1f) = %.2f\n", coef1, coef2, testValue, prediction);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
}
