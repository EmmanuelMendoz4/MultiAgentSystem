package AlgoritmoGenetico;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.util.ArrayList;
import java.util.Random;

public class AgenteInicializarPoblacion extends Agent {
    @Override
    protected void setup() {
        
        System.out.println("Agente Inicializar Poblacion iniciado: " + getLocalName());

        // Agregar comportamiento para inicializar la población
        addBehaviour(new IniciarPoblacion());
    }

    private class IniciarPoblacion extends OneShotBehaviour {
        @Override
        public void action() {
            int tamPoblacion = 200;
            ArrayList<Individuo> poblacion = inicializarPoblacion(tamPoblacion);

            StringBuilder serializedPoblacion = new StringBuilder();
            for (Individuo individuo : poblacion) {
                serializedPoblacion.append(individuo.obtenerBeta0())
                        .append(",")
                        .append(individuo.obtenerBeta1())
                        .append(";");
            }

            // Enviar información de la población (simulado aquí)
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Algoritmo Genetico", AID.ISLOCALNAME)); // Nombre del agente receptor
            msg.setConversationId("poblacion-inicial"); 
            msg.setContent(serializedPoblacion.toString()); // envio de datos
            send(msg);
            System.out.println("Población inicial enviada al Agente de algoritmo genetico.");
        }

        private ArrayList<Individuo> inicializarPoblacion(int tam) {
            Random rand = new Random();
            ArrayList<Individuo> poblacionInicial = new ArrayList<>();
            for (int i = 0; i < tam; i++) {
                double beta0 = rand.nextDouble() * 200 - 100; // Valores entre -100 y 100
                double beta1 = rand.nextDouble() * 200 - 100;
                poblacionInicial.add(new Individuo(beta0, beta1));
            }
            return poblacionInicial;
        }
    }

    static class Individuo {
        private double beta0;
        private double beta1;

        public Individuo(double beta0, double beta1) {
            this.beta0 = beta0;
            this.beta1 = beta1;
        }

        public double obtenerBeta0() {
            return beta0;
        }

        public double obtenerBeta1() {
            return beta1;
        }
    }


    
}
