package br.com.codewriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeWriter {

    private final BufferedWriter writer;
    private int labelCounter;
    private int returnCounter;
    private String fileName;
    private String currentFunction;

    public CodeWriter(Path outputFile) throws IOException {
        this.writer = Files.newBufferedWriter(outputFile);
        this.labelCounter = 0;
        this.returnCounter = 0;
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    private String getBaseAddress(String segment) {
        return switch (segment) {
            case "local"    -> "LCL";
            case "argument" -> "ARG";
            case "this"     -> "THIS";
            case "that"     -> "THAT";
            default -> throw new IllegalArgumentException("Segmento inválido: " + segment);
        };
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.currentFunction = fileName; // fallback para labels fora de funções
    }

    // ─── Bootstrap ────────────────────────────────────────────────────────────

    public void writeInit() throws IOException {
        writeLine("@256");
        writeLine("D=A");
        writeLine("@SP");
        writeLine("M=D");
        writeCall("Sys.init", 0);
    }

    // ─── Controle de fluxo ────────────────────────────────────────────────────

    public void writeLabel(String label) throws IOException {
        writeLine("(" + currentFunction + "$" + label + ")");
    }

    public void writeGoto(String label) throws IOException {
        writeLine("@" + currentFunction + "$" + label);
        writeLine("0;JMP");
    }

    public void writeIf(String label) throws IOException {
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@" + currentFunction + "$" + label);
        writeLine("D;JNE");
    }

    // ─── Funções ──────────────────────────────────────────────────────────────

    public void writeFunction(String functionName, int nLocals) throws IOException {
        this.currentFunction = functionName;
        writeLine("(" + functionName + ")");
        for (int i = 0; i < nLocals; i++) {
            writeLine("@SP");
            writeLine("A=M");
            writeLine("M=0");
            writeLine("@SP");
            writeLine("M=M+1");
        }
    }

    public void writeCall(String functionName, int nArgs) throws IOException {
        String returnLabel = functionName + "$ret." + returnCounter++;

        // empilha endereço de retorno
        writeLine("@" + returnLabel);
        writeLine("D=A");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1");

        // empilha LCL, ARG, THIS, THAT
        for (String reg : new String[]{"LCL", "ARG", "THIS", "THAT"}) {
            writeLine("@" + reg);
            writeLine("D=M");
            writeLine("@SP");
            writeLine("A=M");
            writeLine("M=D");
            writeLine("@SP");
            writeLine("M=M+1");
        }

        // ARG = SP - 5 - nArgs
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@" + (5 + nArgs));
        writeLine("D=D-A");
        writeLine("@ARG");
        writeLine("M=D");

        // LCL = SP
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        // goto functionName
        writeLine("@" + functionName);
        writeLine("0;JMP");

        // rótulo de retorno
        writeLine("(" + returnLabel + ")");
    }

    public void writeReturn() throws IOException {
        // endFrame = LCL (salvo em R14)
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@R14");
        writeLine("M=D");

        // retAddr = *(endFrame - 5) (salvo em R15)
        writeLine("@5");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@R15");
        writeLine("M=D");

        // *ARG = pop()
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("A=M");
        writeLine("M=D");

        // SP = ARG + 1
        writeLine("@ARG");
        writeLine("D=M+1");
        writeLine("@SP");
        writeLine("M=D");

        // restaura THAT, THIS, ARG, LCL a partir de endFrame
        String[] regs = {"THAT", "THIS", "ARG", "LCL"};
        for (int i = 0; i < regs.length; i++) {
            writeLine("@R14");
            writeLine("D=M");
            writeLine("@" + (i + 1));
            writeLine("A=D-A");
            writeLine("D=M");
            writeLine("@" + regs[i]);
            writeLine("M=D");
        }

        // goto retAddr
        writeLine("@R15");
        writeLine("A=M");
        writeLine("0;JMP");
    }

    // ─── Aritmética ───────────────────────────────────────────────────────────

    public void writeArithmetic(String command) throws IOException {
        switch (command) {
            case "add" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("A=A-1");
                writeLine("M=M+D");
            }
            case "sub" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("A=A-1");
                writeLine("M=M-D");
            }
            case "neg" -> {
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=-M");
            }
            case "and" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("A=A-1");
                writeLine("M=M&D");
            }
            case "or" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("A=A-1");
                writeLine("M=M|D");
            }
            case "not" -> {
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=!M");
            }
            case "eq" -> writeComparison("JEQ", "EQ");
            case "gt" -> writeComparison("JGT", "GT");
            case "lt" -> writeComparison("JLT", "LT");
        }
    }

    private void writeComparison(String jump, String prefix) throws IOException {
        String trueLabel = prefix + "_TRUE" + labelCounter;
        String endLabel  = prefix + "_END"  + labelCounter;
        labelCounter++;

        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("A=A-1");
        writeLine("D=M-D");
        writeLine("@" + trueLabel);
        writeLine("D;" + jump);
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("M=0");
        writeLine("@" + endLabel);
        writeLine("0;JMP");
        writeLine("(" + trueLabel + ")");
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("M=-1");
        writeLine("(" + endLabel + ")");
    }

    // ─── Memória ──────────────────────────────────────────────────────────────

    public void writePush(String segment, int index) throws IOException {
        switch (segment) {
            case "constant" -> {
                writeLine("@" + index);
                writeLine("D=A");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            }
            case "local", "argument", "this", "that" -> {
                writeLine("@" + getBaseAddress(segment));
                writeLine("D=M");
                writeLine("@" + index);
                writeLine("D=D+A");
                writeLine("@R13");
                writeLine("M=D");
                writeLine("@R13");
                writeLine("A=M");
                writeLine("D=M");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            }
            case "temp" -> {
                writeLine("@" + (5 + index));
                writeLine("D=M");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            }
            case "pointer" -> {
                writeLine("@" + ((index == 0) ? "THIS" : "THAT"));
                writeLine("D=M");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            }
            case "static" -> {
                writeLine("@" + fileName + "." + index);
                writeLine("D=M");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            }
            default -> throw new IllegalArgumentException("Segmento não suportado: " + segment);
        }
    }

    public void writePop(String segment, int index) throws IOException {
        switch (segment) {
            case "local", "argument", "this", "that" -> {
                writeLine("@" + getBaseAddress(segment));
                writeLine("D=M");
                writeLine("@" + index);
                writeLine("D=D+A");
                writeLine("@R13");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("@R13");
                writeLine("A=M");
                writeLine("M=D");
            }
            case "temp" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("@" + (5 + index));
                writeLine("M=D");
            }
            case "pointer" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("@" + ((index == 0) ? "THIS" : "THAT"));
                writeLine("M=D");
            }
            case "static" -> {
                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");
                writeLine("@" + fileName + "." + index);
                writeLine("M=D");
            }
            default -> throw new IllegalArgumentException("Segmento não suportado: " + segment);
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}