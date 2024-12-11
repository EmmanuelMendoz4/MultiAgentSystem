package SystemGeneticAlgorithm;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.Random;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class GeneticAlgorithmAgent extends Agent {

    private double mutationRate = 0.05; // Tasa de mutación
    private Random randomGenerator = new Random();
    private Individual[] initialPopulation;

    @Override
    protected void setup() {
        doWait(2000);
        
        System.out.println("Agente de Algoritmo Genético " + getLocalName() + " iniciado.");

        // Agregar comportamiento principal
        addBehaviour(new ExecuteGeneticAlgorithm());
    }

    // Clase Dataset para representar el dataset
    static class Dataset {
        double[] xValues;
        double[] yValues;

        public Dataset() {
            this.xValues = new double[]{23, 26, 30, 34, 43, 48, 52, 57, 58};
            this.yValues = new double[]{651, 762, 856, 1063, 1190, 1298, 1421, 1440, 1518};
        }
    }

    private double[] runGeneticAlgorithm(Dataset dataset, Individual[] population) {
        int populationSize = population.length;
        int maxGenerations = 3000;
        double crossoverRate = 0.95;

        int generation = 0;

        // Bucle evolutivo
        while (generation < maxGenerations) {
            Individual[] newPopulation = new Individual[populationSize];

            // Elitismo
            Individual bestIndividual = getFittest(population);
            newPopulation[0] = bestIndividual;

            // Cruce y mutación
            for (int i = 1; i < populationSize; i++) {
                Individual parent1 = selectParent(population);
                Individual parent2 = selectParent(population);
                Individual offspring = crossover(parent1, parent2, crossoverRate);
                mutate(offspring);
                newPopulation[i] = offspring;
            }
            evaluatePopulation(newPopulation, dataset);

            // Actualizar la población
            population = newPopulation;
            generation++;
        }

        // Evaluar la mejor solución
        Individual bestIndividual = getFittest(population);
        return new double[]{bestIndividual.getBeta0(), bestIndividual.getBeta1(), bestIndividual.getFitness()};
    }

    private Individual selectParent(Individual[] population) {
        // Implementar selección de padres (por ejemplo, ruleta o torneo)
        return population[randomGenerator.nextInt(population.length)];
    }

    private Individual crossover(Individual parent1, Individual parent2, double crossoverRate) {
        // Implementar cruce de individuos
        if (randomGenerator.nextDouble() < crossoverRate) {
            double beta0 = (parent1.getBeta0() + parent2.getBeta0()) / 2;
            double beta1 = (parent1.getBeta1() + parent2.getBeta1()) / 2;
            return new Individual(beta0, beta1);
        } else {
            return randomGenerator.nextBoolean() ? parent1 : parent2;
        }
    }

    private void mutate(Individual individual) {
        // Implementar mutación de individuos
        if (randomGenerator.nextDouble() < mutationRate) {
            individual.setBeta0(individual.getBeta0() + randomGenerator.nextGaussian());
            individual.setBeta1(individual.getBeta1() + randomGenerator.nextGaussian());
        }
    }

    private void evaluatePopulation(Individual[] population, Dataset dataset) {
        // Implementar evaluación de la población
        for (Individual individual : population) {
            individual.calculateFitness(dataset);
        }
    }

    private Individual getFittest(Individual[] population) {
        // Implementar evaluación de la mejor solución
        Individual best = population[0];
        for (Individual individual : population) {
            if (individual.getFitness() > best.getFitness()) {
                best = individual;
            }
        }
        return best;
    }

    private class ExecuteGeneticAlgorithm extends OneShotBehaviour {
        @Override
        public void action() {
            Dataset dataset = new Dataset();
            initialPopulation = createInitialPopulation(200);
            double[] solution = runGeneticAlgorithm(dataset, initialPopulation);

            // Enviar la mejor solución encontrada
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("PredictionAgent", AID.ISLOCALNAME));
            msg.setConversationId("predictions");
            msg.setContent(solution[0] + "," + solution[1] + "," + solution[2]);
            send(msg);
            System.out.println("Mejor solución enviada al PredictionAgent.");
        }

        private Individual[] createInitialPopulation(int size) {
            Individual[] population = new Individual[size];
            for (int i = 0; i < size; i++) {
                double beta0 = randomGenerator.nextDouble() * 200 - 100;
                double beta1 = randomGenerator.nextDouble() * 200 - 100;
                population[i] = new Individual(beta0, beta1);
            }
            return population;
        }
    }

    static class Individual {
        private double beta0;
        private double beta1;
        private double fitness;

        public Individual(double beta0, double beta1) {
            this.beta0 = beta0;
            this.beta1 = beta1;
            this.fitness = 0;
        }

        public double getBeta0() {
            return beta0;
        }

        public void setBeta0(double beta0) {
            this.beta0 = beta0;
        }

        public double getBeta1() {
            return beta1;
        }

        public void setBeta1(double beta1) {
            this.beta1 = beta1;
        }

        public double getFitness() {
            return fitness;
        }

        public void calculateFitness(Dataset dataset) {
            // Implementar cálculo de fitness
            double error = 0;
            for (int i = 0; i < dataset.xValues.length; i++) {
                double predictedY = beta0 + beta1 * dataset.xValues[i];
                error += Math.pow(predictedY - dataset.yValues[i], 2);
            }
            this.fitness = 1 / (1 + error);
        }
    }
}
