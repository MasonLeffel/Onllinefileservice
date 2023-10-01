import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

            switch (command) {
                case 'E' -> {
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
                    }else{
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                }
                case 'L' -> {
                    byte[] a = new byte[request.remaining()];
                    request.get(a);
                    File file = new File("ServerFiles/");
                    String[] fileList = file.list();
                    if (fileList != null) {
                        String fileString =String.join("\n", fileList);
                        serverChannel.write(ByteBuffer.wrap(fileString.getBytes(Arrays.toString(fileList))));

                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    }else{
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();

                }
                case 'D'->{
                    byte[] a = new byte[request.remaining()];
                    request.get(a);
                    String fileName = new String(a);
                    File file = new File("ServerFiles/" +fileName);
                    if (file.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            serverChannel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                            fileInputStream.close();
                        }
                    }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    }else{
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                }
                case 'R'->{
                    byte[] a = new byte[request.remaining()];
                    request.get(a);
                    String fileName = new String(a);
                    File file = new File("ServerFiles/" +fileName);
                    if (file.exists()) {
                        File newFile = new File("ServerFiles/");
                        if (file.renameTo(newFile)) {
                            System.out.println("Renamed file from " + fileName + " to " + newFile);
                            byte[] response = "S".getBytes();
                            serverChannel.write(ByteBuffer.wrap(response)); // Send 'S' for success
                        } else {
                            System.err.println("Failed to rename the file.");
                            byte[] response = "F".getBytes();
                            serverChannel.write(ByteBuffer.wrap(response));
                        }

                    }else{
                        System.err.println("File not found.");
                        byte[] response = "NF".getBytes();
                        serverChannel.write(ByteBuffer.wrap(response));

                    serverChannel.close();
                }
                }
            }
        }
    }
}