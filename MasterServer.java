import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

class User{
	
	String email;
	String password;
	float debt;
	List<int> userServers = new ArrayList<int>();
	Lock l = new ReentrantLock();
	
	public User(String email, String password){
		this.email = email;
		this.password = password;
		this.debt = 0;
	}
	
}

class Server{
	
	int serverId;
	String name;
	float requestPrice;
	float auctionPrice;
	char inUse;
	char type;
	String freeCode;
	Lock l = new ReentrantLock();
	
	
	public Server(int id, String name, float price, String code, char type){
		this.serverId = id
		this.name = name;
		this.requestPrice = price;
		this.inUse = N;
		this.freeCode = code;
		this.type = type;
		this.auctionPrice = 0;
	}
	
}

class clientHandler extends Thread {
	
	Socket cs;
	User currentUser = null;
	
	PrintWriter out = new PrintWriter(cs.getOutputStream());
	BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));	
	
	Map<String,User> users;
	Map<int,Server> servers;
	
	clientHandler(Socket cs, Map<String,User> users, Map<int,Server> servers){
		this.cs = cs;
		this.users = users;
		this.servers = servers;

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
			
			aux = new user(m,p)
			
			//fazer lock de users
			
			if(users.get(m) != null){
				users.put(m,aux);
				out.println("User registered successfully./n");
				out.flush();
			}
			else{
				out.println("E-mail already exists. User not registered./n");
				out.flush();			
			}		
			
		}catch(IOException e)	
		return;		
	}
	
	private void userLogIn(){
		
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
			
			//fazer lock dos users
			
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
			
			aux = users.get(m);
			
			//fazer lock do user
			
			if(aux.password != m){			
				out.print("Wrong password!/n");
				out.flush();	
				return;				
			}
			
			currentUser = aux;
			
			out.print("LogIn sucessful!/n");
			out.flush();
							
		}catch(IOException e)
		// nao fazer unlock do user, faz-se no log-out
		return;		
	}
	
	private showServersRented(){
		
		int id;
		String name;
		String code;
		char type;
		float requestPrice;
		float auctionPrice;
		
		//fazer lock do user
		
		List<int> userServers = currentUser.userServers;
		
		for(int i = 0; i < userServers.size(); i++){			
			
			id = userServers.get(i);		
			name = servers.get(id).name;
			code = servers.get(id).freeCode;
			type = servers.get(id).type;
			
			out.print("Server id = "i"; Server name = "name"; Server type = "type"; Server freeing code = "freeCode"; /n");		
		}
		
		out.print("List End./n");
		out.flush;
		
	}
	
	private grantServerRequest(){
		
		String name;
		char type;
		float price;
		String s;
		Server requestedServer;
		
		//fazer lock dos servers
		
		out.print("Pick a server from the List: /n");
		
		for(int id: servers.keySet()){		
			if(servers.get(id).inUse != 'Y'){			
				name = servers.get(id).name;
				type = servers.get(id).type;
				price = servers.get(id).requestPrice;
			
				out.print("ID: "id"; Name: "name"; Type: "type"; Price: "price";\n");
			}
		}
		
		//fazer unlock
		
		out.print("Type Server ID to choose./n");
		out.flush();
		
		s = in.readLine();
		
		//fazer lock dos servidores
		
		requestedServer = servers.get(Integer.parseInt(s));
		
		//fazer unlock
		
		if(requestedServer == null){
			
			out.print("A server with that ID does not exist!/n");
			out.flush();
			return;
			
		}
		
		//fazer lock do servidor requisitado
		
		if(requestedServer.inUse == 'Y' ){
			
			out.print("Server already in use!/n");
			out.flush();
			//fazer unlock
			return;
			
		}
		
		currentUser.userServers.add(requestedServer.id);
		currentUser.debt += requestedServer.requestPrice;
		requestedServer.inUse = 'Y';
		
		out.print("Server rental sucessful! Your freeing code is: "requestedServer.freeCode"/n");
		out.flush();
		//fazer unlock
		return;
	}
	
	private auctionServer(){		
		
		String name;
		char type;
		float price;
		String s;
		Server requestedServer;
		
		//fazer lock dos servers
		
		out.print("Pick a server from the List: /n");
		
		for(int id: servers.keySet()){		
			if(servers.get(id).inUse != 'Y'){			
				name = servers.get(id).name;
				type = servers.get(id).type;
				price = servers.get(id).auctionPrice;
			
				out.print("ID: "id"; Name: "name"; Type: "type"; Current Offer: "price";\n");
			}
		}
		
		//fazer unlock
		
		out.print("Type Server ID to choose./n");
		out.flush();
		
		s = in.readLine();
		
		//fazer lock dos servidores
		
		requestedServer = servers.get(Integer.parseInt(s));
		
		//fazer unlock
		
		if(requestedServer == null){
			
			out.print("A server with that ID does not exist!/n");
			out.flush();
			return;
			
		}
		
		//fazer lock do servidor requisitado
		
		if(requestedServer.inUse == 'Y' ){
			
			out.print("Server already in use!/n");
			out.flush();
			//fazer unlock
			return;
			
		}
		
		out.print("What is your price offer?/n");
		out.flush();
		
		s = in.readLine();
		
		if(Float.parseFloat(s) <= requestedServer.auctionPrice){
			
			out.print("Your offer does not beat the current offer!/n");
			out.flush();
			//fazer unlock
			return;
			
		}
		
		if(Float.parseFloat(s) >= requestedServer.requestPrice){
			
			out.print("Your offer is equal or better to the server's request price! Use the request server option instead!/n");
			out.flush();
			//fazer unlock
			return;
			
		}
		
		
		currentUser.userServers.add(requestedServer.id);
		requestedServer.auctionPrice = Float.parseFloat(s);
		currentUser.debt += requestedServer.auctionPrice;
		requestedServer.inUse = 'A';
		
		out.print("Server rental sucessful! Your freeing code is: "requestedServer.freeCode"/n");
		out.flush();
		//fazer unlock
		return;
		
		
	}
	
	private freeServer(){
		
		String s;
		int id;
		Server serv;
		
		out.print("Please input the freeing code of the server you want to free./n");
		out.flush();
		
		s = in.readLine();
		
		List<int> userServers = currentUser.userServers;
		
		//fazer lock dos servers
		
		for(int i = 0; i < userServers.size(); i++){
			
			id = userServers.get(i);
			serv = servers.get(id);
			
			if(s.equals(serv.freeCode)){
				
				serv.inUse = 'N';
				userServers.remove(i);
				
				out.print("Server successfully freed./n");
				out.flush();
				//fazer unlock
				return;
			}
			
		}
		
		//fazer unlock
		
		out.print("You not currently renting a server with the given freeing code./n");
		out.flush();
		
		return;
	}
	
	private showUserDebt(){
		
		out.print("Your current debt is: "currentUser.debt"./n");
		out.flush();
		
		return;
		
	}
	
	public void run(){	
	
		String s;
		int exit = 0;
		
		while(exit == 0){
		
			if(currentUser == null){
				out.print("1-Log In./n");
				out.print("2-Register as new User./n");
				out.print("3-Exit./n");
				out.print("Select Option/n");
				out.flush();
		
				s = in.readLine();
			
				switch(s){
				
					case "1":
				
						userLogIn();
				
					break;
				
					case "2":
				
						registerUser();
				
					break;
				
					case "3":
				
						exit = 1;
						out.close()
						cs.close();
				
					break;
				
				}		
			}	
		
			else{
			
				out.print("1-Request Server./n");
				out.print("2-Bid on Server./n");
				out.print("3-Show rented Servers./n");
				out.print("4-Free a rented Server./n");
				out.print("5-Show current debt./n");
				out.print("6-Log out./n");
				out.print("Select Option/n");
			
				s = in.readLine();
			
				switch(s){
				
					case "1":
					
						grantServerRequest();
				
					break;
				
					case "2":
					
						auctionServer();
					
					break;
				
					case "3":
				
						showServersRented();
				
					break;
					
					case "4":
				
						freeServer();
				
					break;
					
					case "5":
					
						showUserDebt();
				
					break;
				
					case "6":
										
						currentUser.l.unlock();
						currentUser = null;
				
					break;
					
				}
			}
		}	
	}

class MasterServer {
	
	Map<String,User> users = new HashMap<String,User>();
	Map<int,Server> servers = new HashMap<int,Server>();

	
	private void init(){		
	
		Server serv = new Server(1,"calc64",20.5,"HYTRD",'A'); 
		servers.put(1,serv);
		serv = new Server(2,"calc32",15.5,"MARYC",'A');
		servers.put(2,serv);
		serv = new Server(3,"calc126",25.5,"JDSVV",'A');
		servers.put(3,serv);
		serv = new Server(4,"game1",33.8,"HSNBM",'B');
		servers.put(4,serv);
		serv = new Server(5,"game2",33.8,"WQRXB",'B');
		servers.put(5,serv);
		serv = new Server(6,"game3",33.8,"WAPMM",'B');
		servers.put(6,serv);
		serv = new Server(7,"database200",20.0,"PBNDY",'C');
		servers.put(7,serv);
		serv = new Server(8,"database400",30.0,"FZUTD",'C');
		servers.put(8,serv);
		serv = new Server(9,"database600",40.0,"HVYWA",'C');
		servers.put(8,serv);
		
		return;
		
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