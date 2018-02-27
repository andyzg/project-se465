import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;


class Function {

    public String fname;
    public List<String> fcalls;
    public boolean isMain;

    public Function(String fname, List<String> fc, boolean isMain) {
        this.fname = fname;
        this.fcalls= fc;
        this.isMain = isMain;
    }

    static Function createFunction(String s) {
        String[] lines = s.split("\n");
        List<String> cs = new ArrayList<String>(); // TODO: Change type

        boolean isMain = false;
        String cgn = ""; // call graph node
        // First line is the call graph node stuff
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].endsWith("external node")) {
                continue;
            } else if (lines[i].startsWith("  CS")) {
                String fname = lines[i].split("'")[1];
                System.out.println(fname);
                cs.add(fname);
            } else if (lines[i].startsWith("CallGraph Root")) {
                isMain = true;
            } else if (lines[i].startsWith("Call graph node")) {
                try {
                    cgn = lines[i].split("'")[1];
                    System.out.println(cgn + " function name");
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        return new Function(cgn, cs, isMain);
    }
}

public class ABDT {
    static String[] readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding).split("\n\n");
    }

    public static void main(String[] args) {
        String[] s;
        try {
            s = readFile(args[0], StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Fuck.");
            return;
        }
        List<Function> f = new ArrayList<Function>(s.length);
        for (int i = 1; i < s.length; i++) {
            f.add(Function.createFunction(s[i]));
        }
    }
}
