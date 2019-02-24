package user.com;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import Logger.Logging;
import mOperations.ManagerOper;
import manager.Manager;

public class Clie {
	Logging log = new Logging();
	public Clie(){
		// TODO Auto-generated method stub
				// ---------------------------------------Security
				// Policy-------------------------------------------
				System.setProperty("java.security.policy",
						"file:///C:\\Users\\admin\\eclipse-workspace\\User\\src\\Client.policy");
				if (System.getSecurityManager() == null) {
					System.setSecurityManager(new SecurityManager());
				}
				// ---------------------------------------Security Policy
				// ends-----------------------------------

				try {
					System.out.println("Please enter your user id");
					Scanner sc = new Scanner(System.in); 

					String id = sc.nextLine().toUpperCase().trim();
					
					String name = "";
					String server = id.substring(0,3);
					if(server.equals("CON")) {
						name = "M_Con";
					}
					else 
						if(server.equals("MON")) {
							name = "M_Mon";
						
					}
						else if(server.equals("MCG")) {
							name = "M_MCG";
						}
					Registry registry = LocateRegistry.getRegistry(8086);

					ManagerOper comp = (ManagerOper) registry.lookup(name);

					
					String a = (String) comp.checkUser(id);
					
					if(a.equals("M")) {
						
						new Manager(id);
						log.doLog("Manager:"+id );
					}
					else if(a.equals("U")){
						new User(id);
						log.doLog("Manager:"+id );
					}
					else if(a.equals("N")) {
						System.out.println("Invalid Id");
						log.doLog("Invalid "+id );
						new Clie();
					}


				} catch (Exception e) {
					e.printStackTrace();
				}

	}

	public static void main(String[] args) {
		new Clie();
	}

}
