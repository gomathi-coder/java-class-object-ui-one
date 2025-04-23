import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JavaToXmlConverter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java JavaToXmlConverter <input.java>");
            return;
        }

        String inputFile = args[0];
        try {
            String javaCode = readFile(inputFile);
            String xmlOutput = convertJavaToXml(javaCode);
            
            String outputFile = inputFile.replace(".java", ".xml");
            writeFile(outputFile, xmlOutput);
            System.out.println("XML specification generated: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void writeFile(String fileName, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
    }

    private static String convertJavaToXml(String javaCode) {
        // Remove comments to avoid processing them
        String codeWithoutComments = removeComments(javaCode);
        
        // Extract package declaration
        String packageName = extractPackage(codeWithoutComments);
        
        // Extract imports
        List<String> imports = extractImports(codeWithoutComments);
        
        // Extract class information
        ClassInfo classInfo = extractClassInfo(codeWithoutComments);
        
        // Build XML
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<java-class>\n");
        
        if (packageName != null) {
            xml.append("  <package>").append(packageName).append("</package>\n");
        }
        
        if (!imports.isEmpty()) {
            xml.append("  <imports>\n");
            for (String imp : imports) {
                xml.append("    <import>").append(imp).append("</import>\n");
            }
            xml.append("  </imports>\n");
        }
        
        xml.append("  <class>\n");
        xml.append("    <name>").append(classInfo.name).append("</name>\n");
        xml.append("    <modifiers>").append(classInfo.modifiers).append("</modifiers>\n");
        
        if (classInfo.superClass != null) {
            xml.append("    <extends>").append(classInfo.superClass).append("</extends>\n");
        }
        
        if (!classInfo.interfaces.isEmpty()) {
            xml.append("    <implements>\n");
            for (String iface : classInfo.interfaces) {
                xml.append("      <interface>").append(iface).append("</interface>\n");
            }
            xml.append("    </implements>\n");
        }
        
        // Add fields
        if (!classInfo.fields.isEmpty()) {
            xml.append("    <fields>\n");
            for (FieldInfo field : classInfo.fields) {
                xml.append("      <field>\n");
                xml.append("        <modifiers>").append(field.modifiers).append("</modifiers>\n");
                xml.append("        <type>").append(field.type).append("</type>\n");
                xml.append("        <name>").append(field.name).append("</name>\n");
                if (field.value != null) {
                    xml.append("        <value>").append(field.value).append("</value>\n");
                }
                xml.append("      </field>\n");
            }
            xml.append("    </fields>\n");
        }
        
        // Add methods
        if (!classInfo.methods.isEmpty()) {
            xml.append("    <methods>\n");
            for (MethodInfo method : classInfo.methods) {
                xml.append("      <method>\n");
                xml.append("        <modifiers>").append(method.modifiers).append("</modifiers>\n");
                xml.append("        <return-type>").append(method.returnType).append("</return-type>\n");
                xml.append("        <name>").append(method.name).append("</name>\n");
                
                if (!method.parameters.isEmpty()) {
                    xml.append("        <parameters>\n");
                    for (ParameterInfo param : method.parameters) {
                        xml.append("          <parameter>\n");
                        xml.append("            <type>").append(param.type).append("</type>\n");
                        xml.append("            <name>").append(param.name).append("</name>\n");
                        xml.append("          </parameter>\n");
                    }
                    xml.append("        </parameters>\n");
                }
                
                if (!method.exceptions.isEmpty()) {
                    xml.append("        <throws>\n");
                    for (String exception : method.exceptions) {
                        xml.append("          <exception>").append(exception).append("</exception>\n");
                    }
                    xml.append("        </throws>\n");
                }
                
                xml.append("      </method>\n");
            }
            xml.append("    </methods>\n");
        }
        
        xml.append("  </class>\n");
        xml.append("</java-class>");
        
        return xml.toString();
    }

    private static String removeComments(String code) {
        // Remove single-line comments
        code = code.replaceAll("//.*", "");
        // Remove multi-line comments
        code = code.replaceAll("/\\*.*?\\*/", "");
        return code;
    }

    private static String extractPackage(String code) {
        Matcher matcher = Pattern.compile("package\\s+([^;]+);").matcher(code);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static List<String> extractImports(String code) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = Pattern.compile("import\\s+([^;]+);").matcher(code);
        while (matcher.find()) {
            imports.add(matcher.group(1).trim());
        }
        return imports;
    }

    private static ClassInfo extractClassInfo(String code) {
        ClassInfo classInfo = new ClassInfo();
        
        // Extract class declaration
        Matcher classMatcher = Pattern.compile(
            "(?s)((?:public|protected|private|static|final|abstract|sealed|non-sealed|strictfp)\\s+)*class\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?(?:\\s+implements\\s+([^{]+))?\\s*\\{"
        ).matcher(code);
        
        if (classMatcher.find()) {
            classInfo.modifiers = classMatcher.group(1) != null ? classMatcher.group(1).trim() : "";
            classInfo.name = classMatcher.group(2).trim();
            classInfo.superClass = classMatcher.group(3) != null ? classMatcher.group(3).trim() : null;
            
            if (classMatcher.group(4) != null) {
                String[] interfaces = classMatcher.group(4).trim().split("\\s*,\\s*");
                classInfo.interfaces.addAll(Arrays.asList(interfaces));
            }
        }
        
        // Extract fields
        Matcher fieldMatcher = Pattern.compile(
            "(?s)((?:public|protected|private|static|final|transient|volatile)\\s+)*(?:\\w+(?:<[^>]+>)?\\s+\\[\\])?\\s*(\\w+(?:<[^>]+>)?)\\s+(\\w+)\\s*(?:=\\s*([^;]+))?\\s*;"
        ).matcher(code);
        
        while (fieldMatcher.find()) {
            FieldInfo field = new FieldInfo();
            field.modifiers = fieldMatcher.group(1) != null ? fieldMatcher.group(1).trim() : "";
            field.type = fieldMatcher.group(2).trim();
            field.name = fieldMatcher.group(3).trim();
            field.value = fieldMatcher.group(4) != null ? fieldMatcher.group(4).trim() : null;
            classInfo.fields.add(field);
        }
        
        // Extract methods
        Matcher methodMatcher = Pattern.compile(
            "(?s)((?:public|protected|private|static|final|abstract|synchronized|native|strictfp)\\s+)*(?:<(?:[^<>]++|\\<[^<>]+\\>)++>\\s*)*(\\w+(?:<[^>]+>)?(?:\\[\\])?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+([^{]+))?\\s*(?:\\{|;)"
        ).matcher(code);
        
        while (methodMatcher.find()) {
            MethodInfo method = new MethodInfo();
            method.modifiers = methodMatcher.group(1) != null ? methodMatcher.group(1).trim() : "";
            method.returnType = methodMatcher.group(2).trim();
            method.name = methodMatcher.group(3).trim();
            
            // Process parameters
            String params = methodMatcher.group(4);
            if (params != null && !params.trim().isEmpty()) {
                String[] paramPairs = params.split("\\s*,\\s*");
                for (String pair : paramPairs) {
                    String[] parts = pair.trim().split("\\s+");
                    if (parts.length >= 2) {
                        ParameterInfo param = new ParameterInfo();
                        param.type = parts[0].trim();
                        param.name = parts[1].trim();
                        method.parameters.add(param);
                    }
                }
            }
            
            // Process exceptions
            if (methodMatcher.group(5) != null) {
                String[] exceptions = methodMatcher.group(5).trim().split("\\s*,\\s*");
                for (String ex : exceptions) {
                    method.exceptions.add(ex.trim());
                }
            }
            
            classInfo.methods.add(method);
        }
        
        return classInfo;
    }

    // Helper classes to store extracted information
    static class ClassInfo {
        String name;
        String modifiers = "";
        String superClass;
        List<String> interfaces = new ArrayList<>();
        List<FieldInfo> fields = new ArrayList<>();
        List<MethodInfo> methods = new ArrayList<>();
    }

    static class FieldInfo {
        String modifiers;
        String type;
        String name;
        String value;
    }

    static class MethodInfo {
        String modifiers;
        String returnType;
        String name;
        List<ParameterInfo> parameters = new ArrayList<>();
        List<String> exceptions = new ArrayList<>();
    }

    static class ParameterInfo {
        String type;
        String name;
    }
}