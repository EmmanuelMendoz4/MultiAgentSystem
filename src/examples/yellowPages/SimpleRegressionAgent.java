package examples.yellowPages;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class SimpleRegressionAgent extends Agent {

    protected void setup() {
        System.out.println("Agente " + getLocalName() + " iniciado.");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("simple-regression-service");
        sd.setName("simple-regression-service");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new ReceiveDatasetBehaviour());
    }

    private class ReceiveDatasetBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getConversationId().equals("regression-analysis")) {
                JSONObject json = new JSONObject(msg.getContent());
                JSONArray xArray = json.getJSONArray("x1");
                JSONArray yArray = json.getJSONArray("y");

                double[] xValues = parseJsonArray(xArray);
                double[] yValues = parseJsonArray(yArray);

                double b1 = calculateB1(xValues, yValues);
                double b0 = calculateB0(xValues, yValues, b1);

                JSONObject result = new JSONObject();
                result.put("Coefficients", new double[]{b0, b1});

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(result.toString());
                send(reply);
            } else {
                block();
            }
        }
    }

    private double[] parseJsonArray(JSONArray jsonArray) {
        double[] array = new double[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getDouble(i);
        }
        return array;
    }

    private double calculateB1(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        double numerator = n * sumXY - sumX * sumY;
        double denominator = n * sumX2 - sumX * sumX;

        return numerator / denominator;
    }

    private double calculateB0(double[] x, double[] y, double b1) {
        double meanX = 0, meanY = 0;
        for (double value : x) meanX += value;
        for (double value : y) meanY += value;
        meanX /= x.length;
        meanY /= y.length;
        return meanY - b1 * meanX;
    }
}
