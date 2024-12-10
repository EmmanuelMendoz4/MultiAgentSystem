package AlgoritmoGenetico;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class AgentePredicciones extends Agent {
    @Override
    protected void setup() {
        doWait(5000);
        System.out.println("AgentePredicciones iniciado: " + getLocalName());

        // Agregar comportamiento para recibir mensajes y realizar predicciones
        addBehaviour(new Predicciones());
    }

    private class Predicciones extends OneShotBehaviour {
        @Override
        public void action() {
             ACLMessage msg = receive();
            if (msg != null && "predicciones".equals(msg.getConversationId())) {
           // Procesar coeficientes recibidos
            String[] coeficientes = msg.getContent().split(",");
            double b0 = Double.parseDouble(coeficientes[0]);
            double b1 = Double.parseDouble(coeficientes[1]);
            double r2 = Double.parseDouble(coeficientes[2]);
            System.out.printf("Coeficientes recibidos:\n");
            System.out.printf( "B0: "+b0 + " B1: " + b1 + " r2: " + r2 + "\n");

          
                // Realizar predicciones
                double[] nuevosX = {1, 2, 3, 4, 5};
                for (double x : nuevosX) {
                    double prediccion = b0 + b1 * x;
                    System.out.printf("y = " + b0 + " + " + b1 +" * " + x + " = " + prediccion +"\n");
                }
                System.out.println("\nR^2 = "+ r2);
            }else if (msg != null) {
                System.out.println("Mensaje no reconocido. ID de conversación: " + msg.getConversationId());
            } else {
                block(); // Bloquear hasta recibir un mensaje
            }
        }
    }
}
