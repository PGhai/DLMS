package server.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import Logger.Logging;
import mOperations.ManagerOper;
import mOperations.UserOperation;


//McGill Server 
public class McGillServer implements ManagerOper, UserOperation {
	// Policy--------------------------------------------//
	static byte[] receive = new byte[65535];
	static Hashtable<String, BookInfo> books = new <String, BookInfo>Hashtable();
	static Hashtable<String, Booking> BookingList = new <String, BookInfo>Hashtable();
	static Hashtable<String, ArrayList> WaitList = new Hashtable<String, ArrayList>();
    
	static int[] ports = new int[] {1266,1255};// 1266COnc //1255mcgill
	
	static public class BookInfo {
		String BookName;
		int BookQuantity;

		BookInfo(String name, int Quantity) {
			this.BookName = name;
			this.BookQuantity = Quantity;
		}

	}

	static public class Booking {
		String bookId;
		int nod;

		Booking(String itemID, int nod) {
			this.bookId = itemID;
			this.nod = nod;
		}

	}
	static String res = null;
	static Set<String> manager = new HashSet<String>();
	static Set<String> user = new HashSet<String>();
	McGill mgill = new McGill();
	Logging log = new Logging();
	public static void main(String args[]) throws IOException {
		System.setProperty("java.security.policy",
				"file:///C:\\Users\\admin\\eclipse-workspace\\Concordia\\src\\server.policy");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			String Mang = "M_MCG";
			String User = "U_MCG";

			ManagerOper engine = (ManagerOper) new McGillServer();
			UserOperation engine_2 = (UserOperation) new McGillServer();
			UserOperation stub2 = (UserOperation) UnicastRemoteObject.exportObject(engine_2, 0);
			ManagerOper stub = (ManagerOper) UnicastRemoteObject.exportObject(engine, 0);
			Registry registry = LocateRegistry.getRegistry(8086);

			registry.bind(Mang, stub);
			registry.bind(User, stub2);

			System.out.println("Computer Engine bound or server up");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ---------------------------Security policy
		// ends------------------------------------------//

		books.put("MCG1012", new BookInfo("HelloIndia", 5));
		books.put("MCG1013", new BookInfo("HelloCanada", 5));
		manager.add("MCGM1111");
		user.add("MCGU1111");
	
		DatagramPacket packet = null;
		DatagramSocket ds = new DatagramSocket(1277);
		byte[] receive = new byte[65535];

		while (true) {

			try {
				packet = new DatagramPacket(receive, receive.length);
				ds.receive(packet);
				new ServerThread(receive);
			} finally {
				// TODO: handle finally clause
			}
		}
	}

	public synchronized void findRequest(String itemName, String code, int port) throws Exception {
		String result = " ";
       log.doLog("Request to find Item"+itemName);
		for (String k : books.keySet()) {
			BookInfo info = books.get(k);
			if (info.BookName.equals(itemName)) {
				result = result + k + " " + Integer.toString(info.BookQuantity) + ",";
			}
			// now set up communication with other sevrer and get their books with same name

		}
		// System.out.println("helloo");
		log.doLog("Response: "+result);
		
		mgill.sendPacket(code + result,port);
		// TODO Auto-generated method stub

	}

	public synchronized void bookingRequest(String subs, String code, int port) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Borrow Request processing");
		
		
		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		log.doLog("Request to find Item"+itemID+"from"+userID);
		String response = "";
		int nod = Integer.parseInt(arr[2]);
		if (BookingList.containsKey(userID)) {
			response = "Already borrowed";
		} else {
			if (books.containsKey(itemID) && books.get(itemID).BookQuantity > 0) {
				String bookName = books.get(itemID).BookName;

				books.put(itemID, new BookInfo(bookName, books.get(itemID).BookQuantity - 1));
				BookingList.put(userID, new Booking(itemID,nod));
				System.out.println(books.get(itemID).BookName + books.get(itemID).BookQuantity + "-----boorrowed");
				response = "successful";
			} else if (books.containsKey(itemID) && books.get(itemID).BookQuantity > 0) {
				response = "WaitingList Open";

			} else {
				response = "not Succesfull. Check id or user id";
			}

		}
		log.doLog("Response: "+response);
		mgill.sendPacket(code + response, port);
	}

	public synchronized void  waitingRequest(String subs,String code, int port) throws IOException {
		// TODO Auto-generated method stub
		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		String response = "";
		log.doLog("Request to add in waiting ItemList"+itemID+"from"+userID);
		
		if (WaitList.containsKey(itemID)) {
			ArrayList temp = WaitList.get(itemID);
			temp.add(userID);
		} else {
			ArrayList temp = new ArrayList();
			temp.add(userID);
			WaitList.put(itemID, temp);
		}
		// WaitList.put(itemID, value)
		log.doLog(response);
		mgill.sendPacket(code + response, port);

	}

	public void returnRequest(String subs, String code, int port) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("resquest processing");
		String response = "";
		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		log.doLog("Request to return Item"+itemID+"from"+userID);
		
		if (BookingList.containsKey(userID) && books.containsKey(itemID)) {
			BookingList.remove(userID);
			books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity + 1));
			response = "Successfully returned";
		} else {
			response = "Please check the user ID or Item Id";
		}
		log.doLog(response);
		mgill.sendPacket(code + response, port);

	}
