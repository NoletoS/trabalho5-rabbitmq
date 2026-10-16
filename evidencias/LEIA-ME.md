# Evidências da execução

Esta pasta contém capturas reais da interface RabbitMQ e saídas da execução em 15 de outubro de 2026. Consulte `validacao-local.md` para os resultados medidos e a sequência do teste.

| Arquivo | Evidência |
|---|---|
| `exchange-depois-crescimento.png` | Topic Exchange, vínculos e taxa observada de 6 mensagens/s |
| `queues-antes-dlq.png` | Primeira contagem das filas principais |
| `queues-depois-crescimento.png` | Segunda contagem, com aumento de Ready nas duas filas |
| `queues-com-dlq.png` | Mensagem inválida encaminhada à DLQ |
| `containers-*.txt` | Estado dos quatro contêineres |
| `filas-*.txt` e `filas-*.json` | Contagens obtidas pela API de administração |
| `logs-*.txt` | Publicação, classificações e erro encaminhado à DLQ |

Para registrar uma nova execução, use `scripts/capture-evidence.ps1`. As capturas PNG podem ser feitas na interface em <http://localhost:15672>, com usuário `demo` e senha `demo123` na configuração padrão.
