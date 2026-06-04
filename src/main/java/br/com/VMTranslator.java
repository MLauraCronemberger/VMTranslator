package br.com;

import br.com.codewriter.CodeWriter;
import br.com.parser.Parser;

import java.nio.file.Path;

public class VMTranslator {

    public static void main(String[] args) throws Exception {

        Path input = Path.of(args[0]);

        String outputName = input.toString().replace(".vm", ".asm");

        Parser parser = new Parser(input);
        CodeWriter codeWriter = new CodeWriter(Path.of(outputName));

        String fileName = input.getFileName().toString().replace(".vm", "");

        codeWriter.setFileName(fileName);

        while (parser.hasMoreCommands()) {

            parser.advance();

            switch (parser.commandType()) {

                case C_ARITHMETIC ->
                    codeWriter.writeArithmetic(parser.arg1());

                case C_PUSH ->
                    codeWriter.writePush(
                        parser.arg1(),
                        parser.arg2()
                    );

                case C_POP ->
                    codeWriter.writePop(
                        parser.arg1(),
                        parser.arg2()
                    );
            }
        }

        codeWriter.close();
    }
}