//----------------------------
	public String Reciever(String code) throws IOException {
		String result="";
		DatagramPacket packet = null;
		DatagramSocket ds = new DatagramSocket(1284);
		byte[] receive = new byte[65535];

		while (true) {
			// for (int i = 0; i < Port.length; i++) {

			packet = new DatagramPacket(receive, receive.length);
			ds.receive(packet);
			String ret = "";
			int i = 0;
			while (receive[i] != 0) {
				ret = ret + (char) receive[i];

				i++;
			}
			if (ret.substring(0, 2).equals(code)) {
				result = ret.substring(2);

			}
			break;
		}
		ds.close();
return result;
	}
	//------------------------------------------
	@Override
	public String findItem(String userID, String itemName) throws Exception {
		log.doLog("Request to find Item"+itemName+"from"+userID);
		
        String result="";
        new McGill().sendPacket("40" + itemName, 1266);//ask cocordia
	    result= result + Reciever("40");
		
        new McGill().sendPacket("50" + itemName, 1244);// ask montreal
	    result = Reciever("50");
	    
		
		for (String k : books.keySet()) {
			BookInfo info = books.get(k);
			if (info.BookName.equals(itemName)) {
				result = result + k + " " + Integer.toString(info.BookQuantity) + ",";
			}
			// now set up communication with other sevrer and get their books with same name

		}
	log.doLog(result);	
		return result;

		// TODO Auto-generated method stub

	}

	@Override
	public String borrowItem(String userID, String itemID, int nod) {
		String result = "";
		log.doLog("Request to borrow Item"+itemID+"from"+userID);
		if(BookingList.containsKey(userID) && BookingList.get(userID).bookId == itemID ) {
		if (itemID.substring(0, 3).equals("MCG")) {
			if (books.get(itemID).BookQuantity > 0) {
				BookingList.put(userID, new Booking(itemID, nod));

				books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity - 1));
				result = "Successful";
			} else if (books.get(itemID).BookQuantity <= 0) {
				result = "WaitingList Open";
			}
		}else if (itemID.substring(0, 3).equals("MON")) {
			try {
				mgill.sendPacket("21" + userID + "," + itemID + "," + nod,1244);
				result = Reciever("21");
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		else if (itemID.substring(0, 3).equals("CON")) {
			try {
				mgill.sendPacket("61" + userID + "," + itemID + "," + nod,1266);
				result = Reciever("61");
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		}
		else {
			result ="already borrowed";
		}
		log.doLog(result);
		return result;
	}

	@Override
	public String waitingList(String userID, String itemID) {
		String result = "";
		log.doLog("Request to add waiting list"+itemID+"from"+userID);
		
		if (itemID.substring(0, 3).equals("MCG")) {

			if (WaitList.containsKey(itemID)) {
				ArrayList temp = WaitList.get(itemID);
				temp.add(userID);
			} else {
				ArrayList temp = new ArrayList();
				temp.add(userID);
				WaitList.put(itemID, temp);
			}
			// WaitList.put(itemID, value)
			result = "Successful";
		}
		else if (itemID.substring(0, 3).equals("CON")) {
			try {
				mgill.sendPacket("20" + userID + "," + itemID + ",",1266);
				result = Reciever("20");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (itemID.substring(0, 3).equals("MON")) {
			try {
				mgill.sendPacket("60" + userID + "," + itemID + "," ,1244);
				result = Reciever("60");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.doLog(result);
		return result;
	}

	@Override
	public String returnItem(String userID, String itemID) throws RemoteException {
		String message=userID + "," + itemID;
		String response = "";
		log.doLog("Request to return Item"+itemID+"from"+userID);
		
		if (itemID.substring(0, 3).equals("MCG")) {
			if(BookingList.containsKey(userID) && books.containsKey(itemID) && BookingList.get(userID).equals(itemID)) {
				// remover from bookinglist and update the book info
				
				if(books.get(itemID).BookQuantity == 0) {
					BookingList.remove(userID);
					
					if(WaitList.containsKey(itemID)) {
					 BookingList.put((String) WaitList.get(itemID).get(0), new Booking(itemID,2));
				 response = "Booking assigned to new user"+ (String) WaitList.get(itemID).get(0);
					}
				} else {
				
				
				BookingList.remove(userID);
				books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity + 1));
				
				//update the new user in the queue
				response = "Successfully returned";
			}
			}
			else {
				response ="Please check the userID or Item ID";
			}
		}else if (itemID.substring(0, 3).equals("CON")) {
			try {
				mgill.sendPacket("43" + message,1266);
				response = Reciever("43");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if (itemID.substring(0, 3).equals("MON")) {
			try {
				mgill.sendPacket("33" + message,1244);
				response = Reciever("33");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		log.doLog(response);
		return response;
	}

	@Override
	public String checkUser(String id) {
		if (id.charAt(3) == 'M' & manager.contains(id)) {

			return "M";

		} else if (id.charAt(3) == 'U' & user.contains(id)) {

			return "U";

		} else {
			return "N";

		}
	}

	@Override
	public String addItem(String BookID, String BookName, int Quantity) throws RemoteException {
		// Check for BookIf if already exist
		log.doLog("Request to add Item");
				
		if (books.contains(BookID)) {

					books.put(BookID, new BookInfo(BookName, Quantity + books.get(BookID).BookQuantity));
					log.doLog("Book Id Already Exist-- Value Updated");
					return "Book Id Already Exist-- Value Updated";
				} else {
					books.put(BookID, new BookInfo(BookName, Quantity));
					log.doLog("Successfully added");
					return "Successfully added";
				}

			}

	@Override
	public String removeItem(String BookID, int Quantity) throws RemoteException {
		log.doLog("Request to remove Item"+ BookID+Quantity);
		
		if (books.containsKey(BookID) && Quantity == -1) {
			books.remove(BookID);
			log.doLog(BookID + " is deleted from the list");
			return BookID + " is deleted from the list";

		} else if (books.containsKey(BookID)) {
			BookInfo temp = books.get(BookID);
			temp.BookQuantity = temp.BookQuantity - Quantity;
			books.put(BookID, temp);
			log.doLog(BookID + " Quantity is dec");
			return BookID + " Quantity is dec";
		} else {
			log.doLog(BookID + " is not in the list");
			return BookID + " is not in the list";
		}
	}

	@Override
	public String listItemAvailability(String id) throws RemoteException {
		// TODO Auto-generated method stub
		log.doLog("Request to find item"+id);
		String response = "";
		
		for(String K : books.keySet()) {
			
			response = response+", "+K+" "+books.get(K).BookName+" "+Integer.toString(books.get(K).BookQuantity);
		}
		log.doLog(response);
		return response;
	}
}

class ServerThread implements Runnable {

	byte[] data = null;
	McGillServer ms = new McGillServer();

	public void run() {

		try {
			String pack = data(data);
			if (pack.substring(0, 2).equals("31")) {
				ms.findRequest(pack.substring(2),"31",1234);//con

			}
			else if (pack.substring(0, 2).equals("41")) {
				ms.findRequest(pack.substring(2),"41",1275);//mont

			}	
			else if (pack.substring(0, 2).equals("30")) {//con
				ms.bookingRequest(pack.substring(2),"30",1234);
			}
			
			
			else if (pack.substring(0, 2).equals("42")) {//conc
				ms.bookingRequest(pack.substring(2),"42",1275);
			} else if (pack.substring(0, 2).equals("32")) {
				ms.waitingRequest(pack.substring(2),"32",1234);
			}else if (pack.substring(0, 2).equals("44")) {
				ms.waitingRequest(pack.substring(2),"44",1275);
			} 
			else if (pack.substring(0, 2).equals("63")) {
				ms.returnRequest(pack.substring(2),"63", 1275);
			}
			else if (pack.substring(0, 2).equals("33")) {
				ms.returnRequest(pack.substring(2),"33", 1234);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public ServerThread(byte[] data) {
		this.data = data;
		new Thread(this).start();
	}

	public static String data(byte[] a) {
		if (a == null)
			return null;
		String ret = "";
		int i = 0;
		while (a[i] != 0) {
			ret = ret + (char) a[i];

			i++;
		}
		return ret;

	}
}

