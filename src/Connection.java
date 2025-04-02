import Display.Display;
import ServerSystems.Client;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Objects;

public class Connection {
    private final Display display = new Display("Chat App", 1000, 800, "/icon.png");
    private Display newChatPage = null;

    private final Client client = new Client("localhost", 5050);    // <-------------------------------------------------

    private String connectionStage = "Connect";
    
    private String[] chatList = new String[10];

    private Graphics2D g;

    private String newChatIP = "";
    private boolean locked = false;
    private boolean barOn = true;
    private int barCount = 100;

    int newChatPageDelay = 0;

    private final boolean[] pressedKeys = new boolean[256];

    private String[] chats;

    private String currentChat = "";
    private String messageData = "";

    int enter_delay = 0;
    private int refreshDelay = 0;

    private final Toolkit toolkit = Toolkit.getDefaultToolkit();

    public Connection() throws Exception {}

    public void run() throws Exception {
        do {
            g = display.graphicsInit(new Color(32, 32, 40));
            g.setFont(new Font("Sans-Serif", Font.BOLD, 15));

            if (newChatPage != null)
                updateNewChatPage();

            switch (connectionStage) {
                case "Chat List" -> chatList();
                case "Chat Menu" -> chatMenu();
                case "Query Chat Data" -> queryChatData();
                case "Query Chats" -> queryChats();
                case "Connect" -> connect();
            }

            g.setColor(new Color(37, 37, 45));
            g.fillRect(display.getFrameWidth() - 25, 0, 20, display.getFrameHeight());

            display.render();
        } while (display.getStatus() || newChatPage.getStatus());
    }

    public void queryChatData() throws IOException {
        String input = client.updateClient("GET ChatData @" + currentChat);

        if (input != null) {
            chats = input.split("//;");
            if (Objects.equals(chatList[0], ""))
                chats = new String[] {};

            connectionStage = "Chat List";
        }
    }

