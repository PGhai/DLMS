package concordia.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import Logger.Logging;
import mOperations.ManagerOper;
import mOperations.UserOperation;

public class ConcServer implements ManagerOper, UserOperation {
	static Hashtable<String, BookInfo> books = new <String, BookInfo>Hashtable();
	static Hashtable<String, Booking> BookingList = new <String, BookInfo>Hashtable();
	static Hashtable<String, ArrayList> WaitList = new Hashtable<String, ArrayList>();

	// static int[] ports = new int[] {1244,1255}; //1244MOntrea 1255Mcgill
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
	Concordia con = new Concordia();
	Logging log = new Logging();

	public static void main(String[] args) throws IOException {

		// TODO Auto-generated method stub
		// -------------------------Security
		// Policy--------------------------------------------//
		System.setProperty("java.security.policy",
				"file:///C:\\Users\\admin\\eclipse-workspace\\Concordia\\src\\server.policy");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			String Mang = "M_Con";
			String User = "U_Con";

			ManagerOper engine = new ConcServer();
			UserOperation engine_2 = (UserOperation) new ConcServer();
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

		manager.add("CONM1111");
		manager.add("MCGMM1111");
		manager.add("MONM1111");
		user.add("CONU1111");
		user.add("CONU1234");
		// ---------------add Book--------------
		books.put("CON1012", new BookInfo("HelloIndia", 5));
		books.put("CON1022", new BookInfo("HelloCanada", 5));

		// int[] Port = new int[] { 1234, 1260, 1254 }; // 1234=concordia clien,
		// 1260=Montreal,1254 = McGill
		// int port = 1266;
		DatagramPacket packet = null;
		DatagramSocket ds = new DatagramSocket(1266);
		byte[] receive = new byte[65535];

		while (true) {
			// for (int i = 0; i < Port.length; i++) {

			packet = new DatagramPacket(receive, receive.length);
			ds.receive(packet);

			new ServerThread(receive);
		}
	}

	@Override
	public String checkUser(String id) {
		// TODO Auto-generated method stub
		if (id.charAt(3) == 'M' & manager.contains(id)) {

			return "M";

		} else if (id.charAt(3) == 'U' & user.contains(id)) {

			return "U";

		} else {
			return "N";

		}
	}

	@Override
	public String removeItem(String BookID, int Quantity) {
		System.out.println(BookID);
		System.out.println(books.containsKey(BookID));
		if (books.containsKey(BookID) && Quantity == -1) {
			books.remove(BookID);
			return BookID + " is deleted from the list";

		} else if (books.containsKey(BookID)) {
			BookInfo temp = books.get(BookID);
			temp.BookQuantity = temp.BookQuantity - Quantity;
			books.put(BookID, temp);
			return BookID + " Quantity is dec";
		} else {
			return BookID + " is not in the list";
		}
	}

	@Override
	public String addItem(String BookID, String BookName, int Quantity) {

		// Check for BookIf if already exist
		if (books.contains(BookID)) {

			books.put(BookID, new BookInfo(BookName, Quantity + books.get(BookID).BookQuantity));
			return "Book Id Already Exist-- Value Updated";
		} else {
			books.put(BookID, new BookInfo(BookName, Quantity));
			return "Successfully added";
		}

	}

