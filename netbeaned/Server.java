import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import java.util.concurrent.locks.*;
import java.util.*;

class User{
	
	String email;
	String password;
	float debt;
	List<Integer> userServers;
	Lock l;
	
	public User(String email, String password){
		this.email = email;
		this.password = password;
		this.debt = 0;
		this.userServers = new ArrayList<>();
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
        String userEmail;
	String freeCode;
	Lock l;
	
	
	public Server(int id, String name, float price, String code, char type){
		this.serverId = id;
		this.name = name;
		this.requestPrice = price;
		this.inUse = 'N';
		this.freeCode = code;
		this.type = type;
		this.auctionPrice = 0;
		this.l = new ReentrantLock();
                this.userEmail = "empty";
	}
	
}

class Database{
	
	Map<String,User> users;
	Map<Integer,Server> servers;
	Lock l;
	
	public Database(){	
		this.users = new HashMap<>();
		this.servers = new HashMap<>();
		this.l = new ReentrantLock();
	}
	
}

class ClientHandler extends Thread {
	
	Socket cs;
	User currentUser = null;
	
	PrintWriter out;    // mudei a atribuição para o ClientHandler
	BufferedReader in;	// mudei a atribuição para o ClientHandler
	
	Database database;
        
        int mode;
	
	ClientHandler(Socket cs, Database database, int mode) throws IOException {		
                this.mode = mode;
		this.database = database;
                if(mode  == 2){
                    this.out = new PrintWriter(cs.getOutputStream()); // mudei a atribuição para o ClientHandler
                    this.in = new BufferedReader(new InputStreamReader(cs.getInputStream())); // mudei a atribuição para o ClientHandler
                    this.cs = cs;     
                }
	}
	
