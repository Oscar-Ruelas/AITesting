package javaroo.cmd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javaroo.cmd.UMLClass;
import javaroo.cmd.UMLRelationships;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UMLSaveLoad {
    private static final Gson GSON = new Gson();
    private UMLDiagram umlDiagram;

    public UMLSaveLoad(UMLDiagram umlDiagram) {
        this.umlDiagram = umlDiagram;
    }

    public void saveData(String saveFilePath) {
        try (FileWriter fileWriter = new FileWriter(saveFilePath + ".json")) {
            JsonObject data = new JsonObject();
            data.add("classes", createClassesJsonArray());
            data.add("relationships", createRelationshipsJsonArray());
            GSON.toJson(data, fileWriter);
            System.out.println("Data saved to " + saveFilePath);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private JsonArray createClassesJsonArray() {
        JsonArray classArray = new JsonArray();
        for (UMLClass umlClass : umlDiagram.getClasses().values()) {
            JsonObject classObject = serializeUMLClass(umlClass);
            classArray.add(classObject);
        }
        return classArray;
    }

    private JsonObject serializeUMLClass(UMLClass umlClass) {
        JsonObject classObject = new JsonObject();
        classObject.addProperty("name", umlClass.getName());
        // Assume UMLClass has a method getAttributes() that returns a List<UMLAttribute>
        JsonArray fieldsArray = new JsonArray();
        for (UMLFields fields : umlClass. getFields()) {
            JsonObject fieldObject = new JsonObject();
            fieldObject.addProperty("name", fields.getName());
            fieldsArray.add(fieldObject);
        }
        JsonArray methodsArray = new JsonArray();
        for (UMLMethods methods : umlClass.getMethods()) {
            JsonObject methodObject = new JsonObject();
            methodObject.addProperty("name", methods.getName());
            methodsArray.add(methodObject);
        }
        classObject.add("fields", fieldsArray);
        classObject.add("methods", methodsArray);
        return classObject;
    }

    private JsonArray createRelationshipsJsonArray() {
        JsonArray relationshipArray = new JsonArray();
        for (UMLRelationships relationship : umlDiagram.getRelationships()) {
            JsonObject relationshipObject = serializeUMLRelationship(relationship);
            relationshipArray.add(relationshipObject);
        }
        return relationshipArray;
    }

    private JsonObject serializeUMLRelationship(UMLRelationship relationship) {
        JsonObject relationshipObject = new JsonObject();
        relationshipObject.addProperty("source", relationship.getSource().getName());
        relationshipObject.addProperty("destination", relationship.getDestination().getName());
        relationshipObject.addProperty("type", relationship.getType().toString());
        return relationshipObject;
    }

    public void loadData(String saveFilePath) {
        try (FileReader fileReader = new FileReader(saveFilePath + ".json")) {
            JsonObject data = GSON.fromJson(fileReader, JsonObject.class);
            if (data == null) {
                System.err.println("Error loading data: Data file is empty or corrupted.");
                return;
            }

            JsonArray classesArray = data.getAsJsonArray("classes");
            loadClasses(classesArray);
            loadRelationships(data.getAsJsonArray("relationships"));
            System.out.println("Data loaded from " + saveFilePath);
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void loadClasses(JsonArray classesArray) {
        for (JsonElement classElement : classesArray) {
            JsonObject classObject = classElement.getAsJsonObject();
            String className = classObject.get("name").getAsString();
            UMLClass umlClass = umlDiagram.classExists(className) ?
                    umlDiagram.getClass(className) :
                    umlDiagram.addClass(className);

            JsonArray attributesArray = classObject.getAsJsonArray("attributes");
            for (JsonElement attributeElement : attributesArray) {
                String attributeName = attributeElement.getAsJsonObject().get("name").getAsString();
                umlClass.addAttribute(attributeName);
            }
        }
    }

    private void loadRelationships(JsonArray relationshipsArray) {
        for (JsonElement relationshipElement : relationshipsArray) {
            JsonObject relationshipObject = relationshipElement.getAsJsonObject();
            String sourceName = relationshipObject.get("source").getAsString();
            String destinationName = relationshipObject.get("destination").getAsString();
            UMLRelationships.RelationshipType type = UMLRelationships.RelationshipType.valueOf(relationshipObject.get("type").getAsString());

            UMLClass source = umlDiagram.getClass(sourceName);
            UMLClass destination = umlDiagram.getClass(destinationName);

            if (source != null && destination != null) {
                umlDiagram.addRelationship(source, destination, type);
            }
        }
    }
}



