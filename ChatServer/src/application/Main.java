package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// ����
public class Main extends Application {
	
	// ThreadPool
	ExecutorService executorService;
	
	// Accept Connection
	ServerSocket serverSocket;
	
	// add Client
	Vector<Client> clients = new Vector<Client>();
	
	// startServer()
	// threadPool, serverSocket, accept Connection
	void startServer() {
		// ThreadPool ����
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // PoolSize = 8
		
		// ServerSocket ���� �� ���ε�
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost",9999));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		// ���� ���� (accept()), ���� �۾� ����
		Runnable runnable = new Runnable() {
			// UI ����
			@Override
			public void run() {
				Platform.runLater(() -> { // UI ������Ʈ
					displayText("[Start Server]");
					btnStartStop.setText("Stop");
				});
			
				// accept()
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[���� ����: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]" ;
						Platform.runLater(() -> displayText(message));
						Client client = new Client(socket);
						clients.add(client);
						Platform.runLater(() -> displayText("[���� ����: " + clients.size() + "]"));
					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	}
	
	// stopServer()
	void stopServer() {
		try {
			// ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// serverSocket close
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//executorService close
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			Platform.runLater(() -> {
				displayText("[Stop Server]");
				btnStartStop.setText("Start");
			});
		} catch (Exception e) {
			
		}
	}
	
	// Client class
	class Client {
		Socket socket;
		
		Client(Socket socket) {
			this.socket = socket;
			receive();
		}
		
		// �ޱ� �۾� ����
		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							byte[] byteArr = new byte[100];
							InputStream in = socket.getInputStream();
							
							int readByteCount = in.read(byteArr); // ������ �ޱ�
							
							if (readByteCount == -1) {
								throw new IOException();
							}
							
							String message = "[��û ó��: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							
							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							
							for (Client client : clients) {
								client.send(data);
							}
							
						}
					} catch (Exception e) {
						try {
							clients.remove(Client.this);
							String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							socket.close();
						} catch (Exception e1) {
							
						}
					}
				}
			};
			executorService.submit(runnable);
		}
		
		// ������ �۾� ����
		void send(String data) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						byte[] byteArr = data.getBytes("UTF-8");
						OutputStream out = socket.getOutputStream();
						out.write(byteArr);
						out.flush();
					} catch (Exception e) {
						try {
							String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							clients.remove(Client.this);
							socket.close();
						} catch (Exception e1) {
							
						}
					}
					
				}
			};
			executorService.submit(runnable);
		}
	}
	
	// UI
	TextArea txtDisplay;
	Button btnStartStop;
	@Override
	public void start(Stage primaryStage) throws Exception{
		
		BorderPane root = new BorderPane();
		root.setPrefSize(500, 300);
		
		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		BorderPane.setMargin(txtDisplay, new Insets(0, 0, 2, 0));
		root.setCenter(txtDisplay);
		
		btnStartStop = new Button("Start");
		btnStartStop.setPrefHeight(30);
		btnStartStop.setMaxWidth(Double.MAX_VALUE);
		btnStartStop.setOnAction(e -> {
			if (btnStartStop.getText().equalsIgnoreCase("start")) {
				startServer();
			} else if (btnStartStop.getText().equalsIgnoreCase("stop")) {
				stopServer();
			}
		});
		root.setBottom(btnStartStop);
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Server");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.show();
	}
	
	void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
