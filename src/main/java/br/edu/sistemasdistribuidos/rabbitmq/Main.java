package br.edu.sistemasdistribuidos.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        String role = System.getenv().getOrDefault("APP_ROLE", "");
        if (args.length == 1 && "self-check".equals(args[0])) {
            SelfCheck.run();
            return;
        }
        if (!role.equals("generator") && !role.equals("review") && !role.equals("support")) {
            throw new IllegalArgumentException("APP_ROLE deve ser generator, review ou support");
        }

        TextClassifier classifier = null;
        if (role.equals("review")) {
            classifier = TextClassifier.load("/training/reviews.csv",
                    new String[]{"positiva", "negativa", "neutra"});
        } else if (role.equals("support")) {
            classifier = TextClassifier.load("/training/support.csv",
                    new String[]{"problema de acesso", "problema de desempenho", "problema de conexão"});
        }

        while (!Thread.currentThread().isInterrupted()) {
            try (Connection connection = RabbitConnections.openWithRetry();
                 Channel channel = connection.createChannel()) {
                switch (role) {
                    case "generator" -> new Generator(envInt("MESSAGES_PER_SECOND", 6)).run(channel);
                    case "review" -> new Consumer(Topology.REVIEW_QUEUE, Topology.REVIEW_KEY,
                            classifier, envInt("CONSUMER_DELAY_MS", 1500)).run(channel);
                    case "support" -> new Consumer(Topology.SUPPORT_QUEUE, Topology.SUPPORT_KEY,
                            classifier, envInt("CONSUMER_DELAY_MS", 1500)).run(channel);
                    default -> throw new IllegalStateException("Papel desconhecido");
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (Exception error) {
                System.err.println("Conexão/canal encerrado: " + error.getMessage() + "; reconectando em 3 s");
                Thread.sleep(3_000);
            }
        }
    }

    private static int envInt(String name, int defaultValue) {
        return Integer.parseInt(System.getenv().getOrDefault(name, Integer.toString(defaultValue)));
    }
}
