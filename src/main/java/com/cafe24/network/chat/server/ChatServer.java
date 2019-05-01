package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	public static final int PORT = 7000;
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		List<PrintWriter> listWriters = new ArrayList<PrintWriter>();
		
		try {
			log("start server");
			// 1. 서버 소켓 생성
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			
			// 2. 바인딩			
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			log("server starts    PORT : " + PORT);
			// 3. 요청 대기
			while(true) {
				Socket socket = serverSocket.accept();				
				new Thread(new ChatServerConnection(socket, listWriters)).start();				
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if( serverSocket != null && !serverSocket.isClosed() ) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String log) {
		System.out.println("[server# " +Thread.currentThread().getId()+"] " + log);
	}
}
