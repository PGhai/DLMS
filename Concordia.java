package concordia.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Concordia {
	static byte buf[] = null;

	public void getBooks(String message) throws IOException {

		DatagramSocket ds = new DatagramSocket();
		DatagramPacket DpSend = null;

		InetAddress ip = InetAddress.getLocalHost();
		try {

			while (true) {

				buf = message.getBytes();

				DpSend = new DatagramPacket(buf, buf.length, ip, 1244);// ASk Montreal for books

				ds.send(DpSend);
				break;
				// break the loop if user enters "bye"
//				if (inp.equals("bye"))
//					break;
			}
			ds.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void WaitList(String userID, String itemID, int port) throws SocketException, UnknownHostException {

		DatagramSocket ds = new DatagramSocket();
		DatagramPacket DpSend = null;

		InetAddress ip = InetAddress.getLocalHost();

		String message = "10" + userID + "," + itemID;
		try {

			while (true) {

				buf = message.getBytes();

				DpSend = new DatagramPacket(buf, buf.length, ip, port);// ASk Montreal for books

				ds.send(DpSend);
				break;
				// break the loop if user enters "bye"
//				if (inp.equals("bye"))
//					break;
			}
			ds.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void issueBook(String userID, String itemID, int nod, int port)
			throws SocketException, UnknownHostException {

		DatagramSocket ds = new DatagramSocket();
		DatagramPacket DpSend = null;

		InetAddress ip = InetAddress.getLocalHost();

		String message = "00" + userID + "," + itemID + "," + Integer.toString(nod);
		try {

			while (true) {

				buf = message.getBytes();

				DpSend = new DatagramPacket(buf, buf.length, ip, port);// ASk Montreal for books

				ds.send(DpSend);
				break;
				// break the loop if user enters "bye"
//		if (inp.equals("bye"))
//			break;
			}
			ds.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendPacket(String query,int port)
			throws SocketException, UnknownHostException {

		DatagramSocket ds = new DatagramSocket();
		DatagramPacket DpSend = null;

		InetAddress ip = InetAddress.getLocalHost();

		try {

			while (true) {

				buf = query.getBytes();

				DpSend = new DatagramPacket(buf, buf.length, ip, port);// ASk Montreal for books

				ds.send(DpSend);
				break;
			
				
			}
			ds.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) throws IOException {

		// Concordia Client
		// new Concordia().getBooks("hell");

		// ds.close();
	}

}
