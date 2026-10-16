package br.edu.sistemasdistribuidos.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class Generator {
    private final ObjectMapper mapper = new ObjectMapper();
    private final int messagesPerSecond;

    public Generator(int messagesPerSecond) {
        if (messagesPerSecond < 5) {
            throw new IllegalArgumentException("MESSAGES_PER_SECOND precisa ser pelo menos 5");
        }
        this.messagesPerSecond = messagesPerSecond;
    }

    public void run(Channel channel) throws Exception {
        Topology.declare(channel);
        channel.confirmSelect();
        AtomicReference<String> returnedMessage = new AtomicReference<>();
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) ->
                returnedMessage.set(routingKey + ": " + replyText));
        long intervalNanos = TimeUnit.SECONDS.toNanos(1) / messagesPerSecond;
        long nextDue = System.nanoTime();
        long sequence = 0;
        System.out.printf("Gerador iniciado: %d mensagens/s; exchange=%s%n",
                messagesPerSecond, Topology.EXCHANGE);
        while (!Thread.currentThread().isInterrupted()) {
            DemoSamples.Sample sample = DemoSamples.at(sequence);
            String id = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Message message = new Message(id, now.toString(), sample.type(), sample.content());
            byte[] payload = mapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .contentEncoding("UTF-8")
                    .deliveryMode(2)
                    .messageId(id)
                    .timestamp(Date.from(now))
                    .build();
            returnedMessage.set(null);
            channel.basicPublish(Topology.EXCHANGE, sample.type(), true, properties, payload);
            channel.waitForConfirmsOrDie(5_000);
            if (returnedMessage.get() != null) {
                throw new IllegalStateException("Mensagem sem rota: " + returnedMessage.get());
            }
            System.out.printf("PUBLICADO id=%s chave=%s referência=%s conteúdo=%s%n",
                    id, sample.type(), sample.expected(), sample.content());
            sequence++;
            nextDue += intervalNanos;
            long remaining = nextDue - System.nanoTime();
            if (remaining > 0) {
                TimeUnit.NANOSECONDS.sleep(remaining);
            } else if (remaining < -TimeUnit.SECONDS.toNanos(1)) {
                nextDue = System.nanoTime();
            }
        }
    }
}
