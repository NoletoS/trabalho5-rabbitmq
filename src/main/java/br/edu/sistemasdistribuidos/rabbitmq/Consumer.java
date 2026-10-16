package br.edu.sistemasdistribuidos.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.time.Instant;
import java.util.UUID;

public final class Consumer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String queue;
    private final String expectedType;
    private final TextClassifier classifier;
    private final long delayMillis;

    public Consumer(String queue, String expectedType, TextClassifier classifier, long delayMillis) {
        if (delayMillis < 0) throw new IllegalArgumentException("CONSUMER_DELAY_MS não pode ser negativo");
        this.queue = queue;
        this.expectedType = expectedType;
        this.classifier = classifier;
        this.delayMillis = delayMillis;
    }

    public void run(Channel channel) throws Exception {
        Topology.declare(channel);
        channel.basicQos(1);
        DeliverCallback callback = (consumerTag, delivery) -> {
            long tag = delivery.getEnvelope().getDeliveryTag();
            try {
                Message message = mapper.readValue(delivery.getBody(), Message.class);
                validate(message);
                String result = classifier.predict(message.content);
                Thread.sleep(delayMillis);
                System.out.printf("CLASSIFICADO fila=%s id=%s classe=%s conteúdo=%s%n",
                        queue, message.id, result, message.content);
                channel.basicAck(tag, false);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                channel.basicNack(tag, false, true);
            } catch (Exception error) {
                System.err.printf("ERRO fila=%s tag=%d motivo=%s -> DLQ%n", queue, tag, error.getMessage());
                channel.basicNack(tag, false, false);
            }
        };
        channel.basicConsume(queue, false, callback, consumerTag -> { });
        System.out.printf("Consumidor iniciado: fila=%s classeIA=Smile/NaiveBayes treino=%d vocabulário=%d atraso=%dms prefetch=1%n",
                queue, classifier.examples(), classifier.vocabularySize(), delayMillis);
        while (channel.isOpen() && !Thread.currentThread().isInterrupted()) {
            Thread.sleep(1_000);
        }
        throw new IllegalStateException("Canal do consumidor encerrado");
    }

    private void validate(Message message) {
        if (message == null || message.id == null || message.generatedAt == null
                || message.type == null || message.content == null) {
            throw new IllegalArgumentException("Mensagem sem os campos obrigatórios");
        }
        UUID.fromString(message.id);
        Instant.parse(message.generatedAt);
        if (!expectedType.equals(message.type)) {
            throw new IllegalArgumentException("Tipo incompatível: " + message.type);
        }
        if (message.content.isBlank()) {
            throw new IllegalArgumentException("Conteúdo vazio");
        }
    }
}
