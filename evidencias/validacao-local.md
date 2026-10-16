# Validação local em 15 de outubro de 2026

## Compilação e classificação

- `mvn package` com Maven 3.9.11 e JDK 21: **BUILD SUCCESS**.
- Teste JUnit `TextClassifierTest`: **2 testes, 0 falhas, 0 erros**.
- `java -jar target/app.jar self-check`: **12 de 12 frases de demonstração classificadas conforme os rótulos esperados**. As frases não estão nos CSVs de treino.
- `docker compose config --quiet`: configuração válida.
- `docker compose up --build -d`: as três imagens Java foram construídas e os quatro contêineres iniciaram. O RabbitMQ ficou **healthy**.

## Verificação com RabbitMQ em execução

- A captura `exchange-depois-crescimento.png` mostra o exchange durável do tipo **topic**, o vínculo `review.product` → `reviews.queue`, o vínculo `support.request` → `support.queue` e publicação de **6,0 mensagens/s** naquele instante.
- `queues-antes-dlq.png` registrou **257 Ready** em cada fila principal. Cerca de 18 segundos depois, `queues-depois-crescimento.png` registrou **305 Ready** em `reviews.queue` e **306 Ready** em `support.queue`.
- Os logs em `logs-20261015-163944.txt` mostram publicação contínua e classificações das avaliações como `positiva`, `negativa` e `neutra`, e do suporte como `problema de acesso`, `problema de desempenho` e `problema de conexão`.
- O gerador foi pausado após as capturas de crescimento e somente `reviews.queue` foi esvaziada para que o teste de erro não esperasse centenas de mensagens. `scripts/inject-error.ps1` publicou JSON inválido. O consumidor registrou `ERRO ... -> DLQ`, e `queues-com-dlq.png` mostra **1 Ready** em `messages.dlq`. O gerador foi religado em seguida.
- `containers-20261015-164217.txt` e `filas-20261015-164217.txt` registram os quatro serviços ativos e a DLQ com uma mensagem após o teste.
