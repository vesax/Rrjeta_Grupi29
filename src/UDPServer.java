import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPServer {
    private static final int SERVER_PORT = 9876;

    public static void main(String[] args) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);
            System.out.println("Server is ready to listen...");

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                System.out.println("Client [" + clientAddress.getHostAddress() + ":" + clientPort + "] says: " + clientMessage);


                Thread requestHandler = new Thread(() -> {

                    String response = processRequest(clientMessage);


                    byte[] sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);


                    try {
                        serverSocket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                });

                requestHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String request) {
        String[] parts = request.split(" ", 3);
        String command = parts[0];
        switch (command) {
            case "READ":
                return handleReadRequest(parts[1]);
            case "LIST":
                return listFiles();
            case "EXECUTE":
                return executeCommand(parts[1]);
            case "WRITE":
                return handleWriteRequest(parts[1], parts[2]);
            case "UPLOAD":
                return handleUploadRequest(parts[1], parts[2]);
            case "BROWSE":
                return browseDirectory(parts[1]);
            default:
                return "Unknown request";
        }
    }

    private static String handleReadRequest(String fileName) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(fileName));
            return new String(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading the file";
        }
    }
    private static String listFiles() {
        StringBuilder fileList = new StringBuilder();
        try {
            Files.list(Paths.get(".")).forEach(path -> fileList.append(path.getFileName()).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error listing files";
        }
        return fileList.toString();
    }
    private static String executeCommand(String command) {
        return "Executing command: " + command;
    }

    private static String handleWriteRequest(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
            return "File written successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error writing to the file";
        }
    }
    private static String handleUploadRequest(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
            return "File uploaded successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error uploading the file";
        }
    }
    private static String browseDirectory(String directoryPath) {
        try {
            StringBuilder content = new StringBuilder();

            Files.list(Paths.get(directoryPath)).forEach(path -> {
                if (Files.isDirectory(path)) {
                    content.append("[Directory] ").append(path.getFileName()).append("\n");
                } else {
                    content.append("[File] ").append(path.getFileName()).append("\n");
                }
            });

            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error browsing directory";
        }
    }
}


