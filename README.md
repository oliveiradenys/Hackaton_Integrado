# Hackathon FIAP - Análise Inteligente de Arquitetura de Sistemas

Projeto em microsserviços com Java 21, Spring Boot, RabbitMQ, PostgreSQL, Docker e API Gateway.

## O que o sistema faz

1. Recebe uma análise por nome de arquivo ou por upload real de arquivo.
2. Salva o registro no PostgreSQL com status `PENDING`.
3. Publica o ID da análise na fila RabbitMQ `analysis.queue`.
4. O `ai-processing-service` consome a fila, muda o status para `PROCESSING`, gera a análise e salva o resultado como `DONE`.
5. O `report-service` disponibiliza os relatórios.
6. O `gateway-service` centraliza o acesso externo.

> Observação: se `OPENAI_API_KEY` não estiver configurada, o processamento não quebra. O sistema gera uma análise local de demonstração para o fluxo funcionar de ponta a ponta.

## Serviços

| Serviço | Porta local | Função |
|---|---:|---|
| gateway-service | 8080 | Entrada principal da aplicação |
| upload-service | 8082 | Cria análise e recebe arquivo |
| ai-processing-service | 8081 | Consome fila e gera análise |
| report-service | 8083 | Lista relatórios |
| PostgreSQL | 5432 | Banco de dados |
| RabbitMQ | 5672 / 15672 | Fila e painel web |

## Como subir com Docker

Na raiz do projeto: 

```bash
docker compose up --build
```

O Dockerfile de cada serviço já faz o build Maven dentro do container. Não é obrigatório gerar os JARs antes.

Se quiser usar OpenAI real:

### PowerShell

```powershell
$env:OPENAI_API_KEY="sua_chave_aqui"
docker compose up --build
```

### Linux/Mac/Git Bash

```bash
export OPENAI_API_KEY="sua_chave_aqui"
docker compose up --build
```

## Como testar

### 1. Verificar Gateway

```bash
curl http://localhost:8080/health
```

Resposta esperada:

```txt
gateway-service UP
```

### 2. Criar análise simples pelo nome do arquivo

```bash
curl -X POST "http://localhost:8080/analysis?fileName=diagrama.pdf"
```

Resposta esperada: JSON com `id`, `fileName`, `status=PENDING`.

### 3. Enviar um arquivo real

```bash
curl -X POST "http://localhost:8080/analysis/upload" \
  -F "file=@./docs/architecture.png"
```

Para PDF:

```bash
curl -X POST "http://localhost:8080/analysis/upload" \
  -F "file=@./seu-arquivo.pdf"
```

Arquivos PDF e TXT têm extração de texto automática. Outros tipos são aceitos, mas entram com uma descrição básica.

### 4. Consultar relatórios

Aguarde alguns segundos e rode:

```bash
curl http://localhost:8080/reports
```

Ou por ID:

```bash
curl http://localhost:8080/reports/1
```

## Swagger

```txt
http://localhost:8082/swagger-ui.html
http://localhost:8083/swagger-ui.html
```

## RabbitMQ

```txt
http://localhost:15672
user: guest
password: guest
```

## Correções aplicadas nesta versão

- Corrigidas URLs internas que usavam `localhost` dentro dos containers.
- Adicionadas variáveis de ambiente para Postgres, RabbitMQ e URLs dos serviços.
- Corrigido Gateway para chamar `upload-service` e `report-service` pelo nome dos containers.
- Adicionado endpoint real de upload multipart: `POST /analysis/upload`.
- Adicionada extração de texto de PDF/TXT no `upload-service`.
- Corrigida mensagem na fila para usar ID da análise, evitando conflito com arquivos de mesmo nome.
- Adicionado fallback local caso `OPENAI_API_KEY` não exista ou a chamada para OpenAI falhe.
- Corrigidos testes unitários que estavam incompatíveis com os construtores atuais.
- Dockerfiles alterados para build multi-stage com Maven dentro do container.
- Adicionados healthchecks para Postgres e RabbitMQ.
- Adicionados `.gitignore` e `.dockerignore`.

## Estrutura

```txt
hackathon-fiap
├── upload-service
├── ai-processing-service
├── report-service
├── gateway-service
├── docs/
├── docker-compose.yml
└── README.md
```
