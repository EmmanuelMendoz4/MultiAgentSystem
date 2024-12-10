package AlgoritmoGenetico;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.ArrayList;
import java.util.Random;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class AlgoritmoGenetico extends Agent {

    private double tasaMutacion = 0.05; // Tasa de mutación
    private Random rand = new Random();
    private ArrayList<Individuo> poblacionInicial;

    @Override
    protected void setup() {
        doWait(2000);
        
        System.out.println("Agente de Algoritmo Genético " + getLocalName() + " iniciado.");

        // Agregar comportamiento principal
        addBehaviour(new CalcularAlgoritmoGenetico());
    }

    // Clase Data para representar el dataset
    static class Data {
        double[] x;
        double[] y;

        public Data() {
            this.x = new double[]{23,26,30,34,43,48,52,57,58};//advertising
            this.y = new double[]{651,762,856,1063,1190,1298,1421,1440,1518};//sales
        }
    }

    private double[] resolverAlgoritmoGenetico(Data data, ArrayList<Individuo> poblacion) {
        int tamPoblacion = poblacion.size();
        int limiteGeneraciones = 3000;
        double tasaCruce = 0.95;

        int generacion = 0;

        // Bucle evolutivo
        while (generacion < limiteGeneraciones) {
            ArrayList<Individuo> nuevaPoblacion = new ArrayList<>();

            // Elitismo
            Individuo mejorIndividuo = obtenerMasApto(poblacion);
            nuevaPoblacion.add(mejorIndividuo);

            // Cruce y mutación
            while (nuevaPoblacion.size() < tamPoblacion) {
                Individuo padre1 = ruleta(poblacion);
                Individuo padre2 = ruleta(poblacion);
                ArrayList<Individuo> hijos = cruzar(padre1, padre2, tasaCruce);
                nuevaPoblacion.addAll(hijos);
            }
            mutarPoblacion(nuevaPoblacion);
            evaluarPoblacion(nuevaPoblacion, data);

            // Actualizar la población
            poblacion = nuevaPoblacion;
            generacion++;
        }

        // Obtener el mejor individuo
        Individuo mejor = obtenerMasApto(poblacion);

        double b0 = mejor.obtenerBeta0();
        double b1 = mejor.obtenerBeta1();
        double r2 = mejor.obtenerAptitud();

        return new double[]{b0, b1, r2};
    }


    private void evaluarPoblacion(ArrayList<Individuo> nuevaPoblacion, Data data) {
        for (Individuo individuo : nuevaPoblacion) {
            individuo.evaluarAptitud(data);
        }
    }

    private Individuo ruleta(ArrayList<Individuo> poblacion) {
        double totalAptitud = poblacion.stream().mapToDouble(Individuo::obtenerAptitud).sum();
        double umbral = rand.nextDouble() * totalAptitud;

        double acumulado = 0.0;
        for (Individuo individuo : poblacion) {
            acumulado += individuo.obtenerAptitud();
            if (acumulado >= umbral) {
                return individuo;
            }
        }
        return poblacion.get(rand.nextInt(poblacion.size()));
    }

    private ArrayList<Individuo> cruzar(Individuo padre1, Individuo padre2, double tasaCruce) {
        ArrayList<Individuo> descendencia = new ArrayList<>();
        if (rand.nextDouble() < tasaCruce) {
            double beta0 = rand.nextBoolean() ? padre1.obtenerBeta0() : padre2.obtenerBeta0();
            double beta1 = rand.nextBoolean() ? padre1.obtenerBeta1() : padre2.obtenerBeta1();
            descendencia.add(new Individuo(beta0, beta1));
        } else {
            descendencia.add(new Individuo(padre1.obtenerBeta0(), padre1.obtenerBeta1()));
            descendencia.add(new Individuo(padre2.obtenerBeta0(), padre2.obtenerBeta1()));
        }
        return descendencia;
    }

    private void mutarPoblacion(ArrayList<Individuo> poblacion) {
        for (Individuo individuo : poblacion) {
            if (rand.nextDouble() < tasaMutacion) {
                individuo.mutar(rand);
            }
        }
    }

    private Individuo obtenerMasApto(ArrayList<Individuo> poblacion) {
        return poblacion.stream().max((i1, i2) -> Double.compare(i1.obtenerAptitud(), i2.obtenerAptitud())).orElse(null);
    }

    static class Individuo {
        private double beta0;
        private double beta1;
        private double aptitud;

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

        public double obtenerAptitud() {
            return aptitud;
        }

        public void evaluarAptitud(Data data) {
             // Calcular el promedio de y manualmente
    double sumaY = 0;
    for (double valorY : data.y) {
        sumaY += valorY;
    }
    double meanY = sumaY / data.y.length;

    // Calcular SS Total y SS 
    double ssTotal = 0, ssResidual = 0;
    for (int i = 0; i < data.x.length; i++) {
        double prediction = beta0 + beta1 * data.x[i];
        ssTotal += (data.y[i] - meanY) * (data.y[i] - meanY);
        ssResidual += (data.y[i] - prediction) * (data.y[i] - prediction);
    }

    // Calcular el coeficiente R^2
    if (ssTotal != 0) {
        this.aptitud = 1 - (ssResidual / ssTotal);
    } else {
        this.aptitud = 0; // Evitar división por cero
    }
        }

        public void mutar(Random rand) {
            beta0 += rand.nextGaussian();
            beta1 += rand.nextGaussian();
        }
    }

    private class CalcularAlgoritmoGenetico extends OneShotBehaviour  {
        @Override
        public void action() {

            ACLMessage msg = receive();
            if (msg != null && "poblacion-inicial".equals(msg.getConversationId())) {
           // Procesar el mensaje de población inicial
            String content = msg.getContent();
            poblacionInicial = deserializarPoblacion(content);
            System.out.println("Población inicial recibida y procesada.");
          

            Data data = new Data();

            // Ejecutar el algoritmo genético
            double[] resultado = resolverAlgoritmoGenetico(data,poblacionInicial);

            //Serializar los coeficientes
            String serializedCoeficientes = resultado[0] + "," + resultado[1] + "," + resultado[2];

            // Enviar información de la población (simulado aquí)
            ACLMessage responseMsg = new ACLMessage(ACLMessage.INFORM);
            responseMsg.addReceiver(new AID("Predicciones", AID.ISLOCALNAME)); // Nombre del agente receptor
            responseMsg.setConversationId("predicciones"); 
            responseMsg.setContent(serializedCoeficientes); // envio de datos
            send(responseMsg);
            System.out.println("Coeficientes enviados al Agente Predicciones.");
            }else if(msg != null){
            System.out.println("Mensaje no reconocido. ID de conversación: " + msg.getConversationId());
             }else{
                block();
             }
                
                
          
        }
    }

    private ArrayList<Individuo> deserializarPoblacion(String serializedPoblacion) {
        ArrayList<Individuo> poblacion = new ArrayList<>();
        String[] individuos = serializedPoblacion.split(";");
        for (String individuo : individuos) {
            String[] genes = individuo.split(",");
            double beta0 = Double.parseDouble(genes[0]);
            double beta1 = Double.parseDouble(genes[1]);
            poblacion.add(new Individuo(beta0, beta1));
        }
        return poblacion;
    }
}
