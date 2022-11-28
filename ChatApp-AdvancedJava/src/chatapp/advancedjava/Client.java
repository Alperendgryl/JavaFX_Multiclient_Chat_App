package chatapp.advancedjava;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//MUHAMMED ALPEREN DOĞRUYOL 218CS2085
//ONUR BIÇAK 218CS2103
public class Client extends Thread {

    static String IPAddress;
    static String PortAddress;
    static String UserName;

    DataInputStream inputStream;
    PrintStream outputStream = null;
    Socket clientSocket = null;
    final Client[] clients;

    public Client(Socket clientSocket, Client[] clients) {
        this.clientSocket = clientSocket;
        this.clients = clients;
    }

    public static void main(String[] args) {

        while (true) {
            String tempIPAddress;
            tempIPAddress = JOptionPane.showInputDialog(null, "Enter The IP Address", "IP Address", JOptionPane.INFORMATION_MESSAGE); //parent, question, title, message

            if (tempIPAddress.isEmpty()) {
                JOptionPane.showMessageDialog(null, "IP Address Cannot Be Empty !", "Empty IP Address", JOptionPane.ERROR_MESSAGE); //parent, question, title, message
            } else if (!tempIPAddress.equals("localhost")) {
                JOptionPane.showMessageDialog(null, "IP Address Must Be 'localhost' !", "Wrong IP Address", JOptionPane.ERROR_MESSAGE); //parent, question, title, message
            } else {
                IPAddress = tempIPAddress;
                break;
            }
        }

        while (true) {

            String tempPortAddress;
            tempPortAddress = JOptionPane.showInputDialog(null, "Enter The Port Address", "Port Address", JOptionPane.INFORMATION_MESSAGE); //parent, question, title, message

            if (tempPortAddress.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Port Address Cannot Be Empty !", "Empty Port Address", JOptionPane.ERROR_MESSAGE); //parent, question, title, message
            } else if (!tempPortAddress.equals("8000")) {
                JOptionPane.showMessageDialog(null, "Port Address Must Be '8000' !", "Wrong Port Address", JOptionPane.ERROR_MESSAGE); //parent, question, title, message
            } else {
                PortAddress = tempPortAddress;
                break;
            }
        }

        while (true) {
            String tempUserName;
            tempUserName = JOptionPane.showInputDialog(null, "Enter Your Name And Surname", "Personal Information", JOptionPane.INFORMATION_MESSAGE); //parent, question, title, message

            if (tempUserName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name And Surname Cannot Be Empty !", "Empty Personal Information", JOptionPane.ERROR_MESSAGE); //parent, question, title, message
            } else {
                UserName = tempUserName;
                break;
            }
        }

        ChatController chatController = new ChatController();

        JFrame frame = new ChatUI(chatController);
        frame.setTitle("Chat Page" + " / " + IPAddress + " / " + PortAddress);
        frame.pack(); //sizes the frame so that all its contents are at or above their preferred sizes.
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null); //used to center the gui on the screen.
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            chatController.InitSocket(IPAddress, Integer.parseInt(PortAddress));
        } catch (IOException ex) {
            System.out.println("Cannot Connect " + IPAddress + " / " + PortAddress);
            System.exit(0);
        }
    }

    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new PrintStream(clientSocket.getOutputStream());

            String nickName;
            while (true) {
                outputStream.println("How Should Others Call You ?");
                nickName = inputStream.readLine().trim();
                if (nickName.equals("")) {
                    return;
                }
                break;
            }

            outputStream.println("Welcome " + nickName + "\nType /exit To Leave From The Chat.\n");

            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null && clients[i] == this) {
                    UserName = "Client : " + nickName;
                    System.out.println(UserName + " Connected!\n" + "Active User Count : " + (i + 1));
                    break;
                }
            }
            for (Client client : clients) { //int i = 0; i < clients.length; i++
                //User entered to the chat. Notify all observers
                if (client != null && client != this) {
                    client.outputStream.println("\n" + nickName + " Has Joined. [" + java.time.LocalTime.now() + "]");
                }
            }

            while (true) {
                String line = inputStream.readLine();
                if (line.startsWith("/exit")) { //In order to exit, type /exit
                    outputStream.println("You Are Leaving From The Chat...");
                    System.exit(0);
                    break;
                }

                for (Client client : clients) {
                    //User sendMessage a message.
                    if (client != null && UserName != null) {
                        client.outputStream.println("[" + nickName.toUpperCase() + "] : " + line);
                    }
                }
            }

            for (Client client : clients) { //int i = 0; i < clients.length; i++
                //User left from chat. Notify all observers (Clients)
                if (client != null && client != this) {
                    if (UserName != null) {
                        client.outputStream.println(nickName + " Has Left. [" + java.time.LocalTime.now() + "]");
                    }
                }
            }

            for (int i = 0; i < clients.length; i++) {
                if (clients[i] == this) {
                    clients[i] = null;
                }
            }
            closeStreams();
        } catch (IOException e) {
        }
    }

    private void closeStreams() throws IOException {
        inputStream.close();
        outputStream.close();
        clientSocket.close();
    }

    static class ChatUI extends JFrame implements Observer { //it is an interface.

        /*
        TextField used the text of the field as the command string for the ActionEvent . 
        JTextField will use the command string set with the setActionCommand method if not null,
        otherwise it will use the text of the field as a compatibility with java
         */
        private JTextArea textArea;
        private JTextField textField;
        private JTextArea onlineUsers;
        private JLabel topLabel;
        private final ChatController chatController;

        public ChatUI(ChatController chatController) {
            this.chatController = chatController;
            chatController.addObserver(this); //adds the Observer to the list of observers
            UI();
        }

        private void UI() {
            Box horizontalBox = Box.createHorizontalBox();
            add(horizontalBox, BorderLayout.SOUTH);

            textField = new JTextField();
            horizontalBox.add(textField, BorderLayout.SOUTH);

            topLabel = new JLabel("MULTICLIENT CHAT APPLICATION", SwingConstants.CENTER);
            topLabel.setSize(5, 40);
            add(topLabel, BorderLayout.NORTH);

            onlineUsers = new JTextArea(30, 10); //row, columns
            onlineUsers.setEditable(false);
            onlineUsers.setLineWrap(true);
            onlineUsers.setText("   ONLINE USERS\n");
            add(new JScrollPane(onlineUsers), BorderLayout.EAST);

            textArea = new JTextArea(30, 40); //row, columns
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            ActionListener listener = (ActionEvent e) -> { //implement an action listener to define what should be done when an user performs certain operation
                String str = textField.getText();
                if (str != null && str.trim().length() > 0) {
                    chatController.sendMessage(str);
                }
                textField.selectAll(); //Selects all string from textField
                textField.requestFocus(); // to get the focus on the particular component and also on the window that contains the component.
                textField.setText(""); //reset the textField.
            };
            textField.addActionListener(listener);

            this.addWindowListener(new WindowAdapter() { //public interface WindowListener extends EventListener. The listener interface for receiving window events
                @Override
                public void windowClosing(WindowEvent e) {
                    chatController.closeSocket(); //close the socket
                }
            });
        }

        @Override
        public void update(Observable observable, Object obj) { //Updates the UI depending on the Object argument, receives through the notifyObservers the object who has changedm in the Observable.
            final Object object = obj;
            SwingUtilities.invokeLater(() -> { // invokeLater simply schedules the task and returns;
                textArea.append(object.toString()); //method is used to append the string representation of some argument to the sequence.
                textArea.append("\n");
            });
        }
    }

    static class ChatController extends Observable {

        //Observable is a class and can be monitored by other classes 
        //When an Observable object is updated, it invokes the update() method for each of its Observers to notify that it is changed.
        private Socket socket;
        private OutputStream outputStream;

        @Override
        public void notifyObservers(Object arg) { // Notify observers when something changes in observable class.
            super.setChanged();
            super.notifyObservers(arg);
        }

        public void InitSocket(String server, int port) throws IOException {
            socket = new Socket(server, port);
            outputStream = socket.getOutputStream();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String msg;
                        while ((msg = bufferedReader.readLine()) != null) {
                            notifyObservers(msg);
                        }
                    } catch (IOException ex) {
                        notifyObservers(ex);
                    }
                }
            };
            thread.start();
        }

        public void sendMessage(String text) {
            try {
                outputStream.write((text + "\r\n").getBytes()); // \r moves the cursor to the beginning of the line.
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }
}
