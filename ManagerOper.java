package mOperations;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManagerOper extends Remote {

	 public String checkUser(String id) throws RemoteException;
	 public String addItem(String BookID, String BookName, int Quantity) throws RemoteException;
	 public String removeItem(String BookID, int Quantity) throws RemoteException;
	 public String listItemAvailability(String id) throws RemoteException;

}
