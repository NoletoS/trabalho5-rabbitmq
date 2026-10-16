package br.edu.sistemasdistribuidos.rabbitmq;

/** Frases inéditas em relação aos CSVs de treino, com rótulos só para a conferência. */
public final class DemoSamples {
    public record Sample(String type, String expected, String content) { }

    private static final Sample[] SAMPLES = {
            new Sample(Topology.REVIEW_KEY, "positiva", "Produto excelente, chegou rápido e funciona perfeitamente."),
            new Sample(Topology.REVIEW_KEY, "negativa", "Péssima compra, veio quebrado e a qualidade decepcionou."),
            new Sample(Topology.REVIEW_KEY, "neutra", "O produto chegou hoje; ainda não usei para avaliar."),
            new Sample(Topology.SUPPORT_KEY, "problema de acesso", "Não consigo entrar na conta, a senha é recusada no login."),
            new Sample(Topology.SUPPORT_KEY, "problema de desempenho", "O aplicativo está muito lento e trava ao abrir relatórios."),
            new Sample(Topology.SUPPORT_KEY, "problema de conexão", "A rede cai toda hora e o servidor fica desconectado."),

            new Sample(Topology.REVIEW_KEY, "positiva", "Adorei a compra; ótima qualidade e entrega rápida."),
            new Sample(Topology.REVIEW_KEY, "negativa", "O item é ruim, apresentou defeito e fiquei insatisfeito."),
            new Sample(Topology.REVIEW_KEY, "neutra", "Recebi o item ontem; vou testar antes de dar uma opinião."),
            new Sample(Topology.SUPPORT_KEY, "problema de acesso", "Minha conta foi bloqueada e não consigo autenticar."),
            new Sample(Topology.SUPPORT_KEY, "problema de desempenho", "A página demora muito para carregar e o sistema congela."),
            new Sample(Topology.SUPPORT_KEY, "problema de conexão", "A conexão com a internet caiu e não alcanço o servidor.")
    };

    private DemoSamples() { }

    public static Sample at(long index) {
        return SAMPLES[(int) (index % SAMPLES.length)];
    }

    public static Sample[] all() {
        return SAMPLES.clone();
    }
}
