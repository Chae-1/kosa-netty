package websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;
    private final WebSocketFrameHandler handler;

    // ws:// 요청에 대해서만 정상적으로 처리하기 위한 핸들러
    public HttpRequestHandler(String wsUri, WebSocketFrameHandler channelHandler) {
        this.wsUri = wsUri;
        this.handler = channelHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String uri = req.uri();
        System.out.println(uri);
        // ws://localhost:8082/websocket
        // 해당 url로 요청이 들어온다.
        if (uri.contains(wsUri)) {
            // uri 정보에 websocket이 포함 되어 있으면
            // 쿼리스트링을 추출한다.
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> parameters = queryStringDecoder.parameters();

            Long userId = Long.valueOf(parameters.get("userId").get(0));
            Long roomId = Long.valueOf(parameters.get("roomId").get(0));
            // 이후 다른 핸들러에서 사용할 수 있도록 userId, roomId를 저장
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
            ctx.channel().attr(AttributeKey.valueOf("roomId")).set(roomId);

            // websocket의 버전을 보고 handshaker 객체를 생성해주는 팩토리 클래스
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            // 연결 객체를 만든다.
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            // 메시지를 전송하는 핸들러에 userId를 가진 user가 참여한다고 전송
            if (handshaker == null) {
                // 해당 요청에 websocket 서버를 열 수 없다.
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);

                // 핸드 셰이킹을 수립한 이후, roomId에 맞는 room에 user를 배정한다.
                this.handler.joinUser(ctx, userId, roomId);
            }
        } else {
            // 일반적인 http request의 경우
            // 연결 해제 메시지를 사용자에게 보내고 기존 채널을 닫는다.
            if (HttpUtil.is100ContinueExpected(req)) {
                send100Continue(ctx);
            }
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            res.content().writeBytes("비정상 요청입니다 연결을 종료합니다.".getBytes());

            ChannelFuture f = ctx.writeAndFlush(res);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    // 실제 연결 호스트 위치를 리턴한다.
    private String getWebSocketLocation(FullHttpRequest req) {
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
