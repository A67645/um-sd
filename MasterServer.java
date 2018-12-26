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
	List<int> userServers;
	Lock l;
	
	public User(String email, String password){
		this.email = email;
		this.password = password;
		this.debt = 0;
		this.userServers = new ArrayList<int>();
		this.l = new ReentrantLock();
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
	Lock l;
	
	
	public Server(int id, String name, float price, String code, char type){
		this.serverId = id
		this.name = name;
		this.requestPrice = price;
		this.inUse = N;
		this.freeCode = code;
		this.type = type;
		this.auctionPrice = 0;
		this.l = new ReentrantLock();
	}
	
}

class Database{
	
	Map<String,User> users;
	Map<int,Server> servers;
	Lock l;
	
	public Database(){	
		this.users = new HashMap<String,User>();
		this.servers = new HashMap<int,Server>();
		this.l = new ReentrantLock();
	}
	
}

class clientHandler extends Thread {
	
	Socket cs;
	User currentUser = null;
	
	PrintWriter out = new PrintWriter(cs.getOutputStream());
	BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));	
	
	Database database;
	
	clientHandler(Socket cs, Database database){
		this.cs = cs;
		this.database = database;
	}
	
	private void registerUser() throws InterruptedException{
							
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
			
		database.l.lock();
		try{
			if(database.users.get(m) != null){
				database.users.put(m,aux);
				out.println("User registered successfully./n");
				out.flush();
			}
			else{
				out.println("E-mail already exists. User not registered./n");
				out.flush();			
			}
		}
		finally{ database.l.unlock(); }

		return;		
	}
	
	private void userLogIn() throws InterruptedException{
								
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
			
		database.l.lock();
		try{
			if(database.users.get(m) == null){				
				out.print("No user exists with that e-mail./n");
				out.flush();				
				return;			
			}
		}
		finally{ database.l.unlock(); }
			
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
			
		database.l.lock();
		try{
			aux = database.users.get(m);
		}
		finally{ database.l.unlock(); }
			
		aux.l.lock();
			
		if(aux.password != m){			
			out.print("Wrong password!/n");
			out.flush();
			aux.l.unlock();
			return;				
		}
			
		currentUser = aux;
			
		out.print("LogIn sucessful!/n");
		out.flush();
		
		return;		
	}
	
	private showServersRented() throws InterruptedException{
		
		int id;
		String name;
		String code;
		char type;
		float requestPrice;
		float auctionPrice;
		
		List<int> userServers = currentUser.userServers;
		
		database.l.lock();
		try{
			for(int i = 0; i < userServers.size(); i++){			
				
				id = userServers.get(i);		
				name = database.servers.get(id).name;
				code = database.servers.get(id).freeCode;
				type = database.servers.get(id).type;
			
				out.print("Server id = "i"; Server name = "name"; Server type = "type"; Server freeing code = "freeCode"; /n");		
			}
		}
		finally{ database.l.unlock(); }
		
		out.print("List End./n");
		out.flush;
		
	}
	
	private grantServerRequest() throws InterruptedException{
		
		String name;
		char type;
		float price;
		String s;
		Server requestedServer;
		
		out.print("Pick a server from the List: /n");
		
		database.l.lock();
		try{
			for(int id: database.servers.keySet()){		
				if(database.servers.get(id).inUse != 'Y'){			
					name = database.servers.get(id).name;
					type = database.servers.get(id).type;
					price = database.servers.get(id).requestPrice;
			
					out.print("ID: "id"; Name: "name"; Type: "type"; Price: "price";\n");
				}
			}
		}
		finally{ database.l.lock(); }
		
		out.print("Type Server ID to choose./n");
		out.flush();
		
		s = in.readLine();
		
		database.l.lock();
		try{
			requestedServer = database.servers.get(Integer.parseInt(s));
		}
		finally{ database.l.unlock(); }
			
		if(requestedServer == null){
				
			out.print("A server with that ID does not exist!/n");
			out.flush();
			return;		
		}
			
		requestedServer.l.lock();
		try{
			if(requestedServer.inUse == 'Y' ){			
				out.print("Server already in use!/n");
				out.flush();
				return;
			}
		
			currentUser.userServers.add(requestedServer.id);
			currentUser.debt += requestedServer.requestPrice;
			requestedServer.inUse = 'Y';
		
			out.print("Server rental sucessful! Your freeing code is: "requestedServer.freeCode"/n");
			out.flush();
		}
		finally{ requestedServer.l.unlock(); }
		return;
	}
	
	private auctionServer() throws InterruptedException{		
		
		String name;
		char type;
		float price;
		String s;
		Server requestedServer;

		out.print("Pick a server from the List: /n");
		
		database.l.lock();
		try{
			for(int id: database.servers.keySet()){		
				if(database.servers.get(id).inUse != 'Y'){			
					name = database.servers.get(id).name;
					type = database.servers.get(id).type;
					price = database.servers.get(id).auctionPrice;
				
					out.print("ID: "id"; Name: "name"; Type: "type"; Current Offer: "price";\n");
				}
			}
		}
		finally{ database.l.unlock(); }
		
		out.print("Type Server ID to choose./n");
		out.flush();
		
		s = in.readLine();
		
		database.l.lock();
		try{
			requestedServer = database.servers.get(Integer.parseInt(s));
		}
		finally{ requestedServer.l.lock(); }
		
		if(requestedServer == null){
			
			out.print("A server with that ID does not exist!/n");
			out.flush();
			return;		
		}
		
		requestedServer.l.lock();
		try{
			if(requestedServer.inUse == 'Y' ){				
				out.print("Server already in use!/n");
				out.flush();
				return;	
			}
		
			out.print("What is your price offer?/n");
			out.flush();
		
			s = in.readLine();
		
			if(Float.parseFloat(s) <= requestedServer.auctionPrice){				
				out.print("Your offer does not beat the current offer!/n");
				out.flush();				
				return;			
			}
		
			if(Float.parseFloat(s) >= requestedServer.requestPrice){			
				out.print("Your offer is equal or better to the server's request price! Use the request server option instead!/n");
				out.flush();
				return;
			}
		
		
			currentUser.userServers.add(requestedServer.id);
			requestedServer.auctionPrice = Float.parseFloat(s);
			currentUser.debt += requestedServer.auctionPrice;
			requestedServer.inUse = 'A';
		
			out.print("Server rental sucessful! Your freeing code is: "requestedServer.freeCode"/n");
			out.flush();
		}
		finally{ requestedServer.l.unlock(); }
		
		return;		
	}
	
	private freeServer() throws InterruptedException{
		
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
			database.l.lock();
			try{
				serv = database.servers.get(id);
			}
			finally{ database.l.unlock(); }
			
			serv.l.lock();
			try{
				if(s.equals(serv.freeCode)){
					
					serv.inUse = 'N';
					userServers.remove(i);
					
					out.print("Server successfully freed./n");
					out.flush();
					return;
				}
			}
			finally{ serv.l.unlock(); }			
		}
		
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
	
	Database database;

	private void init(){		
	
		database = new Database();
		Server serv = new Server(1,"calc64",20.5,"HYTRD",'A'); 
		database.servers.put(1,serv);
		serv = new Server(2,"calc32",15.5,"MARYC",'A');
		database.servers.put(2,serv);
		serv = new Server(3,"calc126",25.5,"JDSVV",'A');
		database.servers.put(3,serv);
		serv = new Server(4,"game1",33.8,"HSNBM",'B');
		database.servers.put(4,serv);
		serv = new Server(5,"game2",33.8,"WQRXB",'B');
		database.servers.put(5,serv);
		serv = new Server(6,"game3",33.8,"WAPMM",'B');
		database.servers.put(6,serv);
		serv = new Server(7,"database200",20.0,"PBNDY",'C');
		database.servers.put(7,serv);
		serv = new Server(8,"database400",30.0,"FZUTD",'C');
		database.servers.put(8,serv);
		serv = new Server(9,"database600",40.0,"HVYWA",'C');
		database.servers.put(8,serv);
		
		return;
		
	}
	
	public static void main(String[] args){
		
		int port = 1111;
		ServerSocket ss = new ServerSocket(port);
		
		init();
			
		while(true){			
			Socket cs = ss.accept();
			new ClientHandler(cs,database).start();		
		}		
	}
	
}