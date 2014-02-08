package client.slaves;

public interface IClientInInterface 
{
	void onUserCameOnline(String userName);
	void onUserWentOffline(String userName);
	void onMessageReceived(String sender, String message);
	void onConnectionClosed();
}
