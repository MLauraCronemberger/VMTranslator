package br.com.codewriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeWriter {

    private final BufferedWriter writer;
    private int labelCounter;
    private String fileName;

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

    public void setFileName(String fileName) {
         this.fileName = fileName;
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
            
            case "eq" -> {

                String trueLabel = "EQ_TRUE" + labelCounter;
                String endLabel = "EQ_END" + labelCounter;
                labelCounter++;

                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");

                writeLine("A=A-1");
                writeLine("D=M-D");

                writeLine("@" + trueLabel);
                writeLine("D;JEQ");

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

            case "gt" -> {

                String trueLabel = "GT_TRUE" + labelCounter;
                String endLabel = "GT_END" + labelCounter;
                labelCounter++;

                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");

                writeLine("A=A-1");
                writeLine("D=M-D");

                writeLine("@" + trueLabel);
                writeLine("D;JGT");

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

            case "lt" -> {

                String trueLabel = "LT_TRUE" + labelCounter;
                String endLabel = "LT_END" + labelCounter;
                labelCounter++;

                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");

                writeLine("A=A-1");
                writeLine("D=M-D");

                writeLine("@" + trueLabel);
                writeLine("D;JLT");

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

        }
}

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

                String pointer = (index == 0) ? "THIS" : "THAT";

                writeLine("@" + pointer);
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

            default ->
                throw new IllegalArgumentException(
                    "Segmento não suportado: " + segment
                );
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

                String pointer = (index == 0) ? "THIS" : "THAT";

                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");

                writeLine("@" + pointer);
                writeLine("M=D");
            }

            case "static" -> {

                writeLine("@SP");
                writeLine("AM=M-1");
                writeLine("D=M");

                writeLine("@" + fileName + "." + index);
                writeLine("M=D");
            }

            default ->
                throw new IllegalArgumentException(
                    "Segmento não suportado: " + segment
                );
        }
}

    public void close() throws IOException {
        writer.close();
    }
}