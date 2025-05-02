package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.model.Administrator;
import proyecto.nexpay.web.model.Nexpay;
import proyecto.nexpay.web.model.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class Server extends JFrame implements Runnable {

    private JTextArea textArea;
    private ServerSocket server;
    private Nexpay nexp;

    public Server() {
        this.nexp = Nexpay.getInstance();
        setBounds(1200, 300, 400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            System.out.println("Starting server...");
            server = new ServerSocket(9091);

            while (true) {
                System.out.println("Waiting for client connection...");
                Socket socket = server.accept();
                System.out.println("Client connected.");

                new Thread(() -> processClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClient(Socket socket) {
        try (DataInputStream inputFlow = new DataInputStream(socket.getInputStream());
             DataOutputStream outputFlow = new DataOutputStream(socket.getOutputStream())) {

            String id = inputFlow.readUTF();
            String password = inputFlow.readUTF();

            textArea.append("\nID: " + id);
            textArea.append("\nPassword: " + password);
            textArea.append("\n---------------------------");

            // Check if the ID and password match the administrator
            Administrator administrator = Administrator.getInstance();
            if (administrator.getId().equals(id) && administrator.getPassword().equals(password)) {
                outputFlow.writeUTF("ACCESS_GRANTED_ADMINISTRATOR");
                textArea.append("\nAccess granted: Administrator\n");
                return;
            }

            SimpleList<User> users = nexp.getUsers();
            for (User user : users) {
                if (user.getId().equals(id) && user.getPassword().equals(password)) {
                    outputFlow.writeUTF("ACCESS_GRANTED_USER");
                    textArea.append("\nAccess granted: User\n");
                    return;
                }
            }

            // Check if the ID exists for a regular user or the administrator
            boolean userExists = users.stream().anyMatch(u -> u.getId().equals(id));
            boolean adminExists = administrator.getId().equals(id);

            if (userExists || adminExists) {
                outputFlow.writeUTF("ACCESS_DENIED");
                textArea.append("\nAccess denied: Incorrect password\n");
            } else {
                // If the ID does not exist as a user or administrator
                outputFlow.writeUTF("USER_NOT_REGISTERED");
                textArea.append("\nAccess denied: User not registered\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

