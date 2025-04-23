import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class JavaClassGenerator {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JavaClassGenerator <input-xml-file>");
            return;
        }

        String xmlFilePath = args[0];
        String outputFilePath = xmlFilePath.replace(".xml", ".java");

        try {
            // Parse XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(xmlFilePath));

            // Get class element
            NodeList classNodes = document.getElementsByTagName("class");
            if (classNodes.getLength() == 0) {
                System.out.println("No class definition found in XML");
                return;
            }

            Element classElement = (Element) classNodes.item(0);

            // Generate Java class
            String javaCode = generateJavaClass(classElement);

            // Write to output file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
                writer.print(javaCode);
                System.out.println("Java class generated successfully: " + outputFilePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateJavaClass(Element classElement) {
        StringBuilder sb = new StringBuilder();

        // Class declaration
        String className = getElementText(classElement, "name");
        String modifiers = getElementText(classElement, "modifiers");
        
        sb.append(modifiers.isEmpty() ? "" : modifiers + " ").append("class ").append(className).append(" {\n\n");

        // Fields
        Element fieldsElement = (Element) classElement.getElementsByTagName("fields").item(0);
        NodeList fieldNodes = fieldsElement.getElementsByTagName("field");
        
        for (int i = 0; i < fieldNodes.getLength(); i++) {
            Element fieldElement = (Element) fieldNodes.item(i);
            String fieldModifiers = getElementText(fieldElement, "modifiers");
            String fieldType = getElementText(fieldElement, "type");
            String fieldName = getElementText(fieldElement, "name");
            
            sb.append("    ").append(fieldModifiers.isEmpty() ? "" : fieldModifiers + " ")
              .append(fieldType).append(" ").append(fieldName).append(";\n");
        }
        sb.append("\n");

        // Methods
        Element methodsElement = (Element) classElement.getElementsByTagName("methods").item(0);
        NodeList methodNodes = methodsElement.getElementsByTagName("method");
        
        for (int i = 0; i < methodNodes.getLength(); i++) {
            Element methodElement = (Element) methodNodes.item(i);
            String methodModifiers = getElementText(methodElement, "modifiers");
            String returnType = getElementText(methodElement, "return-type");
            String methodName = getElementText(methodElement, "name");
            
            // Method signature
            sb.append("    ").append(methodModifiers.isEmpty() ? "" : methodModifiers + " ")
              .append(returnType).append(" ").append(methodName).append("(");
            
            // Parameters
            Element parametersElement = (Element) methodElement.getElementsByTagName("parameters").item(0);
            if (parametersElement != null) {
                NodeList parameterNodes = parametersElement.getElementsByTagName("parameter");
                List<String> params = new ArrayList<>();
                
                for (int j = 0; j < parameterNodes.getLength(); j++) {
                    Element paramElement = (Element) parameterNodes.item(j);
                    String paramType = getElementText(paramElement, "type");
                    String paramName = getElementText(paramElement, "name");
                    params.add(paramType + " " + paramName);
                }
                
                sb.append(String.join(", ", params));
            }
            
            sb.append(") ");
            
            // Method body
            String methodDefinition = getElementText(methodElement, "definition");
            sb.append(methodDefinition).append("\n\n");
        }

        sb.append("}");
        return sb.toString();
    }

    private static String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }
}