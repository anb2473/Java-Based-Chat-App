package ServerSystems;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Server {
    private final ServerSocket server;
    private DataOutputStream out;
    private DataInputStream in;

    private HashMap<String, File> clientData = new HashMap<>();

    private File[] userData = new File("UserData").listFiles();

    public Server() throws IOException {
        server = new ServerSocket(5000);
    }

    public Server(int PORT) throws IOException {
        server = new ServerSocket(PORT);
    }

    public void acceptConnections() throws IOException {
        while (true) {
            Socket client = server.accept();

            if (client.isConnected())
                new Thread(() -> {
                    String address = client.getInetAddress().getHostAddress();

                    while (true) {
                        StringBuilder output = new StringBuilder();
                        try {
                            in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                            String input = in.readUTF();
                            if (input.startsWith("GET")){
                                if (input.contains("ChatData")){
                                    String chatAddress = input.split("@")[1];

                                    output = new StringBuilder(Files.readString(Paths.get("UserData/" + address + "/" + chatAddress)));
                                }

                                if (input.substring(4).equals("ChatList")) {
                                    File[] userChats = clientData.get(address).listFiles();

                                    assert userChats != null;
                                    for (File chat : userChats)
                                        output.append(chat.getName()).append("//;");
                                }
                            }
                            if (input.startsWith("POST")){
                                if (input.contains("Message")){
                                    String[] splitInput = input.split("_");
                                    String message = splitInput[2];
                                    String chatAddress = splitInput[3];

                                    try (FileWriter writer = new FileWriter("UserData/" + address + "/" + chatAddress, true)) {
                                        writer.write(message + "@" + address + "//;");
                                    } catch (IOException _) {}

                                    try (FileWriter writer = new FileWriter("UserData/" + chatAddress + "/" + address, true)) {
                                        writer.write(message + "@" + address + "//;");
                                    } catch (IOException _) {}
                                }
                                else {
                                    String chatIP = input.split("//")[1];
                                    File chatFile = new File("UserData/" + address + "/" + chatIP);
                                    chatFile.createNewFile();
                                    userData = new File("UserData").listFiles();
                                }
                            }
                            else if (input.equals("CONNECT")){
                                boolean successfullyConnected = false;
                                for (File user : userData){
                                    if (user.toString().equals("UserData/" + address)){
                                        output = new StringBuilder("SUCCESS");
                                        clientData.put(address, user);
                                        successfullyConnected = true;
                                        break;
                                    }
                                }

                                if (!successfullyConnected){
                                    File file = new File("UserData/" + address);
                                    if (!file.mkdir() && !file.exists())
                                        throw new RuntimeException("Failed to create user directory");

                                    output = new StringBuilder("SUCCESS");

                                    clientData.put(address, file);
                                }
                            }

                            out = new DataOutputStream(client.getOutputStream());
                            out.writeUTF(output.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5050);

        server.acceptConnections();
    }
}
