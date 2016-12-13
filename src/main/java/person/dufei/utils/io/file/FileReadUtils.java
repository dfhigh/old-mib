package person.dufei.utils.io.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileReadUtils {

    private static final ObjectMapper OM = new ObjectMapper();

    public static String getContent(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.deleteCharAt(sb.length()-1).toString();
        } catch (Exception e) {
            log.error("caught exception when reading file content from {}", filePath, e);
            throw new RuntimeException("failed to read file " + filePath, e);
        }
    }

    public static <T> T getDeserializedContent(String filePath, Class<T> clazz) {
        String content = getContent(filePath);
        try {
            return OM.readValue(content, clazz);
        } catch (Exception e) {
            log.error("caught exception when deserializing content {} for {}...", content, clazz);
            throw new RuntimeException("failed to deserialize content", e);
        }
    }

    public static <T> T getDeserializedContent(String filePath, TypeReference<T> ref) {
        String content = getContent(filePath);
        try {
            return OM.readValue(content, ref);
        } catch (Exception e) {
            log.error("caught exception when deserializing content {} for {}...", content, ref);
            throw new RuntimeException("failed to deserialize content", e);
        }
    }

    public static List<String> getLines(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = Lists.newArrayList();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            log.error("caught exception when reading file content from {}", filePath, e);
            throw new RuntimeException("failed to read file " + filePath, e);
        }
    }

    public static List<List<String>> getFields(String filePath, String delimiter) {
        List<String> lines = getLines(filePath);
        List<List<String>> fields = Lists.newArrayListWithCapacity(lines.size());
        for (String line : lines) {
            String[] lineFields = line.split(delimiter);
            fields.add(Arrays.stream(lineFields).collect(Collectors.toList()));
        }
        return fields;
    }

    public static BufferedReader getReader(String filePath) {
        try {
            return new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            log.error("caught exception when creating buffered reader for {}", filePath);
            throw new RuntimeException("unable to create reader for " + filePath, e);
        }
    }

}
