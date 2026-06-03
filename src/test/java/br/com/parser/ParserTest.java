package br.com.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do Parser.
 *
 * Cria um arquivo .vm temporário em memória para cada teste,
 * sem depender de arquivos externos.
 */
public class ParserTest {

    // cria um arquivo .vm temporário com o conteúdo passado
    private Path createTempVm(String content) throws IOException {
        Path temp = Files.createTempFile("test", ".vm");
        Files.writeString(temp, content);
        temp.toFile().deleteOnExit();
        return temp;
    }

    // ─── hasMoreCommands e advance ────────────────────────────────────────────

    @Test
    void testArquivoVazioNaoTemComandos() throws IOException {
        Path vm = createTempVm("");
        Parser parser = new Parser(vm);
        assertFalse(parser.hasMoreCommands());
    }

    @Test
    void testIgnoraComentariosELinhasVazias() throws IOException {
        Path vm = createTempVm("""
            // isso é um comentário
            
            // outro comentário
            """);
        Parser parser = new Parser(vm);
        assertFalse(parser.hasMoreCommands()); // nenhum comando real
    }

    @Test
    void testIgnoraComentarioInline() throws IOException {
        Path vm = createTempVm("push constant 5 // empilha 5");
        Parser parser = new Parser(vm);

        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
        assertEquals("constant", parser.arg1());
        assertEquals(5, parser.arg2());
    }

    // ─── commandType ─────────────────────────────────────────────────────────

    @Test
    void testCommandTypePush() throws IOException {
        Path vm = createTempVm("push constant 10");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
    }

    @Test
    void testCommandTypePop() throws IOException {
        Path vm = createTempVm("pop local 0");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals(Parser.CommandType.C_POP, parser.commandType());
    }

    @Test
    void testCommandTypeArithmeticAdd() throws IOException {
        Path vm = createTempVm("add");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals(Parser.CommandType.C_ARITHMETIC, parser.commandType());
    }

    @Test
    void testTodosOsComandosAritmeticos() throws IOException {
        String[] comandos = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
        for (String cmd : comandos) {
            Path vm = createTempVm(cmd);
            Parser parser = new Parser(vm);
            parser.advance();
            assertEquals(
                Parser.CommandType.C_ARITHMETIC,
                parser.commandType(),
                "Falhou para: " + cmd
            );
        }
    }

    // ─── arg1 ─────────────────────────────────────────────────────────────────

    @Test
    void testArg1ParaPush() throws IOException {
        Path vm = createTempVm("push local 2");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals("local", parser.arg1());
    }

    @Test
    void testArg1ParaPop() throws IOException {
        Path vm = createTempVm("pop argument 1");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals("argument", parser.arg1());
    }

    @Test
    void testArg1ParaArithmeticRetornaOComando() throws IOException {
        Path vm = createTempVm("add");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals("add", parser.arg1()); // para aritmético, arg1 é o próprio comando
    }

    // ─── arg2 ─────────────────────────────────────────────────────────────────

    @Test
    void testArg2ParaPush() throws IOException {
        Path vm = createTempVm("push constant 42");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals(42, parser.arg2());
    }

    @Test
    void testArg2ParaPop() throws IOException {
        Path vm = createTempVm("pop temp 6");
        Parser parser = new Parser(vm);
        parser.advance();
        assertEquals(6, parser.arg2());
    }

    // ─── múltiplos comandos em sequência ─────────────────────────────────────

    @Test
    void testSequenciaDeComandos() throws IOException {
        Path vm = createTempVm("""
            push constant 7
            push constant 8
            add
            pop local 0
            """);

        Parser parser = new Parser(vm);

        // push constant 7
        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
        assertEquals("constant", parser.arg1());
        assertEquals(7, parser.arg2());

        // push constant 8
        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
        assertEquals(8, parser.arg2());

        // add
        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_ARITHMETIC, parser.commandType());
        assertEquals("add", parser.arg1());

        // pop local 0
        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_POP, parser.commandType());
        assertEquals("local", parser.arg1());
        assertEquals(0, parser.arg2());

        // acabou
        assertFalse(parser.hasMoreCommands());
    }
}