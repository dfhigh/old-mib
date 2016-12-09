package person.dufei.utils.io.file;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class FileReadUtils {

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

    public static BufferedReader getReader(String filePath) {
        try {
            return new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            log.error("caught exception when creating buffered reader for {}", filePath);
            throw new RuntimeException("unable to create reader for " + filePath, e);
        }
    }

}
