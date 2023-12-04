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

                    public static void main(String[] args) {
                if (args.length != 2) {
                    System.out.print("Syntax: TCPEFileSystem <ServerIP> <ServerPort");
                    return;
                }

                ExecutorService executorService = Executors.newFixedThreadPool(5); // Adjust the pool size based on your requirements

                try (Scanner scanner = new Scanner(System.in)) {
                    String command;
                    do {
                        System.out.print("Enter: \n" +
                                " 'L' for List\n" +
                                " 'R' for Rename\n" +
                                " 'E' for Delete\n" +
                                " 'U' for Upload\n" +
                                " 'D' for Download\n" +
                                " 'Q' to Quit\n"
                        );
                        command = scanner.nextLine().toUpperCase();

                        SocketChannel channel = null;

                        try {
                            channel = SocketChannel.open();
                            channel.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

                            switch (command) {
                                case "U":
                                    executorService.execute(new UploadHandler(channel, command, scanner));
                                    break;
                                case "D":
                                    executorService.execute(new DownloadHandler(channel, command, scanner));
                                    break;
                                default:
                                    executorService.execute(new MultiThreadedFileClient(channel, command, scanner));
                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (channel != null && channel.isOpen()) {
                                try {
                                    channel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } while (!command.equals("Q"));
                } finally {
                    executorService.shutdown();
                }
            }
        }
        class UploadHandler implements Runnable {
        private final SocketChannel channel;
        private final String command;
        private final Scanner scanner;

        UploadHandler(SocketChannel channel, String command, Scanner scanner) {
            this.channel = channel;
            this.command = command;
            this.scanner = scanner;
        }

        @Override
        public void run() {
            ByteBuffer code = ByteBuffer.allocate(FileClient.STATUS_CODE_LENGTH);
            byte[] a = new byte[FileClient.STATUS_CODE_LENGTH];
            String fileName;
            ByteBuffer request;
            String serverResponse;
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
            System.out.println();
        }
        }


    class DownloadHandler implements Runnable {
        private final SocketChannel channel;
        private final String command;
        private final Scanner scanner;

        DownloadHandler(SocketChannel channel, String command, Scanner scanner) {
            this.channel = channel;
            this.command = command;
            this.scanner = scanner;
        }

        @Override
        public void run() {
            ByteBuffer code = ByteBuffer.allocate(FileClient.STATUS_CODE_LENGTH);
            byte[] a = new byte[FileClient.STATUS_CODE_LENGTH];
            String fileName;
            ByteBuffer request;
            String serverResponse;
            System.out.println("Please enter file name");
            fileName = scanner.nextLine();
            request = ByteBuffer.wrap((command + fileName).getBytes());
            try {
                channel.write(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                channel.shutdownOutput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
}

class MultiThreadedFileClient implements Runnable {
    private final SocketChannel channel;
    private final String command;
    private final Scanner scanner;
    MultiThreadedFileClient(SocketChannel channel, String command, Scanner scanner) {
        this.channel = channel;
        this.command = command;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        String fileName;
        ByteBuffer request;
        String serverResponse;
        ByteBuffer code = ByteBuffer.allocate(FileClient.STATUS_CODE_LENGTH);
        byte[] a = new byte[FileClient.STATUS_CODE_LENGTH];
        switch (command) {
            case "E": {
                System.out.println("Please enter file name");
                fileName = scanner.nextLine();
                request = ByteBuffer.wrap((command + fileName).getBytes());
                try {
                    channel.write(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.shutdownOutput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.read(code);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                code.flip();
                code.get(a);

                serverResponse = new String(a);
                if (serverResponse.equals("S")) {
                    System.out.println("File deleted");
                } else if (serverResponse.equals("F")) {
                    System.out.println("File not found");
                }
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }

            case "L": {
                request = ByteBuffer.wrap((command).getBytes());
                try {
                    channel.write(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.shutdownOutput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.read(code);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                code.flip();
                code.get(a);
                serverResponse = new String(a);

                if (serverResponse.equals("S")) {
                    ByteBuffer message = ByteBuffer.allocate(1024);
                    try {
                        channel.read(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    message.flip();

                    String fileList = new String(message.array(), 0, message.limit());
                    System.out.println("Files on server: ");
                    System.out.println(fileList);
                } else {
                    System.out.println("No files available on the server.");
                }
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "R": {
                System.out.println("Please enter current file name");
                fileName = scanner.nextLine();
                System.out.println("Please enter new file name");
                String newFileName = scanner.nextLine();
                ByteBuffer requestRename = ByteBuffer.wrap((command + fileName + '$' + newFileName).getBytes());
                try {
                    channel.write(requestRename);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.shutdownOutput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.read(code);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                code.flip();
                code.get(a);
                serverResponse = new String(a);
                if (serverResponse.equals("S")) {
                    System.out.println("File renamed to ->  " + newFileName);
                } else if (serverResponse.equals("F")) {
                    System.out.println("Rename error\n");
                }
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }

            case "Q": {
                request = ByteBuffer.wrap((command).getBytes());
                try {
                    channel.write(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    channel.shutdownOutput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Bye!");
                break;
            }

            default: {
                System.out.println("Invalid command!\n");
            }
        }
    }
}


