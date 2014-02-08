package client.slaves;

import java.io.DataInputStream;
import java.io.IOException;

import protocol.Protocol.ProtocolIn;

import javafx.util.Callback;

public class SlaveIn extends Thread
{
	private DataInputStream mInFromServer;
	private String mCurrentSenderName;
	private IClientInInterface mClientIn;
	
	public SlaveIn(IClientInInterface clientIn, DataInputStream inFromServer)
	{
		System.out.println("SlaveIn::SlaveIn()");
		
		mInFromServer = inFromServer;
		mClientIn = clientIn;
	}
	
	@Override
	public void run()
	{
		System.out.println("SlaveIn::run()");
		
		while(true)
		{
			try 
			{
				ProtocolIn currentStep = ProtocolIn.values()[mInFromServer.readInt()];
				switch(currentStep)
				{
				case GET_USER_CAME_ONLINE_NAME:
					mClientIn.onUserCameOnline(mInFromServer.readUTF());
					break;
				case GET_USER_CAME_OFFLINE_NAME:
					mClientIn.onUserWentOffline(mInFromServer.readUTF());
					break;
				case GET_SENDER_NAME:
					mCurrentSenderName = mInFromServer.readUTF();
					break;
				case GET_MESSAGE:
					mClientIn.onMessageReceived(mCurrentSenderName, mInFromServer.readUTF());
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
		mClientIn.onConnectionClosed();
	}
}
