package examples.yellowPages;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import org.json.JSONObject;
import org.json.JSONArray;

public class SearchAgent extends Agent {

    public class DataSet {

        protected double[] x;
        protected double[] x1;
        protected double[] x2;
        protected double[] y;

        public DataSet(double[] x, double[] y) {
            this.x = x;
            //this.x1 = x;
            this.y = y;
        }

        public DataSet(double[] x1, double[] x2, double[] y) {
            this.x1 = x1;
            this.x2 = x2;
            this.y = y;
        }
    }

    public class SimpleLinearRegression {

        public void predictions(double[] coefficients, double[] newIndependentVariables) {
            System.out.println("\n\nPredicciones basadas en la regresión lineal simple:");
            for (double independentVariable : newIndependentVariables) {
                double result = (coefficients[0] + coefficients[1] * independentVariable);
                System.out.println("y = " + coefficients[0] + " + " + coefficients[1] + " * (" + independentVariable + ") = " + result);
            }
        }
    }

    public class MultipleLinearRegression {

        public void predictions(double[] coefficients, double[] newIndependentVariable1, double[] newIndependentVariable2) {
            System.out.println("\n\nPredicciones basadas en la regresión lineal múltiple:");
            for (int i = 0; i < newIndependentVariable1.length; i++) {
                double independentVariable1 = newIndependentVariable1[i];
                double independentVariable2 = newIndependentVariable2[i];
                double result = (coefficients[0] + coefficients[1] * independentVariable1 + coefficients[2] * independentVariable2);
                System.out.println("y = " + coefficients[0] + " + " + coefficients[1] + " (" + independentVariable1 + ") + "
                        + coefficients[2] + " (" + independentVariable2 + ") = " + result);
            }
        }
    }

    private class PolynomialRegression {

        public void predictions(double[] coefficients, double[] newX) {
            System.out.println("\n\nPredicciones basadas en la regresión polinómica:");
            for (double x : newX) {
                double prediction = coefficients[0] + coefficients[1] * x + coefficients[2] * Math.pow(x, 2);
                System.out.println("y = " + coefficients[0] + " + " + coefficients[1] + " * (" + x + ") + "
                        + coefficients[2] + " * (" + x + ")^2 = " + prediction);
            }
        }
    }

    protected void setup() {
        System.out.println("\n\n ====================== Agente " + getLocalName() + " iniciado. ====================== \n");
        doWait(2000);
        addBehaviour(new ClassificationAndRegressionBehaviour());
    }

    private class ClassificationAndRegressionBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            // Definir el conjunto de datos
            DataSet data = new DataSet(
                    new double[]{1,2,3}, // x o x1 Para regresiones simples y polinomiales
                    new double[]{4,5,6}, // x2 (solo para regresión múltiple)
                    new double[]{5,10,15} // y
            );

            // Buscar agentes de clasificación
            AID classificationAgent = searchAgent("classification-service");
            if (classificationAgent == null) {
                System.out.println("No se encontró un agente de clasificación.");
                return;
            }

            // Enviar solicitud de clasificación
            ACLMessage classMsg = new ACLMessage(ACLMessage.REQUEST);
            classMsg.addReceiver(classificationAgent);
            classMsg.setContent(serializeVariablesToJson(data));
            classMsg.setConversationId("classification-analysis");
            send(classMsg);

