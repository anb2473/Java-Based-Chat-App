package ServerSystems;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client() throws IOException {
        socket = new Socket("localhost", 5000);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    }

    public Client(String ServerIP) throws IOException {
        socket = new Socket(ServerIP, 5000);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    }

    public Client(int PORT) throws IOException {
        socket = new Socket("localhost", PORT);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    }

    public Client(String ServerIP, int PORT) throws IOException {
        socket = new Socket(ServerIP, PORT);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    }

    public Client(int PORT, String ServerIP) throws IOException {
        socket = new Socket(ServerIP, PORT);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    }

    public String updateClient(String output) throws IOException {
        if (output != null)
            out.writeUTF(output);

        return in.readUTF();
    }

    public String getClientIP(){
        return socket.getInetAddress().toString();
    }
}
