import java.net.*;
import java.io.*;
import java.util.*;

public class Client  {

	private ObjectInputStream input;		
	private ObjectOutputStream output;	
	private Socket socket;
	private String server;
	private String username;
	private int port;

	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			return false;
		}
	
		try
		{
			input  = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (Exception e) {
			return false;
		}

		new ListenFromServer().start();
		
		try {
			output.writeObject("\t\t" + username);
		}
		
		catch (Exception e) {
			disconnect();
			return false;
		}
		return true;
	}
	
	void sendMessage(String msg) {
		try {
			output.writeObject(msg);
		}
		catch(IOException e) {}
	}

	private void disconnect() {

		sendMessage("Disconnected");
		
		try { 
			if(input != null) 
				input.close();
			if(output != null) 
				output.close();
			if(socket != null) 
				socket.close();
		}
		catch (Exception e) {}
	}

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		
		String serverAddress = "localhost";

		System.out.print("Enter your name: ");
		String userName = scan.nextLine();

		Client client = new Client("localhost", 12345, userName);
		if(!client.start())
			return;
		
		while(true) {
			System.out.print("Type your Message: ");
			String msg = scan.nextLine();

			if(msg.equals("kill"))
				break;

			client.sendMessage(msg);
		}
		client.disconnect();	
	}

	class ListenFromServer extends Thread implements Runnable {

		public void run() {
			while(true) {
				try {
					String msg = (String) input.readObject();
					System.out.println(msg);
					System.out.print("Type your Message: ");
				}
				catch(Exception e) {
					break;
				}
			}
		}
	}
}
