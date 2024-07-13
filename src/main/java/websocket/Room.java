package websocket;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import websocket.utils.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Room {
    private final Long roomId;
    private final Set<UserConnection> connectedUsers;

    public Room(Long roomId) {
        this.roomId = roomId;
        connectedUsers = new HashSet<>();
    }

    public void join(UserConnection user) {
        connectedUsers.add(user);
        System.out.println("current Size = " + connectedUsers.size());
    }

    public void disConnect(UserConnection connection) {
        connectedUsers.remove(connection);
        System.out.println("after Remove Size = " + connectedUsers.size());
    }

    private String saveMessage(ChatMessage chatMessage) {
        System.out.println("Room.saveMessage");
        String json = JsonParser.objectToJson(chatMessage);
        System.out.println(json);
        try {
            HttpClient client = HttpClient.newHttpClient();
            String str = "http://localhost:8080/api/meeting/room/" + chatMessage.getRoomId();
            System.out.println(str);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(str))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(send);
                return json;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (
                URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadCastMessage(ChatMessage chatMessage) {
        Long sendUserId = chatMessage.getUserId();
        String json = saveMessage(chatMessage);
        System.out.println(connectedUsers.size());
        for (UserConnection connectedUser : connectedUsers) {
            if (!connectedUser.isSame(sendUserId))
                connectedUser.sendMessage(json);
        }
    }
}
