import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
   private  final static int Status_Code_length =1;
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.print("Syntax: TCPEFileSystem <ServerIP> <ServerPort");
            return;
        }
        String message;
        int serverPort = Integer.parseInt(args[1]);
        do {
            Scanner scanner = new Scanner(System.in);
            message = scanner.nextLine().toUpperCase();
            System.out.print("Enter 'upload,''download,'rename,' or 'delete' : ");// put file defults here
            switch (message) {
                case "delete":
                    System.out.println("Please enter file name");
                    String filename= scanner.nextLine();
                    ByteBuffer buffer = ByteBuffer.wrap((message+filename).getBytes());
                    SocketChannel channel = SocketChannel.open();

                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(buffer);
                    channel.shutdownOutput();
                    ByteBuffer code = ByteBuffer.allocate(Status_Code_length);
                    channel.read(code);
                    code.flip();
                    byte[] a =new byte[Status_Code_length];
                    code.get(a);
                    System.out.println(new String(a));

                    break;
                case "u":

                    break;
                case "down":
                    break;
                case "r":
                    break;
                case "Q":

                    break;

                default:
                    System.out.println("Invalid command!");


            }
} while (message.equals("q"));{
        }
    }
}
//while (message.equals("Q")) {
//            // read  the buffer content and write that to TCP channel
//
//
//
//            if (bytesRead > 0) {
//                serverReply.flip();
//                byte[] serverMessage = new byte[bytesRead];
//                serverReply.get(serverMessage);
//                System.out.println(new String(serverMessage));
//            } else {
//                System.out.print("Error: no respone from the server.");
//            }
//            channel.close();
//            channel.close();


