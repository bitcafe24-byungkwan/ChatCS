package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ChatServerConnection implements Runnable {
	private String _nickName = null;
	private Socket _sock = null;
	List<PrintWriter> listWriters = null;
	private static HashSet<String> _nickList = new HashSet<>();

	public ChatServerConnection(Socket socket, List<PrintWriter> listWriters) {
		this._sock = socket;
		this.listWriters = listWriters;
	}

	@Override
	public void run() {
		try {
			// 4. IO 스트림 받아오기
			InputStream is = _sock.getInputStream();
			OutputStream os = _sock.getOutputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(os, "utf-8"), true);

			while (true) {
				String request = br.readLine();

				if (request == null) {					
					doQuit(pr);
					break;
				}

				String[] tokens = request.split(":");
				if (tokens.length < 2) {
					if ("quit".equals(tokens[0])) {
						doQuit(pr);
						break;
					}
					continue;
				}
				if ("join".equals(tokens[0])) {
					if (!doJoin(request.substring(5), pr)) {
						pr.println("DUPLICATE");
						break;
					}
				} else if ("message".equals(tokens[0])) {
					int start = request.indexOf(":") + 1;
					doMessage(request.substring(start));
				}

			}
		} catch (IOException e) {
			ChatServer.log(_nickName + "IOException occured");
			_nickList.remove(_nickName);
		}
	}

	private void doQuit(PrintWriter writer) {
		removeWriter(writer);

		String data = this._nickName + "님이 퇴장했습니다.";
		ChatServer.log("disconnected " + _nickName);
		_nickList.remove(_nickName);
		broadcast(data);
	}

	private void removeWriter(PrintWriter writer) {
		synchronized (listWriters) {
			listWriters.remove(writer);
		}
	}

	private void doMessage(String data) {
		broadcast(this._nickName + ":" + data);
	}

	private Boolean doJoin(String nickName, PrintWriter writer) {
		this._nickName = nickName;
		if (_nickList.contains(nickName))
			return false;
		if (!_nickList.add(nickName))
			return false;

		
		String data = nickName + "님이 입장하였습니다.";
		ChatServer.log("Join     " + nickName);
		writer.println("SUCCESS");
		broadcast(data);

		// writer pool에 저장
		addWriter(writer);
		return true;
	}

	private void addWriter(PrintWriter writer) {
		synchronized (listWriters) {
			listWriters.add(writer);
		}
	}

	private void broadcast(String data) {
		synchronized (listWriters) {
			for (PrintWriter writer : listWriters) {
				writer.println(data);
				writer.flush();
			}
		}
	}

}
