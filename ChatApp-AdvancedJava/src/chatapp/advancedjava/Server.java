package chatapp.advancedjava;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import javax.swing.JOptionPane;

//MUHAMMED ALPEREN DOĞRUYOL 218CS2085
//ONUR BIÇAK 218CS2103
public class Server {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int clientCount = 10;
    private static final Client[] client = new Client[clientCount];

    public static void main(String args[]) {

        int portNumber = 8000;
        System.out.println("Server Started At : " + new Date() + " -- " + portNumber);

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int clientIndex = 0;
                for (clientIndex = 0; clientIndex < clientCount; clientIndex++) {
                    if (client[clientIndex] == null) {
                        (client[clientIndex] = new Client(clientSocket, client)).start();
                        break;
                    }
                }
                if (clientIndex == clientCount) {
                    try (PrintStream outputStream = new PrintStream(clientSocket.getOutputStream())) {
                        outputStream.println("Cannot Connect --> Server is Full !");
                        JOptionPane.showMessageDialog(null, "Cannot Connect To The Server : Server is Full !");
                    }
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}
