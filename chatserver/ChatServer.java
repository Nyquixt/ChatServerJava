package chatserver;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
/*
Course: 20203 - Techniques in Programming
Names of Group Members: Kien Nguyen, Trieu Truong, Minh Nguyen, Hy Dang, Megan Phan
Project: Lab 4&5
Objective: Write a chat server using java.net.Socket and java.net.ServerSocket
How To Use:
    - Enter user's nickname, host address to connect to (port number is always 5555)
    - Hit "Connect"
    - Start chatting with other users in the server
    - Hit "Disconnect" to quit the server
Components added in successfully:
    - Notify other users when a user enters the chat room
    - Notify other users when a user leaves the chat room
Note: The server can handle as many clients until the CPU crashes thanks to the use of ArrayList
*/
public class ChatServer {

    private static final int portNumber = 5555;

    private int serverPort;
    private ArrayList<ClientThread> clientsList; // an arraylist to store connected clients
    private static ServerSocket serverSocket;
    
    
    //constructor
    public ChatServer(int portNumber){
        this.serverPort = portNumber;
    }
    
    public ArrayList<ClientThread> getClients(){
        return clientsList;
    }
    
    private void acceptingClients(){
        clientsList = new ArrayList<ClientThread>();
        serverSocket = null;
        try{
            serverSocket = new ServerSocket(serverPort); //startServer
            while(true){
                try{ // accept clients
                    Socket socket = serverSocket.accept();
                    ClientThread clientThread = new ClientThread(this, socket);
                    Thread thread = new Thread(clientThread);
                    thread.start(); // start the client thread to handle clients
                    System.out.println("Connected: " + serverSocket.getLocalPort());
                    
                    clientsList.add(clientThread); // add client(s) to arraylist
                    for(ClientThread client : clientsList){
                        System.out.println(client);
                    }
                } catch (IOException e){
                    System.out.println("Failed on: " + serverPort);
                }
            }
        } catch(IOException e){
            System.err.println("Cannot listen to port " + serverPort);
            System.exit(1);
        }
    }
    
    public static void main(String[] args){
        ChatServer server = new ChatServer(portNumber);
        server.acceptingClients();
    }
}

class ClientThread implements Runnable {
    private Socket socket;
    private ChatServer server;
    private PrintWriter pw; 
    private BufferedReader br;

    public ClientThread(ChatServer server, Socket socket){
        this.server = server;
        this.socket = socket;
    }

    private PrintWriter getWriter(){
        return pw;
    }

    @Override
    public void run() {
        try{
            // setup
            this.pw = new PrintWriter(socket.getOutputStream(), false);            
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // read nickname
            String name = br.readLine().trim();

            // start communicating
            while(!socket.isClosed()){ // read data to server from clients
               String msgin, msgout = "";
               if((msgin = br.readLine()) != null){
                   msgout = msgin;
                    for(int i = 0; i < server.getClients().size(); i++){
                        server.getClients().get(i).getWriter().println(name + " >> " + msgout); //write data back to the clients
                        server.getClients().get(i).getWriter().flush();
                    }
               }
            }
            
            // clean up. set the thread to null
            for(ClientThread eachClient : server.getClients()){
                if(eachClient == this){
                    eachClient = null;
                }
            }
            br.close();
            pw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
