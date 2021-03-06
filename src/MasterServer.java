import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.Object;

class User{
	
	String email;
	String password;
	List<Integer> userServers = new ArrayList<Integer>();
	
	public User(String email, String password){
		this.email = email;
		this.password = password;
	}
	
}

class Server{
	
	int serverId;
	String name;
	float requestPrice;
	float auctionPrice;
	char inUse;
	String freeCode;
	
	
	public Server(int id, String name, float price, String code){
		this.serverId = id;
		this.name = name;
		this.requestPrice = price;
		this.inUse = 'N';
		this.freeCode = code;
	}
	
}

class clientHandler extends Thread {
	
	Socket cs;
	User currentUser;
	
	PrintWriter out = new PrintWriter(cs.getOutputStream());
	BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));	
	
	Map<String,User> users;
	Map<Integer,Server> serversType1;
	Map<Integer,Server> serversType2;
	Map<Integer,Server> serversType3;
	
	clientHandler(Socket cs, Map<String,User> users, Map<Integer,Server> serversType1, Map<Integer,Server> ServersType2, Map<Integer,Server> ServersType3){
		this.cs = cs;
		this.users = users;
		this.serversType1 = serversType1;
		this.serversType2 = serversType2;
		this.serversType3 = serversType3;
	}
	
	private void registerUser(){
		
		try{
							
			String m,p;
			User aux;
		
			out.println("What is your e-mail?/n");
			out.flush();
		
			while(true){				
				m = in.readLine();
				if(m == null){ 
					out.print("Invalid e-mail!Try again!/n");
					out.flush();
				}
				else{
					break;
				}			
			}
			
			out.println("What is your password?/n");
			out.flush();
			
			while(true){				
				p = in.readLine();
				if(p == null){ 
					out.print("Invalid password!Try again!/n");
					out.flush();
				}
				else{
					break;
				}			
			}
			
			aux = new User(m,p);
			
			//fazer lock
			
			if(users.get(m) != null){
				users.put(m,aux);
				out.println("User registered successfully./n");
				out.flush();
			}
			else{
				out.println("E-mail already exists. User not registered./n");
				out.flush();			
			}		
			
		}catch(IOException e){}		
		return;		
	}
	
	public void userLogIn(){
		
		try{
						
			String m,p;
			User aux;
		
			out.println("What is your e-mail?/n");
			out.flush();
		
			while(true){				
				m = in.readLine();
				if(m == null){ 
					out.print("Invalid e-mail!Try again!/n");
					out.flush();
				}
				else{
					break;
				}			
			}
			
			//fazer lock
			
			if(users.get(m) == null){				
				out.print("No user exists with that e-mail./n");
				out.flush();				
				return;			
			}
			
			out.println("What is your password?/n");
			out.flush();
			
			while(true){				
				p = in.readLine();
				if(p == null){ 
					out.print("Invalid password!Try again!/n");
					out.flush();
				}
				else{
					break;
				}			
			}
			
			//fazer lock
			
			aux = users.get(m);
			
			if(aux.password != m){			
				out.print("Wrong password!/n");
				out.flush();	
				return;				
			}
			
			currentUser = aux;
			
			out.print("LogIn sucessful!/n");
			out.flush();
							
		}catch(IOException e){}
		return;		
	}
	
	private void showAccountInfo(){
		
	}
	
	private void grantServerRequest(){
		
	}
	
	private void auctionServer(){		
		
	}
	
	private void freeServer(){
		
	}
	
	public void run(){	
		
	}
	
}

class MasterServer {
	
	Map<String,User> users = new HashMap<String,User>();
	Map<Integer,Server> serversType1 = new HashMap<Integer,Server>();
	Map<Integer,Server> serversType2 = new HashMap<Integer,Server>();
	Map<Integer,Server> serversType3 = new HashMap<Integer,Server>();
	
	private void init(){		
		
	}
	
	public static void main(String[] args){
		
		int port = 1111;
		ServerSocket ss = new ServerSocket(port);
		
		init();
			
		while(true){			
			Socket cs = ss.accept();
			new ClientHandler(cs).start();		
		}		
	}
	
}