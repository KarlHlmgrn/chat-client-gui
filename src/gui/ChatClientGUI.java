package gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class ChatClientGUI extends Application {
    private ChatClientHeadless client = new ChatClientHeadless();
    public TextArea messages = new TextArea("\n\n\n\n\n");
    public TextArea onlineUsers = new TextArea();
    private Scene scene;
    // private String storedIP;
    // private String storedPort;
    // private String storedAccountName;
    // private String storedPassword;

    public void chatRoom(Stage primaryStage) {
        Text roomID = new Text("Room ID: " + client.roomID);
        Button leave = new Button("Leave room");
        TextField messageToSend = new TextField();
        messages.setEditable(false);
        onlineUsers.setEditable(false);
        onlineUsers.setMaxWidth(150.0);
        messageToSend.setPromptText("Type message here");
        messageToSend.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
               try {
                ClientMessageSender.send(messageToSend.getText(), client);
                messageToSend.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            } 
            }
        });

        leave.setOnAction(action -> {
            try {
                // client.killReceiverThread();
                // client.stopConnection();
                // client.startConnection(storedIP, storedPort);
                // client.logIn(storedAccountName, storedPassword);
                // client.createRecieverThread(this);
                // roomScene(primaryStage, "You left room " + roomID);
                client.serverError();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        HBox header = new HBox(5, roomID, leave);
        header.setAlignment(Pos.CENTER);
        HBox middle = new HBox(1, messages, onlineUsers);
        middle.setAlignment(Pos.CENTER);
        VBox page = new VBox(5, header, middle, messageToSend);
        page.setAlignment(Pos.CENTER);

        scene.setRoot(page);
        primaryStage.setScene(scene);
    }

    public void logInScene(Stage primaryStage, String errorText) {
        Text errorText2 = new Text(errorText);
        TextField accountName = new TextField();
        accountName.setPromptText("Username");
        accountName.setMaxWidth(150.0);
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setMaxWidth(150.0);

        Button logIn = new Button("Log in");
        Button signUp = new Button("Sign up");
        HBox buttons = new HBox(10, logIn, signUp);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        logIn.setOnAction(action -> {
            try {
                String response = client.logIn(accountName.getText(), password.getText());
                if(response.equals("success")) {
                    // storedAccountName = accountName.getText();
                    // storedPassword = password.getText();
                    roomScene(primaryStage, "");
                } else {
                    logInScene(primaryStage, response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        signUp.setOnAction(action -> {
            try {
                String response = client.createAccount(accountName.getText(), password.getText());
                if(response.equals("success")) {
                    // storedAccountName = accountName.getText();
                    // storedPassword = password.getText();
                    roomScene(primaryStage, "");
                } else {
                    logInScene(primaryStage, response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        VBox vbox = new VBox(10, errorText2, accountName, password, buttons);
        vbox.setAlignment(Pos.CENTER);

        scene.setRoot(vbox);
        primaryStage.setScene(scene);
    }

    public void roomScene(Stage primaryStage, String error) {
        Text errorText = new Text(error);
        Button createRoom = new Button("Create new room");
        TextField joinRoom = new TextField();
        joinRoom.setPromptText("Room ID");

        createRoom.setOnAction(action -> {
            try {
                client.createRoom();
                client.createRecieverThread(this);
                chatRoom(primaryStage);
            } catch (IOException e) {
                try {
                    client.serverError();
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            chatRoom(primaryStage);
        });

        joinRoom.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    String response = client.joinRoom(joinRoom.getText());
                    if(response.equals("success")) {
                        client.createRecieverThread(this);
                        chatRoom(primaryStage);
                    } else {
                        roomScene(primaryStage, "There isn't a room with this ID");
                    }
                } catch (IOException e) {
                    try {
                        client.serverError();
                    } catch (IOException | InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        
        HBox hbox = new HBox(5, createRoom, joinRoom);
        hbox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox(5, errorText, hbox);
        vbox.setAlignment(Pos.CENTER);
        scene.setRoot(vbox);
        primaryStage.setScene(scene);
    }

    public void connectionScene(Stage primaryStage) {
        TextField ip = new TextField();
        ip.setPromptText("ip");
        TextField port = new TextField();
        port.setPromptText("port");
        Button connect = new Button("Connect");

        connect.setOnAction(action -> {
            try {
                client.startConnection(ip.getText(), port.getText(), this);
                // storedIP = ip.getText();
                // storedPort = port.getText();
                logInScene(primaryStage, "");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        port.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    client.startConnection(ip.getText(), port.getText(), this);
                    logInScene(primaryStage, "");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        HBox hbox = new HBox(5, ip, port, connect);
        hbox.setAlignment(Pos.CENTER);
        scene.setRoot(hbox);
        primaryStage.setScene(scene);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Chat-Clientv0.1");

        Text text = new Text("Chat Client v0.1");
        VBox vbox = new VBox(text);
        vbox.setAlignment(Pos.CENTER);

        scene = new Scene(vbox, 500, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
        connectionScene(primaryStage);
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }

}
