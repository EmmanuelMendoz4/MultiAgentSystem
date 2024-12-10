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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithmAgent extends Agent {

    private final double mutationRate = 0.01; // Tasa de mutación
    private final int tournamentSize = 5; // Tamaño del torneo para selección
    private final int populationSize = 100; // Tamaño de la población
    private final double crossoverRate = 0.95; // Tasa de cruce
    private final double fitnessThreshold = 0.95; // Umbral para detener el algoritmo
    private final int maxGenerations = 100; // Máximo número de generaciones

    @Override
    protected void setup() {

        System.out.println("Agente de Algoritmo Genético " + getLocalName() + " iniciado.");

        // Registrar el servicio de algoritmo genético
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("genetic-algorithm-service");
        sd.setName("genetic-algorithm");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReceiveDatasetBehaviour());
    }

    private double[] parseJsonArray(JSONArray jsonArray) {
        double[] array = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getDouble(i);
        }
        return array;
    }

    private String runGeneticAlgorithm(Data data) {
        ArrayList<Individual> population = initializePopulation();

        int generation = 0;
        while (generation < maxGenerations) {
            evaluatePopulation(population, data);

            // Verificar si se alcanza el umbral de fitness
            Individual best = getFittest(population);
            if (best.getFitness() >= fitnessThreshold) {
                System.out.println("\nUmbral alcanzado en la generación " + generation);
                break;
            }

            ArrayList<Individual> newPopulation = new ArrayList<>();

            // Reproducción
            while (newPopulation.size() < populationSize) {
                Individual parent1 = selectParentByTournament(population);
                Individual parent2 = selectParentByTournament(population);

                if (Math.random() < crossoverRate) {
                    newPopulation.addAll(crossover(parent1, parent2));
                } else {
                    newPopulation.add(new Individual(parent1));
                }
            }

            mutatePopulation(newPopulation);
            population = newPopulation;
            generation++;
        }

        // Retornar los mejores coeficientes encontrados
        Individual best = getFittest(population);
        double[] coefficients = {best.getBeta0(), best.getBeta1()};

        JSONObject result = new JSONObject();
        result.put("Coefficients", coefficients);

        return result.toString();
    }

    private ArrayList<Individual> initializePopulation() {
        ArrayList<Individual> population = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < populationSize; i++) {
            double beta0 = rand.nextDouble() * 200 - 100; // Valores entre -100 y 100
            double beta1 = rand.nextDouble() * 200 - 100;
            population.add(new Individual(beta0, beta1));
        }
        return population;
    }

    private void evaluatePopulation(ArrayList<Individual> population, Data data) {
        for (Individual individual : population) {
            individual.calculateFitness(data);
        }
    }

    private Individual selectParentByTournament(ArrayList<Individual> population) {
        Random rand = new Random();
        ArrayList<Individual> tournament = new ArrayList<>();

        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }

        return tournament.stream().max((i1, i2) -> Double.compare(i1.getFitness(), i2.getFitness())).orElse(null);
    }

    private ArrayList<Individual> crossover(Individual parent1, Individual parent2) {
        ArrayList<Individual> offspring = new ArrayList<>();
        Random rand = new Random();

        // Promediar los genes de los padres
        double beta0 = (parent1.getBeta0() + parent2.getBeta0()) / 2.0;
        double beta1 = (parent1.getBeta1() + parent2.getBeta1()) / 2.0;

        offspring.add(new Individual(beta0, beta1));
        return offspring;
    }

    private void mutatePopulation(ArrayList<Individual> population) {
        Random rand = new Random();
        for (Individual individual : population) {
            if (Math.random() < mutationRate) {
                individual.mutate(rand);
            }
        }
    }

    private Individual getFittest(ArrayList<Individual> population) {
        return population.stream().max((i1, i2) -> Double.compare(i1.getFitness(), i2.getFitness())).orElse(null);
    }

    private class ReceiveDatasetBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getConversationId().equals("genetic-analysis")) {

                // Procesar datos recibidos
                JSONObject json = new JSONObject(msg.getContent());
                JSONArray xArray = json.getJSONArray("x1");
                JSONArray yArray = json.getJSONArray("y");
                
                double[] x = parseJsonArray(xArray);
                double[] y = parseJsonArray(yArray);
                
                //Comprobación de los valores recibidos
                System.out.println("Valores recibidos para x: " + Arrays.toString(x));
                System.out.println("Valores recibidos para y: " + Arrays.toString(y));
                 
                
                Data data = new Data(x, y);

                // Ejecutar el algoritmo genético
                String result = runGeneticAlgorithm(data);

                // Enviar los resultados al agente solicitante
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(result);
                send(reply);
                System.out.println("Coeficientes calculados enviados al agente solicitante.");
            } else {
                block();
            }
        }
    }

    static class Data {
        double[] x;
        double[] y;

        public Data(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Individual {
        private double beta0;
        private double beta1;
        private double fitness;

        public Individual(double beta0, double beta1) {
            this.beta0 = beta0;
            this.beta1 = beta1;
        }

        public Individual(Individual other) {
            this.beta0 = other.beta0;
            this.beta1 = other.beta1;
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
            double meanY = Arrays.stream(data.y).average().orElse(0.0);
            double ssTotal = Arrays.stream(data.y).map(yi -> Math.pow(yi - meanY, 2)).sum();
            double ssResidual = 0.0;

            for (int i = 0; i < data.x.length; i++) {
                double prediction = beta0 + beta1 * data.x[i];
                ssResidual += Math.pow(data.y[i] - prediction, 2);
            }

            fitness = ssTotal > 0 ? 1 - (ssResidual / ssTotal) : 0;
        }

        public void mutate(Random rand) {
            if (rand.nextBoolean()) {
                beta0 += rand.nextGaussian();
            } else {
                beta1 += rand.nextGaussian();
            }
        }
    }
}
