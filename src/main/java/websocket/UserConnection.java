package websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

// 1.
// 2. 이를 전달 받아 Chat Database에 저장한다.

public class UserConnection {
    private ChannelHandlerContext handler;
    private Long userId;

    // UserConnection 정보를 저장하는 클래스
    public UserConnection(ChannelHandlerContext handler, Long userId) {
        this.handler = handler;
        this.userId = userId;
    }

    public boolean isSame(UserConnection user) {
        return user.userId == this.userId;
    }

    public void sendMessage(String chatMessage) {
        System.out.println("UserConnection.sendMessage: " + chatMessage);
        handler.channel().writeAndFlush(new TextWebSocketFrame(chatMessage));
    }
}
