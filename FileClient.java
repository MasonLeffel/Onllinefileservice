
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileClient {
    final static int STATUS_CODE_LENGTH = 1;

    public static void main(String[] args) throws Exception {
        ExecutorService es= Executors.newFixedThreadPool(4);
        if (args.length != 2) {
            System.out.print("Syntax: TCPEFileSystem <ServerIP> <ServerPort");
            return;
        }
        String command;

        int serverPort = Integer.parseInt(args[1]);
        do {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter: \n" +
                    " 'L' for List\n" +
                    " 'R' for Rename\n" +
                    " 'E' for Delete\n" +
                    " 'U' for Upload\n" +
                    " 'D' for Download\n" +
                    " 'Q' to Quit\n"
            );
            command = scanner.nextLine().toUpperCase();

            ByteBuffer code = ByteBuffer.allocate(STATUS_CODE_LENGTH);
            byte[] a = new byte[STATUS_CODE_LENGTH];

            String fileName = "";
            ByteBuffer request;
            String serverResponse;

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
                        System.out.println("File deleted");
                    } else if (serverResponse.equals("F")) {
                        System.out.println("File not found");
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
                        System.out.println("Files on server: ");
                        System.out.println(fileList);
                    } else {
                        System.out.println("No files available on the server.");
                    }
                    channel.close();
                    break;
                }
                case "U":
                    String finalFileName = fileName;
                    es.submit(() -> uploadFile(channel, finalFileName));
                    break;
                case "D":
                    String finalFileName1 = fileName;
                    es.submit(() -> downloadFile(channel, finalFileName1));
                    break;

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
                    serverResponse = new String(a);
                    if (serverResponse.equals("S")) {
                        System.out.println("File renamed to ->  " + newFileName);
                    } else if (serverResponse.equals("F")) {
                        System.out.println("Rename error\n");
                    }
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
                    System.out.println("Invalid command!\n");
                }
            }
        } while (!command.equals("Q"));
        es.shutdown();
    }
    private static void uploadFile(SocketChannel channel, String fileName) {
        String command = "";
        Scanner scanner =new Scanner(System.in);
        ByteBuffer request;
        String serverResponse;
        ByteBuffer code = ByteBuffer.allocate(STATUS_CODE_LENGTH);
        byte[] a = new byte[STATUS_CODE_LENGTH];
        System.out.println("Please enter file name to upload");
        fileName = scanner.nextLine();
        File file = new File("ClientFiles/" + fileName);

        if (!file.exists()) {
            System.out.println("File not found");

        }

        try {
            request = ByteBuffer.allocate(2000);
            request.put(command.getBytes());
            request.putInt(fileName.length());
            request.put(fileName.getBytes());

            FileInputStream fs = new FileInputStream(file);
            FileChannel fc = fs.getChannel();

            do {
                request.flip();
                channel.write(request);
                request.clear();
            } while (fc.read(request) >= 0);

            channel.shutdownOutput();

            channel.read(code);
            code.flip();
            code.get(a);
            serverResponse = new String(a);

            if (serverResponse.equals("S")) {
                System.out.println("File successfully uploaded!");
            } else if (serverResponse.equals("F")) {
                System.out.println("Upload failed\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during file upload: " + e.getMessage());
        }
    }

    private static void downloadFile(SocketChannel channel, String fileName) {
        Scanner scanner= new Scanner(System.in);
        String command = "";
        ByteBuffer request;
        String serverResponse;
        ByteBuffer code = ByteBuffer.allocate(STATUS_CODE_LENGTH);
        byte[] a = new byte[STATUS_CODE_LENGTH];
        try {
            System.out.println("Please enter file name");
            fileName = scanner.nextLine();
            request = ByteBuffer.wrap((command + fileName).getBytes());
            channel.write(request);
            channel.shutdownOutput();
            code.clear();

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(channel.socket().getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("F")) {
                        System.out.println("File not found");
                        break;
                    }
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream("ClientFiles/DownloadedFiles/" + fileName, true);
                        fileOutputStream.write(line.getBytes());
                        System.out.println("File downloaded successfully.");
                        fileOutputStream.close();
                    } catch (IOException e) {
                        System.err.println("Error while downloading the file: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Error while reading from the server: " + e.getMessage());
            }
            channel.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while downloading the file: " + e.getMessage());
        }
    }
}
