import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws Exception{
        if(args.length !=2){
            System.out.print("Syntax: TCPEchoClient <ServerIP> <ServerPort");
            return;
        }
        while (true) {
            int serverPort = Integer.parseInt(args[1]);

            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();
            System.out.print("Enter 'upload,''download,'rename,' or 'delete' : ");// put file defults here

            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(args[0], serverPort));

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            // read  the buffer content and write that to TCP channel

            channel.write(buffer);

            ByteBuffer serverReply = ByteBuffer.allocate(1024);
            int bytesRead = channel.read(serverReply);
            if (bytesRead > 0) {
                serverReply.flip();
                byte[] serverMessage = new byte[bytesRead];
                serverReply.get(serverMessage);
                System.out.println(new String(serverMessage));
            } else {
                System.out.printf("Error: no respone from the server.");
            }
            channel.close();
        }
    }
}


