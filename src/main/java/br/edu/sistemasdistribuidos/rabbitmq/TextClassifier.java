package br.edu.sistemasdistribuidos.rabbitmq;

import smile.classification.DiscreteNaiveBayes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Classificador local: texto -> bag of words -> Naive Bayes multinomial do Smile. */
public final class TextClassifier {
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "as", "o", "os", "um", "uma", "de", "da", "das", "do", "dos",
            "e", "em", "na", "nas", "no", "nos", "para", "por", "com", "que",
            "meu", "minha", "meus", "minhas", "seu", "sua", "foi", "esta", "estao",
            "eu", "ele", "ela", "isso", "esse", "essa", "ao", "aos");

    private final String[] labels;
    private final Map<String, Integer> vocabulary;
    private final DiscreteNaiveBayes model;
    private final int examples;

    private TextClassifier(String[] labels, Map<String, Integer> vocabulary,
                           DiscreteNaiveBayes model, int examples) {
        this.labels = labels;
        this.vocabulary = vocabulary;
        this.model = model;
        this.examples = examples;
    }

    public static TextClassifier load(String resource, String[] labels) throws IOException {
        InputStream input = TextClassifier.class.getResourceAsStream(resource);
        if (input == null) {
            throw new IOException("Base de treinamento não encontrada: " + resource);
        }
        List<Example> examples = new ArrayList<>();
        Map<String, Integer> vocabulary = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) continue;
                int separator = line.indexOf(';');
                if (separator <= 0 || separator == line.length() - 1) {
                    throw new IOException("Exemplo inválido em " + resource + ":" + lineNumber);
                }
                String label = line.substring(0, separator).trim();
                String content = line.substring(separator + 1).trim();
                int classIndex = indexOf(labels, label);
                if (classIndex < 0) {
                    throw new IOException("Classe desconhecida em " + resource + ":" + lineNumber + " -> " + label);
                }
                List<String> tokens = tokenize(content);
                if (tokens.isEmpty()) {
                    throw new IOException("Texto sem termos em " + resource + ":" + lineNumber);
                }
                for (String token : tokens) {
                    vocabulary.computeIfAbsent(token, ignored -> vocabulary.size());
                }
                examples.add(new Example(classIndex, tokens));
            }
        }
        if (examples.size() < labels.length * 3) {
            throw new IOException("A base " + resource + " precisa de pelo menos 3 exemplos por classe");
        }
        int[] perClass = new int[labels.length];
        DiscreteNaiveBayes model = new DiscreteNaiveBayes(
                DiscreteNaiveBayes.Model.MULTINOMIAL, labels.length, vocabulary.size());
        for (Example example : examples) {
            perClass[example.classIndex]++;
            model.update(vector(example.tokens, vocabulary), example.classIndex);
        }
        for (int count : perClass) {
            if (count < 3) throw new IOException("Classe com menos de 3 exemplos em " + resource);
        }
        return new TextClassifier(labels.clone(), Map.copyOf(vocabulary), model, examples.size());
    }

    public String predict(String text) {
        int[] features = vector(tokenize(text), vocabulary);
        int recognizedTerms = 0;
        for (int count : features) recognizedTerms += count;
        if (recognizedTerms == 0) {
            throw new IllegalArgumentException("Texto vazio ou sem termos conhecidos pelo modelo");
        }
        int prediction = model.predict(features);
        if (prediction < 0 || prediction >= labels.length) {
            throw new IllegalStateException("Predição inválida: " + prediction);
        }
        return labels[prediction];
    }

    public int examples() { return examples; }
    public int vocabularySize() { return vocabulary.size(); }

    private static int indexOf(String[] labels, String label) {
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(label)) return i;
        }
        return -1;
    }

    private static int[] vector(List<String> tokens, Map<String, Integer> vocabulary) {
        int[] vector = new int[vocabulary.size()];
        for (String token : tokens) {
            Integer index = vocabulary.get(token);
            if (index != null) vector[index]++;
        }
        return vector;
    }

    static List<String> tokenize(String text) {
        if (text == null) return List.of();
        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        List<String> result = new ArrayList<>();
        for (String token : normalized.split("[^a-z0-9]+")) {
            if (token.length() >= 3 && !STOP_WORDS.contains(token)) result.add(token);
        }
        return result;
    }

    private record Example(int classIndex, List<String> tokens) { }
}