	public String Reciever(String code) throws IOException {
		String result = "";

		int port = 1234;
		DatagramPacket packet = null;
		DatagramSocket ds = new DatagramSocket(1234);
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

	@Override // User-------------------------------Operations---------------------------------
	public String findItem(String userID, String itemName) throws Exception {
		String result = "";
		log.doLog("Request find Item from " + userID + "for " + itemName);
		new Concordia().sendPacket("01" + itemName, 1244);// ask montreal
		result = Reciever("01");
		new Concordia().sendPacket("31" + itemName, 1277);// ask mcgill
		result = result + Reciever("31");

		for (String k : books.keySet()) {
			BookInfo info = books.get(k);
			if (info.BookName.equals(itemName)) {
				result = result + k + " " + Integer.toString(info.BookQuantity) + ",";
			}
			// now set up communication with other sevrer and get their books with same name

		}
		log.doLog("Response: " + result);

		System.out.println(result);
		return result;

		// TODO Auto-generated method stub

	}

	@Override
	public String borrowItem(String userID, String itemID, int nod) {
		// TODO Auto-generated method stub

		log.doLog("Request borrow Item from " + userID + "for " + itemID + "for days-" + Integer.toString(nod));

		String message = userID + "," + itemID + "," + Integer.toString(nod);
		String result = "";
		if (itemID.substring(0, 3).equals("CON")) {
			if (books.get(itemID).BookQuantity > 0) {
				BookingList.put(userID, new Booking(itemID, nod));

				books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity - 1));
				result = "Successful";
			} else if (books.get(itemID).BookQuantity <= 0) {
				result = "WaitingList Open";
			}
		} else if (itemID.substring(0, 3).equals("MON")) {
			log.doLog("Booking request send to Montreal");
			try {
				con.sendPacket("00" + message, 1244);
				// new Concordia().issueBook(userID, itemID, nod, 1244);
				result = Reciever("00");

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (itemID.substring(0, 3).equals("MCG")) {
			log.doLog("Booking request send to McGill");
			System.out.println("Booking request send to McGill");
			try {
				con.sendPacket("30" + message, 1277);
				// new Concordia().issueBook(userID, itemID, nod, 1277);
				result = Reciever("30");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		log.doLog("Response" + result);

		return result;
	}

	@Override
	public String waitingList(String userID, String itemID) throws RemoteException {
		log.doLog("Waiting request Item from " + userID + "for " + itemID);
		String result = "";
		if (itemID.substring(0, 3).equals("CON")) {

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
		} else {
			if (itemID.substring(0, 3).equals("MON")) {

				try {

					con.WaitList(userID, itemID, 1244);
					result = Reciever("77");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				if (itemID.substring(0, 3).equals("MCG")) {

					try {

						con.WaitList(userID, itemID, 1277);
						result = Reciever("32");
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
		log.doLog("Response: " + result);
		return result;
	}

	@Override
	public String returnItem(String userID, String itemID) throws RemoteException {
		// TODO Auto-generated method stub
		log.doLog("Request return Item from " + userID + "for " + itemID);
		System.out.println("Req recieved");
		//int qua = books.get(itemID).BookQuantity;
		String response = "";
		if (itemID.substring(0, 3).equals("CON")) {
			if (BookingList.containsKey(userID) && books.containsKey(itemID)
					&& BookingList.get(userID).equals(itemID)) {
				
				if(books.get(itemID).BookQuantity == 0) {
					BookingList.remove(userID);
					
					if(WaitList.containsKey(itemID)) {
					 BookingList.put((String) WaitList.get(itemID).get(0), new Booking(itemID,2));
				 response = "Booking assigned to new user"+ (String) WaitList.get(itemID).get(0);
					}
				} else {
				// remover from bookinglist and update the book info
				BookingList.remove(userID);
				books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity + 1));
				}// update the new user in the queue
				response = "Successfully returned";
			} else {
				response = "Please check the userID or Item ID";
			}
		} else if (itemID.substring(0, 3).equals("MON")) {

			try {
				// make a string of the query you wna to send
				// for sending return request to other server we use the code 11 appended with
				// the query
				String query = "11" + userID + "," + itemID;
				con.sendPacket(query, 1244);

				response = Reciever("11");

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (itemID.substring(0, 3).equals("MCG")) {

			try {
				// make a string of the query you wna to send
				// for sending return request to other server we use the code 11 appended with
				// the query
				String query = "33" + userID + "," + itemID;
				con.sendPacket(query, 1277);

				response = Reciever("33");

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else {
			response = "Please check the userID or Item ID";
		}
		
		
		log.doLog("Response: " + response);

		return response;
	}

	// ------------------------

	public synchronized void waitingRequest(String subs, String code, int port) throws IOException {
		// TODO Auto-generated method stub
		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		String response = "";
		log.doLog("Request to add in waiting ItemList" + itemID + "from" + userID);

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
		con.sendPacket("22" + response, 1275);
//make this
	}

	public synchronized void returnRequest(String subs, String code, int port) throws IOException {
		// TODO Auto-generated method stub

		System.out.println("resquest processing");
		String response = "";
		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		log.doLog("Return request:" + userID + "-" + itemID);
		if (BookingList.containsKey(userID) && books.containsKey(itemID)) {
			BookingList.remove(userID);
			books.put(itemID, new BookInfo(books.get(itemID).BookName, books.get(itemID).BookQuantity + 1));
			response = "Successfully returned";
		} else {
			response = "Please check the user ID or Item Id";
		}
		log.doLog(response);
		con.sendPacket(code + response, port);

	}

	public synchronized void bookingRequest(String subs, String code, int port) throws IOException {
		System.out.println("Borrow Request processing");

		String[] arr = subs.split(",");
		String userID = arr[0];
		String itemID = arr[1];
		log.doLog("Booking    request:" + userID + "-" + itemID);
		String response = "";
		int nod = Integer.parseInt(arr[2]);
		if (BookingList.containsKey(userID)) {
			response = "Already borrowed";
		} else {
			if (books.containsKey(itemID) && books.get(itemID).BookQuantity > 0) {
				String bookName = books.get(itemID).BookName;

				books.put(itemID, new BookInfo(bookName, books.get(itemID).BookQuantity - 1));
				BookingList.put(userID, new Booking(itemID, nod));
				System.out.println(books.get(itemID).BookName + books.get(itemID).BookQuantity + "-----boorrowed");
				response = "successful";
			} else if (books.containsKey(itemID) && books.get(itemID).BookQuantity < 0) {
				response = "WaitingList Open";

			} else {
				response = "not Succesfull. Check id or user id";
			}

		}
		log.doLog(response);
		con.sendPacket(code + response, port);
	}

	public synchronized void findRequest(String itemName, String code, int port) throws Exception {
		System.out.println("System recieve a message");
		String result = "";
		log.doLog("find request for:" + itemName);
		for (String k : books.keySet()) {
			BookInfo info = books.get(k);
			if (info.BookName.equals(itemName)) {
				result = result + k + " " + Integer.toString(info.BookQuantity);
			}

		}
		log.doLog(result);
		System.out.println(result);
		con.sendPacket(code + result, port);
		// TODO Auto-generated method stub

	}

	@Override
	public String listItemAvailability(String id) throws RemoteException {
		// TODO Auto-generated method stub
		log.doLog("Request to find item" + id);
		String response = "";

		for (String K : books.keySet()) {

			response = response + ", " + K + " " + books.get(K).BookName + " "
					+ Integer.toString(books.get(K).BookQuantity);
		}
		log.doLog(response);
		return response;
	}
}

class ServerThread implements Runnable {

	byte[] data = null;

	ConcServer cs = new ConcServer();

	public void run() {

		try {
			String pack = data(data);
			if (pack.substring(0, 2).equals("40")) {
				cs.findRequest(pack.substring(2), "40", 1284);// mcgill

			} else if (pack.substring(0, 2).equals("60")) {// mont

				cs.findRequest(pack.substring(2), "60", 1275);

			} else if (pack.substring(0, 2).equals("15")) {// mont
				cs.bookingRequest(pack.substring(2), "15", 1275);
			} else if (pack.substring(0, 2).equals("61")) {// mcgill
				cs.bookingRequest(pack.substring(2), "61", 1284);
			} else if (pack.substring(0, 2).equals("22")) {
				cs.waitingRequest(pack.substring(2), "22", 1275);
			} else if (pack.substring(0, 2).equals("20")) {
				cs.waitingRequest(pack.substring(2), "20", 1284);
			} else if (pack.substring(0, 2).equals("43")) {
				cs.returnRequest(pack.substring(2), "43", 1284);
			} else if (pack.substring(0, 2).equals("23")) {
				cs.returnRequest(pack.substring(2), "23", 1275);
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
