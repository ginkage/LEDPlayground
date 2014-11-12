package com.ginkage.ledplayground;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
	private String serverMessage;
	public static final String SERVERIP = "10.10.100.254"; //your computer IP address
	public static final int SERVERPORT = 8899;
	private OnMessageReceived mMessageListener = null;
	private boolean mRun = false;

	private BufferedWriter out = null;
	private BufferedReader in = null;

	/**
	 *  Constructor of the class. OnMessagedReceived listens for the messages received from server
	 */
	public TCPClient(OnMessageReceived listener) {
		mMessageListener = listener;
	}

	/**
	 * Sends the message entered by client to the server
	 * @param msg text entered by client
	 */
	public void sendMessage(int[] msg) {
		if (out != null) {
			try {
				for (int i = 0; i < msg.length; i++)
					out.write(msg[i]);
				out.flush();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopClient() {
		mRun = false;
	}

	public boolean isConnected() {
		return (in != null);
	}

	public void run() {
		mRun = true;

		try {
			//here you must put your computer's IP address.
			InetAddress serverAddr = InetAddress.getByName(SERVERIP);

			Log.e("TCP Client", "C: Connecting...");

			//create a socket to make the connection with the server
			Socket socket = new Socket(serverAddr, SERVERPORT);

			try {
				//send the message to the server
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				Log.e("TCP Client", "C: Sent.");
				Log.e("TCP Client", "C: Done.");

				//receive the message which the server sends back
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				//in this while the client listens for the messages sent by the server
				while (mRun) {
					serverMessage = in.readLine();

					if (serverMessage != null && mMessageListener != null) {
						//call the method messageReceived from MyActivity class
						mMessageListener.messageReceived(serverMessage);
					}
					serverMessage = null;
				}

				Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
			}
			catch (Exception e) {
				Log.e("TCP", "S: Error", e);
				in = null;
				out = null;
				mRun = false;
				if (mMessageListener != null)
					mMessageListener.messageReceived(e.getMessage());
			}
			finally {
				//the socket must be closed. It is not possible to reconnect to this socket
				// after it is closed, which means a new socket instance has to be created.
				socket.close();
			}
		}
		catch (Exception e) {
			Log.e("TCP", "C: Error", e);
			in = null;
			out = null;
			mRun = false;
			if (mMessageListener != null)
				mMessageListener.messageReceived(e.getMessage());
		}
	}

	//Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
	//class at on asynckTask doInBackground
	public interface OnMessageReceived {
		public void messageReceived(String message);
	}
}