    public void chatList() throws IOException {
        int y = 60;

        refreshDelay--;
        if (refreshDelay <= 0){
            refreshDelay = 100;
            queryChatData();
        }

        for (String chat : chats) {
            if (chat.isEmpty())
                continue;
            String[] chatData = chat.split("@");
            String stringChatData = chatData[0];
            String chatAddress = chatData[1];

            int x = 30;
            if (Objects.equals(chatAddress, client.getClientIP().substring(10))) {
                x = display.getFrameWidth() - g.getFontMetrics().stringWidth(stringChatData) - 58;

                g.setColor(new Color(62, 62, 70));
                g.drawRoundRect(x, y, g.getFontMetrics().stringWidth(stringChatData) + 12, 30, 10, 10);
            }
            else{
                g.setColor(new Color(62, 62, 180));
                g.fillRoundRect(x, y, g.getFontMetrics().stringWidth(stringChatData) + 12, 30, 10, 10);
                g.setColor(new Color(62, 62, 230));
                g.drawRoundRect(x, y, g.getFontMetrics().stringWidth(stringChatData) + 12, 30, 10, 10);
            }

            g.setColor(new Color(225, 225, 230));
            g.drawString(stringChatData, x + 5, y + 20);

            y += 40;
        }

        g.setColor(new Color(32, 32, 40));
        g.fillRect(0, display.getFrameHeight() - 120, display.getFrameWidth(), 120);

        g.setColor(new Color(37, 37, 45));
        if (display.mouseCollide(new Rectangle(30, display.getFrameHeight() - 100, display.getFrameWidth() - 80, 40))){
            if (display.getMousePressed()){
                locked = true;
            }
            g.fillRoundRect(30, display.getFrameHeight() - 100, display.getFrameWidth() - 80, 40, 10, 10);
        }
        else if (display.getMousePressed()){
             locked = false;
        }

        g.setColor(new Color(62, 62, 70));
        g.drawRoundRect(30, display.getFrameHeight() - 100, display.getFrameWidth() - 80, 40, 10, 10);

        g.setColor(new Color(225, 225, 230));
        g.drawString(messageData, 40, display.getFrameHeight() - 73);

        if (locked){
            if (barOn)
                try {
                    g.fillRect(g.getFontMetrics().stringWidth(messageData) + 40, display.getFrameHeight() - 85, 2, 16);
                } catch (ArithmeticException e){
                    g.fillRect(40, 90, 2, 16);
                }

            barCount -= 1;
            if (barCount <= 0) {
                barOn = !barOn;
                barCount = 100;
            }

            boolean[] keys = display.getKeys();
            boolean isUppercase = keys[KeyEvent.VK_SHIFT];

            if (toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
                isUppercase = true;

            if (keys[KeyEvent.VK_ENTER] && enter_delay <= 0){
                System.out.println(currentChat);
                client.updateClient("POST_Message_" + messageData.replace("//", "/").replace("_", " ") + "_" + currentChat);
                messageData = "";
                enter_delay = 100;
            }

            enter_delay--;

            int indx = 0;
            for (boolean key : keys) {
                if (indx == KeyEvent.VK_SHIFT || indx == KeyEvent.VK_CAPS_LOCK || indx == KeyEvent.VK_ENTER) {
                    indx++;
                    continue;
                }

                if (indx == KeyEvent.VK_BACK_SPACE && key && !pressedKeys[indx]){
                    pressedKeys[indx] = true;
                    try {
                        messageData = messageData.substring(0, messageData.length() - 1);
                    } catch (StringIndexOutOfBoundsException _) {}
                    indx++;
                    continue;
                }

                if (g.getFontMetrics().stringWidth(messageData) > 890)
                    continue;

                char letter = (char) indx;
                if (!isUppercase)
                    letter = Character.toLowerCase(letter);

                if (key && !pressedKeys[indx]) {
                    messageData += letter;
                    pressedKeys[indx] = true;
                }

                if (pressedKeys[indx] && !key)
                    pressedKeys[indx] = false;

                indx++;
            }
        }

        g.setColor(new Color(37, 37, 45));
        if (display.mouseCollide(new Rectangle(50, 50, 50, 50))) {
            g.fillRoundRect(50, 50, 50, 50, 10, 10);
            if (display.getMousePressed()) {
                connectionStage = "Chat Menu";
            }
        }

        g.setColor(new Color(62, 62, 70));
        g.drawRoundRect(50, 50, 50, 50, 10, 10);

        g.setColor(new Color(225, 225, 230));
        g.drawString("Back", 57, 80);

    }

    public void updateNewChatPage() throws IOException {
        if (!newChatPage.getStatus()) {
            newChatPage = null;
            return;
        }

        Graphics2D newChatPageG = newChatPage.graphicsInit(new Color(32, 32, 40));
        newChatPageG.setFont(new Font("Sans-Serif", Font.BOLD, 15));

        newChatPageG.setColor(new Color(37, 37, 45));
        if (newChatPage.mouseCollide(new Rectangle(30, 20, 110, 35))){
            if (newChatPage.getMousePressed()){
                newChatPage.killProgram();
                newChatPage = null;
                client.updateClient("POST_" + newChatIP.replace("_", " "));
                return;
            }
            newChatPageG.fillRoundRect(30, 20, 110, 35, 10, 10);
        }

        newChatPageG.setColor(new Color(62, 62, 70));
        newChatPageG.drawRoundRect(30, 20, 110, 35, 10, 10);

        newChatPageG.setColor(new Color(225, 225, 230));
        newChatPageG.drawString("Create Chat", 40, 43);

        newChatPageG.setColor(new Color(37, 37, 45));
        if (newChatPage.mouseCollide(new Rectangle(30, 80, 420, 35))){
            if (newChatPage.getMousePressed()){
                locked = true;
                barCount = 100;
            }
            newChatPageG.fillRoundRect(30, 80, 420, 35, 10, 10);
        }
        else if (newChatPage.getMousePressed())
            locked = false;

        newChatPageG.setColor(new Color(62, 62, 70));
        newChatPageG.drawRoundRect(30, 80, 420, 35, 10, 10);

        if (newChatIP.isEmpty() && !locked) {
            newChatPageG.setColor(new Color(125, 125, 130));
            newChatPageG.drawString("localhost", 40, 103);
        }
        else {
            newChatPageG.setColor(new Color(225, 225, 230));
            newChatPageG.drawString(newChatIP, 40, 103);
        }

        if (locked){
            if (barOn)
                try {
                    newChatPageG.fillRect(newChatPageG.getFontMetrics().stringWidth(newChatIP) + 40, 90, 2, 16);
                } catch (ArithmeticException e){
                    newChatPageG.fillRect(40, 90, 2, 16);
                }

            barCount -= 1;
            if (barCount <= 0) {
                barOn = !barOn;
                barCount = 100;
            }

            boolean[] keys = newChatPage.getKeys();
            boolean isUppercase = keys[KeyEvent.VK_SHIFT];
            int indx = 0;

            if (toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
                isUppercase = true;

            if (keys[KeyEvent.VK_ENTER]){
                newChatPage.killProgram();
                newChatPage = null;
                client.updateClient("POST_" + newChatIP.replace("_", " "));
                return;
            }

            for (boolean key : keys) {
                if (indx == KeyEvent.VK_SHIFT || indx == KeyEvent.VK_CAPS_LOCK || indx == KeyEvent.VK_ENTER) {
                    indx++;
                    continue;
                }

                if (indx == KeyEvent.VK_BACK_SPACE && key && !pressedKeys[indx]){
                    pressedKeys[indx] = true;
                    try {
                        newChatIP = newChatIP.substring(0, newChatIP.length() - 1);
                    } catch (StringIndexOutOfBoundsException _) {}
                    indx++;
                    continue;
                }

                if (newChatPageG.getFontMetrics().stringWidth(newChatIP) > 390)
                    continue;

                char letter = (char) indx;
                if (!isUppercase)
                    letter = Character.toLowerCase(letter);

                if (key && !pressedKeys[indx]) {
                    newChatIP += letter;
                    pressedKeys[indx] = true;
                }

                if (pressedKeys[indx] && !key)
                    pressedKeys[indx] = false;

                indx++;
            }
        }

        newChatPage.render();
    }

    public void connect() throws IOException {
        String input = client.updateClient("CONNECT");

        if (Objects.equals(input, "SUCCESS"))
            connectionStage = "Query Chats";
    }

    public void queryChats() throws IOException {
        String input = client.updateClient("GET ChatList");

        if (input != null) {
            chatList = input.split("//;");
            if (Objects.equals(chatList[0], ""))
                chatList = new String[] {};

            connectionStage = "Chat Menu";
        }
    }

    public void chatMenu() throws Exception {
        int y = 60;

        refreshDelay--;
        if (refreshDelay <= 0){
            refreshDelay = 100;
            queryChats();
        }


        for (String chatName : chatList) {
            g.setColor(new Color(37, 37, 45));
            if (display.mouseCollide(new Rectangle(30, y, 922, 100))){
                if (display.getMousePressed()) {
                    connectionStage = "Query Chat Data";
                    currentChat = chatName;
                }
                g.fillRoundRect(30, y, display.getFrameWidth() - 82, 100, 20, 20);
            }

            g.setColor(new Color(62, 62, 70));
            g.drawRoundRect(30, y, display.getFrameWidth() - 82, 100, 20, 20);

            g.setFont(new Font("Sans-Serif", Font.BOLD, 35));
            g.setColor(new Color(225, 225, 230));
            g.drawString(chatName, 50, y + 63);

            g.setFont(new Font("Sans-Serif", Font.BOLD, 15));

            y += 120;
        }

        g.setColor(new Color(32, 32, 40));
        g.fillRect(0, 0, display.getFrameWidth(), 50);

        g.setColor(new Color(37, 37, 45));
        if (display.mouseCollide(new Rectangle(display.getFrameWidth() - 242, 20, 90, 25))){
            if (display.getMousePressed() && newChatPage == null && newChatPageDelay <= 0){
                newChatPageDelay = 20;
                newChatPage = new Display("Create New Chat", 500, 500, "/icon.png", false, true);
            }
            g.fillRoundRect(display.getFrameWidth() - 242, 20, 90, 25, 10, 10);
        }

        newChatPageDelay--;

        g.setColor(new Color(62, 62, 70));
        g.drawRoundRect(display.getFrameWidth() - 242, 20, 90, 25, 10, 10);

        g.setColor(new Color(225, 225, 230));
        g.drawString("New", display.getFrameWidth() - 215, 38);

        g.setColor(new Color(37, 37, 45));
        if (display.mouseCollide(new Rectangle(display.getFrameWidth() - 142, 20, 90, 25)))
            g.fillRoundRect(display.getFrameWidth() - 142, 20, 90, 25, 10, 10);

        g.setColor(new Color(62, 62, 70));
        g.drawRoundRect(display.getFrameWidth() - 142, 20, 90, 25, 10, 10);
    }

    public static void main(String[] args) throws Exception {
        Connection connection1 = new Connection();
        connection1.run();
    }
}