import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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

            byte[] a = new byte[request.remaining()];
            request.get(a);


            switch (command) {
                //Delete
                case 'E': {
                    String fileName = new String(a);
                    System.out.println("File to delete: " + fileName);
                    File file = new File("ServerFiles/" + fileName);


                    if (file.exists()) {
                        success = file.delete();
                    }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }

                    break;
                }

                //List
                case 'L': {
                    File directory = new File("ServerFiles/");
                    File[] fileList = directory.listFiles();
                    StringBuilder response = new StringBuilder();

                    if (fileList != null) {
                        success = true;
                        for (File file : fileList) {
                            response.append(file.getName()).append("\n");
                        }
                    }

                    System.out.println(response);

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap(("S" + response).getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    break;
                }

                case 'D': {
                    String fileName = new String(a);
                    File file = new File("ServerFiles/" + fileName);
                    if (file.exists()) {
                        success = true;
                    }


                   try{
                       FileInputStream fileInputStream = new FileInputStream(file);
                       byte[] fileBuffer = new byte[8000];
                       int bytesRead;

                       while ((bytesRead = fileInputStream.read(fileBuffer)) != -1) {
                           serverChannel.write(ByteBuffer.wrap(fileBuffer, 0, bytesRead));
                           fileInputStream.close();
                       }
                   }catch (IOException e){
                       success = false;
                   }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                    break;
                }

                //Rename
                case 'R': {
                    String message = new String(a);
                    String[] fileNameRequest = message.split("\\$");
                    String fileName = fileNameRequest[0];
                    String newName = fileNameRequest[1];

                    System.out.println("Renaming: " + fileName);
                    System.out.println("To: " + newName);
                    File file = new File("ServerFiles/" + fileName);
                    File newFile = new File("ServerFiles/" + newName);
                    if (file.exists()) {
                        success = file.renameTo(newFile);
                        System.out.println(success);
                    }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    break;
                }

                case 'Q':{
                    break;
                }
            }
            serverChannel.close();
        }
    }
}
