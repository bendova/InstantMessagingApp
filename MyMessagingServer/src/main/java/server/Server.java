package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import protocol.Protocol.ProtocolIn;
import server.slaves.IServerIn;
import server.slaves.IServerOut;
import server.slaves.SlaveIn;
import server.slaves.SlaveOut;
import server.slaves.SlaveOut.StatusChange;

public class Server extends Thread implements IServerIn, IServerOut
{
	private class ClientServices
	{
		private SlaveIn mSlaveIn;
		private SlaveOut mSlaveOut;
		private Socket mSocket;
		
		public ClientServices(Socket socket, SlaveIn slaveIn, SlaveOut slaveOut)
		{
			mSocket = socket;
			mSlaveIn = slaveIn;
			mSlaveOut = slaveOut;
		}
		public Socket getSocket()
		{
			return mSocket;
		}
		public SlaveIn getSlaveIn()
		{
			return mSlaveIn;
		}
		public SlaveOut getSlaveOut()
		{
			return mSlaveOut;
		}
	}
	
	private ServerSocket mServerSocket;
	private IServerController mServerController;
	private Map<String, ClientServices> mUserServices;
	private boolean mIsShuttingDown = false;

	public Server(IServerController serverController, int portNumber) throws IOException
	{
		System.out.println("Server::Server() portNumber " + portNumber);
		
		mServerSocket = new ServerSocket(portNumber);
		mServerController = serverController;
		mUserServices = new ConcurrentHashMap<String, ClientServices>();
	}
	
	public void run()
	{
		System.out.println("Server::run()");
		
		while(true)
		{
			try
			{
				System.out.println("Server::run() waiting on port " + mServerSocket.getLocalPort());
				
				while(true)
				{
					initConnection(mServerSocket.accept());
				}
			}
			catch(SocketTimeoutException e)
			{
				System.out.println("Server::run() server timed out!");
				mServerController.onDisconnect();
				break;
			}
			catch(IOException e)
			{
				System.out.println("Server::run() " + e.getMessage());
				mServerController.onDisconnect();
				break;
			}
		}
	}
	
	private void initConnection(Socket socket) throws IOException
	{
		System.out.println("Server::initConnection() connected to " + socket.getRemoteSocketAddress());
		
		InputStream inFromClient = socket.getInputStream();
		DataInputStream input = new DataInputStream(inFromClient);
		
		ProtocolIn currentStepIn = ProtocolIn.values()[input.readInt()];
		if(currentStepIn.equals(ProtocolIn.GET_USER_NAME))
		{
			String clientName = input.readUTF();
			
			OutputStream outToClient = socket.getOutputStream();
			DataOutputStream output = new DataOutputStream(outToClient);
			
			if(mUserServices.get(clientName) == null)
			{
				System.out.println("Server::run() client connected: " + clientName);
				
				output.writeBoolean(true);
				onClientAdded(clientName, socket, input, output);
			}
			else 
			{
				output.writeBoolean(false);
			}
		}
	}
	
	private void onClientAdded(final String clientName, Socket serverSocket,
			DataInputStream inFromClient, DataOutputStream outToClient)
	{
		SlaveIn slaveIn = new SlaveIn(this, clientName, inFromClient);
		slaveIn.start();
		SlaveOut slaveOut = new SlaveOut(this, clientName, outToClient);
		slaveOut.start();
		
		ClientServices services = new ClientServices(serverSocket, slaveIn, slaveOut);
		mUserServices.put(clientName, services);
		notifyOfUserStatusChange(clientName, StatusChange.CAME_ONLINE);
		initUsersList(clientName);
		
		mServerController.onClientConnected(clientName);
	}
	
	public void onClientDisconnected(String client)
	{
		if(mIsShuttingDown == false)
		{
			synchronized (mUserServices)
			{
				ClientServices services = mUserServices.get(client);
				if(services != null)
				{
					mUserServices.remove(client);
					try 
					{
						services.getSocket().close();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					services.getSlaveOut().interrupt();
					notifyOfUserStatusChange(client, StatusChange.WENT_OFFLINE);
					mServerController.onClientDisconnected(client);
				}
			}
		}
	}
	
	public void closeServer()
	{
		System.out.println("Server::closeServer()");
	
		try 
		{
			mIsShuttingDown = true;
			closeConnections();
			mServerSocket.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void closeConnections()
	{
		System.out.println("Server::closeConnections()");
		
		if(mUserServices.isEmpty() == false)
		{
			Iterator<Entry<String, ClientServices>> iterator = mUserServices.entrySet().iterator();
			while(iterator.hasNext())
			{
				Entry<String, ClientServices> entry = iterator.next();
				try 
				{
					entry.getValue().getSocket().close();
					entry.getValue().getSlaveOut().interrupt();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			mUserServices.clear();
		}
	}
	
	public void initUsersList(String recipient)
	{
		Iterator<Entry<String, ClientServices>> iterator = mUserServices.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, ClientServices> entry = iterator.next();
			String userNameString = entry.getKey();
			if(recipient != userNameString)
			{
				mUserServices.get(recipient).getSlaveOut().notifyOfUserStatusChange(entry.getKey(), 
					StatusChange.CAME_ONLINE);
			}
		}
	}
	
	public void sendMessage(String recipientName, String sender, String message)
	{
		mUserServices.get(recipientName).getSlaveOut().sendMessage(sender, message);
	}
	
	public void notifyOfUserStatusChange(String userName, StatusChange statusChange)
	{
		Iterator<Entry<String, ClientServices>> iterator = mUserServices.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, ClientServices> entry = iterator.next();
			String recipientName = entry.getKey();
			if(recipientName != userName)
			{
				mUserServices.get(entry.getKey()).getSlaveOut().notifyOfUserStatusChange(userName, statusChange);
			}
		}
	}
}
