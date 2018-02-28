import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;


class Bug {

    String call;
    String fname;
    String p1;
    String p2;

    public Bug(String call, String fname, String p1, String p2) {
        this.call = call;
        this.fname = fname;
        if (p1.compareTo(p2) < 0) {
            this.p1 = p1;
            this.p2 = p2;
        } else {
            this.p1 = p2;
            this.p2 = p1;
        }
    }

    public void print(int support, float confidence) {
        // TODO: Sort p1 and p2
        System.out.format("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n",
                this.call, this.fname, this.p1, this.p2, support, confidence);
    }
}

class Function {

    public String fname;
    public HashMap<Integer, String> fcalls;
    public boolean isMain;

    public Function(String fname, HashMap<Integer, String> fc, boolean isMain) {
        this.fname = fname;
        this.fcalls = fc;
        this.isMain = isMain;
        if (this.isMain) {
            this.fname = "main";
        }
    }

    public String getName() {
        return this.fname;
    }

    public HashMap<Integer, String> getCalls() {
        return this.fcalls;
    }

    public boolean isMain() {
        return this.isMain;
    }

    static Function createFunction(String s, HashMap<Integer, String> hmap) {
        String[] lines = s.split("\n");
        HashMap<Integer, String> cs = new HashMap<Integer, String>();

        boolean isMain = false;
        String cgn = ""; // call graph node
        // First line is the call graph node stuff
        for (int i = 0; i < lines.length; i++) {
            // Check for each different type of line
            if (lines[i].endsWith("external node")) {
                // Ignore external node
                continue;
            } else if (lines[i].startsWith("  CS")) {
                // Call site
                String fname = lines[i].split("'")[1];
                cs.put(fname.hashCode(), fname);
            } else if (lines[i].startsWith("CallGraph Root")) {
                // Root, dont use
                return null;
            } else if (lines[i].startsWith("Call graph node")) {
                if (lines[i].startsWith("Call graph node <<null function")) {
                    return null;
                }

                // Function header
                try {
                    cgn = lines[i].split("'")[1];
                    hmap.put(cgn.hashCode(), cgn);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Main program
                    cgn = "null";
                    hmap.put("null".hashCode(), "null");
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
        HashMap<Integer, String> hmap = new HashMap<Integer, String>();
        int support = 3;
        int confidence = 65;
        if (args.length > 1) {
            support = Integer.parseInt(args[1]);
            if (args.length > 2) {
                confidence = Integer.parseInt(args[2]);
            }
        }
        // System.out.println("Support: " + Integer.toString(support));
        // System.out.println("Confidence: " + Integer.toString(confidence));
        String[] s;
        try {
            s = readFile(args[0], StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Fuck.");
            return;
        }
        List<Function> f = new ArrayList<Function>(s.length);
        for (int i = 0; i < s.length; i++) {
            if (i == 0 && !s[i].startsWith("Call")) {
                continue;
            }
            Function fn = Function.createFunction(s[i], hmap);
            if (fn != null) {
                f.add(fn);
            }
        }

        HashMap<Integer, HashMap<Integer, Boolean>> containFunction = new HashMap<Integer, HashMap<Integer, Boolean>>(f.size());

        // Initialize each hashmap
        for (int i = 0; i < f.size(); i++) {
            containFunction.put(f.get(i).getName().hashCode(), new HashMap<Integer, Boolean>());
        }

        // Make a mapping to see which functions are called where
        for (int i = 0; i < f.size(); i++) {
            HashMap<Integer, String> fcalls = f.get(i).getCalls();
            for (Integer k : fcalls.keySet()) {
                containFunction.get(k).put(f.get(i).getName().hashCode(), true);
            }
        }

        for (int i = 0; i < f.size(); i++) {
            for (int j = 0; j < f.size(); j++) {
                Function f1 = f.get(i);
                Function f2 = f.get(j);
                if (f1.getName().hashCode() == f2.getName().hashCode()) {
                    continue;
                }

                HashMap<Integer, Boolean> f1Calls = containFunction.get(f1.getName().hashCode());
                HashMap<Integer, Boolean> f2Calls = containFunction.get(f2.getName().hashCode());
                int match = 0;
                int total = 0;


                List<Bug> bugs = new ArrayList<Bug>();
                for (Integer k : f1Calls.keySet()) {
                    if (f2Calls.containsKey(k)) {
                        match += 1;
                    } else {
                        bugs.add(new Bug(f1.getName(), hmap.get(k), f1.getName(), f2.getName()));
                    }
                    total += 1;
                }

                float c = (float) match / total * 100;
                if ((match >= support &&
                        c >= (float) confidence-0.01 &&
                        match != total)) {
                    for (int k = 0; k < bugs.size(); k++) {
                        bugs.get(k).print(match, c);
                    }
                }
            }
        }
    }
}
