package websocket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;
    private final WebSocketFrameHandler handler;

    public HttpRequestHandler(String wsUri, WebSocketFrameHandler channelHandler) {
        this.wsUri = wsUri;
        this.handler = channelHandler;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String uri = req.uri();
        System.out.println(uri);
        if (uri.contains(wsUri)) {
            // Handle WebSocket upgrade handshake
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> parameters = queryStringDecoder.parameters();

            Long userId = Long.valueOf(parameters.get("userId").get(0));
            Long roomId = Long.valueOf(parameters.get("roomId").get(0));
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
            ctx.channel().attr(AttributeKey.valueOf("roomId")).set(roomId);

            handler.joinUser(ctx, userId, roomId);
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);

            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);

            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        } else {
            // Handle HTTP request
            if (HttpUtil.is100ContinueExpected(req)) {
                send100Continue(ctx);
            }
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            res.content().writeBytes("Hello World".getBytes());

            ChannelFuture f = ctx.writeAndFlush(res);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String h = req.headers().get(HttpHeaderNames.HOST);
        System.out.println(h);
        String location = h + "/websocket";
        return "ws://" + location;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
