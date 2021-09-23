package gui;

import java.io.*;
import java.util.ArrayList;
import javafx.scene.text.*;
import javafx.scene.control.TextArea;

public class ClientMessageReceiver implements Runnable {
    private BufferedReader in;
    public ArrayList<String> messageAccountNames = new ArrayList<>();
    public ArrayList<String> messages = new ArrayList<>();
    private TextArea messagesGUI;
    private ChatClientHeadless client;
    private ChatClientGUI clientGUI;

    public ClientMessageReceiver(BufferedReader in, ChatClientGUI clientGUI, ChatClientHeadless client) {
        this.in = in;
        this.messagesGUI = clientGUI.messages;
        this.clientGUI = clientGUI;
        this.client = client;
    }

    @Override
    public void run() {
        while(true) {
            try {
                String accountName = in.readLine();
                String message = in.readLine();
                if(message.equals("Left the room")) {
                    client.onlineRoomUsers.remove(accountName);
                    clientGUI.onlineUsers.setText(String.join("\n", client.onlineRoomUsers));
                } else if(message.equals("Just joined the chat!")) {
                    client.onlineRoomUsers.add(accountName);
                    clientGUI.onlineUsers.setText(String.join("\n", client.onlineRoomUsers));
                }

                messagesGUI.appendText(accountName + "\n");
                messagesGUI.appendText(message + "\n\n");
                messagesGUI.setScrollTop(Double.MAX_VALUE);
            } catch (IOException e) {
                try {
                    client.serverError();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    } 
}
