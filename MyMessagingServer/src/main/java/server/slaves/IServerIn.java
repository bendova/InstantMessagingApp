package server.slaves;

public interface IServerIn 
{
	void sendMessage(String recipientName, String sender, String message);	
	void onClientDisconnected(String client);
}
