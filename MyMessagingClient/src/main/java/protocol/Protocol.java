package protocol;

public class Protocol 
{
	public enum ProtocolOut
	{
		SET_USER_NAME,
		SET_RECIPIENT,
		SET_MESSAGE
	}
	
	public enum ProtocolIn
	{
		GET_USER_CAME_ONLINE_NAME,
		GET_USER_CAME_OFFLINE_NAME,
		GET_SENDER_NAME,
		GET_MESSAGE
	}
}
