package websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Objects;

public class UserConnection {
    private ChannelHandlerContext handler;
    private Long userId;

    public UserConnection(ChannelHandlerContext handler, Long userId) {
        this.handler = handler;
        this.userId = userId;
    }

    public boolean isSameUser(UserConnection user) {
        return user.userId == this.userId;
    }

    public void sendMessage(String chatMessage) {
        handler.channel().writeAndFlush(new TextWebSocketFrame(chatMessage));
        // 2. 이를 전달 받아 Chat Database에 저장한다.
    }

    public boolean isSame(Long sendUserId) {
        return userId == sendUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConnection that = (UserConnection) o;
        return Objects.equals(handler, that.handler) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, userId);
    }
}
