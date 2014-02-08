package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import client.slaves.IClientInInterface;
import client.slaves.IClientOutInterface;
import client.slaves.SlaveIn;
import client.slaves.SlaveOut;

import main.IClient;
import main.IClientGUI;
import main.MainMenuController;

import protocol.Protocol.ProtocolOut;

import javafx.application.Platform;

public class Client implements IClient, IClientGUI, IClientInInterface, IClientOutInterface
{
	private MainMenuController mMenuController;
	private boolean mIsConnectionCreated = false;	
	
	private SlaveOut mSlaveOut;
	private SlaveIn mSlaveIn;
	private Socket mClientSocket;
	private DataOutputStream mOutToServer;
	private DataInputStream mInFromServer;
	
	public Client(MainMenuController menuController)
	{
		mMenuController = menuController;
		mMenuController.setClient(this);
	}
	
	public void closeClient()
	{
		if(mIsConnectionCreated)
		{
			closeConnection();
		}
	}
	
	public void onConnectToggled()
	{
		if(mIsConnectionCreated)
		{
			closeConnection();
		}
		else 
		{
			openConnection();
		}
	}

	private void openConnection()
	{
		System.out.println("Client::openConnection()");
		
		mIsConnectionCreated = initConnection();
		if(mIsConnectionCreated)
		{
			mMenuController.setConnectButtonText("Disconnect");
			
			mSlaveOut = new SlaveOut(this, mOutToServer);
			mSlaveOut.start();
			
			mSlaveIn = new SlaveIn(this, mInFromServer);
			mSlaveIn.start();
		}
	}
	
	private boolean initConnection()
	{
		System.out.println("Client::initConnection()");
		
		boolean hasConnected = false;
		try 
		{
			mClientSocket = new Socket(mMenuController.getServerName(), 
					mMenuController.getPortNumber());
			
			System.out.println("Client::initConnection() Connected to " + mClientSocket.getRemoteSocketAddress());
			
			OutputStream outToServer = mClientSocket.getOutputStream();
			mOutToServer = new DataOutputStream(outToServer);
			mOutToServer.writeInt(ProtocolOut.SET_USER_NAME.ordinal());
			mOutToServer.writeUTF(mMenuController.getUserName());
			
			InputStream inFromServer = mClientSocket.getInputStream();
			mInFromServer = new DataInputStream(inFromServer);
			
			hasConnected = mInFromServer.readBoolean();
			
			System.out.println("Client::initConnection() server says is connected: " + hasConnected);
		} 
		catch (IOException e) 
		{
			System.out.println("Client::initConnection() " + e.getMessage());
		}
		return hasConnected;
	}
	
	private void closeConnection()
	{
		System.out.println("Client::closeConnection()");
		
		try 
		{
			mClientSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		onConnectionClosed();
	}
	
	public synchronized void onConnectionClosed()
	{
		if(mIsConnectionCreated)
		{
			mIsConnectionCreated = false;
			Platform.runLater(new Runnable() 
			{
				public void run() 
				{
					mMenuController.clearUsersList();
					mMenuController.setConnectButtonText("Connect to server");
				}
			});
			mSlaveOut.interrupt();
			mSlaveOut = null;
			mSlaveIn = null;
			mOutToServer = null;
			mInFromServer = null;
			mClientSocket = null;
		}
	}
	
	public void sendMessage(String to, String message)
	{
		if(mSlaveOut != null)
		{
			mSlaveOut.sendMessage(to, message);
		}
	}
	
	public void onMessageReceived(String sender, String message)
	{
		mMenuController.onReceivedMessage(sender, message);
	}
	
	public void onUserCameOnline(final String userName)
	{
		Platform.runLater(new Runnable() 
		{
			public void run() 
			{
				mMenuController.addUserToList(userName);
			}
		});
	}
	public void onUserWentOffline(final String userName)
	{
		Platform.runLater(new Runnable() 
		{
			public void run() 
			{
				mMenuController.removeUserFromList(userName);
			}
		});
	}
}
