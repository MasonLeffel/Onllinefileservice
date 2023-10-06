import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception {
        int port = 3001;
        ServerSocketChannel welcomeChannel = ServerSocketChannel.open();

        //server should bind to this port
        welcomeChannel.socket().bind(new InetSocketAddress(port));
        while (true) {
            SocketChannel serverChannel = welcomeChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(2500);
            int numBytes = 0;
            do {
                numBytes = serverChannel.read(request);
            } while (request.position() < request.capacity() && numBytes >= 0);

            request.flip();
            char command = (char) request.get();

            System.out.println("Receive command: " + command);
            System.out.println();

            boolean success = false;

            byte[] a = new byte[request.remaining()];
            request.get(a);

            ByteBuffer messageCode;


            switch (command) {
                //Delete
                case 'E' -> {
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

                }


                //List
                case 'L' -> {
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
                }


                //Download
                case 'D' -> {
                    String fileName = new String(a);
                    File file = new File("ServerFiles/" + fileName);
                    if (file.exists()) {
                        messageCode = ByteBuffer.wrap("S".getBytes());
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                ByteBuffer dataBuffer = ByteBuffer.wrap(line.getBytes());
                                serverChannel.write(dataBuffer);
                                dataBuffer.clear();
                            }
                            bufferedReader.close();
                        } catch (IOException e) {
                            ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                            serverChannel.write(code);
                        }
                    } else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                    }
                    serverChannel.close();
                }


                //Upload
                case 'U' -> {
                    String message = new String(a);
                    String[] fileRequest = message.split("\\$");
                    String fileName = fileRequest[0];
                    String detail = fileRequest[1];
                    System.out.println("Detail: " + detail);

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("ServerFiles/" + fileName));
                        writer.write(detail);
                        writer.close();
                        System.out.println("String written to file successfully.");

                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serverChannel.write(code);
                    } catch (IOException e) {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serverChannel.write(code);
                        e.printStackTrace();
                    }

                    serverChannel.close();
                }


                //Rename
                case 'R' -> {
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
                }
                case 'Q' -> {

                }
            }
            serverChannel.close();
        }
    }
}
