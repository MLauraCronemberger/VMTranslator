# 🔧 VM Translator → Assembly Hack (Nand2Tetris)

Este projeto implementa o **VM Translator completo dos Projects 07 e 08** do curso **Nand2Tetris**.

Desenvolvido em **Java**, o sistema recebe um arquivo `.vm` ou um diretório contendo múltiplos arquivos `.vm` (código intermediário gerado pelo compilador Jack) e os traduz para **Assembly Hack** (`.asm`), compatível com o CPU Emulator oficial do curso.

A implementação suporta:

* Operações aritméticas e lógicas;
* Acesso à memória (`push` e `pop`);
* Controle de fluxo (`label`, `goto`, `if-goto`);
* Definição e chamada de funções (`function`, `call`, `return`);
* Tradução de múltiplos arquivos `.vm`;
* Geração automática do bootstrap (`SP = 256` + `call Sys.init`) quando necessário.

---

## 📁 Estrutura do Projeto

```text
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
│           ├── 7/                             🔹 Arquivos de teste do Project 07
│           │   ├── StackArithmetic/
│           │   │   ├── SimpleAdd/
│           │   │   └── StackTest/
│           │   └── MemoryAccess/
│           │       ├── BasicTest/
│           │       ├── PointerTest/
│           │       └── StaticTest/
│           │
│           └── 8/                             🔹 Arquivos de teste do Project 08
│               ├── ProgramFlow/
│               │   ├── BasicLoop/
│               │   └── FibonacciSeries/
│               │
│               └── FunctionCalls/
│                   ├── SimpleFunction/
│                   ├── NestedCall/
│                   ├── FibonacciElement/
│                   └── StaticsTest/
│
├── pom.xml                                    🔹 Configuração Maven (dependências, build, plugins)
├── README.md
└── .gitignore
```

---

## 🔍 Como o tradutor funciona

O arquivo `.vm` passa por dois estágios antes de virar Assembly:

```text
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
│  "push constant 7"      → C_PUSH            │
│  "add"                  → C_ARITHMETIC      │
│  "pop local 0"          → C_POP             │
│  "label LOOP"           → C_LABEL           │
│  "goto END"             → C_GOTO            │
│  "if-goto LOOP"         → C_IF              │
│  "function Main.main 2" → C_FUNCTION        │
│  "call Sys.init 0"      → C_CALL            │
│  "return"               → C_RETURN          │
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

* **Parser:** lê arquivos `.vm`, remove comentários e linhas vazias, classifica todos os comandos da linguagem VM (`C_PUSH`, `C_POP`, `C_ARITHMETIC`, `C_LABEL`, `C_GOTO`, `C_IF`, `C_FUNCTION`, `C_CALL` e `C_RETURN`) e disponibiliza seus argumentos;

* **CodeWriter:** recebe os comandos classificados e gera as instruções Assembly Hack equivalentes, incluindo operações de pilha, controle de fluxo, chamadas de função, restauração de frames e bootstrap;

* **VMTranslator:** é o ponto de entrada da aplicação. Recebe um arquivo `.vm` ou um diretório contendo múltiplos arquivos `.vm`, instancia Parser e CodeWriter, coordena o processo de tradução e gera automaticamente o bootstrap quando necessário.

---

## 🚀 Como executar

### Pré-requisitos

* Java 17+
* Maven 3.6+

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

### 🔹 Traduzir um diretório contendo múltiplos arquivos `.vm`

```bash
java -jar target/vmtranslator.jar <caminho-do-diretorio>
```

Será gerado um único arquivo `.asm` com o mesmo nome da pasta:

```text
NomeDaPasta/
├── Arquivo1.vm
├── Arquivo2.vm
├── Sys.vm
└── NomeDaPasta.asm
```

Quando o programa utiliza chamadas de função, o tradutor gera automaticamente o bootstrap:

```text
SP = 256
call Sys.init
```

---

### 🔹 Rodar os 5 testes do Project 07

**StackArithmetic — SimpleAdd**

```bash
java -jar target/vmtranslator.jar src/test/resources/7/StackArithmetic/SimpleAdd/SimpleAdd.vm
```

**StackArithmetic — StackTest**

```bash
java -jar target/vmtranslator.jar src/test/resources/7/StackArithmetic/StackTest/StackTest.vm
```

**MemoryAccess — BasicTest**

```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/BasicTest/BasicTest.vm
```

**MemoryAccess — PointerTest**

```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/PointerTest/PointerTest.vm
```

**MemoryAccess — StaticTest**

```bash
java -jar target/vmtranslator.jar src/test/resources/7/MemoryAccess/StaticTest/StaticTest.vm
```

---

### 🔹 Rodar os testes do Project 08

**ProgramFlow — BasicLoop**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/ProgramFlow/BasicLoop/
```

