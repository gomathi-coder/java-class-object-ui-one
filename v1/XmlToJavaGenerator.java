import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class XmlToJavaGenerator {

    public static void main(String[] args) {
        try {
            // Parse object definition XML
            File objectFile = new File("object_definition.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document objectDoc = dBuilder.parse(objectFile);
            objectDoc.getDocumentElement().normalize();

            // Parse execution specification XML
            File executionFile = new File("execution_spec.xml");
            Document executionDoc = dBuilder.parse(executionFile);
            executionDoc.getDocumentElement().normalize();

            // Generate Java code
            String javaCode = generateJavaCode(objectDoc, executionDoc);

            // Write to file
            try (PrintWriter out = new PrintWriter("MainExecutor.java")) {
                out.println(javaCode);
            }

            System.out.println("Java file generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateJavaCode(Document objectDoc, Document executionDoc) {
        StringBuilder sb = new StringBuilder();

        // Get the class name
        String className = objectDoc.getDocumentElement().getAttribute("class");

        // Get object properties
        NodeList objectNodes = objectDoc.getElementsByTagName("object");
        Node objectNode = objectNodes.item(0);
        Element objectElement = (Element) objectNode;

        String instanceId = getElementValue(objectElement, "instance-id");
        String name = getElementValue(objectElement, "name");
        String age = getElementValue(objectElement, "age");
        String weight = getElementValue(objectElement, "weight");
        String classNumber = getElementValue(objectElement, "classNumber");
        String section = getElementValue(objectElement, "section");

        // Build the Java code
        sb.append("public class MainExecutor\n{\n");
        sb.append("\tpublic static void main(String[] args)\n\t{\n");
        
        // Object creation and initialization
        sb.append("\t\t").append(className).append(" ").append(instanceId).append(" = new ").append(className).append("();\n");
        sb.append("\t\t").append(instanceId).append(".name = \"").append(name).append("\";\n");
        sb.append("\t\t").append(instanceId).append(".age = ").append(age).append(";\n");
        sb.append("\t\t").append(instanceId).append(".weight = ").append(weight).append(";\n");
        sb.append("\t\t").append(instanceId).append(".classNumber = ").append(classNumber).append(";\n");
        sb.append("\t\t").append(instanceId).append(".section = \"").append(section).append("\";\n\n");

        // Method executions
        NodeList executionNodes = executionDoc.getElementsByTagName("execution");
        for (int i = 0; i < executionNodes.getLength(); i++) {
            Node executionNode = executionNodes.item(i);
            if (executionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element executionElement = (Element) executionNode;
                
                String execInstanceId = getElementValue(executionElement, "instance-id");
                if (!execInstanceId.equals(instanceId)) continue;
                
                Element methodElement = (Element) executionElement.getElementsByTagName("method").item(0);
                String methodName = getElementValue(methodElement, "name");
                
                sb.append("\t\t").append(execInstanceId).append(".").append(methodName).append("(");
                
                // Handle parameters if they exist
                NodeList parameters = methodElement.getElementsByTagName("parameter");
                for (int j = 0; j < parameters.getLength(); j++) {
                    Element parameter = (Element) parameters.item(j);
                    String value = getElementValue(parameter, "value");
                    
                    // Try to parse as number, otherwise treat as string
                    try {
                        Integer.parseInt(value);
                        sb.append(value);
                    } catch (NumberFormatException e) {
                        sb.append("\"").append(value).append("\"");
                    }
                    
                    if (j < parameters.getLength() - 1) {
                        sb.append(", ");
                    }
                }
                
                sb.append(");\n");
            }
        }

        sb.append("\t}\n}");
        return sb.toString();
    }

    private static String getElementValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}