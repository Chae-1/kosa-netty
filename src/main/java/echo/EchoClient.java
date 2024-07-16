package echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class EchoClient {
    private String host;
    private Integer port;
    private Channel channel;

    public EchoClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage (String message){
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        } else {
            System.err.println("Channel is not active. Unable to send message.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        EchoClient echoClient = new EchoClient("localhost", 8082);
        new Thread(() -> {
            try {
                echoClient.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter message to send: ");
            String message = scanner.nextLine();
            echoClient.sendMessage(message);
        }
    }


    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        try {
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringEncoder(), new StringDecoder(), new EchoClientHandler());
                        }
                    });

            ChannelFuture future = b.connect(host, port).sync();
            channel = future.channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    class EchoClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("Sending message to server"); // 추가된 로그
            ctx.writeAndFlush("Sii");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ctx.read();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close(); // 예외 발생 시 채널 닫기
        }
    }
}







