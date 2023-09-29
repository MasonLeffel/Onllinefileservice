import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception {
        int port = 3000;
        ServerSocketChannel welcomeChannel = ServerSocketChannel.open();

        //server should bind to this port
        welcomeChannel.socket().bind(new InetSocketAddress(port));
        while (true) {
            SocketChannel serverChannel = welcomeChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(2500);
            int numBytes = 0;
            do {
                numBytes = serverChannel.read(request);
            } while (numBytes >= 0);

            request.flip();
            char command = (char) request.get();

            System.out.println("Receive command: " + command);

            boolean success = false;

            switch (command) {
                case 'D': {
                    byte[] a = new byte[request.remaining()];
                    request.get(a);
                    String fileName = new String(a);
                    System.out.println("File to delete: " + fileName);
                    File file = new File("ServerFiles/" + fileName);

                    if (file.exists()) {
                        success = file.delete();
                    }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                    break;
                }

                case 'L': {

                }
            }
        }
    }
}