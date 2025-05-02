package proyecto.nexpay.web.persistence;

import proyecto.nexpay.web.model.User;
import proyecto.nexpay.web.fileUtil.FileUtil;
import proyecto.nexpay.web.datastructures.SimpleList;
import java.io.IOException;

public class UserPersistence {
    private static final String FILE_PATH = "src/main/resources/persistence/files/Users.txt";
    private static final String FILE_PATH_XML = "src/main/resources/persistence/XML/Users.data";
    private static final String FILE_PATH_BIN = "src/main/resources/persistence/binary/BinaryUsers.data";
    private static UserPersistence instance;

    public static UserPersistence getInstance() {
        if (instance == null) {
            synchronized (UserPersistence.class) {
                if (instance == null) {
                    instance = new UserPersistence();
                }
            }
        }
        return instance;
    }

    public void saveAllUsers(SimpleList<User> users) {
        StringBuilder userText = new StringBuilder();

        for (User user : users) {
            userText.append(user.getId()).append("@@");
            userText.append(user.getPassword()).append("@@");
            userText.append(user.getName()).append("@@");
            userText.append(user.getEmail()).append("@@");
            userText.append(user.getPhone()).append("@@");
            userText.append(user.getAddress()).append("@@");
            userText.append(user.getTotalBalance()).append("\n");
        }

        try {
            FileUtil.saveFile(FILE_PATH, userText.toString(), false);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public SimpleList<User> loadUsers() throws IOException {
        SimpleList<User> users = new SimpleList<>();

        SimpleList<String> content = FileUtil.readFile(FILE_PATH);

        try {
            for (String userText : content) {
                String[] split = userText.split("@@");
                if (split.length == 7) {
                    User user = new User(split[0], split[1], split[2], split[3], split[4], split[5], Double.parseDouble(split[6]));
                    users.addLast(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users from the file: " + e.getMessage());
        }
        return users;
    }

    public SimpleList<User> loadUsersXML() {
        try {
            return (SimpleList<User>) FileUtil.loadSerializedXMLResource(FILE_PATH_XML);
        } catch (IOException e) {
            System.err.println("Error loading users from XML file: " + e.getMessage());
            return new SimpleList<>();
        } catch (ClassCastException e) {
            System.err.println("Conversion error loading users from XML: " + e.getMessage());
            return new SimpleList<>();
        }
    }

    public void saveUserToXML(User user) {
        try {
            SimpleList<User> users = loadUsersXML();

            if (users == null) {
                users = new SimpleList<>();
            }

            users.addLast(user);

            FileUtil.saveSerializedXMLResource(FILE_PATH_XML, users);
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public void saveUserToBinary(User user) {
        try {
            SimpleList<User> users = loadUsersBinary();

            if (users == null) {
                users = new SimpleList<>();
            }

            users.addLast(user);

            FileUtil.saveSerializedResource(FILE_PATH_BIN, users);
            System.out.println("User record saved in binary: " + user);

        } catch (Exception e) {
            System.out.println("Error saving users in binary: " + e.getMessage());
        }
    }

    private SimpleList<User> loadUsersBinary() {
        try {
            return (SimpleList<User>) FileUtil.loadSerializedResource(FILE_PATH_BIN);
        } catch (Exception e) {
            System.out.println("Error loading users from binary: " + e.getMessage());
            return null;
        }
    }
}


