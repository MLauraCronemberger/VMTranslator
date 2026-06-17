package br.com.codewriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do CodeWriter.
 *
 * Cada teste gera um arquivo .asm temporário, lê o conteúdo
 * e verifica se as instruções Assembly geradas estão corretas.
 */
public class CodeWriterTest {

    private Path outputFile;
    private CodeWriter codeWriter;

    @BeforeEach
    void setup() throws IOException {
        outputFile = Files.createTempFile("test", ".asm");
        outputFile.toFile().deleteOnExit();
        codeWriter = new CodeWriter(outputFile);
        codeWriter.setFileName("Test");
    }

    // lê o arquivo gerado e retorna como string
    private String output() throws IOException {
        codeWriter.close();
        return Files.readString(outputFile);
    }

    // verifica se todas as linhas esperadas aparecem no output, em ordem
    private void assertContainsInOrder(String output, String... lines) {
        int lastIndex = -1;
        for (String line : lines) {
            int index = output.indexOf(line, lastIndex + 1);
            assertTrue(index >= 0,
                "Linha esperada não encontrada (ou fora de ordem): '" + line + "'\nOutput:\n" + output);
            lastIndex = index;
        }
    }

    // ─── push constant ────────────────────────────────────────────────────────

    @Test
    void testPushConstant() throws IOException {
        codeWriter.writePush("constant", 7);
        String out = output();

        assertContainsInOrder(out, "@7", "D=A", "@SP", "A=M", "M=D", "@SP", "M=M+1");
    }

    // ─── push segmentos com ponteiro (local, argument, this, that) ────────────

    @Test
    void testPushLocal() throws IOException {
        codeWriter.writePush("local", 2);
        String out = output();

        // deve usar LCL como base e somar o índice
        assertContainsInOrder(out, "@LCL", "D=M", "@2", "D=D+A");
        assertTrue(out.contains("M=M+1")); // incrementa SP
    }

    @Test
    void testPushArgument() throws IOException {
        codeWriter.writePush("argument", 1);
        String out = output();
        assertTrue(out.contains("@ARG"));
    }

    @Test
    void testPushThis() throws IOException {
        codeWriter.writePush("this", 0);
        String out = output();
        assertTrue(out.contains("@THIS"));
    }

    @Test
    void testPushThat() throws IOException {
        codeWriter.writePush("that", 3);
        String out = output();
        assertTrue(out.contains("@THAT"));
    }

    // ─── push temp ────────────────────────────────────────────────────────────

    @Test
    void testPushTemp() throws IOException {
        codeWriter.writePush("temp", 6);
        String out = output();

        // temp 6 → endereço fixo 5 + 6 = 11
        assertTrue(out.contains("@11"));
        assertTrue(out.contains("M=M+1")); // incrementa SP
    }

    // ─── push pointer ─────────────────────────────────────────────────────────

    @Test
    void testPushPointer0() throws IOException {
        codeWriter.writePush("pointer", 0);
        String out = output();
        assertTrue(out.contains("@THIS")); // pointer 0 = THIS
    }

    @Test
    void testPushPointer1() throws IOException {
        codeWriter.writePush("pointer", 1);
        String out = output();
        assertTrue(out.contains("@THAT")); // pointer 1 = THAT
    }

    // ─── push static ──────────────────────────────────────────────────────────

    @Test
    void testPushStatic() throws IOException {
        codeWriter.writePush("static", 5);
        String out = output();
        assertTrue(out.contains("@Test.5")); // fileName.index
    }

    // ─── pop segmentos ────────────────────────────────────────────────────────

    @Test
    void testPopLocal() throws IOException {
        codeWriter.writePop("local", 0);
        String out = output();

        // calcula endereço, salva em R13, depois desempilha para lá
        assertContainsInOrder(out, "@LCL", "D=M", "@0", "D=D+A", "@R13", "M=D");
        assertTrue(out.contains("AM=M-1")); // decrementa SP
    }

    @Test
    void testPopArgument() throws IOException {
        codeWriter.writePop("argument", 2);
        String out = output();
        assertTrue(out.contains("@ARG"));
        assertTrue(out.contains("@2"));
    }

    @Test
    void testPopTemp() throws IOException {
        codeWriter.writePop("temp", 3);
        String out = output();
        // temp 3 → endereço fixo 5 + 3 = 8
        assertTrue(out.contains("@8"));
        assertTrue(out.contains("AM=M-1")); // decrementa SP
    }

    @Test
    void testPopPointer0() throws IOException {
        codeWriter.writePop("pointer", 0);
        String out = output();
        assertTrue(out.contains("@THIS"));
        assertTrue(out.contains("AM=M-1"));
    }

    @Test
    void testPopStatic() throws IOException {
        codeWriter.writePop("static", 2);
        String out = output();
        assertTrue(out.contains("@Test.2"));
        assertTrue(out.contains("AM=M-1"));
    }

    // ─── operações aritméticas ────────────────────────────────────────────────

    @Test
    void testAdd() throws IOException {
        codeWriter.writeArithmetic("add");
        String out = output();
        assertContainsInOrder(out, "@SP", "AM=M-1", "D=M", "A=A-1", "M=M+D");
    }

    @Test
    void testSub() throws IOException {
        codeWriter.writeArithmetic("sub");
        String out = output();
        assertContainsInOrder(out, "@SP", "AM=M-1", "D=M", "A=A-1", "M=M-D");
    }

    @Test
    void testNeg() throws IOException {
        codeWriter.writeArithmetic("neg");
        String out = output();
        assertContainsInOrder(out, "@SP", "A=M-1", "M=-M");
    }

