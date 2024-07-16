package echo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(msg);
        ctx.write(msg); // 받은 메시지를 그대로 쓰기 (에코)
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // server ->
        ctx.flush(); // 버퍼에 있는 데이터를 전송
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close(); // 예외 발생 시 채널 닫기
    }
}
