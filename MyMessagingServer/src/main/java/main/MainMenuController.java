package main;

import java.net.URL;
import java.util.ResourceBundle;

import server.ServerController;

import com.sun.javafx.collections.ObservableListWrapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class MainMenuController extends Parent implements Initializable
{
    private ResourceBundle mResources;
    private URL mLocation;
	
    @FXML
	private TextField portTF;
    @FXML
    private Button startButton;
    @FXML
    private ListView<String> usersList;
    
    private IServerControllerGUI mServerController;
    
    private ObservableList<String> mUserList;
    
    @FXML
	public void initialize(URL location, ResourceBundle resources) 
	{
    	System.out.println("MainMenuController::initialize()");
		
    	mResources = resources;
    	mLocation = location;
    	
    	mUserList = FXCollections.observableArrayList();
    	usersList.setItems(mUserList);
	}
    
    public void setServerController(IServerControllerGUI serverController)
    {
    	mServerController = serverController;
    }
    
    @FXML
    private void onStart()
    {
		if(mServerController != null)
		{
			System.out.println("MainMenuController::onStart()");
			
			mServerController.onStartButtonPressed();
		}
    }
    
    public void addUser(String name)
    {
    	usersList.getItems().add(name);
    }
    
    public void removeUser(String name)
    {
    	usersList.getItems().remove(name);
    }
    
    public void clearUserList()
    {
    	usersList.getItems().clear();
    }
    
    public void setStartButtonText(String text)
    {
    	startButton.setText(text);
    }
    
    public int getPortNumber()
    {
    	int portNumber;
    	try 
    	{
        	portNumber = Integer.parseInt(portTF.getText());
		} 
    	catch (NumberFormatException e) 
		{
    		portNumber = -1;
		}
    	return portNumber;
    }
}
