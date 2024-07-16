package websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import websocket.utils.JsonParser;

import java.util.*;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Map<Long, Room> rooms = new HashMap<>();

    public void joinUser(ChannelHandlerContext ctx, Long userId, Long roomId) {
        rooms.putIfAbsent(roomId, new Room(roomId));
        Room room = rooms.get(roomId);
        UserConnection user = new UserConnection(ctx, userId);
        room.join(user);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) throws Exception {
        System.out.println(webSocketFrame);
        Long roomId = (Long) ctx.channel().attr(AttributeKey.valueOf("roomId")).get();
        Long userId = (Long) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        System.out.println("userId = " + userId);
        System.out.println("roomId = " + roomId);

        Room room = rooms.get(roomId);
        if (webSocketFrame instanceof TextWebSocketFrame f) {
            String request = f.text(); // 내용 -> Json
            System.out.println(request);
            handleRequest(room, request);
        } else {
            room.disConnect(new UserConnection(ctx, userId));
            throw new UnsupportedOperationException("연결 해체");
        }
    }

    private void handleRequest(Room room, String request) {
        ChatMessage chatMessage = JsonParser.stringToOjbect(request, ChatMessage.class);
        room.broadCastMessage(chatMessage);
    }
}
