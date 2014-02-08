package server;

public interface IServerController 
{
	void onClientConnected(String clientName);
	void onClientDisconnected(String clientName);
	void onDisconnect();
}
