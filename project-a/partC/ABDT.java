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

    public void takeCalls(Function f) {
        fcalls.putAll(f.getCalls());
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
        int nested = 0;
        if (args.length > 1) {
            support = Integer.parseInt(args[1]);
            if (args.length > 2) {
                confidence = Integer.parseInt(args[2]);
                if (args.length > 3) {
                    nested = Integer.parseInt(args[3]);
                }
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
        Map<Integer, Function> functions = new HashMap<>();
        for (int i = 0; i < s.length; i++) {
            if (i == 0 && !s[i].startsWith("Call")) {
                continue;
            }
            Function fn = Function.createFunction(s[i], hmap);
            if (fn != null) {
                functions.put(fn.getName().hashCode(), fn);
            }
        }

        // for every nested level of analysis, expand one level of calls
        for (int i = 0; i < nested; i++) {
            for (Function func : functions.values()) {
                Set<Integer> calls = func.getCalls().keySet();
                Set<Function> toTake = new HashSet<>();

                for (Integer call : calls) {
                    toTake.add(functions.get(call));
                }

                for (Function expanded : toTake) {
                    func.takeCalls(expanded);
                }
            }
        }

        HashMap<Integer, HashMap<Integer, Boolean>> containFunction = new HashMap<Integer, HashMap<Integer, Boolean>>(functions.size());

        // Initialize each hashmap
        for (Function f : functions.values()) {
            containFunction.put(f.getName().hashCode(), new HashMap<Integer, Boolean>());
        }

        // Make a mapping to see which functions are called where
        for (Function f : functions.values()) {
            HashMap<Integer, String> fcalls = f.getCalls();
            for (Integer k : fcalls.keySet()) {
                containFunction.get(k).put(f.getName().hashCode(), true);
            }
        }

        // for every nested level of analysis, we push function calls up one call
        // for (int i = 0; i < nested; i++) {
        //     for (Integer called : containFunction.keySet()) {
        //         Set<Integer> grandCallers = new HashSet<>();

        //         for (Integer caller : containFunction.get(called).keySet()) {
        //             grandCallers.addAll(containFunction.get(caller).keySet());
        //         }

        //         for (Integer grandCaller : grandCallers) {
        //             if (grandCaller != called) {
        //                 containFunction.get(grandCaller).put(called, true);
        //             }
        //         }
        //     }
        // }

        for (Function f1 : functions.values()) {
            for (Function f2 : functions.values()) {
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
