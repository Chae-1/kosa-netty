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
        // 서버 설정을 지원해주는 bootstrap 클래스
        ServerBootstrap b = new ServerBootstrap();
        try {
            b
                    // 사용할 이벤트 그룹 지정, server 이므로, boss, worker 그룹을 매핑
                    .group(bossGroup, workerGroup)
                    // 사용할 서버 채널 인스턴스를 지정
                    // Nio를 사용한 서버 채널을 지정.
                    .channel(NioServerSocketChannel.class)
                    // 서버 채널에 request(연결)가 들어올 때 마다 로그를 InFO 레벨의 로그를 출력하도록 설정하는 부분
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // client가 netty에 연결이 체결됐을 때 client 채널이 사용할 이벤트 핸들러를 지정한다.
                        // Channel에 Pipeline을 받아서 핸들러를 등록하기 위해 주로 사용하는 클래스.
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            WebSocketFrameHandler channelHandler = new WebSocketFrameHandler();
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec()); // Http 헤더의 검증을 위해서 사용된다.
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024)); // Http 메세지에 담긴 Content를 합칠래 사용되는 핸들러
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
