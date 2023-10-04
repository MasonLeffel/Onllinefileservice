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

            byte[] a = new byte[request.remaining()];
            request.get(a);

            StringBuilder response = new StringBuilder();

            do {
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
                        serverChannel.close();
                    }

                    //List
                    case 'L': {
                        File file = new File("ServerFiles/");
                        String[] fileList = file.list();
                        if (fileList != null) {
                            String fileString = String.join("\n", fileList);
                            System.out.println(fileString);
                            serverChannel.write(ByteBuffer.wrap(fileString.getBytes(Arrays.toString(fileList))));

                            ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                            serverChannel.write(code);
                        } else {
                            ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                            serverChannel.write(code);
                        }
                        serverChannel.close();

                    }

                    case 'D': {
                        String fileName = new String(a);
                        File file = new File("ServerFiles/" + fileName);
                        if (file.exists()) {
                            success = true;
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
                        } else {
                            ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                            serverChannel.write(code);
                        }
                        serverChannel.close();
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
                        }
                    }

                    if (success) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                    case 'U':{
                        request.get(a);
                        String fileName = new String(a);
                        byte[] writer =fileName.getBytes();
                        File file = new File("ServerFiles/");
                        if (file.canWrite()){
                            serverChannel.write(ByteBuffer.wrap(writer));
                            success=true;
                        }
                        if (success){
                            serverChannel.write(ByteBuffer.wrap("S".getBytes()));
                        }
                        else {
                            serverChannel.write(ByteBuffer.wrap("F".getBytes()));
                        }
                }
            }
            break;
        }while (command != 'Q');
    }
}
}