	private void registerUser() throws InterruptedException, IOException{
							
		String m = null;
		String p = null;
		User aux;
                Scanner sc = new Scanner(System.in);
		
                if(mode == 2){
                    out.println("What is your e-mail?\n");
                    out.flush();
                }
                else{
                    System.out.println("What is your e-mail?\n");
                }
	
		while(true){
			if(mode == 2){
                            try{
                                m = in.readLine(); 
                            }
                            catch(IOException e){}
                        }
                        else{
                            m = sc.nextLine();
                        } 
			if(m == null){ 
                                if(mode == 2){
                                    out.println("Invalid e-mail!Try again!\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("Invalid e-mail!Try again!\n");
                                }
			}
			else{
				break;
			}			
		}
		
                if(mode == 2){
                    out.println("What is your password?\n");
                    out.flush();
                }
                else{
                    System.out.println("What is your password?\n");
                }
			
		while(true){	                  
                        if(mode == 2){
                            try{
                                p = in.readLine(); 
                            }
                            catch(IOException e){}
                        }
                        else{
                            p = sc.nextLine();
                        }                            
			if(p == null){ 
                                if(mode == 2){
                                    out.println("Invalid password!Try again!\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("Invalid password!Try again!\n");
                                }
			}
			else{
				break;
			}			
		}
			
		aux = new User(m,p);
			
		database.l.lock();
		try{
			if(database.users.get(m) == null){
				database.users.put(m,aux);
                                if(mode == 2){
                                    out.println("User registered successfully.\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("User registered successfully.\n");
                                }
			}
			else{
                                if(mode == 2){
                                    out.println("E-mail already exists. User not registered.\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("E-mail already exists. User not registered.\n");
                                }
			}
		}
		finally{ database.l.unlock(); }

	}
	
	private void userLogIn() throws InterruptedException, IOException{
								
		String m = null;
		String p = null;
		User aux;
                Scanner sc = new Scanner(System.in);
		
                if(mode == 2){
                    out.println("What is your e-mail?\n");
                    out.flush();
                }
                else{
                    System.out.println("What is your e-mail?\n");
                }
		
		while(true){		
			if(mode == 2){
                            try{
                                m = in.readLine(); 
                            }
                            catch(IOException e){}
                        }
                        else{
                            m = sc.nextLine();
                        } 
			if(m == null){ 
                                if(mode == 2){
                                    out.println("Invalid e-mail!Try again!\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("Invalid e-mail!Try again!\n");
                                }
			}
			else{
				break;
			}			
		}
			
		database.l.lock();
		try{
			if(database.users.get(m) == null){				
                                if(mode == 2){
                                    out.println("No user exists with that e-mail.\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("No user exists with that e-mail.\n");
                                }
				return;			
			}
		}
		finally{ database.l.unlock(); }
			
		if(mode == 2){
                    out.println("What is your password?\n");
                    out.flush();
                }
                else{
                    System.out.println("What is your password?\n");
                }
			
		while(true){
			if(mode == 2){
                            try{
                                p = in.readLine(); 
                            }
                            catch(IOException e){}
                        }
                        else{
                            p = sc.nextLine();
                        }
			if(p == null){ 
                                if(mode == 2){
                                    out.println("Invalid password!Try again!\n");
                                    out.flush();
                                }
                                else{
                                    System.out.println("Invalid password!Try again!\n");
                                }
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
			
		if(!aux.password.equals(p)){			
                        if(mode == 2){
                            out.println("Wrong password!\n");
                            out.flush();
                        }
                        else{
                            System.out.println("Wrong password!\n");
                        }
			aux.l.unlock();
			return;				
		}
			
		currentUser = aux;

                if(mode == 2){
                    out.println("LogIn sucessful!\n");
                    out.flush();
                }
                else{
                    System.out.println("LogIn sucessful!\n");
                }
		
	}
	
	private void showServersRented() throws InterruptedException{
		
		int id;
		String name;
		String code;
		char type;
		
		List<Integer> userServers = currentUser.userServers;
		
		database.l.lock();
		try{
			for(int i = 0; i < userServers.size(); i++){			
				
				id = userServers.get(i);		
				name = database.servers.get(id).name;
				code = database.servers.get(id).freeCode; //Alterei de freeCode para code
				type = database.servers.get(id).type;
			
                                if(mode == 2){
                                    out.println("Server id = " + id + "; Server name = " + name + "; Server type = " + type + "; Server freeing code = " + code + "; \n");
                                    out.flush();
                                }
				else{
                                    System.out.println("Server id = " + id + "; Server name = " + name + "; Server type = " + type + "; Server freeing code = " + code + "; \n");
                                }		
			}
		}
		finally{ database.l.unlock(); }
                
                if(mode == 2){
                    out.println("List End.");
                    out.flush();
                }
                else{
                    System.out.println("List End.");
                }
		
	}
	
	private void grantServerRequest() throws InterruptedException, IOException{
		
		String name;
		char type;
		float price;
		String s = null;
		Server requestedServer;
                Scanner sc = new Scanner(System.in);
                
		if(mode == 2){
                    out.println("Pick a server from the List:");
                }
                else{
                    System.out.println("Pick a server from the List:");
                }
		
		database.l.lock();
		try{
			for(int id: database.servers.keySet()){		
				if(database.servers.get(id).inUse != 'Y'){			
					name = database.servers.get(id).name;
					type = database.servers.get(id).type;
					price = database.servers.get(id).requestPrice;
                                        if(mode == 2){
                                            out.println("ID: " + id + "; Name: " + name + "; Type: " + type + "; Price: " + price + ";");
                                        }
                                        else{
                                            System.out.println("ID: " + id + "; Name: " + name + "; Type: " + type + "; Price: " + price + ";");
                                        }
				}
			}
		}
		finally{ database.l.lock(); }
		
                if(mode == 2){
                    out.println("Type Server ID to choose.");
                    out.flush();
                }
                else{
                    System.out.println("Type Server ID to choose.");
                }
		
		if(mode == 2){
                    try{
                        s = in.readLine(); 
                    }
                    catch(IOException e){}
                }
                else{
                    s = sc.nextLine();
                } 
		
		database.l.lock();
		try{
			requestedServer = database.servers.get(Integer.parseInt(s));
		}
                catch(NumberFormatException n){ requestedServer = null; }
		finally{ database.l.unlock(); }
			
		if(requestedServer == null){
                    if(mode == 2){
			out.println("A server with that ID does not exist!");
			out.flush();
                    }
                    else{
                        System.out.println("A server with that ID does not exist!");
                    }
			return;		
		}
			
		requestedServer.l.lock();
		try{
			if(requestedServer.inUse == 'Y' ){
                            if(mode == 2){
				out.println("Server already in use!");
				out.flush();
                            }
                            else{
                                System.out.println("Server already in use!");
                            }
				return;
			}
		
			currentUser.userServers.add(requestedServer.serverId); //Alterei de id para serverId
			currentUser.debt += requestedServer.requestPrice;
			requestedServer.inUse = 'Y';
                        requestedServer.userEmail = currentUser.email;
		
                        if(mode == 2){
                            out.println("Server rental sucessful! Your freeing code is: " + requestedServer.freeCode);
                            out.flush();
                        }
                        else{
                            System.out.println("Server rental sucessful! Your freeing code is: " + requestedServer.freeCode);
                        }
		}
		finally{ requestedServer.l.unlock(); }
	}
	
	private void auctionServer() throws InterruptedException, IOException{		
		
		String name;
		char type;
		float price;
		String s = null;
		Server requestedServer = null;
                Scanner sc = new Scanner(System.in);
                User oldUser;
                
                if(mode == 2){
                    out.println("Pick a server from the List:");
                }
                else{
                    System.out.println("Pick a server from the List:");
                }
                    		
		database.l.lock();
		try{
			for(int id: database.servers.keySet()){		
				if(database.servers.get(id).inUse != 'Y'){			
					name = database.servers.get(id).name;
					type = database.servers.get(id).type;
					price = database.servers.get(id).auctionPrice;
                                    
                                    if(mode == 2){    
					out.println("ID: " + id + "; Name: " + name + "; Type: " + type + "; Current Offer: " + price + ";");
                                    }
                                    else{
                                        System.out.println("ID: " + id + "; Name: " + name + "; Type: " + type + "; Current Offer: " + price + ";");
                                    }
				}
			}
		}
		finally{ database.l.unlock(); }
		
                if(mode == 2){
                    out.println("Type Server ID to choose.");
                    out.flush();
                }
                else{
                    System.out.println("Type Server ID to choose.");
                }
		
		if(mode == 2){
                    try{
                        s = in.readLine(); 
                    }
                    catch(IOException e){}
                }
                else{
                    s = sc.nextLine();
                } 
		
		database.l.lock();
		try{
			requestedServer = database.servers.get(Integer.parseInt(s));
		}
                catch(NumberFormatException n){ requestedServer = null; }
		finally{ database.l.unlock(); }
		
		if(requestedServer == null){
			
                    if(mode == 2){
			out.println("A server with that ID does not exist!");
			out.flush();
                    }
                    else{
                        System.out.println("A server with that ID does not exist!");
                    }
			return;		
		}
				
		requestedServer.l.lock();
		try{
			if(requestedServer.inUse == 'Y' ){	
                            if(mode == 2){
				out.println("Server already in use!");
				out.flush();
                            }
                            else{
                                System.out.println("Server already in use!");
                            }
                            return;	
			}
                        
                        if(mode == 2){
                            out.println("What is your price offer?");
                            out.flush();
                        }
                        else{
                            System.out.println("What is your price offer?");
                        }
		
			if(mode == 2){
                            try{
                                s = in.readLine(); 
                                }
                            catch(IOException e){}
                        }
                        else{
                            s = sc.nextLine();
                        } 
		
			if(Float.parseFloat(s) <= requestedServer.auctionPrice){
                            if(mode == 2){
				out.println("Your offer does not beat the current offer!");
				out.flush();
                            }
                            else{
                                System.out.println("Your offer does not beat the current offer!");
                            }
                            return;			
			}
		
			if(Float.parseFloat(s) >= requestedServer.requestPrice){
                                if(mode == 2){
                                    out.println("Your offer is equal or better to the server's request price! Use the request server option instead!");
                                    out.flush();
                                }
                                else{
                                    System.out.println("Your offer is equal or better to the server's request price! Use the request server option instead!");
                                }
				return;
			}
		
		
			currentUser.userServers.add(requestedServer.serverId);  //Alterei de id para serverId
			requestedServer.auctionPrice = Float.parseFloat(s);
			currentUser.debt += requestedServer.auctionPrice;
			requestedServer.inUse = 'A';
                        
                        database.l.lock();
                        try{
                            oldUser = database.users.get(requestedServer.userEmail);
                            
                            if(oldUser != null){
                                oldUser.l.lock();
                                try{
                                    for(int i = 0; i < oldUser.userServers.size(); i++){
                                        if(oldUser.userServers.get(i)==requestedServer.serverId){
                                            oldUser.userServers.remove(i);
                                            break;
                                        }
                                    }
                                }
                                finally{oldUser.l.unlock();}
                            }                           
                        }
                        finally{database.l.unlock();}
                        
                        requestedServer.userEmail = currentUser.email;
		
                        if(mode == 2){
                            out.println("Server rental sucessful! Your freeing code is: " + requestedServer.freeCode + "\n");
                            out.flush();
                        }
                        else{
                            System.out.println("Server rental sucessful! Your freeing code is: " + requestedServer.freeCode + "\n");
                        }
		}
		finally{ 
			requestedServer.l.unlock(); 
		}
		
	}
	
	private void freeServer() throws InterruptedException, IOException{
		
		String s = "0";
		int id;
		Server serv;
                Scanner sc = new Scanner(System.in);
		
                if(mode == 2){
                    out.println("Please input the freeing code of the server you want to free.\n");
                    out.flush();
                }
                else{
                    System.out.println("Please input the freeing code of the server you want to free.\n");
                }
		
		if(mode == 2){
                    try{
                        s = in.readLine(); 
                        }
                    catch(IOException e){}
                }
                else{
                    s = sc.nextLine();
                }  
		
		List<Integer> userServers = currentUser.userServers;
		
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
                                        serv.auctionPrice = 0;
                                        serv.userEmail = "empty";
					userServers.remove(i);
					
                                        if(mode == 2){
                                                out.println("Server successfully freed.\n");
                                                out.flush();
                                        }
                                        else{
                                            System.out.println("Server successfully freed.\n");
                                        }                                       
					return;
				}
			}
			finally{ serv.l.unlock(); }			
                }
                
                if(mode == 2){
                        out.println("You are not currently renting a server with the given freeing code.\n");
                        out.flush();
                }
                else{
                        System.out.println("You are not currently renting a server with the given freeing code.\n");
                }  		
	}
	
	private void showUserDebt() throws IOException, InterruptedException{		
                if(mode == 2){
                    out.println("Your current debt is: " + currentUser.debt + ".\n");
                    out.flush();
                }
                else{
                    System.out.println("Your current debt is: " + currentUser.debt + ".\n");
                }	
	}
	
        @Override
	public void run(){	
	
		String s = "0";
		int exit = 0;
                Scanner sc = new Scanner(System.in);
		
		while(exit == 0){
		
			if(currentUser == null){
                                
                                if(mode == 2){
                                    out.println("1-Log In. ");
                                    out.println("2-Register as new User. ");
                                    out.println("3-Exit. ");
                                    out.println("Select Option ");
                                    out.flush();
                                }
                                else{
                                    System.out.println("1-Log In. ");
                                    System.out.println("2-Register as new User. ");
                                    System.out.println("3-Exit. ");
                                    System.out.println("Select Option ");
                                }
		
				if(mode == 2){
                                    try{
                                        s = in.readLine(); 
                                    }
                                    catch(IOException e){}
                                }
                                else{
                                    s = sc.nextLine();
                                } 
			
				switch(s){
				
					case "1":
						try{
							userLogIn();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
				
					break;
				
					case "2":
						try{
							registerUser();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
						
				
					break;
				
					case "3":
				
						exit = 1;
                                                if(mode == 2){
                                                    out.close();
                                                    try{
                                                    	cs.close();
                                                    }
                                                    catch(IOException e){}                                                   
                                                }
				
					break;
				
				}		
			}	
		
			else{
                                if(mode == 2){
                                    out.println("1-Request Server. ");
                                    out.println("2-Bid on Server. ");
                                    out.println("3-Show rented Servers. ");
                                    out.println("4-Free a rented Server. ");
                                    out.println("5-Show current debt. ");
                                    out.println("6-Log out. ");
                                    out.println("Select Option ");
                                    out.flush();
                                }
                                else{
                                    System.out.println("1-Request Server. ");
                                    System.out.println("2-Bid on Server. ");
                                    System.out.println("3-Show rented Servers. ");
                                    System.out.println("4-Free a rented Server. ");
                                    System.out.println("5-Show current debt. ");
                                    System.out.println("6-Log out. ");
                                    System.out.println("Select Option ");
                                }
				
				if(mode == 2){
                                    try{
                                        s = in.readLine(); 
                                    }
                                    catch(IOException e){}
                                }
                                else{
                                    s = sc.nextLine();
                                } 
						
				switch(s){
				
					case "1":
						try{
							grantServerRequest();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
				
					break;
				
					case "2":
						try{
							auctionServer();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
					
					break;
				
					case "3":
						try{
							showServersRented();
						}
						catch(InterruptedException ie){}
				
					break;
					
					case "4":
						try{
							freeServer();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
				
					break;
					
					case "5":
						try{
							showUserDebt();
						}
						catch(IOException e){}
						catch(InterruptedException ie){}
				
					break;
				
					case "6":
										
						currentUser.l.unlock();
						currentUser = null;
				
					break;
					
				}
			}
		}	
	}
}

class MasterServer {
	
	static Database database;

	private static void init(){		
	
		database = new Database();
		Server serv = new Server(1,"calc64",20.5f,"HYTRD",'A'); 
		database.servers.put(1,serv);
		serv = new Server(2,"calc32",15.5f,"MARYC",'A');
		database.servers.put(2,serv);
		serv = new Server(3,"calc126",25.5f,"JDSVV",'A');
		database.servers.put(3,serv);
		serv = new Server(4,"game1",33.8f,"HSNBM",'B');
		database.servers.put(4,serv);
		serv = new Server(5,"game2",33.8f,"WQRXB",'B');
		database.servers.put(5,serv);
		serv = new Server(6,"game3",33.8f,"WAPMM",'B');
		database.servers.put(6,serv);
		serv = new Server(7,"database200",20.0f,"PBNDY",'C');
		database.servers.put(7,serv);
		serv = new Server(8,"database400",30.0f,"FZUTD",'C');
		database.servers.put(8,serv);
		serv = new Server(9,"database600",40.0f,"HVYWA",'C');
		database.servers.put(8,serv);
                User use = new User("email","password");
                database.users.put("email",use);
                use = new User("password","email");
                database.users.put("password",use);
				
	}
	
	public static void main(String[] args){
		
		int port = 1111;
		ServerSocket ss;
                int mode;
                Scanner sc = new Scanner(System.in);
                
		init();
               		
                System.out.println("Input 1 for standalone mode, 2 for client mode. "); 
                mode = Integer.parseInt(sc.nextLine());
                
                if(mode == 2){              
                    while(true){	
                        try{
                            ss = new ServerSocket(port);
                            Socket cs = ss.accept();
                            new ClientHandler(cs,database,mode).start();
                        }
                    	catch(IOException e){}
                    }	
                }
                if(mode == 1){                                               
                    try{
                        Socket cs = null;
                            new ClientHandler(cs,database,mode).start();
                    }
                    catch(IOException e){}
                            
                    while(true){                                                              
                    }
                }
	}
	
}