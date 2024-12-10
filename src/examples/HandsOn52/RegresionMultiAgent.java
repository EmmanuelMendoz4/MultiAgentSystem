package HandsOn5;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class RegresionMultiAgent extends Agent {

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        addBehaviour(new RegresionBehaviour());
    }

    // El comportamiento del agente que ejecutara la regresion
    private class RegresionBehaviour extends Behaviour {
        private boolean done = false;

        public void action() {
            linearRegression linearR = new linearRegression();
            cubicRegression cubicR = new cubicRegression();
            quadraticRegression quadraticR = new quadraticRegression();
            coefficients coef = new coefficients();
            dataSet data = new dataSet();
            discreteMathematics dicret = new discreteMathematics();

            double[] x = data.batchSize;
            double[] y = data.output;

            // Realiza las regresiones usan;do LSR
            double[] linearRe = linearR.calcularLinearRegression(x, y);
            double[] quadraticRe = quadraticR.calcularQuadraticRegression(x, y);
            double[] cubicRe = cubicR.calcularCubicRegression(x, y);

            // Calcular coeficientes Regresion Cubica
            double[] yPredictionsC = dicret.sumatoriaC(x, cubicRe);
            double mediaY = dicret.media(y);
            double explainedVarianceC = dicret.sumatoriayMediaY(yPredictionsC, mediaY);
            double totalVariance = dicret.sumatoriayMediaY(y, mediaY);
            double determinationC = coef.determinationCoefficientC(explainedVarianceC, totalVariance);
            double correlationC = coef.correlationCoefficient2(determinationC);

            // Calcular coeficientes Regresion Quadratica
            double[] yPredictionsQ = dicret.sumatoriaQ(x, quadraticRe);
            double explainedVarianceQ = dicret.sumatoriayMediaY(yPredictionsQ, mediaY);
            double determinationQ = coef.determinationCoefficientC(explainedVarianceQ, totalVariance);
            double correlationQ = coef.correlationCoefficient2(determinationQ);

            // Calcula correlacion y determinacion
            double correlation = coef.correlationCoefficient(x, y);
            double determination = coef.determinationCoefficient(correlation);

            double[] nuevosX = { 97, 95, 91, 97, 83 };
            // Imprime las curvas de regresion y predicciones
            System.out.println("\nCurva en Modelo Lineal:\n y = " + linearRe[0] + " + " + linearRe[1] + "(x)");
            System.out.println("\nPredicciones: ");
            linearR.prediccionesS(linearRe, nuevosX);
            System.out.println("\nCoeficiente de correlacion = " + correlation);
            System.out.println("Coeficiente de determinacion = " + determination + "%");

            System.out.println("\n ======================================================================================================");

            System.out.println("\nCurva en Modelo Cuadratico:\n y = " + quadraticRe[2] + " + " + quadraticRe[1] + "(x) + " + quadraticRe[0] + "(x^2) ");
            System.out.println("\nPredicciones: ");
            quadraticR.prediccionesQ(quadraticRe, nuevosX);
            System.out.println("\nCoeficiente de correlacion = " + correlationQ);
            System.out.println("Coeficiente de determinacion = " + determinationQ + "%");

            System.out.println("\n ======================================================================================================");

            System.out.println("\nCurva en Modelo Cúbico:\n y = " + cubicRe[3] + " + " + cubicRe[2] + "(x) + " + cubicRe[1] + "(x^2) + " + cubicRe[0] + "(x^3) ");
            System.out.println("\nPredicciones: ");
            cubicR.prediccionesC(cubicRe, nuevosX);
            System.out.println("\nCoeficiente de correlacion = " + correlationC);
            System.out.println("Coeficiente de determinacion = " + determinationC + "%");
            System.out.println("\n");

            done = true;
        }

        public boolean done() {
            return done;
        }

        /*public int onEnd() {
            myAgent.doDelete(); // El agente se elimina al terminar
            return super.onEnd();
        }*/
    }
}
