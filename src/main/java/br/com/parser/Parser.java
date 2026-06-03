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
        C_POP          
    }
 
    // Palavras que são comandos aritméticos/lógicos (não têm argumentos)
    private static final List<String> ARITHMETIC_COMMANDS = List.of(
        "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"
    );
 
    private final List<String[]> commands; 
    private int current = -1;             
    private String[] currentCommand;      
 
    /**
     * Recebe o caminho do arquivo .vm, lê todas as linhas,
     * remove comentários e linhas vazias, e tokeniza cada comando.
     */
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
        if (cmd.equals("push"))                return CommandType.C_PUSH;
        if (cmd.equals("pop"))                 return CommandType.C_POP;
 
        throw new RuntimeException("Comando desconhecido: " + cmd);
    }
 
    /**
     * Retorna o primeiro argumento do comando atual.
     *
     * Para C_ARITHMETIC: retorna o próprio comando (ex: "add")
     * Para C_PUSH/C_POP: retorna o segmento (ex: "local", "constant")
     */
    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            return currentCommand[0]; // o comando é o próprio argumento
        }
        return currentCommand[1]; // push/pop: segundo token é o segmento
    }
 
    /**
     * Retorna o segundo argumento do comando atual (o índice).
     * Só deve ser chamado para C_PUSH e C_POP.
     *
     * Exemplo: "push local 2" → arg2() retorna 2
     */
    public int arg2() {
        return Integer.parseInt(currentCommand[2]);
    }
}