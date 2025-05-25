package proyecto.nexpay.web.persistence;

import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.model.PointHistoryEntry;

import java.io.File;
import java.io.IOException;

public class PointHistoryPersistence {

    private static final String BINARY_FILE_PATH = "src/main/resources/persistence/binary/PointHistory.data";
    private static PointHistoryPersistence instance;

    public static PointHistoryPersistence getInstance() {
        if (instance == null) {
            synchronized (PointHistoryPersistence.class) {
                if (instance == null) {
                    instance = new PointHistoryPersistence();
                }
            }
        }
        return instance;
    }

    public void saveAllPointHistory(SimpleList<PointHistoryEntry> history) {
        try {
            File file = new File(BINARY_FILE_PATH);
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs(); // crea los directorios necesarios
            }

            FileUtil.saveSerializedResource(BINARY_FILE_PATH, history);
        } catch (Exception e) {
            System.err.println("Error saving point history: " + e.getMessage());
        }
    }


    public SimpleList<PointHistoryEntry> loadPointHistory() {
        File file = new File(BINARY_FILE_PATH);
        if (!file.exists()) {
            return new SimpleList<>();
        }

        try {
            return (SimpleList<PointHistoryEntry>) FileUtil.loadSerializedResource(BINARY_FILE_PATH);
        } catch (Exception e) {
            System.err.println("Error loading point history: " + e.getMessage());
            return new SimpleList<>();
        }
    }
}
