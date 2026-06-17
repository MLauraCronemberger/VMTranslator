package br.com.parser;

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

        assertFalse(parser.hasMoreCommands());
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
        String[] comandos = {
                "add", "sub", "neg",
                "eq", "gt", "lt",
                "and", "or", "not"
        };

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

    // ─── novos commandType (Parte 2) ─────────────────────────────────────────

    @Test
    void testCommandTypeLabel() throws IOException {
        Path vm = createTempVm("label LOOP");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_LABEL, parser.commandType());
        assertEquals("LOOP", parser.arg1());
    }

    @Test
    void testCommandTypeGoto() throws IOException {
        Path vm = createTempVm("goto END");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_GOTO, parser.commandType());
        assertEquals("END", parser.arg1());
    }

    @Test
    void testCommandTypeIfGoto() throws IOException {
        Path vm = createTempVm("if-goto LOOP");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_IF, parser.commandType());
        assertEquals("LOOP", parser.arg1());
    }

    @Test
    void testCommandTypeFunction() throws IOException {
        Path vm = createTempVm("function Main.main 2");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_FUNCTION, parser.commandType());
        assertEquals("Main.main", parser.arg1());
        assertEquals(2, parser.arg2());
    }

    @Test
    void testCommandTypeCall() throws IOException {
        Path vm = createTempVm("call Math.multiply 3");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_CALL, parser.commandType());
        assertEquals("Math.multiply", parser.arg1());
        assertEquals(3, parser.arg2());
    }

    @Test
    void testCommandTypeReturn() throws IOException {
        Path vm = createTempVm("return");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(Parser.CommandType.C_RETURN, parser.commandType());
    }

    // ─── arg1 ────────────────────────────────────────────────────────────────

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

        assertEquals("add", parser.arg1());
    }

    @Test
    void testArg1EmReturnLancaExcecao() throws IOException {
        Path vm = createTempVm("return");
        Parser parser = new Parser(vm);

        parser.advance();

        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                parser::arg1
        );
    }

    // ─── arg2 ────────────────────────────────────────────────────────────────

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

    @Test
    void testArg2ParaFunction() throws IOException {
        Path vm = createTempVm("function Main.main 3");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(3, parser.arg2());
    }

    @Test
    void testArg2ParaCall() throws IOException {
        Path vm = createTempVm("call Math.multiply 2");
        Parser parser = new Parser(vm);

        parser.advance();

        assertEquals(2, parser.arg2());
    }

    // ─── múltiplos comandos em sequência ────────────────────────────────────

    @Test
    void testSequenciaDeComandos() throws IOException {
        Path vm = createTempVm("""
            push constant 7
            push constant 8
            add
            pop local 0
            """);

        Parser parser = new Parser(vm);

        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
        assertEquals("constant", parser.arg1());
        assertEquals(7, parser.arg2());

        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_PUSH, parser.commandType());
        assertEquals(8, parser.arg2());

        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_ARITHMETIC, parser.commandType());
        assertEquals("add", parser.arg1());

        assertTrue(parser.hasMoreCommands());
        parser.advance();
        assertEquals(Parser.CommandType.C_POP, parser.commandType());
        assertEquals("local", parser.arg1());
        assertEquals(0, parser.arg2());

        assertFalse(parser.hasMoreCommands());
    }

    @Test
    void testSequenciaComandosParte2() throws IOException {
        Path vm = createTempVm("""
            function Main.main 0
            label LOOP
            if-goto LOOP
            goto END
            call Math.add 2
            return
            """);

        Parser parser = new Parser(vm);

        parser.advance();
        assertEquals(Parser.CommandType.C_FUNCTION, parser.commandType());

        parser.advance();
        assertEquals(Parser.CommandType.C_LABEL, parser.commandType());

        parser.advance();
        assertEquals(Parser.CommandType.C_IF, parser.commandType());

        parser.advance();
        assertEquals(Parser.CommandType.C_GOTO, parser.commandType());

        parser.advance();
        assertEquals(Parser.CommandType.C_CALL, parser.commandType());

        parser.advance();
        assertEquals(Parser.CommandType.C_RETURN, parser.commandType());

        assertFalse(parser.hasMoreCommands());
    }
}