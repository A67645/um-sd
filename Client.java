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
import java.util.concurrent.locks.*;
import java.util.*;


class Client {
		
	public static String sIn;
	public static String sOut;
	public static PrintWriter out;
	public static BufferedReader in;
	public static Socket cs;
	public static int quit;
	public static int loggedIn;
		
	public static void main(String[] args) throws Exception{
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		quit = 0; 
		loggedIn = 0;
		
		try{
			cs = new Socket(host, port);
			out = new PrintWriter(cs.getOutputStream());
			in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
		}
		catch(IOException e){}	
		
		while(quit == 0){
			if(loggedIn == 0){
				try{
					mainMenu();
				}
				catch(IOException e){}
			}
			else{
				try{
					loggedInMenu();
				}
				catch(IOException e){}
			}
		}
		
	
	}
	
	private static void mainMenu(){
		
		while(sIn.equals("Select Option/n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		switch(sOut){
			
			case "1":
				
				logIn();
				
				break;
					
			case "2":
			
				registerAcc();
			
			break;
					
			case "3":
			
				quit = 1;
				out.close();
				cs.close();
			
			break;
			
			default:
			
				System.out.println("Invalid Option!");
			
		}
		
	}
	
	private static void loggedInMenu(){
		
		while(sIn.equals("Select Option/n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		switch(sOut){
			
			case "1":
			
				requestServer();
			
			break;
					
			case "2":
			
				bidOnServer();
			
			break;
					
			case "3":
			
				fetchRentedServers();
			
			break;
					
			case "4":
			
				freeServer();
			
			break;
			
			case "5":
			
				consultDebt();
			
			break;
					
			case "6": 
			
				loggedIn = 0;
			
			break;
			
			default:
			
				System.out.println("Invalid Option!");
			
		}
		
	}
	
	private static void logIn(){
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
				
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.equals("Invalid e-mail!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}
				
		if(sIn.equals("No user exists with that e-mail./n")){					
			return;				
		}
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.equals("Invalid password!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}
				
		if(sIn.equals("Wrong password!/n")){					
			return;				
		}				
				
		sIn = in.readLine();
		System.out.println(sIn);
		loggedIn = 1;	
		return;		
		
	}
	
	private static void registerAcc(){
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
				
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.equals("Invalid e-mail!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.equals("Invalid password!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}			
				
		sIn = in.readLine();
		System.out.println(sIn);
		return;

	}
	
	private static void requestServer(){
		
		while(sIn.equals("Type Server ID to choose./n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
		
	}
	
	private static void bidOnServer(){
		
		while(sIn.equals("Type Server ID to choose./n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
		
		if(sIn.equals("A server with that ID does not exist!/n") || sIn.equals("Server already in use!/n")){			
			return;			
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
		
		if(sIn.equals("Your offer does not beat the current offer!/n") || sIn.equals("Your offer is equal or better to the server's request price! Use the request server option instead!/n")){			
			return;			
		}
		
		sIn = in.readLine();
		System.out.println(sIn);
		return;
		
	}
	
	private static void freeServer(){
		
		sIn = in.readLine();
		System.out.println(sIn);
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
		
	}
	
	private static void fetchRentedServers(){
		
		while(sIn.equals("List End./n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
	}
	
	private static void consultDebt(){
		
		sIn = in.readLine();
		System.out.println(sIn);
		
	}

}