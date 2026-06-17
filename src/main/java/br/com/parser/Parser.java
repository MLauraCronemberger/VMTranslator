package br.com.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {

    public enum CommandType {
        C_ARITHMETIC,  
        C_PUSH,        
        C_POP,         
        C_LABEL,       
        C_GOTO,        
        C_IF,          
        C_FUNCTION,    
        C_CALL,        
        C_RETURN       
    }

    private static final List<String> ARITHMETIC_COMMANDS = List.of(
        "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"
    );

    private final List<String[]> commands;
    private int current = -1;
    private String[] currentCommand;

    /*Recebe o caminho do arquivo .vm, lê todas as linhas, remove comentários e linhas vazias, e tokeniza cada comando.*/
    
    public Parser(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);

        this.commands = lines.stream()
            .map(line -> {
                // remove comentário inline (ex: "push constant 5 // comentário")
                int commentIndex = line.indexOf("//");
                return commentIndex >= 0 ? line.substring(0, commentIndex) : line;
            })
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .map(line -> line.split("\\s+"))
            .collect(Collectors.toList());
    }

    public boolean hasMoreCommands() {
        return current + 1 < commands.size();
    }

    public void advance() {
        current++;
        currentCommand = commands.get(current);
    }

    public CommandType commandType() {
        String cmd = currentCommand[0].toLowerCase();

        if (ARITHMETIC_COMMANDS.contains(cmd)) return CommandType.C_ARITHMETIC;

        return switch (cmd) {
            case "push"     -> CommandType.C_PUSH;
            case "pop"      -> CommandType.C_POP;
            case "label"    -> CommandType.C_LABEL;
            case "goto"     -> CommandType.C_GOTO;
            case "if-goto"  -> CommandType.C_IF;
            case "function" -> CommandType.C_FUNCTION;
            case "call"     -> CommandType.C_CALL;
            case "return"   -> CommandType.C_RETURN;
            default -> throw new RuntimeException("Comando desconhecido: " + cmd);
        };
    }

    /**
     * Retorna o primeiro argumento do comando atual.
     *
     * C_ARITHMETIC → o próprio comando (ex: "add")
     * C_PUSH/POP   → o segmento (ex: "local", "constant")
     * C_LABEL/GOTO/IF → o nome do rótulo (ex: "LOOP")
     * C_FUNCTION/CALL → o nome da função (ex: "Main.main")
     * Não deve ser chamado para C_RETURN.
     */
    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            return currentCommand[0];
        }
        return currentCommand[1];
    }

    /**
     * Retorna o segundo argumento do comando atual (número inteiro).
     * Só deve ser chamado para C_PUSH, C_POP, C_FUNCTION e C_CALL.
     *
     * C_PUSH/POP      → índice do segmento (ex: 2)
     * C_FUNCTION      → número de variáveis locais (ex: 3)
     * C_CALL          → número de argumentos (ex: 2)
     */
    public int arg2() {
        return Integer.parseInt(currentCommand[2]);
    }
}