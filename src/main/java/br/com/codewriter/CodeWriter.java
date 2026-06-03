package br.com.codewriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeWriter {

    private final BufferedWriter writer;
    private int labelCounter;

    public CodeWriter(Path outputFile) throws IOException {
        this.writer = Files.newBufferedWriter(outputFile);
        this.labelCounter = 0;
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    private String getBaseAddress(String segment) {
    return switch (segment) {
        case "local" -> "LCL";
        case "argument" -> "ARG";
        case "this" -> "THIS";
        case "that" -> "THAT";
        default -> throw new IllegalArgumentException(
            "Segmento inválido: " + segment
        );
    };
}

    public void writeArithmetic(String command) throws IOException {

        switch (command){
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
        }

}

    public void writePush(String segment, int index) throws IOException {

        if ("constant".equals(segment)) {

            writeLine("@" + index);
            writeLine("D=A");

            writeLine("@SP");
            writeLine("A=M");
            writeLine("M=D");

            writeLine("@SP");
            writeLine("M=M+1");
        }

        else if ("local".equals(segment)) {

            writeLine("@LCL");
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
}

    public void writePop(String segment, int index) throws IOException {

    if ("local".equals(segment)) {

        writeLine("@LCL");
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
}

    public void close() throws IOException {
        writer.close();
    }
}