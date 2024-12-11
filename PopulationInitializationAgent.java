package SystemGeneticAlgorithm;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.util.Random;

public class PopulationInitializationAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Agente Inicializar Poblacion iniciado: " + getLocalName());
        addBehaviour(new InitializePopulationBehaviour());
    }

    private class InitializePopulationBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            int populationSize = 200;
            Individual[] population = initializePopulation(populationSize);

            StringBuilder serializedPopulation = new StringBuilder();
            for (Individual individual : population) {
                serializedPopulation.append(individual.getBeta0())
                        .append(",")
                        .append(individual.getBeta1())
                        .append(";");
            }

            // Enviar información de la población (simulado aquí)
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Algoritmo Genetico", AID.ISLOCALNAME)); // Nombre del agente receptor
            msg.setConversationId("poblacion-inicial"); 
            msg.setContent(serializedPopulation.toString()); // envio de datos
            send(msg);
            System.out.println("Población inicial enviada al Agente de algoritmo genetico.");
        }

        private Individual[] initializePopulation(int size) {
            Random rand = new Random();
            Individual[] initialPopulation = new Individual[size];
            for (int i = 0; i < size; i++) {
                double beta0 = rand.nextDouble() * 200 - 100; // Valores entre -100 y 100
                double beta1 = rand.nextDouble() * 200 - 100;
                initialPopulation[i] = new Individual(beta0, beta1);
            }
            return initialPopulation;
        }
    }

    static class Individual {
        private double beta0;
        private double beta1;

        public Individual(double beta0, double beta1) {
            this.beta0 = beta0;
            this.beta1 = beta1;
        }

        public double getBeta0() {
            return beta0;
        }

        public double getBeta1() {
            return beta1;
        }
    }
}
