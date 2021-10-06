package gui;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.lang.StringBuilder;

public class ChatClientHeadless {
    private Socket clientSocket;
    public PrintWriter out;
    public BufferedReader in;
    public String accountName;
    public String roomID;
    public ChatClientGUI clientGUI;
    public static ArrayList<String> messageAccountNames = new ArrayList<>();
    public static ArrayList<String> messages = new ArrayList<>();
    public ArrayList<String> onlineUsers = new ArrayList<>();
    public Map<String, String> friends = new HashMap<>();
    private ExecutorService receiverThread;

    public void clearScreen() throws IOException, InterruptedException {  
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            Runtime.getRuntime().exec("clear");
        }
    }  

    public void startConnection(String ip, String port, ChatClientGUI clientGUI) throws IOException, InterruptedException {
        try {
            clientSocket = new Socket(ip, Integer.valueOf(port));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientGUI = clientGUI;
        } catch(IOException e) {
            serverError();
        }
    }

    public String createAccount(String accountName, String password) throws IOException {
        out.println("createAccount");
        out.println(accountName);
        out.println(password);
        String response = in.readLine();
        return response;
    }

    public String logIn(String accountName, String password) throws IOException {
        out.println("logIn");
        out.println(accountName);
        out.println(password);
        String response = in.readLine();
        if(response.equals("success")) {
            String friendsResponse = "";
            while(true) {
                friendsResponse = in.readLine();
                if(!friendsResponse.equals("done")) {
                    String friendName = friendsResponse.split("\\$")[0];
                    String friendRoom = friendsResponse.split("\\$")[1];
                    friends.put(friendName, friendRoom);
                } else {break;}
            }
        }
        return response;
    }

    public void createRoom() throws IOException {
        out.println("createRoom");
        this.roomID = in.readLine();
    }

    public String joinRoom(String requestedRoomID) throws IOException {
        out.println("joinRoom");
        out.println(requestedRoomID);
        String response = in.readLine();
        if(response.equals("success")) {
            this.roomID = requestedRoomID;
            response = "";
            while(true) {
                response = in.readLine();
                if(!response.equals("done")) {
                    onlineUsers.add(response);
                    Text username = new Text(response);
                    username.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    HBox namesHBox = new HBox(10, username);
                    Region region1 = new Region();
                    HBox.setHgrow(region1, Priority.ALWAYS);
                    if(friends.containsKey(response)) {
                        Button btn = new Button("Friends!");
                        btn.setDisable(true);
                        namesHBox.getChildren().addAll(region1, btn);
                    } else {
                        Button btn = new Button("Add friend");
                        btn.setOnAction(action -> {
                            try {
                                ClientMessageSender.send(("/add " + accountName), this);
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
                } else {break;}
            }
            return "success";
        } else {return "error";}
        
    }

    public void createRecieverThread(ChatClientGUI clientGUI) {
        receiverThread = Executors.newSingleThreadExecutor();
        ClientMessageReceiver messageReceiver = new ClientMessageReceiver(in, clientGUI, this);
        receiverThread.execute(messageReceiver);
    }

    public void killReceiverThread() {
        receiverThread.shutdown();
    }

    public void updateConsole() throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i<messages.size(); i++) {
            stringBuilder.append(messageAccountNames.get(i) + "\n");
            stringBuilder.append(messages.get(i) + "\n\n");
        }
        clearScreen();
        System.out.println(stringBuilder);
        System.out.print("Send: ");

    }

    public void serverError() throws IOException, InterruptedException {
        System.out.println("Can't connect to the server, shutting down...");
        System.exit(0);
        stopConnection();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public void main(String[] args) throws IOException, InterruptedException {
    }
}
