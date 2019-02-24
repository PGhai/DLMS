package mOperations;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserOperation extends Remote {
	public String findItem(String userID,String itemName) throws RemoteException, Exception;
	public String borrowItem(String userID,String itemID,int nod) throws RemoteException;
	public String waitingList(String userID,String itemID) throws RemoteException;
	public String returnItem(String userID,String itemID) throws RemoteException;
	
}
