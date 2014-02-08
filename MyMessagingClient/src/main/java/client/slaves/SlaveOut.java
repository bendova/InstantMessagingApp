package client.slaves;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import protocol.Protocol.ProtocolOut;

import javafx.util.Callback;

public class SlaveOut extends Thread
{	
	private class Message
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
	
	private BlockingQueue<Message> mMessageBlockingQueue;
	
	private DataOutputStream mOutToServer;
	private IClientOutInterface mClientOut;
	
	public SlaveOut(IClientOutInterface clientOut, DataOutputStream outToServer)
	{
		System.out.println("SlaveOut::SlaveOut()");

		mClientOut = clientOut;
		mOutToServer = outToServer;
		mMessageBlockingQueue = new LinkedBlockingQueue<Message>();
	}
	
	@Override
	public void run()
	{
		System.out.println("SlaveOut::run()");
		
		while(true)
		{
			try 
			{
				Message message = mMessageBlockingQueue.take();
				
				mOutToServer.writeInt(ProtocolOut.SET_RECIPIENT.ordinal());
				mOutToServer.writeUTF(message.getRecipient());
				
				mOutToServer.writeInt(ProtocolOut.SET_MESSAGE.ordinal());
				mOutToServer.writeUTF(message.getContent());
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
		mClientOut.onConnectionClosed();
	}
	
	public void sendMessage(String recipient, String content)
	{
		System.out.println("SlaveOut::sendMessage() " + content);
		
		mMessageBlockingQueue.offer(new Message(recipient, content));
	}
}
