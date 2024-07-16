package websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WebSocketServer {
    private final Integer port;

    public WebSocketServer(Integer port) {
        this.port = port;
    }

    public void start() throws Exception {
        // 클라이언트와 연결을 체결할 bossGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 연결된 클라이언트와 실제 이벤트를 담당하는 부분.
        // 채널을 초기화하는 과정에서 pipeline에 추가하는 핸들러를 호출하는 이벤트 그룹.
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        try {
            b
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // client가 netty에 연결이 체결됐을 때 사용할 이벤트 핸들러를 지정한다.
                        //
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println("WebSocketServer.initChannel");
                            WebSocketFrameHandler channelHandler = new WebSocketFrameHandler();
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast("httpRequestHandler", new HttpRequestHandler("/websocket", channelHandler));
                            pipeline.addAfter("httpRequestHandler", "webSocketFrameHandler", channelHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 120)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            System.out.println("서버 시작 완료.");
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        WebSocketServer webSocketServer = new WebSocketServer(8082);
        webSocketServer.start();
    }
}
