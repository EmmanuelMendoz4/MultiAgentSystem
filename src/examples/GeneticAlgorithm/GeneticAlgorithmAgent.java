package agents;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class GeneticAlgorithmAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("GeneticAlgorithmAgent iniciado.");

        // Comportamiento para recibir datos y ejecutar el algoritmo genético
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    // Parse the JSON content from the received message
                    JSONObject json = new JSONObject(msg.getContent());
                    double[] x = parseJsonArray(json.getString("x"));
                    double[] y = parseJsonArray(json.getString("y"));

                    Data data = new Data(x, y);
                    Individual best = runGeneticAlgorithm(data);

                    // Enviar resultados al ControllerAgent
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);

                    // Crear el mensaje JSON correctamente
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("Coefficients", new double[]{best.getBeta0(), best.getBeta1()});
                    reply.setContent(resultJson.toString());  // Asegúrate de usar el formato JSON adecuado
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }

    private Individual runGeneticAlgorithm(Data data) {
        ArrayList<Individual> population = initializePopulation(100);
        int generations = 100;
        double threshold = 0.95;

        for (int gen = 0; gen < generations; gen++) {
            evaluatePopulation(population, data);

            Individual best = getFittest(population);
            System.out.println("Generación " + gen + " | Mejor fitness: " + best.getFitness());

            if (best.getFitness() >= threshold) {
                return best;
            }

            population = reproduce(population);
        }

        return getFittest(population);
    }

    private ArrayList<Individual> initializePopulation(int size) {
        ArrayList<Individual> population = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            population.add(new Individual(rand.nextDouble() * 200 - 100, rand.nextDouble() * 200 - 100));
        }
        return population;
    }

    private void evaluatePopulation(ArrayList<Individual> population, Data data) {
        for (Individual individual : population) {
            individual.calculateFitness(data);
        }
    }

    private ArrayList<Individual> reproduce(ArrayList<Individual> population) {
        Random rand = new Random();
        ArrayList<Individual> newPopulation = new ArrayList<>();
        while (newPopulation.size() < population.size()) {
            Individual parent1 = selectParent(population, rand);
            Individual parent2 = selectParent(population, rand);

            double beta0 = (parent1.getBeta0() + parent2.getBeta0()) / 2;
            double beta1 = (parent1.getBeta1() + parent2.getBeta1()) / 2;
            newPopulation.add(new Individual(beta0, beta1));
        }
        return newPopulation;
    }

    private Individual selectParent(ArrayList<Individual> population, Random rand) {
        return population.get(rand.nextInt(population.size()));
    }

    private Individual getFittest(ArrayList<Individual> population) {
        return population.stream().max((a, b) -> Double.compare(a.getFitness(), b.getFitness())).orElse(null);
    }

    private double[] parseJsonArray(String arrayStr) {
        arrayStr = arrayStr.replace("[", "").replace("]", "");
        String[] parts = arrayStr.split(",");
        double[] array = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            array[i] = Double.parseDouble(parts[i].trim());
        }
        return array;
    }

    // Clases auxiliares internas
    private static class Data {
        double[] x;
        double[] y;

        public Data(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }

        public double[] getX() {
            return x;
        }

        public double[] getY() {
            return y;
        }
    }

    private static class Individual {
        private double beta0;
        private double beta1;
        private double fitness;

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

        public double getFitness() {
            return fitness;
        }

        public void calculateFitness(Data data) {
            double[] x = data.getX();
            double[] y = data.getY();

            double meanY = Arrays.stream(y).average().orElse(0.0);
            double ssTotal = Arrays.stream(y).map(yi -> Math.pow(yi - meanY, 2)).sum();
            double ssResidual = 0.0;

            for (int i = 0; i < x.length; i++) {
                double prediction = beta0 + beta1 * x[i];
                ssResidual += Math.pow(y[i] - prediction, 2);
            }

            fitness = ssTotal > 0 ? 1 - (ssResidual / ssTotal) : 0;
        }
    }
}
