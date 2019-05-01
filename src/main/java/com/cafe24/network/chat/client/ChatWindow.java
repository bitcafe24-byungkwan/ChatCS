package com.cafe24.network.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private PrintWriter _pw;
	private Socket _sock;
	public ChatWindow(String name, Socket sock) throws UnsupportedEncodingException, IOException {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		_sock = sock;
		_pw = new PrintWriter(new OutputStreamWriter(_sock.getOutputStream(), "utf-8"), true);
		new Thread(new ChatClientReceiveThread(sock)).start();
	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});

		
		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {				
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
					sendMessage();
			}
		});
		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
	}
	private void finish() {
		//소켓 정리
		try {			
			if (_sock != null && !_sock.isClosed())
				_sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	private void updateTextArea(String message) {
		textArea.append(message);
		textArea.append("\n");
	}
	
	private void sendMessage() {		
		
		if (textField.getText().equals("quit"))	{
			_pw.println("quit:");
			finish();
		}		
		String msg = "message:" + textField.getText() + "\r\n";
		_pw.println(msg);
		
		textField.setText("");
		textField.requestFocus();
	}
	
	private class ChatClientReceiveThread implements Runnable{
		Socket _sock = null;
		
		ChatClientReceiveThread(Socket socket){
			_sock = socket;
		}
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "utf-8"));
				while(true) {
					String msg = br.readLine();
					updateTextArea(msg);
				}
			}
			catch (SocketException e) {
				System.out.println("SocketException Occurd");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
