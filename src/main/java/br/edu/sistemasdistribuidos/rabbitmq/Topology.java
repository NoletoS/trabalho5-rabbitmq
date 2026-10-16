package br.edu.sistemasdistribuidos.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;

public final class Topology {
    public static final String EXCHANGE = "messages.topic";
    public static final String DEAD_EXCHANGE = "messages.dead";
    public static final String REVIEW_KEY = "review.product";
    public static final String SUPPORT_KEY = "support.request";
    public static final String REVIEW_QUEUE = "reviews.queue";
    public static final String SUPPORT_QUEUE = "support.queue";
    public static final String DEAD_QUEUE = "messages.dlq";
    private static final String DEAD_KEY = "failed";

    private Topology() { }

    public static void declare(Channel channel) throws IOException {
        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC, true);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT, true);
        channel.queueDeclare(DEAD_QUEUE, true, false, false, null);
        channel.queueBind(DEAD_QUEUE, DEAD_EXCHANGE, DEAD_KEY);

        Map<String, Object> deadLetterArguments = Map.of(
                "x-dead-letter-exchange", DEAD_EXCHANGE,
                "x-dead-letter-routing-key", DEAD_KEY);
        channel.queueDeclare(REVIEW_QUEUE, true, false, false, deadLetterArguments);
        channel.queueDeclare(SUPPORT_QUEUE, true, false, false, deadLetterArguments);
        channel.queueBind(REVIEW_QUEUE, EXCHANGE, REVIEW_KEY);
        channel.queueBind(SUPPORT_QUEUE, EXCHANGE, SUPPORT_KEY);
    }
}