    @Test
    void testAnd() throws IOException {
        codeWriter.writeArithmetic("and");
        String out = output();
        assertContainsInOrder(out, "@SP", "AM=M-1", "D=M", "A=A-1", "M=M&D");
    }

    @Test
    void testOr() throws IOException {
        codeWriter.writeArithmetic("or");
        String out = output();
        assertContainsInOrder(out, "@SP", "AM=M-1", "D=M", "A=A-1", "M=M|D");
    }

    @Test
    void testNot() throws IOException {
        codeWriter.writeArithmetic("not");
        String out = output();
        assertContainsInOrder(out, "@SP", "A=M-1", "M=!M");
    }

    // ─── operações relacionais (geram labels únicos) ──────────────────────────

    @Test
    void testEq() throws IOException {
        codeWriter.writeArithmetic("eq");
        String out = output();

        // deve ter o label EQ_TRUE e EQ_END e usar JEQ
        assertTrue(out.contains("EQ_TRUE"));
        assertTrue(out.contains("EQ_END"));
        assertTrue(out.contains("D;JEQ"));
        assertTrue(out.contains("M=-1")); // true = -1
        assertTrue(out.contains("M=0"));  // false = 0
    }

    @Test
    void testGt() throws IOException {
        codeWriter.writeArithmetic("gt");
        String out = output();

        assertTrue(out.contains("GT_TRUE"));
        assertTrue(out.contains("GT_END"));
        assertTrue(out.contains("D;JGT"));
    }

    @Test
    void testLt() throws IOException {
        codeWriter.writeArithmetic("lt");
        String out = output();

        assertTrue(out.contains("LT_TRUE"));
        assertTrue(out.contains("LT_END"));
        assertTrue(out.contains("D;JLT"));
    }

    @Test
    void testLabelsUnicosParaDoisEq() throws IOException {
        // dois eq seguidos devem gerar labels diferentes
        codeWriter.writeArithmetic("eq");
        codeWriter.writeArithmetic("eq");
        String out = output();

        // deve conter EQ_TRUE0 e EQ_TRUE1 — nunca o mesmo label duas vezes
        assertTrue(out.contains("EQ_TRUE0"));
        assertTrue(out.contains("EQ_TRUE1"));
    }

    // ─── Controle de fluxo (Parte 2) ───────────────────────────────────────────

@Test
void testWriteLabel() throws IOException {
    codeWriter.writeLabel("LOOP");
    String out = output();

    assertTrue(out.contains("(Test$LOOP)"));
}

@Test
void testWriteGoto() throws IOException {
    codeWriter.writeGoto("END");
    String out = output();

    assertContainsInOrder(
            out,
            "@Test$END",
            "0;JMP"
    );
}

@Test
void testWriteIf() throws IOException {
    codeWriter.writeIf("LOOP");
    String out = output();

    assertContainsInOrder(
            out,
            "@SP",
            "AM=M-1",
            "D=M",
            "@Test$LOOP",
            "D;JNE"
    );
}

// ─── Funções (Parte 2) ─────────────────────────────────────────────────────

@Test
void testWriteFunctionSemLocais() throws IOException {
    codeWriter.writeFunction("Main.main", 0);
    String out = output();

    assertTrue(out.contains("(Main.main)"));
}

@Test
void testWriteFunctionComLocais() throws IOException {
    codeWriter.writeFunction("Main.main", 3);
    String out = output();

    assertTrue(out.contains("(Main.main)"));

    // cada variável local gera M=0
    int ocorrencias = out.split("M=0", -1).length - 1;
    assertEquals(3, ocorrencias);

    // cada variável local incrementa o SP
    int incrementos = out.split("M=M\\+1", -1).length - 1;
    assertEquals(3, incrementos);
}

@Test
void testWriteCall() throws IOException {
    codeWriter.writeCall("Main.fibonacci", 2);
    String out = output();

    assertTrue(out.contains("@Main.fibonacci$ret.0"));
    assertTrue(out.contains("(Main.fibonacci$ret.0)"));

    assertTrue(out.contains("@LCL"));
    assertTrue(out.contains("@ARG"));
    assertTrue(out.contains("@THIS"));
    assertTrue(out.contains("@THAT"));

    assertContainsInOrder(
            out,
            "@Main.fibonacci",
            "0;JMP"
    );
}

@Test
void testWriteCallGeraLabelsDeRetornoUnicos() throws IOException {
    codeWriter.writeCall("Main.main", 0);
    codeWriter.writeCall("Main.main", 0);

    String out = output();

    assertTrue(out.contains("Main.main$ret.0"));
    assertTrue(out.contains("Main.main$ret.1"));
}

@Test
void testWriteReturn() throws IOException {
    codeWriter.writeReturn();
    String out = output();

    // endFrame em R14
    assertTrue(out.contains("@R14"));

    // retAddr em R15
    assertTrue(out.contains("@R15"));

    // restauração dos segmentos
    assertTrue(out.contains("@THAT"));
    assertTrue(out.contains("@THIS"));
    assertTrue(out.contains("@ARG"));
    assertTrue(out.contains("@LCL"));

    // retorno
    assertContainsInOrder(
            out,
            "@R15",
            "A=M",
            "0;JMP"
    );
}

@Test
void testWriteInit() throws IOException {
    codeWriter.writeInit();
    String out = output();

    assertContainsInOrder(
            out,
            "@256",
            "D=A",
            "@SP",
            "M=D"
    );

    // writeInit chama Sys.init através de writeCall
    assertTrue(out.contains("@Sys.init$ret.0"));
    assertTrue(out.contains("@Sys.init"));
    assertTrue(out.contains("0;JMP"));
}
}