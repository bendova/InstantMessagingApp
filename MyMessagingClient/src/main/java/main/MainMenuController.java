package main;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class MainMenuController extends Parent implements Initializable
{
	@FXML
	private Button connectButton;
	@FXML
	private TextField hostNameTF;
	@FXML
	private TextField portTF;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea inputArea;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField userNameTF;
    @FXML
    private ListView usersListView;
    
	private Map<String, StringBuffer> mUserMessages;
    private String mSelectedUser;
	
    private IClientGUI mClient;
    
    @SuppressWarnings("unchecked")
	@FXML
	public void initialize(URL location, ResourceBundle resources) 
	{
    	System.out.println("MainMenuController::initialize()");
		
    	mUserMessages = new HashMap<String, StringBuffer>();
    	
    	usersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	usersListView.getSelectionModel().getSelectedItems().addListener(
    	new ListChangeListener<String>() 
    	{
			public void onChanged(ListChangeListener.Change<? extends String> change) 
			{
				if(change.next() && change.wasAdded())
				{
					onUserSelected(change.getAddedSubList().get(0));
				}
			}
		});
	}
    
    private void onUserSelected(String selectedUser)
    {
    	mSelectedUser = selectedUser;
		textArea.setText(mUserMessages.get(mSelectedUser).toString());
    }
    
    public void setClient(IClientGUI client)
    {
    	mClient = client;
    }
    
    @FXML
    private void onSendMessage()
    {
    	if((mSelectedUser != null) && (mSelectedUser.length() > 0))
    	{
	    	String input = inputArea.getText();
	    	if(input.length() > 0)
	    	{
		    	inputArea.clear();
		    	if(input.endsWith("\n") == false)
		    	{
		    		input += "\n";
		    	}
		    	
		    	textArea.appendText("You: ");
		    	textArea.appendText(input);
		    	mUserMessages.get(mSelectedUser).append("You: ");
		    	mUserMessages.get(mSelectedUser).append(input);
		    	if(mClient != null)
		    	{
		    		mClient.sendMessage(mSelectedUser, input);
		    	}
	    	}
    	}
    }
    
    @SuppressWarnings("unused")
	@FXML
    private void onKeyReleased(KeyEvent event)
    {
    	if(event.getCode().equals(KeyCode.ENTER))
    	{
    		onSendMessage();
    	}
    }
    
    @SuppressWarnings("unused")
	@FXML
    private void onConnectToggled()
    {
    	System.out.println("MainMenuController::onConnectToggled()");
    	
    	mClient.onConnectToggled();
    }
    
    public void setConnectButtonText(String text)
    {
    	connectButton.setText(text);
    }
    
    public void clearUsersList()
    {
    	usersListView.getItems().clear();
    }
    public int getPortNumber()
    {
    	int portNumber;
    	try
    	{
    		portNumber = Integer.parseInt(portTF.getText());
    	}
    	catch(NumberFormatException e)
    	{
    		portNumber = -1;
    	}
    	return portNumber;
    }
    public String getServerName()
    {
    	return hostNameTF.getText();
    }
    public String getUserName()
    {
    	return userNameTF.getText();
    }
    
    @SuppressWarnings("unchecked")
	public void addUserToList(String name)
    {
    	usersListView.getItems().add(name);
    	mUserMessages.put(name, new StringBuffer(""));
    }
    public void removeUserFromList(String name)
    {
    	usersListView.getItems().remove(name);
    	mUserMessages.remove(name);
    	if(name.equals(mSelectedUser))
    	{
    		mSelectedUser = null;
    	}
    }
    
    public void onReceivedMessage(String from, String message)
    {
    	mUserMessages.get(from).append(from);
    	mUserMessages.get(from).append(": ");
    	mUserMessages.get(from).append(message);
    	if(from.equals(mSelectedUser))
    	{
    		textArea.appendText(from);
    		textArea.appendText(": ");
        	textArea.appendText(message);
    	}
    }
}
