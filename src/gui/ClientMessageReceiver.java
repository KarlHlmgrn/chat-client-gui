package gui;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
                String accountName = in.readLine();
                String message = in.readLine();
                if(message.equals("/(leave") && accountName.equals(client.accountName)) {
                    break;
                }
                Platform.runLater(() -> {
                    if(message.equals("Left the room")) {
                        int index = client.onlineUsers.indexOf(accountName);
                        client.onlineUsers.remove(accountName);
                        clientGUI.onlineUsersRoot.getChildren().remove(index);
                    } else if(message.equals("Just joined the chat!")) {
                        client.onlineUsers.add(accountName);
                        Text username = new Text(accountName);
                        username.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                        HBox namesHBox = new HBox(10, username);
                        Region region1 = new Region();
                        HBox.setHgrow(region1, Priority.ALWAYS);
                        if(client.friends.containsKey(accountName)) {
                            Button btn = new Button("Friends!");
                            btn.setDisable(true);
                            namesHBox.getChildren().addAll(region1, btn);
                        } else if(accountName.equals(client.accountName)) {
                            Button btn = new Button("You");
                            btn.setDisable(true);
                            namesHBox.getChildren().addAll(region1, btn);
                        } else {
                            Button btn = new Button("Add friend");
                            btn.setOnAction(action -> {
                                try {
                                    ClientMessageSender.send(("/add " + accountName), client);
                                    btn.setText("Friends!");
                                    btn.setDisable(true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            namesHBox.getChildren().addAll(region1, btn);
                        }
                        namesHBox.setAlignment(Pos.CENTER_LEFT);
                        clientGUI.onlineUsersRoot.getChildren().add(namesHBox);
                    }

                    Text accountText = new Text(accountName);
                    accountText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    if(!accountName.equals(client.accountName)) {
                        messageRoot.getChildren().add(accountText);
                    } else {
                        Region region = new Region();
                        HBox.setHgrow(region, Priority.ALWAYS);
                        HBox tempHBox = new HBox(region, accountText);                        
                        messageRoot.getChildren().add(tempHBox);
                    }
                    if(!message.startsWith("http")) {
                        if(!accountName.equals(client.accountName)) {
                            messageRoot.getChildren().add(new Text(message + "\n"));
                        } else {
                            Region region = new Region();
                            HBox.setHgrow(region, Priority.ALWAYS);
                            HBox tempHBox = new HBox(region, new Text(message + "\n"));                            
                            messageRoot.getChildren().add(tempHBox);
                        }
                    } else {
                        Image image = new Image(message);
                        ImageView im = new ImageView(image);
                        if(!image.isError()) {
                            im.setFitWidth(100);
                            im.setPreserveRatio(true);
                            Region region = new Region();
                            HBox.setHgrow(region, Priority.ALWAYS);
                            HBox tempHBox = new HBox(region, im);                        
                            messageRoot.getChildren().add(tempHBox);
                        } else {
                            if(!accountName.equals(client.accountName)) {
                                messageRoot.getChildren().add(new Text("Error getting image\n"));
                            } else {
                                Region region = new Region();
                                HBox.setHgrow(region, Priority.ALWAYS);
                                HBox tempHBox = new HBox(region, new Text("Error getting image\n"));                            
                                messageRoot.getChildren().add(tempHBox);
                            }
                        }
                    }
                    clientGUI.messagePane.layout();
                    clientGUI.messagePane.setVvalue(1.0);
                });

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
