package me.dessie.dessielib.storageapi.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TestComparator {

    public static boolean compareJson(File file1, File file2) {
        JsonElement element1 = readFile(file1);
        JsonElement element2 = readFile(file2);

        if(element1 == null || element2 == null) return false;
        if(element1.equals(element2)) return true;

        System.out.println("\n\nExpected: " + element2);
        System.out.println("Actual: " + element1);

        return false;
    }

    public static boolean compareYaml(File file1, File file2) {
        JsonElement element1 = convertYaml(file1);
        JsonElement element2 = convertYaml(file2);

        if(element1 == null || element2 == null) return false;
        if(element1.equals(element2)) return true;

        System.out.println("\n\nExpected: " + element1);
        System.out.println("Actual: " + element2);

        return false;
    }

    public static boolean isValid(File file) {
        return readFile(file) != null;
    }

    //Reads a .json and converts it into a JsonElement, if it's invalid, parsing it with yaml will be attempted, before returning null if both fail.
    private static JsonElement readFile(File file) {
        try {
            return JsonParser.parseReader(new FileReader(file));
        } catch (JsonParseException e) {
            return convertYaml(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JsonElement convertYaml(File file) {
        try {
            Object value = new ObjectMapper(new YAMLFactory()).readValue(file, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return JsonParser.parseString(jsonWriter.writeValueAsString(value));
        } catch (JsonParseException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
