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

    public void writeArithmetic(String command) throws IOException {

    if ("add".equals(command)) {

        writeLine("@SP");
        writeLine("AM=M-1");

        writeLine("D=M");

        writeLine("A=A-1");

        writeLine("M=M+D");
    }

        if ("sub".equals(command)) {

        writeLine("@SP");
        writeLine("AM=M-1");

        writeLine("D=M");

        writeLine("A=A-1");

        writeLine("M=M-D");
    }

        if ("neg".equals(command)) {

        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("M=-M");
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
}

    public void writePop(String segment, int index) throws IOException {
    }

    public void close() throws IOException {
        writer.close();
    }
}