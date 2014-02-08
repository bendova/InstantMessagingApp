package server.slaves;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import protocol.Protocol.ProtocolOut;
import server.Server;

public class SlaveOut extends Thread
{
	public enum StatusChange
	{
		CAME_ONLINE,
		WENT_OFFLINE
	}
	
	private interface Request {};
	
	private class Message implements Request
	{
		private String mRecipient;
		private String mContent;
		
		public Message(String recipient, String content)
		{
			mRecipient = recipient;
			mContent = content;
		}
		
		public String getRecipient()
		{
			return mRecipient;
		}
		public String getContent()
		{
			return mContent;
		}
	}
	
	private class NotifyOfUserStatusChange implements Request
	{
		private StatusChange mStatusChange;
		private String mUserName;
		
		public NotifyOfUserStatusChange(String userName, StatusChange statusChange)
		{
			mUserName = userName;
			mStatusChange = statusChange;
		}
		public String getUserName()
		{
			return mUserName;
		}
		public StatusChange getStatusChangeType()
		{
			return mStatusChange;
		}
	}
	
	private IServerOut mServerOut;
	private String mClientName;
	private DataOutputStream mOutClient;
	
	private BlockingQueue<Request> mRequestBlockingQueue;
	
	public SlaveOut(IServerOut serverOut, String client, DataOutputStream outToClient)
	{
		System.out.println("SlaveOut::SlaveOut()");
		
		mServerOut = serverOut;
		mClientName = client;
	
		mOutClient = outToClient;
		mRequestBlockingQueue = new LinkedBlockingQueue<Request>();
	}
	
	@Override
	public void run()
	{
		System.out.println("SlaveOut::run()");
		
		while(true)
		{
			try 
			{
				Request request = mRequestBlockingQueue.take();
				if(request instanceof Message)
				{
					processRequest((Message)request);
				}
				else if(request instanceof NotifyOfUserStatusChange)
				{
					processRequest((NotifyOfUserStatusChange)request);
				}
			} 
			catch (IOException e) 
			{
				//e.printStackTrace();
				System.out.println("SlaveOut::run() disconnected: " + e.getMessage());
				break;
			}
			catch (InterruptedException e1) 
			{
				//e1.printStackTrace();
				System.out.println("SlaveOut::run() interrupted: " + e1.getMessage());
				break;
			}
		}
		mServerOut.onClientDisconnected(mClientName);
	}
	
	public void sendMessage(String sender, String content)
	{
		mRequestBlockingQueue.offer(new Message(sender, content));
	}
	public void notifyOfUserStatusChange(String userName, StatusChange statusChange)
	{
		mRequestBlockingQueue.offer(new NotifyOfUserStatusChange(userName, statusChange));
	}
	
	private void processRequest(Message message) throws IOException
	{
		mOutClient.writeInt(ProtocolOut.SET_SENDER_NAME.ordinal());
		mOutClient.writeUTF(message.getRecipient());
		
		mOutClient.writeInt(ProtocolOut.SET_MESSAGE.ordinal());
		mOutClient.writeUTF(message.getContent());
	}
	
	private void processRequest(NotifyOfUserStatusChange statusChange) throws IOException
	{
		if(statusChange.getStatusChangeType() == StatusChange.CAME_ONLINE)
		{
			mOutClient.writeInt(ProtocolOut.SET_USER_CAME_ONLINE_NAME.ordinal());
		}
		else if(statusChange.getStatusChangeType() == StatusChange.WENT_OFFLINE)
		{
			mOutClient.writeInt(ProtocolOut.SET_USER_CAME_OFFLINE_NAME.ordinal());
		}
		mOutClient.writeUTF(statusChange.getUserName());
	}
}