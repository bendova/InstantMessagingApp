package protocol;

public class Protocol 
{
	public enum ProtocolIn
	{
		GET_USER_NAME,
		GET_RECIPIENT,
		GET_MESSAGE
	}
	
	public enum ProtocolOut
	{
		SET_USER_CAME_ONLINE_NAME,
		SET_USER_CAME_OFFLINE_NAME,
		SET_SENDER_NAME,
		SET_MESSAGE
	}
}
