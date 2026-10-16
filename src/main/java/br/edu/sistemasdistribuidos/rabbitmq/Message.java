package br.edu.sistemasdistribuidos.rabbitmq;

public class Message {
    public String id;
    public String generatedAt;
    public String type;
    public String content;

    public Message() { }

    public Message(String id, String generatedAt, String type, String content) {
        this.id = id;
        this.generatedAt = generatedAt;
        this.type = type;
        this.content = content;
    }
}

