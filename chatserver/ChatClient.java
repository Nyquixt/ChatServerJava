/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

/**
 *
 * @author kiennguyen
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class ChatClient extends JFrame implements ActionListener{

    private Socket socket;
    private ServerThread serverThread;
    
    // GUI instances
    private JTextArea displayMessages;
    private JTextField inputMessages;
    private JButton sendBtn;
    private JButton connectBtn;
    private JLabel nickNameLabel;
    private JTextField inputNickname;
    private JLabel hostLabel;
    private JTextField hostText;
    private JLabel portLabel;
    private JTextField portText;
    private JScrollPane scroll;
    private JScrollBar vertical;
        
    
    // communicating
    private BufferedReader br;
    private PrintWriter pw;
    
    // GUI constructor
    private ChatClient(){
        //GUI
        setTitle("Chat Client");
        setSize(600, 400);
        setLocation(100, 100);
        setResizable(false);
        getContentPane().setLayout(null);
        
        displayMessages = new JTextArea();
        scroll = new JScrollPane();
        scroll.setSize(580, 300);
        scroll.setLocation(10, 10);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vertical = scroll.getVerticalScrollBar();
        displayMessages.setEditable(false);
        scroll.getViewport().add(displayMessages);
        getContentPane().add(scroll);
        displayMessages.setText("Welcome to the Ultimate Chat Server!!!\n" + "Please enter <host>, <port>, and <nickname> to connect to the server!!!");
        
        inputMessages = new JTextField();
        inputMessages.setSize(380, 35);
        inputMessages.setLocation(10, 310);
        getContentPane().add(inputMessages);
        inputMessages.addActionListener(this);
        inputMessages.setEnabled(false);
        
        sendBtn = new JButton();
        sendBtn.setText("Send");
        sendBtn.setSize(100, 35);
        sendBtn.setLocation(390, 310);
        getContentPane().add(sendBtn);
        sendBtn.addActionListener(this);
        sendBtn.setEnabled(false);
        
        connectBtn = new JButton();
        connectBtn.setText("Connect");
        connectBtn.setSize(100, 35);
        connectBtn.setLocation(495, 310);
        connectBtn.setBackground(Color.green);
        connectBtn.setOpaque(true);
        getContentPane().add(connectBtn);
        connectBtn.addActionListener(this);
        
        inputNickname = new JTextField();
        inputNickname.setSize(90, 30);
        inputNickname.setLocation(90, 345);
        getContentPane().add(inputNickname);
        
        nickNameLabel = new JLabel();
        nickNameLabel.setText("Nickname: ");
        nickNameLabel.setSize(130, 20);
        nickNameLabel.setLocation(10, 350);
        getContentPane().add(nickNameLabel);
        
        hostLabel = new JLabel();
        hostLabel.setText("Host: ");
        hostLabel.setSize(50, 20);
        hostLabel.setLocation(200, 350);
        getContentPane().add(hostLabel);
        
        hostText = new JTextField();
        hostText.setSize(150, 30);
        hostText.setLocation(250, 345);
        getContentPane().add(hostText);
        
        portLabel = new JLabel();
        portLabel.setText("Port: ");
        portLabel.setSize(50, 20);
        portLabel.setLocation(420, 350);
        getContentPane().add(portLabel);
        
        portText = new JTextField();
        portText.setSize(100, 30);
        portText.setLocation(470, 345);
        portText.setText("5555");
        portText.setEnabled(false);
        getContentPane().add(portText);
    }

    public void actionPerformed(ActionEvent e) {
        try{
            if(e.getActionCommand().equals("Connect") && !inputNickname.equals("") && !hostText.equals("")){
                this.setTitle(inputNickname.getText());
                
                // set text area text to null
                displayMessages.setText("");
                
                String host = hostText.getText();
                int port = Integer.parseInt(portText.getText());
                
                // create socket
                socket = new Socket(host, port);
                
                // create BufferedReader and PrintWriter to write and read to and from server
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pw = new PrintWriter(socket.getOutputStream(), false);
                
                pw.println(inputNickname.getText()); // write nickname to server
                pw.flush();
                
                
                // start server thread
                serverThread = new ServerThread(this.br, this.displayMessages);
                serverThread.start();
                
                // notify all users that this user has entered the chat room
                pw.println("User " + inputNickname.getText() + " has entered the chat room."); 
                pw.flush();
                
                // adjust the GUI
                sendBtn.setEnabled(true);
                sendBtn.setBackground(Color.blue);
                sendBtn.setOpaque(true);
                inputMessages.setEnabled(true);
                hostText.setEnabled(false);
                portText.setEnabled(false);
                inputNickname.setEnabled(false);
                connectBtn.setText("Disconnect");
                connectBtn.setBackground(Color.red);
                connectBtn.setOpaque(true);
            }
            else if(e.getActionCommand().equals("Disconnect")){
                // notify all users that this user has left the chat room
                pw.println("User " + inputNickname.getText() + " has left the chat room.");
                pw.flush();
                
                // close, interrupt everything & adjust the GUI
                serverThread.interrupt();
                socket.close();
                br.close();
                pw.close();
                sendBtn.setEnabled(false);
                sendBtn.setOpaque(false);
                inputMessages.setEnabled(false);
                hostText.setEnabled(true);
                portText.setEnabled(true);
                inputNickname.setEnabled(true);
                connectBtn.setText("Connect");
                displayMessages.setText("Goodbye " + inputNickname.getText() + ". See you later!");
                inputMessages.setText("");
                connectBtn.setBackground(Color.green);
                connectBtn.setOpaque(true);
            }
            else if(e.getActionCommand().equals("Send") || sendBtn.isEnabled() && e.getSource() == inputMessages){
                vertical.setValue(vertical.getMaximum()); // scrollbar always scrolls to bottom
                pw.println(inputMessages.getText()); // write messages to server
                pw.flush();
                inputMessages.setText("");
                System.out.println("Sending messages");
            }
        } catch(UnknownHostException ex){
            ex.printStackTrace();
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        ChatClient window = new ChatClient();
        window.setVisible(true);
    }
}

class ServerThread extends Thread {
    
    private BufferedReader br;
    private JTextArea displayMessages;
    
    public ServerThread(BufferedReader br, JTextArea displayMessages){
        this.br = br;
        this.displayMessages = displayMessages;
    }
    
    @Override
    public void run() {
        try {
            String msgout;
            while((msgout = br.readLine()) != null){ // read and output messages received from server into the text area
                displayMessages.append(msgout + '\n');
            }
        } catch (IOException e) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Error reading from server");
        }
    }
    
}
