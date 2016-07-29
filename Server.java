import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	private ArrayList<ClientThread> clients;
	private int port;
	private boolean alive;
	
	public Server(int port) {
		this.port = port;
		clients = new ArrayList<ClientThread>();
	}
	
	public void start() {
		alive = true;
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);

			while(alive) 
			{
				System.out.println("Server is up and waiting for connections.");

				Socket socket = serverSocket.accept();  	
				if(!alive)
					break;

				ClientThread t = new ClientThread(socket);  
				clients.add(t);							
				t.start();
			}
			try {
				serverSocket.close();
				
				for(int i = 0; i < clients.size(); ++i) {
					ClientThread t = clients.get(i);
					
					try {
						t.input.close();
						t.output.close();
						t.socket.close();
					
					}
					catch(Exception ioE) {}
				}
			}
			catch(Exception e) {}
		}
		catch (Exception e) {}
	}	

	private void stop() {
		
		alive = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {}
	}

	private synchronized void broadcast(String mes) {
		for(int i = 0; i < clients.size(); i++) {
			ClientThread t = clients.get(i);
			t.writeMsg(mes);
		}
	}

	public static void main(String[] args) {
				
		Server server = new Server(12345);
		server.start();
	}

	class ClientThread extends Thread implements Runnable{
		Socket socket;
		ObjectInputStream input;
		ObjectOutputStream output;
		String username;
		String msg;

		public ClientThread(Socket socket) {
			this.socket = socket;
			try
			{
				output = new ObjectOutputStream(socket.getOutputStream());
				input  = new ObjectInputStream(socket.getInputStream());
				
				username = (String) input.readObject();

				broadcast(username + " Just connected.");
			}
			catch (Exception e) {
				return;
			}
		}

		public void run() {
			boolean alive = true;
			while(alive) {
				try {
					msg = (String) input.readObject();
				}
				catch (Exception e) {
					break;				
				}

				broadcast(username + ": " + msg);
			}
			close();
		}
		
		private void close() {
			try {
				if(output != null) 
					output.close();
			
				if(input != null) 
					input.close();

				if(socket != null) 
					socket.close();
			}
			catch (Exception e) {}
		}

		private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				output.writeObject(msg);
			}
			catch(IOException e) {
			}
			return true;
		}
	}
}
