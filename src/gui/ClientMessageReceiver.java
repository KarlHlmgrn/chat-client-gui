package gui;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ClientMessageReceiver implements Runnable {
    private BufferedReader in;
    public ArrayList<String> messageAccountNames = new ArrayList<>();
    public ArrayList<String> messages = new ArrayList<>();
    private VBox messageRoot;
    private ChatClientHeadless client;
    private ChatClientGUI clientGUI;

    public ClientMessageReceiver(BufferedReader in, ChatClientGUI clientGUI, ChatClientHeadless client) {
        this.in = in;
        this.messageRoot = clientGUI.messageRoot;
        this.clientGUI = clientGUI;
        this.client = client;
    }

    @Override
    public void run() {
        while(true) {
            try {
                BufferedImage image = null;
                String accountName = in.readLine();
                String message = in.readLine();
                if(message.equals("Left the room")) {
                    client.onlineRoomUsers.remove(accountName);
                    clientGUI.onlineUsers.setText(String.join("\n", client.onlineRoomUsers));
                } else if(message.equals("Just joined the chat!")) {
                    client.onlineRoomUsers.add(accountName);
                    clientGUI.onlineUsers.setText(String.join("\n", client.onlineRoomUsers));
                }

                Platform.runLater(() -> {
                    Text accountText = new Text(accountName);
                    accountText.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    messageRoot.getChildren().add(accountText);
                    if(!message.startsWith("http")) {
                        messageRoot.getChildren().add(new Text(message + "\n"));
                    } else {
                        try {
                            ImageView im = new ImageView(new Image(message));
                            im.setFitWidth(100);
                            im.setPreserveRatio(true);
                            messageRoot.getChildren().add(im);
                        } catch(Exception e) {
                            messageRoot.getChildren().add(new Text(message + "\n"));
                        }
                    }
                    clientGUI.messagePane.layout();
                    clientGUI.messagePane.setVvalue(1.0);
                }
                );
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
