package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// 서버
public class Main extends Application {
	
	// ThreadPool
	ExecutorService executorService;
	
	// ServerSocket
	ServerSocket serverSocket;
	
	// Vector<Client>
	Vector<Client> clients = new Vector<Client>();
	
	// startServer()
	// threadPool, serverSocket, accept Connection
	void startServer() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost",9990));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// 람다식 + Platform
				Platform.runLater(() -> {
					
				});
				
			}
		}
	}
	
	// stopServer()
	void stopServer() {
		
	}
	
	// Client class
	class Client {
		
	}
	
	// UI
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