            // Recibir recomendación de análisis
            ACLMessage classReply = blockingReceive();
            if (classReply != null && classReply.getPerformative() == ACLMessage.INFORM) {
                String analysisType = classReply.getContent();
                System.out.println("\nTipo de análisis recomendado: " + analysisType + "\n");

                // Determinar el tipo de servicio de regresión
                String regressionServiceType = "";
                switch (analysisType) {
                    case "Simple Linear Regression":
                        regressionServiceType = "simple-regression-service";
                        break;
                    case "Multiple Linear Regression":
                        regressionServiceType = "multiple-regression-service";
                        break;
                    case "Polynomial Regression":
                        regressionServiceType = "polynomial-regression-service";
                        break;
                    default:
                        System.out.println("Tipo de análisis desconocido.");
                        return;
                }

                // Buscar el agente de regresión adecuado y enviar los datos
                AID regressionAgent = searchAgent(regressionServiceType);
                if (regressionAgent != null) {
                    ACLMessage regressionRequest = new ACLMessage(ACLMessage.REQUEST);
                    regressionRequest.addReceiver(regressionAgent);
                    regressionRequest.setContent(serializeVariablesToJson(data));
                    regressionRequest.setConversationId("regression-analysis");
                    send(regressionRequest);

                    // Recibir parámetros de regresión
                    ACLMessage regressionReply = blockingReceive();
                    if (regressionReply != null && regressionReply.getPerformative() == ACLMessage.INFORM) {
                        String jsonContent = regressionReply.getContent();
                        double[] coefficients = extractCoefficientsFromJson(jsonContent);

                        // Ejemplo de datos nuevos para realizar predicciones
                        double[] newX1 = {10.5, 20.3, 30.7, 40.2, 50.8};
                        double[] newX2 = {5.1, 15.6, 25.4, 35.9, 45.3};

                        switch (analysisType) {
                            case "Simple Linear Regression":
                                // Realizar predicciones con regresión simple
                                SimpleLinearRegression simpleRegression = new SimpleLinearRegression();
                                simpleRegression.predictions(coefficients, newX1);

                                // Interactuar con el GeneticAlgorithmAgent
                                AID geneticAgent = searchAgent("genetic-algorithm-service");
                                if (geneticAgent != null) {
                                    ACLMessage geneticRequest = new ACLMessage(ACLMessage.REQUEST);
                                    geneticRequest.addReceiver(geneticAgent);
                                    geneticRequest.setContent(serializeVariablesToJson(data));
                                    geneticRequest.setConversationId("genetic-analysis");
                                    send(geneticRequest);

                                    ACLMessage geneticReply = blockingReceive();
                                    if (geneticReply != null && geneticReply.getPerformative() == ACLMessage.INFORM) {
                                        System.out.println("Parámetros optimizados recibidos: " + geneticReply.getContent());
                                        double[] geneticCoefficients = extractCoefficientsFromJson(geneticReply.getContent());

                                        // Comparar modelos
                                        compareModels(coefficients, geneticCoefficients, data.x, data.y);
                                    }
                                } else {
                                    System.out.println("No se encontró el agente genético.");
                                }
                                break;

                            case "Multiple Linear Regression":
                                MultipleLinearRegression multipleRegression = new MultipleLinearRegression();
                                multipleRegression.predictions(coefficients, newX1, newX2);
                                break;

                            case "Polynomial Regression":
                                PolynomialRegression polynomialRegression = new PolynomialRegression();
                                polynomialRegression.predictions(coefficients, newX1);
                                break;

                            default:
                                break;
                        }

                    } else {
                        System.out.println("No se encontró el agente de regresión adecuado.");
                    }
                } else {
                    System.out.println("No se encontró el agente de regresión adecuado.");
                }
            }
        }

        private void compareModels(double[] simpleCoefficients, double[] geneticCoefficients, double[] x, double[] y) {
            if (simpleCoefficients == null) {
                System.out.println("Error: Los coeficientes simples recibidos son nulos.");
                return;
            }
            if (geneticCoefficients == null) {
                System.out.println("Error: Los coeficientes genéticos recibidos son nulos.");
                return;
            }

            double simpleError = calculateError(simpleCoefficients, x, y);
            double geneticError = calculateError(geneticCoefficients, x, y);

            System.out.println("\nError de modelo simple: " + simpleError);
            System.out.println("Error de modelo genético: " + geneticError);

            if (simpleError < geneticError) {
                System.out.println("El modelo de regresión simple es más óptimo.");
            } else {
                System.out.println("El modelo optimizado por el agente genético es más óptimo.");
            }
        }

        private double calculateError(double[] coefficients, double[] x, double[] y) {
            if (x == null) {
                System.out.println("Error: Los datos de x son nulos.");
                return Double.MAX_VALUE; // Un valor grande como indicador de error
            }
            if (y == null) {
                System.out.println("Error: Los datos de y son nulos.");
                return Double.MAX_VALUE; // Un valor grande como indicador de error
            }
            if (coefficients == null) {
                System.out.println("Error: Los coeficientes son nulos.");
                return Double.MAX_VALUE; // Un valor grande como indicador de error
            }

            double error = 0.0;
            for (int i = 0; i < x.length; i++) {
                double predicted = coefficients[0] + coefficients[1] * x[i];
                error += Math.pow(predicted - y[i], 2);
            }
            return error;
        }

        private AID searchAgent(String serviceType) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            template.addServices(sd);
            try {
                DFAgentDescription[] results = DFService.search(myAgent, template);
                if (results.length > 0) {
                    return results[0].getName();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private double[] extractCoefficientsFromJson(String jsonContent) {
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONArray coefficientsArray = jsonObject.getJSONArray("Coefficients");
        double[] coefficients = new double[coefficientsArray.length()];
        for (int i = 0; i < coefficientsArray.length(); i++) {
            coefficients[i] = coefficientsArray.getDouble(i);
        }
        return coefficients;
    }

    // Convertir la data a formato json
    private String serializeVariablesToJson(DataSet data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if (data.x != null) {
            sb.append("\"x\": [");
            for (int i = 0; i < data.x.length; i++) {
                sb.append(data.x[i]);
                if (i < data.x.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }

        if (data.x1 != null) {
            sb.append("\"x1\": [");
            for (int i = 0; i < data.x1.length; i++) {
                sb.append(data.x1[i]);
                if (i < data.x1.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }

        if (data.x2 != null) {
            sb.append("\"x2\": [");
            for (int i = 0; i < data.x2.length; i++) {
                sb.append(data.x2[i]);
                if (i < data.x2.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }

        sb.append("\"y\": [");
        for (int i = 0; i < data.y.length; i++) {
            sb.append(data.y[i]);
            if (i < data.y.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("] }"); // Asegura que el JSON termine con "}"

        return sb.toString();
    }
}
