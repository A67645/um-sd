import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.BufferedReader;

class Client {
		
	public static void main(String[] args) throws Exception{
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		Socket cs = new Socket(host, port);
		PrintWriter out = new PrintWriter(cs.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
	
		String sIn;
		String sOut;
		int quit = 0; 
		int loggedIn = 0;
		
		while(quit == 0){
			if(loggedIn == 0){
				mainMenu();
			}
			else{
				loggedInMenu();
			}
		}
		
	
	}
	
	private void mainMenu(){
		
		while(sIn.equals("Select Option/n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		switch(sOut){
			
			case 1: sOut = "1";
				
				logIn();
				
				break;
					
			case 2: sOut = "2";
			
				registerAcc();
			
			break;
					
			case 3: sOut = "3";
			
				quit = 1;
				out.shutdownOutput();
				out.close();
				cs.close();
			
			break;
			
			default:
			
				System.out.println("Invalid Option!");
			
		}
		
	}
	
	private void loggedInMenu(){
		
		while(sIn.equals("Select Option/n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		switch(sOut){
			
			case 1: sOut = "1";
			
				requestServer();
			
			break;
					
			case 2: sOut = "2";
			
				bidOnServer();
			
			break;
					
			case 3: sOut = "3";
			
				fetchRentedServers();
			
			break;
					
			case 4: sOut = "4";
			
				freeServer();
			
			break;
			
			case 5: sOut = "5";
			
				consultDebt();
			
			break;
					
			case 6: sOut = "6";
			
				loggedIn = 0;
			
			break;
			
			default:
			
				System.out.println("Invalid Option!");
			
		}
		
	}
	
	private void logIn(){
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
				
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.compare("Invalid e-mail!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}
				
		if(sIn.compare("No user exists with that e-mail./n")){					
			return;				
		}
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.compare("Invalid password!Try again!/n")){
					
			sOut = System.console().readLine();
			out.println(sOut);
			out.flush();
					
			sIn = in.readLine();
			System.out.println(sIn);
					
		}
				
		if(sIn.compare("Wrong password!/n")){					
			return;				
		}				
				
		sIn = in.readLine();
		System.out.println(sIn);
		loggedIn = 1;	
		return;		
		
	}
	
	private void registerAcc(){
		
		sIn = in.readLine();
		System.out.println(sIn);
				
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
				
		sIn = in.readLine();
		System.out.println(sIn);
				
		while(sIn.compare("Invalid e-mail!Try again!/n")){
					
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
				
		while(sIn.compare("Invalid password!Try again!/n")){
					
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
	
	private void requestServer(){
		
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
	
	private void bidOnServer(){
		
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
	
	private void freeServer(){
		
		sIn = in.readLine();
		System.out.println(sIn);
		
		sOut = System.console().readLine();
		out.println(sOut);
		out.flush();
		
		sIn = in.readLine();
		System.out.println(sIn);
		
	}
	
	private void fetchRentedServers(){
		
		while(sIn.equals("List End./n") != true){	
			sIn = in.readLine();
			System.out.println(sIn);		
		}
		
	}
	
	private void consultDebt(){
		
		sIn = in.readLine();
		System.out.println(sIn);
		
	}

}