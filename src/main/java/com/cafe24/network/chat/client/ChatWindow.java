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
	private void updateTextArea(String msg) {
		
		if(msg.length()>5)
		{			
			if(msg.substring(0,5).equals("whis:"))
			{
				String[] tokens = msg.split(":");
				if(tokens[1].equals(frame.getTitle())) {
					int index = msg.indexOf("[whis]",5+tokens[1].length());
					textArea.append(msg.substring(index));
					textArea.append("\n");
				} 
				return;				
			}
		}
		
		String[] tokens = msg.split(":");
		if (tokens[0].equals(frame.getTitle()))
		{
			msg = "[ME]" + msg.substring(msg.indexOf(":")+1);
		}
		textArea.append(msg);
		textArea.append("\n");
	}
	
	private void sendMessage() {		
		
		if (textField.getText().equals("quit"))	{
			_pw.println("quit:");
			finish();
		};
		
		//귓속말
		// /whis:nickname:ma
		String msg;
		if(textField.getText().length()>5)
		{			
			if (textField.getText().substring(0,6).equals("/whis:"))
			{

				int indexMsg = textField.getText().indexOf(":", 6);
				if (indexMsg > 0) {
					updateTextArea("try to:" + textField.getText().substring(6,indexMsg)
							+ "-->" + textField.getText().substring(indexMsg+1));
					msg = "whis:" + frame.getTitle() + ":" 
				+ textField.getText().substring(6,indexMsg)
				+":"+textField.getText().substring(indexMsg+1)
				+ "\r\n";
					_pw.println(msg);
					
					

					textField.setText("");
					textField.requestFocus();
					return;
				}
			}
		}

		msg = "message:" + textField.getText() + "\r\n";		

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
