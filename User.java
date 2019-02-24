package user.com;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import Logger.Logging;
import mOperations.UserOperation;

public class User {
	static String id = null;
	Logging log = new Logging();
	public User(String id) {
		
		String name = "";
		String server = id.substring(0,3);
		if(server.equals("CON")) {
			name = "U_Con";
		}
		else 
			if(server.equals("MON")) {
				name = "U_Mon";
			
		}
			else if(server.equals("MCG")) {
				name = "U_MCG";
			}
		String response = "";

		// TODO Auto-generated constructor stub
		System.setProperty("java.security.policy",
				"file:///C:\\Users\\admin\\eclipse-workspace\\User\\src\\Client.policy");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			
			
			Registry registry = LocateRegistry.getRegistry(8086);

			UserOperation comp = (UserOperation) registry.lookup(name);
			System.out.println("Options:\n 1. Find Item\n2.Borrow Item\n3.Return Item ");
			System.out.println("Enter options");
			int opt = Integer.parseInt(new Scanner(System.in).nextLine());

			if (opt == 1) {
				System.out.println("Enter book name");
				String bookName = new Scanner(System.in).nextLine();
				response = comp.findItem(id, bookName);
			}
			else if(opt==2) {
				System.out.println("Enter book id");
				String itemID = new Scanner(System.in).nextLine();
				System.out.println("Enter no. of days");
				int nod = Integer.parseInt(new Scanner(System.in).nextLine());
				response = comp.borrowItem(id, itemID, nod);
				if(response.equals("successful")) {
					//good
				}else if (response.equals("WaitingList Open")){
					System.out.println("Do you want to added to the waiting queue? y\n");
					String resp = new Scanner(System.in).nextLine();
					if(resp.equals("y") || resp.equals("Y")) {
					response = comp.waitingList(id,itemID);
					}
						
				}
			}
			else if (opt == 3){
				System.out.println("Enter book id");
				String itemID = new Scanner(System.in).nextLine();
				response = comp.returnItem(id,itemID);
			}

			log.doLog(response);
			System.out.println(response);
			new Clie();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addItem(String bookId) {
		System.out.println("Enter Book Name");
		String BookName = new Scanner(System.in).nextLine();
		int Quantity = Integer.parseInt(new Scanner(System.in).nextLine());

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
