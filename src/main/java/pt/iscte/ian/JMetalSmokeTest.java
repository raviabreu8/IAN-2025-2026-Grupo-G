package pt.iscte.ian;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class JMetalSmokeTest {

    public void run() {
        double randomValue = JMetalRandom.getInstance().nextDouble();

        System.out.println("Teste JMetal executado com sucesso.");
        System.out.println("Valor aleatório gerado pelo JMetal: " + randomValue);
    }
}