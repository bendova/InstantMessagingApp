package server.slaves;

import java.io.DataInputStream;
import java.io.IOException;

import protocol.Protocol.ProtocolIn;
import server.Server;

public class SlaveIn extends Thread
{
	private IServerIn mServerIn;
	private String mClientName;
	private DataInputStream mInFromClient;
	
	private String mCurrentRecipientName;
	
	public SlaveIn(IServerIn serverIn, String clientName, DataInputStream inFromClient)
	{
		System.out.println("SlaveIn::SlaveIn()");
		
		mServerIn = serverIn;
		mClientName = clientName;
	
		mInFromClient = inFromClient;
	}
	
	@Override
	public void run()
	{
		System.out.println("SlaveIn::run()");
		
		while(true)
		{
			try 
			{
				ProtocolIn currentStepIn = ProtocolIn.values()[mInFromClient.readInt()];
				switch(currentStepIn)
				{
				case GET_RECIPIENT:
					mCurrentRecipientName = mInFromClient.readUTF();
					break;
				case GET_MESSAGE:
					String message = mInFromClient.readUTF();
					mServerIn.sendMessage(mCurrentRecipientName, mClientName, message);
					break;
				}
			} 
			catch (IOException e) 
			{
				//e.printStackTrace();
				System.out.println("SlaveIn::run() disconnected: " + e.getMessage());
				break;
			}
		}
		mServerIn.onClientDisconnected(mClientName);
	}
}
