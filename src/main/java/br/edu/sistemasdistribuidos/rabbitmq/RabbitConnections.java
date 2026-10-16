package br.edu.sistemasdistribuidos.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;

public final class RabbitConnections {
    private RabbitConnections() { }

    public static Connection openWithRetry() throws InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        factory.setUsername(System.getenv().getOrDefault("RABBITMQ_USER", "demo"));
        factory.setPassword(System.getenv().getOrDefault("RABBITMQ_PASS", "demo123"));
        // O processo refaz a conexão e declara a topologia em cada tentativa.
        factory.setAutomaticRecoveryEnabled(false);
        while (true) {
            try {
                return factory.newConnection();
            } catch (Exception error) {
                System.err.println("RabbitMQ indisponível: " + error.getMessage() + "; nova tentativa em 3 s");
                Thread.sleep(Duration.ofSeconds(3).toMillis());
            }
        }
    }
}
