package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {
	private static final String SERVER_IP = "192.168.1.63";
	private static final int SERVER_PORT = 7000;
	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		Socket sock;
		try {
			while (true) {

				System.out.println("대화명을 입력하세요.");
				System.out.print(">>> ");
				name = scanner.nextLine();

				if (name.isEmpty() == false) {
					if ("quit".equals(name)) {
						break;
					}
					sock = new Socket();
					sock.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
					PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "utf-8"), true);
					String request = "join:" + name + "\r\n";
					pw.println(request);
					BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream(), "utf-8"));
					String msg = br.readLine();
					if ("SUCCESS".equals(msg))
					{
						new ChatWindow(name, sock).show();
						break;
					}
					else if("DUPLICATE".equals(msg))
					{
						System.out.println("중복된 대화명입니다.\n");
						sock.close();
						continue;
					}
					else
					{
						System.out.println("Unexpected Error.\n");
					}
				} 
				System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		//1. 소켓 만들고
		//2. iostream
		//3. join 프로토콜
		
	}

}
