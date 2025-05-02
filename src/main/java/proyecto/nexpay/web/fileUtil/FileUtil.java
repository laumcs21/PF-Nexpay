package proyecto.nexpay.web.fileUtil;

import proyecto.nexpay.web.datastructures.SimpleList;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.FileInputStream;

public class FileUtil {

    public static void saveFile(String path, String content, Boolean appendContent) throws IOException {
        File file = new File(path);
        File directory = file.getParentFile();

        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(path, appendContent);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }

    public static SimpleList<String> readFile(String path) throws IOException {
        SimpleList<String> content = new SimpleList<>();
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            content.addLast(line);
        }
        br.close();
        fr.close();
        return content;
    }

    public static void saveLogRecord(String logMessage, int level, String action, String logFilePath) {
        Logger LOGGER = Logger.getLogger(action);
        FileHandler fileHandler = null;

        try {
            fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.addHandler(fileHandler);

            switch (level) {
                case 1:
                    LOGGER.log(Level.INFO, action + "," + logMessage);
                    break;
                case 2:
                    LOGGER.log(Level.WARNING, action + "," + logMessage);
                    break;
                case 3:
                    LOGGER.log(Level.SEVERE, action + "," + logMessage);
                    break;
                default:
                    break;
            }

        } catch (SecurityException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (fileHandler != null) {
                fileHandler.close();
                LOGGER.removeHandler(fileHandler);
            }
        }
    }

    public static Object loadSerializedResource(String filePath) throws Exception {
        Object object;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(filePath));
            object = ois.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            if (ois != null) ois.close();
        }
        return object;
    }

    public static void saveSerializedResource(String filePath, Object object) throws Exception {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(object);
        } catch (Exception e) {
            throw e;
        } finally {
            if (oos != null) oos.close();
        }
    }

    public static Object loadSerializedXMLResource(String filePath) throws IOException {
        XMLDecoder xmlDecoder;
        Object xmlObject;

        xmlDecoder = new XMLDecoder(new FileInputStream(filePath));
        xmlObject = xmlDecoder.readObject();
        xmlDecoder.close();
        return xmlObject;
    }

    public static void saveSerializedXMLResource(String filePath, Object object) throws IOException {
        XMLEncoder xmlEncoder;
        xmlEncoder = new XMLEncoder(new FileOutputStream(filePath));
        xmlEncoder.writeObject(object);
        xmlEncoder.close();
    }
}

