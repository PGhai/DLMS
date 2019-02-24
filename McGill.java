package server.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner; 

	  // Client
	public class McGill{
		
	static byte buf[] = null;

	public void sendPacket(String message,int port) throws IOException{
		System.out.println(message);
		DatagramSocket ds = new DatagramSocket();
		DatagramPacket DpSend = null;

		InetAddress ip = InetAddress.getLocalHost();
		
try {
		
		while (true) {
               
				buf = message.getBytes();
			
				DpSend = new DatagramPacket(buf, buf.length, ip, port);//ASk Montreal for books

				ds.send(DpSend);
                break;
				// break the loop if user enters "bye"
//				if (inp.equals("bye"))
//					break;
			}
	
	
}
	catch(Exception e) {
	e.printStackTrace();
}

	}
	public static void main(String[] args) throws IOException {
                
		//Concordia Client
		//new Concordia().getBooks("hell");

		//ds.close();
		}

	}
