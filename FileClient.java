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
            message = scanner.nextLine();
            System.out.print("Enter 'upload,''download,'rename,' or 'delete' or 'Q' to quit: ");// put file defults here
            String filename= scanner.nextLine();
            ByteBuffer code = ByteBuffer.allocate(Status_Code_length);
            byte[] a =new byte[Status_Code_length];
            ByteBuffer buffer = ByteBuffer.wrap((message+filename).getBytes());
            switch (message) {
                case "delete":
                    System.out.println("Please enter file name");
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(buffer);
                    channel.shutdownOutput();
                    channel.read(code);
                    code.flip();
                    code.get(a);
                    System.out.println(new String(a));
                    channel.close();
                    break;
                case "upload":

                    break;
                case "download":

                    break;
                case "rename":
                    System.out.println("Please enter file name");
                    String newFileName;
                    SocketChannel channel2 =SocketChannel.open();
                    channel2.connect(new InetSocketAddress(args[0], serverPort));
                    channel2.write(buffer);
                    channel2.shutdownOutput();
                    channel2.read(code);
                    code.flip();
                    code.get(a);
                    System.out.println(new String(a));
                    channel2.close();
                    break;
                case "Q":

                    break;

                default:
                    System.out.println("Invalid command!");


            }
} while (message.equals("Q"));{
        }
    }
}

