package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChatClientGUI extends Application {
    private ChatClientHeadless client = new ChatClientHeadless();
    public VBox messageRoot = new VBox();
    public ScrollPane messagePane = new ScrollPane(messageRoot);
    public VBox onlineUsersRoot = new VBox();
    public ScrollPane onlineUsersPane = new ScrollPane(onlineUsersRoot);
    private Scene scene;
    // private String storedIP;
    // private String storedPort;
    // private String storedAccountName;
    // private String storedPassword;

    public void chatRoom(Stage primaryStage) {
        Text roomID = new Text("Room ID: " + client.roomID);
        Button leave = new Button("Leave room");
        TextField messageToSend = new TextField();

        onlineUsersPane.setPrefViewportWidth(primaryStage.getWidth()*0.3);
        onlineUsersPane.fitToWidthProperty().set(true);
        onlineUsersPane.setPrefViewportHeight(primaryStage.getHeight());

        messagePane.setPrefViewportWidth(primaryStage.getWidth()*0.7);
        messagePane.fitToWidthProperty().set(true);
        messagePane.setPrefViewportHeight(primaryStage.getHeight());

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
                ClientMessageSender.send("/leaveroom", client);
                messageRoot.getChildren().clear();
                client.onlineUsers.clear();
                onlineUsersRoot.getChildren().clear();
                roomScene(primaryStage, "You left room " + client.roomID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        HBox header = new HBox(5, roomID, leave);
        header.setAlignment(Pos.CENTER);
        HBox middle = new HBox(1, messagePane, onlineUsersPane);
        middle.setAlignment(Pos.CENTER);
        VBox page = new VBox(5, header, middle, messageToSend);
        page.setAlignment(Pos.CENTER);

        scene.setRoot(page);
        primaryStage.setScene(scene);
    }

    public void createNewUserScene(Stage primaryStage) {
        FileChooser imageChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");
        FileChooser.ExtensionFilter extFilterpng = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        imageChooser.getExtensionFilters().addAll(extFilterJPG, extFilterjpg, extFilterPNG, extFilterpng);

        Circle circle = new Circle();
        circle.setRadius(75);
        circle.setFill(Color.valueOf("#666666"));
        circle.setStrokeWidth(4.0);
        circle.setStroke(Paint.valueOf("#333333"));
        Text plus = new Text("+");
        plus.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        StackPane imageUpload = new StackPane(circle, plus);

        Button submit = new Button("Submit");
        VBox root = new VBox(10, imageUpload);
        root.setAlignment(Pos.CENTER);

        imageUpload.setOnMouseClicked(action -> {
            File file = imageChooser.showOpenDialog(primaryStage);
            try {
                BufferedImage pic = ImageIO.read(file);
                ImageView temp = new ImageView(SwingFXUtils.toFXImage(pic, null));
                
                temp.setFitHeight(400);
                temp.setPreserveRatio(true);
                temp.setSmooth(true);
                temp.setCache(true);
                BufferedImage picScaled = SwingFXUtils.fromFXImage(temp.snapshot(null, null), null);
                BufferedImage[] picArray = {picScaled};
                Rectangle cropViewFinder = new Rectangle(0,0);
                cropViewFinder.setOpacity(10);
                cropViewFinder.setFill(Color.valueOf("#037ffc"));
                StackPane imageToCrop = new StackPane(temp, cropViewFinder);

                Stage crop = new Stage();
                Scene cropScene = new Scene(imageToCrop, picScaled.getWidth(), picScaled.getHeight());
                System.out.println(picScaled.getWidth() + "x" + picScaled.getHeight());
                int[] coords = new int[2];
                // imageToCrop.setOnMouseClicked(click -> {
                    // System.out.println("clicked");
                    // cropViewFinder.setX(click.getX());
                    // cropViewFinder.setY(click.getY());
                    imageToCrop.setOnDragDetected(dragStart -> {
                        coords[0] = (int)dragStart.getX();
                        coords[1] = (int)dragStart.getY();
                        System.out.println(coords[0] + "x" + coords[1] + "y");
                        imageToCrop.startFullDrag();
                    });
                    imageToCrop.setOnMouseDragExited(drag -> {
                        System.out.println("dragged");
                        // while(drag.isPrimaryButtonDown()) {
                        //     if(drag.getX() > drag.getY()) {
                        //         cropViewFinder.setWidth(drag.getY());
                        //         cropViewFinder.setHeight(drag.getY());
                        //     } else {
                        //         cropViewFinder.setWidth(drag.getX());
                        //         cropViewFinder.setHeight(drag.getX());
                        //     }
                        // }
                        if(drag.getX() > drag.getY()) {
                            picArray[0] = picScaled.getSubimage(coords[0], coords[1], (int)(drag.getY() - coords[1]), (int)(drag.getY() - coords[1]));
                        } else {
                            picArray[0] = picScaled.getSubimage(coords[0], coords[1], (int)(drag.getX() - coords[0]), (int)(drag.getX() - coords[0]));
                        }
                        // crop.close();
                        // System.out.println("closed");
                        ImageView image = new ImageView(SwingFXUtils.toFXImage(picArray[0], null));
                        root.getChildren().clear();
                        image.setFitHeight(32);
                        image.setPreserveRatio(true);
                        image.setSmooth(true);
                        image.setCache(true);
                        Text previewName = new Text(client.accountName);
                        previewName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                        VBox previewMessage = new VBox(previewName, new Text("Preview"));
                        HBox preview = new HBox(5, image, previewMessage);
                        
                        preview.setAlignment(Pos.CENTER);
                        root.getChildren().addAll(preview, submit);
                    });
                // });
                crop.setScene(cropScene);
                crop.show();
                ImageView image = new ImageView(SwingFXUtils.toFXImage(picArray[0], null));
                root.getChildren().clear();
                image.setFitHeight(32);
                image.setPreserveRatio(true);
                image.setSmooth(true);
                image.setCache(true);
                Text previewName = new Text(client.accountName);
                previewName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                VBox previewMessage = new VBox(previewName, new Text("Preview"));
                HBox preview = new HBox(5, image, previewMessage);
                
                preview.setAlignment(Pos.CENTER);
                root.getChildren().addAll(preview, submit);

            } catch (IOException | IllegalArgumentException e) {
                Boolean error = true;
            }
        });

        scene.setRoot(root);
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
                    createNewUserScene(primaryStage);
                    // storedAccountName = accountName.getText();
                    // storedPassword = password.getText();
                    // roomScene(primaryStage, "");
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
        joinRoom.maxWidth(100.0);
        joinRoom.setPromptText("Room ID");
        Button refresh = new Button("Refresh friends");
        VBox friendsList = new VBox(5);

        if(client.friends.size() > 0) {
            for(Map.Entry<String, String> entry : client.friends.entrySet()) {
                Text friendName = new Text(entry.getKey());
                Button friendRoom = new Button(entry.getValue());
                if(entry.getValue().equals("Offline")) {
                    friendRoom.setDisable(true);
                } else {
                    friendRoom.setOnAction(action -> {
                        try {
                            String response = client.joinRoom(friendRoom.getText());
                            if(response.equals("success")) {
                                client.createRecieverThread(this);
                                chatRoom(primaryStage);
                            } else {
                                roomScene(primaryStage, "There was an unknown error");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                Region region1 = new Region();
                HBox.setHgrow(region1, Priority.ALWAYS);
                HBox friendTemp = new HBox(10, friendName, region1, friendRoom);
                friendTemp.setAlignment(Pos.CENTER_LEFT);
                friendsList.getChildren().add(friendTemp);
            }
        }

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

        refresh.setOnAction(action -> {
            try {
                client.refreshFriends();
                friendsList.getChildren().clear();
                for(Map.Entry<String, String> entry : client.friends.entrySet()) {
                    Text friendName = new Text(entry.getKey());
                    Button friendRoom = new Button(entry.getValue());
                    if(entry.getValue().equals("Offline")) {
                        friendRoom.setDisable(true);
                    } else {
                        friendRoom.setOnAction(event -> {
                            try {
                                String response = client.joinRoom(friendRoom.getText());
                                if(response.equals("success")) {
                                    client.createRecieverThread(this);
                                    chatRoom(primaryStage);
                                } else {
                                    roomScene(primaryStage, "There was an unknown error");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    Region region1 = new Region();
                    HBox.setHgrow(region1, Priority.ALWAYS);
                    HBox friendTemp = new HBox(10, friendName, region1, friendRoom);
                    friendTemp.setAlignment(Pos.CENTER_LEFT);
                    friendsList.getChildren().add(friendTemp);
                }
                if(client.friends.size() > 0) {
                    VBox room = new VBox(5, errorText, createRoom, joinRoom);
                    VBox friendsVBox = new VBox(10, refresh, friendsList);
                    refresh.setAlignment(Pos.CENTER);
                    friendsVBox.setAlignment(Pos.CENTER);
                    HBox divide = new HBox(50, friendsVBox, room);
                    divide.setAlignment(Pos.CENTER);
                    room.setAlignment(Pos.CENTER);
                    friendsList.setAlignment(Pos.CENTER);
                    scene.setRoot(divide);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        VBox room = new VBox(5, errorText, createRoom, joinRoom);
        VBox friendsVBox = new VBox(10, refresh, friendsList);
        refresh.setAlignment(Pos.CENTER);
        friendsVBox.setAlignment(Pos.CENTER);
        HBox divide = new HBox(50, friendsVBox, room);
        divide.setAlignment(Pos.CENTER);
        room.setAlignment(Pos.CENTER);
        friendsList.setAlignment(Pos.CENTER);
        scene.setRoot(divide);
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
