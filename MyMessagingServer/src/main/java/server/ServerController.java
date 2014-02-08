package server;

import java.io.IOException;

import main.IServerControllerGUI;
import main.MainMenuController;

import javafx.application.Platform;

public class ServerController implements main.IServerController, IServerControllerGUI, 
										server.IServerController
{
	private MainMenuController mMenuController;
	private Server mServer;
	private boolean mIsServerStarted = false;
	
	public ServerController(MainMenuController menuController) 
	{
		System.out.println("ServerController::ServerController()");
		
		mMenuController = menuController;
		mMenuController.setServerController(this);
	}
	
	public void stop()
	{
		System.out.println("ServerController::stop() mIsServerStarted " + mIsServerStarted);
		
		if(mIsServerStarted)
		{
			closeServer();
		}
	}
	
	public void onStartButtonPressed()
	{
		System.out.println("ServerController::onStartButtonPressed() mIsServerStarted " + mIsServerStarted);
		
		if(mIsServerStarted)
		{
			closeServer();
		}
		else 
		{
			startServer();
		}
	}
	
	private void startServer()
	{
		System.out.println("ServerController::startServer()");
		
		int port = mMenuController.getPortNumber();
		try 
		{
			mServer = new Server(this, port);
			mServer.start();
			mIsServerStarted = true;
			mMenuController.setStartButtonText("Stop");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void closeServer()
	{
		System.out.println("ServerController::closeServer()");
		
		mIsServerStarted = false;
		mMenuController.setStartButtonText("Start");
		mMenuController.clearUserList();
		mServer.closeServer();
	}
	
	public void onDisconnect()
	{
		stop();
	}
	
	public void onClientConnected(final String clientName)
	{
		Platform.runLater(new Runnable() 
		{
			public void run() 
			{
				mMenuController.addUser(clientName);
			}
		});
	}
	
	public void onClientDisconnected(final String clientName)
	{
		Platform.runLater(new Runnable() 
		{
			public void run() 
			{
				mMenuController.removeUser(clientName);
			}
		});
	}
}
