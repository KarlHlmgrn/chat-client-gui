package gui;

import java.io.*;

public class ClientMessageSender {

    public static void send(String message, ChatClientHeadless client) throws IOException {
        client.out.println(message);
        // String response = in.readLine();
        // if(response.equals("received")) {
        //     ChatClient.messages.put("You", message);
        //     return true;
        // } else {
        //     return false;
        // }
    }
    
}
