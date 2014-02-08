package main;

import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import client.Client;

public class Main extends Application
{
	private final String LAYOUTS_PATH = "../";
	private final String MAIN_MENU_PATH = LAYOUTS_PATH + "MainMenu.fxml";
	
	private Stage mStage;
	private IClient mClient;
	
	public static void main(String[] arg)
	{
		String[] args = new String[1];
		args[0] = "";
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		mStage = primaryStage;
		mStage.setTitle("MainClient");
		
		MainMenuController menuController = (MainMenuController)loadScene(MAIN_MENU_PATH);
		if(menuController != null)
		{
			mClient = new Client(menuController);
		}
	}
	
	@Override
	public void stop() throws Exception
	{
		if(mClient != null)
		{
			mClient.closeClient();
		}
	}

	private Parent loadScene(String scenePath)
	{
		Parent rootGroup = null;
		FXMLLoader loader;		
		try 
		{
			loader = new FXMLLoader();
			loader.setBuilderFactory(new JavaFXBuilderFactory());
			loader.setLocation(getClass().getResource(scenePath));
			InputStream inputStream = getClass().getResourceAsStream(scenePath);
			rootGroup = (Parent)loader.load(inputStream);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
		
		Scene scene = new Scene(rootGroup);
		mStage.setScene(scene);
		mStage.show();
		return (Parent) loader.getController();
	}
}
