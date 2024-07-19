package websocket;

import io.netty.util.internal.ConcurrentSet;
import websocket.utils.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Room {
    private final Long roomId;
    private final Set<UserConnection> connectedUsers;

    public Room(Long roomId) {
        this.roomId = roomId;
        connectedUsers = new ConcurrentSet<>();
    }

    // user 정보를 삽입.
    public void join(UserConnection user) {
        connectedUsers.add(user);
        System.out.println("current Size = " + connectedUsers.size());
    }

    // 기존에 같은 user 정보를 가지고 있는 모든 채널 정보를 삭제.
    public void disConnect(UserConnection user) {
        connectedUsers.parallelStream()
                .filter(cu -> cu.isSame(user))
                .forEach(cu -> connectedUsers.remove(cu));

        System.out.println("after Remove Size = " + connectedUsers.size());
    }

    // Spring 서버에 실제 클라이언트가 보낸 메시지를 저장하는 요청을 보내는 부분
    private String saveMessage(ChatMessage chatMessage) {
        String json = JsonParser.objectToJson(chatMessage);
        System.out.println(json);
        try {
            // HttpClient를 사용하여 Was에 message 등록 요청
            HttpClient client = HttpClient.newHttpClient();
            String str = "http://localhost/api/meeting/room/" + chatMessage.getRoomId();
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadCastMessage(ChatMessage chatMessage) {
        String json = saveMessage(chatMessage);
        // 현재 ROOM에 있는 모든 유저에 메시지를 전송한다.
        // 이는 클라이언트에 실시간 채팅을 위해 필요한 부분이다.
        for (UserConnection connectedUser : connectedUsers) {
            connectedUser.sendMessage(json);
        }
    }
}
