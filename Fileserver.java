import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Fileserver { public static void main(String[] args) {

    try {

        ServerSocketChannel wellcomChannel = ServerSocketChannel.open();
        wellcomChannel.bind(new InetSocketAddress(3000));

        while (true){
            SocketChannel socketChannel = wellcomChannel.accept();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //read from TCP channel and write into the buffer
            int bytesRead= socketChannel.read(buffer);
            buffer.flip();

            byte[] a =new byte[bytesRead];
            buffer.get(a);
            System.out.println(new String(a));
            buffer.rewind();
            socketChannel.write(buffer);
            socketChannel.close();
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
}
