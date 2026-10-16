package br.edu.sistemasdistribuidos.rabbitmq;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextClassifierTest {
    @Test
    void classificaFrasesDaDemonstracaoForaDaBaseDeTreino() throws Exception {
        TextClassifier reviews = TextClassifier.load("/training/reviews.csv",
                new String[]{"positiva", "negativa", "neutra"});
        TextClassifier support = TextClassifier.load("/training/support.csv",
                new String[]{"problema de acesso", "problema de desempenho", "problema de conexão"});
        for (DemoSamples.Sample sample : DemoSamples.all()) {
            TextClassifier classifier = sample.type().equals(Topology.REVIEW_KEY) ? reviews : support;
            assertEquals(sample.expected(), classifier.predict(sample.content()), sample.content());
        }
    }

    @Test
    void textoSemTermosConhecidosNaoProduzClasseArbitraria() throws Exception {
        TextClassifier reviews = TextClassifier.load("/training/reviews.csv",
                new String[]{"positiva", "negativa", "neutra"});
        assertThrows(IllegalArgumentException.class, () -> reviews.predict("xyzzy quux"));
    }
}
