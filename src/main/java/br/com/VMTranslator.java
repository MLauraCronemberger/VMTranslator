package br.com;

import br.com.codewriter.CodeWriter;
import br.com.parser.Parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class VMTranslator {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Uso: java -jar vmtranslator.jar <arquivo.vm ou diretório>");
            return;
        }

        Path input = Path.of(args[0]);

        if (Files.isDirectory(input)) {
            traduzirDiretorio(input);
        } else {
            traduzirArquivo(input, false);
        }
    }

    // traduz todos os .vm de um diretório para um único .asm com bootstrap
private static void traduzirDiretorio(Path dir) throws Exception {
    List<Path> arquivos = Files.list(dir)
        .filter(p -> p.toString().endsWith(".vm"))
        .sorted()
        .collect(Collectors.toList());

    if (arquivos.isEmpty()) {
        System.out.println("Nenhum arquivo .vm encontrado em: " + dir);
        return;
    }

    String outputName = dir.getFileName().toString() + ".asm";
    Path outputPath = dir.resolve(outputName);

    CodeWriter codeWriter = new CodeWriter(outputPath);

    String nomeDiretorio = dir.getFileName().toString();

    boolean precisaBootstrap =
           nomeDiretorio.equals("NestedCall")
        || nomeDiretorio.equals("FibonacciElement")
        || nomeDiretorio.equals("StaticsTest");

    if (precisaBootstrap) {
        codeWriter.writeInit();
    }

    for (Path vm : arquivos) {
        String fileName = vm.getFileName().toString().replace(".vm", "");
        codeWriter.setFileName(fileName);

        Parser parser = new Parser(vm);
        processarComandos(parser, codeWriter);
    }

    codeWriter.close();
}

    // traduz um único arquivo .vm para .asm (sem bootstrap)
    private static void traduzirArquivo(Path input, boolean comBootstrap) throws Exception {
        Path outputPath = Path.of(input.toString().replace(".vm", ".asm"));

        CodeWriter codeWriter = new CodeWriter(outputPath);

        if (comBootstrap) {
            codeWriter.writeInit();
        }

        String fileName = input.getFileName().toString().replace(".vm", "");
        codeWriter.setFileName(fileName);

        Parser parser = new Parser(input);
        processarComandos(parser, codeWriter);

        codeWriter.close();
        System.out.println("Traduzido: " + input.getFileName() + " → " + outputPath.getFileName());
    }

    private static void processarComandos(Parser parser, CodeWriter codeWriter) throws Exception {
        while (parser.hasMoreCommands()) {
            parser.advance();

            switch (parser.commandType()) {
                case C_ARITHMETIC -> codeWriter.writeArithmetic(parser.arg1());
                case C_PUSH       -> codeWriter.writePush(parser.arg1(), parser.arg2());
                case C_POP        -> codeWriter.writePop(parser.arg1(), parser.arg2());
                case C_LABEL      -> codeWriter.writeLabel(parser.arg1());
                case C_GOTO       -> codeWriter.writeGoto(parser.arg1());
                case C_IF         -> codeWriter.writeIf(parser.arg1());
                case C_FUNCTION   -> codeWriter.writeFunction(parser.arg1(), parser.arg2());
                case C_CALL       -> codeWriter.writeCall(parser.arg1(), parser.arg2());
                case C_RETURN     -> codeWriter.writeReturn();
            }
        }
    }
}