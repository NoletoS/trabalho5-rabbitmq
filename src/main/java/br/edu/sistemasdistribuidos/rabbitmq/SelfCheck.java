package br.edu.sistemasdistribuidos.rabbitmq;

public final class SelfCheck {
    private SelfCheck() { }

    public static void run() throws Exception {
        TextClassifier reviews = TextClassifier.load("/training/reviews.csv",
                new String[]{"positiva", "negativa", "neutra"});
        TextClassifier support = TextClassifier.load("/training/support.csv",
                new String[]{"problema de acesso", "problema de desempenho", "problema de conexão"});
        int correct = 0;
        DemoSamples.Sample[] samples = DemoSamples.all();
        for (DemoSamples.Sample sample : samples) {
            TextClassifier classifier = sample.type().equals(Topology.REVIEW_KEY) ? reviews : support;
            String predicted = classifier.predict(sample.content());
            boolean passed = predicted.equals(sample.expected());
            if (passed) correct++;
            System.out.printf("%s esperado=%s previsto=%s texto=%s%n",
                    passed ? "OK" : "FALHA", sample.expected(), predicted, sample.content());
        }
        System.out.printf("Resultado: %d/%d exemplos de demonstração corretos%n", correct, samples.length);
        if (correct != samples.length) {
            throw new IllegalStateException("Os modelos não classificaram todos os exemplos de demonstração");
        }
    }
}
