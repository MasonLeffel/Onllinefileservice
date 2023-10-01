import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
   final static int Status_Code_length =1;
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.print("Syntax: TCPEFileSystem <ServerIP> <ServerPort");
            return;
        }
        String message;

        int serverPort = Integer.parseInt(args[1]);
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.print("""
                    Enter 'U' for Upload\s
                    'D' for Download\s
                    'E' for Delete\s
                     'Q' to quit\s
                     'R for rename: '""");// put file defults here
            message = scanner.nextLine().toUpperCase();
            String filename= scanner.nextLine();
            ByteBuffer code = ByteBuffer.allocate(Status_Code_length);
            byte[] a =new byte[Status_Code_length];
            ByteBuffer request = ByteBuffer.wrap((message+filename).getBytes());
            switch (message) {
                case "E" -> {
                    System.out.println("Please enter file name");
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput();
                    channel.read(code);
                    code.flip();
                    code.get(a);
                    System.out.println(new String(a));
                    channel.close();
                }
                case "U" -> {

                }
                case "D" -> {
                    System.out.println("Please enter file name");
                    filename = scanner.nextLine();
                    SocketChannel channel2 = SocketChannel.open();
                    channel2.connect(new InetSocketAddress(args[0], serverPort));
                    ByteBuffer requestBuffer = ByteBuffer.wrap(("download " + filename).getBytes());
                    channel2.write(requestBuffer);
                    channel2.shutdownOutput();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
                         InputStream inputStream = channel2.socket().getInputStream()) {
                        int bytesRead;

                        while ((bytesRead = inputStream.read(request.array())) != -1) {
                            fileOutputStream.write(request.array(), 0, bytesRead);
                        }

                        System.out.println("File downloaded successfully.");
                    } catch (IOException e) {
                        System.err.println("Error while downloading the file: " + e.getMessage());
                    }


                    channel2.close();
                }
                case "R" -> {
                    System.out.println("Please enter file name");
                    String newFileName = scanner.nextLine();
                    ByteBuffer requestRename = ByteBuffer.wrap((message + newFileName).getBytes());
                    SocketChannel channel3 = SocketChannel.open();
                    channel3.connect(new InetSocketAddress(args[0], serverPort));
                    channel3.write(requestRename);
                    channel3.shutdownOutput();
                    channel3.read(code);
                    code.flip();
                    code.get(a);
                    System.out.println(new String(a));
                    channel3.close();
                }
                case "Q" -> {
                    break;
                }
                default -> System.out.println("Invalid command!");
            }
} while (message.equals("Q"));{
        }
    }
}

