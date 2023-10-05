import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    final static int STATUS_CODE_LENGTH = 1;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.print("Syntax: TCPEFileSystem <ServerIP> <ServerPort");
            return;
        }
        String command;

        int serverPort = Integer.parseInt(args[1]);
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter: \n" +
                    " 'L' for list\n" +
                    " 'R' for rename\n" +
                    " 'E' for Delete\n" +
                    " 'U' for Upload\n" +
                    " 'D' for Download\n" +
                    " 'Q' to quit\n"
            );
            command = scanner.nextLine().toUpperCase();

            ByteBuffer code = ByteBuffer.allocate(STATUS_CODE_LENGTH);
            byte[] a = new byte[STATUS_CODE_LENGTH];

            String fileName = null;
            ByteBuffer request = null;
            String serverResponse = null;

            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(args[0], serverPort));

            switch (command) {
                case "E": {
                    System.out.println("Please enter file name");
                    fileName = scanner.nextLine();
                    request = ByteBuffer.wrap((command + fileName).getBytes());
                    channel.write(request);
                    channel.shutdownOutput();
                    channel.read(code);
                    code.flip();
                    code.get(a);

                    serverResponse = new String(a);
                    if (serverResponse.equals("S")) {
                        System.out.println("File deleted\n");
                    } else if (serverResponse.equals("F")) {
                        System.out.println("File not found\n");
                    }
                    channel.close();
                    break;
                }

                case "L": {
                    request = ByteBuffer.wrap((command).getBytes());
                    channel.write(request);
                    channel.shutdownOutput();
                    channel.read(code);
                    code.flip();
                    code.get(a);
                    serverResponse = new String(a);

                    if (serverResponse.equals("S")) {
                        ByteBuffer message = ByteBuffer.allocate(1024);
                        channel.read(message);
                        message.flip();

                        String fileList = new String(message.array(), 0, message.limit());
                        System.out.println("List of files available on the server:");
                        System.out.println(fileList);
                    } else {
                        System.out.println("No files available on the server.");
                    }
                    channel.close();
                    break;
                }

                case "U": {

                }

                case "D": {
                    System.out.println("Please enter file name");
                    fileName = scanner.nextLine();
                    request = ByteBuffer.wrap((command + fileName).getBytes());
                    channel.write(request);
                    channel.shutdownOutput();


                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream("ClientFiles/DownloadedFiles/" + fileName);
                        byte[] fileBuffer = new byte[8000];
                        InputStream inputStream = channel.socket().getInputStream();
                        int bytesRead;

                        while ((bytesRead = inputStream.read(request.array())) != -1) {
                            fileOutputStream.write(request.array(), 0, bytesRead);
                        }

                        System.out.println("File downloaded successfully.");
                    } catch (IOException e) {
                        System.err.println("Error while downloading the file: " + e.getMessage());
                    }


                    channel.close();
                    break;
                }

                case "R": {
                    System.out.println("Please enter current file name");
                    fileName = scanner.nextLine();
                    System.out.println("Please enter new file name");
                    String newFileName = scanner.nextLine();
                    ByteBuffer requestRename = ByteBuffer.wrap((command + fileName + '$' + newFileName).getBytes());
                    channel.write(requestRename);
                    channel.shutdownOutput();
                    channel.read(code);
                    code.flip();
                    code.get(a);
                    System.out.println(new String(a));
                    channel.close();
                    break;
                }

                case "Q": {
                    request = ByteBuffer.wrap((command).getBytes());
                    channel.write(request);
                    channel.shutdownOutput();

                    System.out.println("Bye!");
                    break;
                }

                default: {
                    if (!command.equals("Q")) {
                        System.out.println("Invalid command!\n");
                    }
                }
            }
        } while (!command.equals("Q"));
    }
}