**ProgramFlow — FibonacciSeries**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/ProgramFlow/FibonacciSeries/
```

**FunctionCalls — SimpleFunction**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/FunctionCalls/SimpleFunction/
```

**FunctionCalls — NestedCall**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/FunctionCalls/NestedCall/
```

**FunctionCalls — FibonacciElement**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/FunctionCalls/FibonacciElement/
```

**FunctionCalls — StaticsTest**

```bash
java -jar target/vmtranslator.jar src/test/resources/8/FunctionCalls/StaticsTest/
```

Para validar qualquer teste, abra o **CPU Emulator**, vá em `File → Load Script`, selecione o arquivo `.tst` correspondente e clique em **Run**.

Resultado esperado:

```text
Comparison ended successfully.
```

---

### 🔹 Rodar os testes unitários (JUnit 5)

```bash
mvn test
```

Saída esperada:

```text
BUILD SUCCESS
```

Os testes cobrem:

| Classe           | O que cobre                                                                                                                                      |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `ParserTest`     | Filtragem de comentários, classificação de todos os tipos de comandos VM, extração correta de `arg1` e `arg2` e sequência de múltiplos comandos  |
| `CodeWriterTest` | Segmentos de memória, operações aritméticas e lógicas, labels, desvios, definição de funções, chamadas de função, retorno de funções e bootstrap |

---

## ✅ Status de validação — Projects 07 e 08

Todos os programas foram traduzidos e validados no CPU Emulator oficial do Nand2Tetris:

| Programa                         | Foco                                 | Resultado |
| -------------------------------- | ------------------------------------ | --------- |
| `StackArithmetic/SimpleAdd`      | Operações aritméticas simples        | ✅ Passou  |
| `StackArithmetic/StackTest`      | Operações da pilha                   | ✅ Passou  |
| `MemoryAccess/BasicTest`         | Push e pop nos segmentos principais  | ✅ Passou  |
| `MemoryAccess/PointerTest`       | Segmento pointer (`this`/`that`)     | ✅ Passou  |
| `MemoryAccess/StaticTest`        | Segmento static                      | ✅ Passou  |
| `ProgramFlow/BasicLoop`          | Controle de fluxo básico             | ✅ Passou  |
| `ProgramFlow/FibonacciSeries`    | Loops e desvios condicionais         | ✅ Passou  |
| `FunctionCalls/SimpleFunction`   | Definição e retorno de funções       | ✅ Passou  |
| `FunctionCalls/NestedCall`       | Chamadas de função aninhadas         | ✅ Passou  |
| `FunctionCalls/FibonacciElement` | Recursão e bootstrap                 | ✅ Passou  |
| `FunctionCalls/StaticsTest`      | Múltiplos arquivos e segmento static | ✅ Passou  |

---

## 📌 Observações

* O tradutor aceita tanto um único arquivo `.vm` quanto um diretório contendo múltiplos arquivos `.vm`;
* Os arquivos `.asm` gerados aparecem no mesmo diretório do `.vm` de origem — isso é intencional, pois o CPU Emulator espera o `.asm` na mesma pasta do `.tst`;
* Quando necessário, o bootstrap (`SP = 256` + `call Sys.init`) é gerado automaticamente;
* Labels locais são qualificados pelo escopo da função atual (`FunctionName$Label`), evitando colisões de nomes;
* O segmento `static` utiliza o formato `NomeDoArquivo.indice`, permitindo a tradução de múltiplos arquivos sem conflitos de símbolos;
* Os arquivos `.asm` e `.out` não são versionados no repositório (estão no `.gitignore`) pois são saídas geradas em tempo de execução;
* Para validar no CPU Emulator, use sempre o `.tst` — nunca carregue o `.asm` diretamente, pois o `.tst` inicializa a RAM e compara com o gabarito `.cmp` automaticamente.

---

## 👥 Créditos

**Aluna:** Maria Laura Rangel Urbano Cronemberger  
**Matrícula:** 20250071287  
**Disciplina:** EECP0026 — Compiladores  
**Professor:** Prof. Dr. Sergio Souza Costa  
**Instituição:** UFMA — Universidade Federal do Maranhão  
**Semestre:** 2026.1  

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---