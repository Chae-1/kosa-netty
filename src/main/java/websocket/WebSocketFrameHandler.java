package websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import websocket.utils.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// 메시지 기록을 db에 저장한다.
// room ->
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
