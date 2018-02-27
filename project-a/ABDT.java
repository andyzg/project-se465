import java.io.*;


public class ABDT {
    public static void main(String[] args) {
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(args[0]);

            // This will reference one line at a time
            String line = null;

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println( "Unable to open file '" + args[0]+ "'");
        } catch (IOException ex) {
            System.out.println( "Error reading file '" + args[0] + "'");
        }
    }
}
