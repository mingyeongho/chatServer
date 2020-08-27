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

// 서버
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
		// ThreadPool 생성
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // PoolSize = 8
		
		// ServerSocket 생성 및 바인딩
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost",9999));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		// 연결 수락 (accept()), 수락 작업 생성
		Runnable runnable = new Runnable() {
			// UI 관련
			@Override
			public void run() {
				Platform.runLater(() -> { // UI 업데이트
					displayText("[Start Server]");
					btnStartStop.setText("Stop");
				});
			
				// accept()
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[연결 수락: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]" ;
						Platform.runLater(() -> displayText(message));
						Client client = new Client(socket);
						clients.add(client);
						Platform.runLater(() -> displayText("[연결 개수: " + clients.size() + "]"));
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
			// 모든 소켓 닫기
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
		
		// 받기 작업 생성
		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							byte[] byteArr = new byte[100];
							InputStream in = socket.getInputStream();
							
							int readByteCount = in.read(byteArr); // 데이터 받기
							
							if (readByteCount == -1) {
								throw new IOException();
							}
							
							String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							
							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							
							for (Client client : clients) {
								client.send(data);
							}
							
						}
					} catch (Exception e) {
						try {
							clients.remove(Client.this);
							String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(() -> displayText(message));
							socket.close();
						} catch (Exception e1) {
							
						}
					}
				}
			};
			executorService.submit(runnable);
		}
		
		// 보내기 작업 생성
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
							String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
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
