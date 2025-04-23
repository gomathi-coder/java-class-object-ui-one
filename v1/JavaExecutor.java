import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaExecutor {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a Java file name as argument.");
            return;
        }

        String javaFileName = args[0];
        executeJavaFileAndCreateXML(javaFileName);
    }

    public static void executeJavaFileAndCreateXML(String javaFileName) {
        try {
            // Step 1: Remove .java extension if present to get the class name
            String className = javaFileName.replace(".java", "");
            
            // Step 2: Compile the Java file
            System.out.println("Compiling " + javaFileName + "...");
            Process compileProcess = Runtime.getRuntime().exec("javac " + javaFileName);
            waitForProcess(compileProcess, "compilation");
            
            // Step 3: Execute the compiled class
            System.out.println("Executing " + className + "...");
            Process executeProcess = Runtime.getRuntime().exec("java " + className);
            
            // Step 4: Capture the output
            String output = captureOutput(executeProcess);
            waitForProcess(executeProcess, "execution");
            
            // Step 5: Create XML file
            String xmlFileName = className + "_output.xml";
            createXMLFile(xmlFileName, output);
            
            System.out.println("Output saved to " + xmlFileName);
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String captureOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        
        // Read standard output
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        // Read error output
        InputStream errorStream = process.getErrorStream();
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        while ((line = errorReader.readLine()) != null) {
            output.append("[ERROR] ").append(line).append("\n");
        }
        
        return output.toString();
    }

    private static void waitForProcess(Process process, String processName) throws InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println(processName + " process failed with exit code " + exitCode);
        }
    }

    private static void createXMLFile(String fileName, String content) throws IOException {
        // Escape special XML characters
        String escapedContent = content
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
        
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<executionOutput>\n");
            writer.write("  <output>" + escapedContent + "</output>\n");
            writer.write("</executionOutput>");
        }
    }
}