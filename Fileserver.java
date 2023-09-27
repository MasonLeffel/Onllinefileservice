import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Fileserver {
    public static void main(String[] args) throws Exception {
    int port = 3000;
    ServerSocketChannel welcomeChannel = ServerSocketChannel.open();
    welcomeChannel.socket().bind(new InetSocketAddress(port));
    while (true){
        SocketChannel serverChannel =welcomeChannel.accept();
        ByteBuffer request =ByteBuffer.allocate(2500);
       int numbBytes =0;
       do {


           numbBytes = serverChannel.read(request);
       }while (numbBytes >=0);
        //while(SocketChannel.read(request) >=0);
        char command = (char) request.get();
        System.out.println("recived command:" +command);
        switch (command){
            case 'D':
                byte[] a = new byte[request.remaining()];
                request.get(a);
                String fileName = new String(a);
                File file = new File(fileName);//add to file system
                boolean success =false;
                if(file.exists()) {
                    success = file.delete();
                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("01".getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("00".getBytes());
                        serverChannel.write(code);
                    }
                }
                serverChannel.close();
            case 'L':
                break;
        }

        }

    }
}




    /*try {

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
}*/
