# 🔧 VM Translator → Assembly Hack (Nand2Tetris)

Este projeto implementa a **primeira parte do VM Translator** para o projeto **Nand2Tetris**.

Desenvolvido em **Java**, o sistema recebe arquivos `.vm` (código intermediário gerado pelo compilador Jack) e os traduz para **Assembly Hack** (`.asm`), compatível com o CPU Emulator oficial do curso.

---

## 📁 Estrutura do Projeto

```
VMTranslator/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/com/
│   │           ├── VMTranslator.java          🔹 Ponto de entrada — orquestra Parser e CodeWriter
│   │           ├── parser/
│   │           │   └── Parser.java            🔹 Lê o .vm, filtra e classifica cada comando
│   │           └── codewriter/
│   │               └── CodeWriter.java        🔹 Traduz cada comando para Assembly Hack
│   │
│   └── test/
│       ├── java/
│       │   └── br/com/
│       │       ├── parser/
│       │       │   └── ParserTest.java        🔹 Testes unitários do Parser (JUnit 5)
│       │       └── codewriter/
│       │           └── CodeWriterTest.java    🔹 Testes unitários do CodeWriter (JUnit 5)
│       │
│       └── resources/
│           └── 7/                             🔹 Arquivos de teste do Project 07
│               ├── StackArithmetic/
│               │   ├── SimpleAdd/             🔹 Teste: operações aritméticas simples
│               │   └── StackTest/             🔹 Teste: todas as operações da pilha
│               └── MemoryAccess/
│                   ├── BasicTest/             🔹 Teste: push e pop nos segmentos principais
│                   ├── PointerTest/           🔹 Teste: segmento pointer (this/that)
│                   └── StaticTest/            🔹 Teste: segmento static
│
├── pom.xml          🔹 Configuração Maven (dependências, build, plugins)
├── README.md
└── .gitignore
```

---

## 🔍 Como o tradutor funciona

O arquivo `.vm` passa por dois estágios antes de virar Assembly:

```
Arquivo .vm
     │
     ▼
┌─────────────────────────────────────────────┐
│  1. PARSER                                  │
│                                             │
│  Lê o arquivo linha por linha.              │
│  Remove comentários e linhas vazias.        │
│  Classifica cada comando:                   │
│                                             │
│  "push constant 7"  → C_PUSH, "constant", 7 │
│  "add"              → C_ARITHMETIC, "add"   │
│  "pop local 0"      → C_POP, "local", 0     │
└─────────────────┬───────────────────────────┘
                  │ tipo + argumentos
                  ▼
┌─────────────────────────────────────────────┐
│  2. CODEWRITER                              │
│                                             │
│  Recebe cada comando e escreve as           │
│  instruções Assembly Hack equivalentes.     │
│                                             │
│  push constant 7 →  @7                      │
│                      D=A                    │
│                      @SP                    │
│                      A=M                    │
│                      M=D                    │
│                      @SP                    │
│                      M=M+1                  │
└────────────────┬────────────────────────────┘
                 │
                 ▼
        Arquivo .asm gerado
   (compatível com CPU Emulator)
```

### O que cada classe faz

- **Parser:** lê arquivos `.vm`, filtra comentários e linhas vazias, e expõe três informações por comando: o tipo (`C_PUSH`, `C_POP`, `C_ARITHMETIC`), o primeiro argumento (`"local"`, `"constant"`, `"add"`) e o segundo argumento quando houver (o índice numérico). Não sabe nada de Assembly — só lê e classifica;
- **CodeWriter:** recebe os comandos classificados e emite as instruções Assembly Hack equivalentes, gerando o arquivo `.asm`;
- **VMTranslator:** é o ponto de entrada. Recebe o caminho do `.vm` como argumento, instancia o Parser e o CodeWriter, e orquestra os dois: passa o arquivo para o Parser e, para cada comando, chama o CodeWriter traduzir.

---

## 🚀 Como executar

### Pré-requisitos

- Java 17+
- Maven 3.6+

### 🔹 Build

```bash
mvn clean package
```

---

### 🔹 Traduzir um arquivo `.vm` para Assembly

```bash
java -jar target/vmtranslator.jar <caminho-do-arquivo.vm>
```

O arquivo `.asm` é gerado no mesmo diretório do `.vm`.

---

### 🔹 Rodar os 5 testes do Project 07

**StackArithmetic — SimpleAdd** (operações aritméticas simples):
```bash
java -jar target/vmtranslator.jar src/test/resources/7/StackArithmetic/SimpleAdd/SimpleAdd.vm
```

**StackArithmetic — StackTest** (todas as operações da pilha):
```bash
java -jar target/vmtranslator.jar src/test/resources/7/StackArithmetic/StackTest/StackTest.vm
```

**MemoryAccess — BasicTest** (push e pop nos segmentos principais):
```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/BasicTest/BasicTest.vm
```

**MemoryAccess — PointerTest** (segmento pointer):
```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/PointerTest/PointerTest.vm
```

**MemoryAccess — StaticTest** (segmento static):
```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/StaticTest/StaticTest.vm
```

Cada comando gera um `.asm` na mesma pasta do `.vm`. Para validar, abra o **CPU Emulator**, vá em `File → Load Script`, selecione o `.tst` da respectiva pasta e clique em Run. O resultado esperado é:

```
Comparison ended successfully.
```

---

### 🔹 Rodar os testes unitários (JUnit 5)

```bash
mvn test
```

Saída esperada:

```
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

Os testes cobrem:

| Classe | Testes | O que cobre |
|---|---|---|
| `ParserTest` | 13 | Filtragem de comentários e linhas vazias, classificação de todos os tipos de comando, extração correta de arg1 e arg2, sequência de múltiplos comandos |
| `CodeWriterTest` | 24 | Todos os segmentos de push e pop, todas as operações aritméticas e lógicas, operações relacionais com labels únicos |

---

## ✅ Status de validação — Project 07

Todos os programas foram traduzidos e validados no CPU Emulator oficial do nand2tetris:

| Programa | Foco | Resultado |
|---|---|---|
| `StackArithmetic/SimpleAdd` | Operações aritméticas simples | ✅ Passou |
| `StackArithmetic/StackTest` | Todas as operações da pilha | ✅ Passou |
| `MemoryAccess/BasicTest` | Push e pop nos segmentos principais | ✅ Passou |
| `MemoryAccess/PointerTest` | Segmento pointer (this/that) | ✅ Passou |
| `MemoryAccess/StaticTest` | Segmento static | ✅ Passou |

---

## 📌 Observações

- Os arquivos `.asm` gerados aparecem no mesmo diretório do `.vm` de origem — isso é intencional, pois o CPU Emulator espera o `.asm` na mesma pasta do `.tst`
- Os arquivos `.asm` e `.out` não são versionados no repositório (estão no `.gitignore`) pois são saídas geradas em tempo de execução
- Para validar no CPU Emulator, use sempre o `.tst` — nunca carregue o `.asm` diretamente, pois o `.tst` inicializa a RAM e compara com o gabarito `.cmp` automaticamente

---

## 👥 Créditos

**Aluna:** Maria Laura Rangel Urbano Cronemberger 
**Matrícula:** 20250071287 
**Disciplina:** EECP0026 — Compiladores  
**Professor:** Prof. Dr. Sergio Souza Costa  
**Instituição:** UFMA — Universidade Federal do Maranhão  
**Semestre:** 2026.1
**Linguagem Utilizada:** Java 17

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---