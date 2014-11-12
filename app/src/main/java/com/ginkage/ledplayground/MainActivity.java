package com.ginkage.ledplayground;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;

public class MainActivity extends Activity {
	public static final String SSID = "\"EASYCOLOR\"";
	private SeekBar seekBar = null;
	private TextView statusText = null;
	private TCPClient mTcpClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		seekBar = (SeekBar) findViewById(R.id.color_picker);
		if (seekBar != null)
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					setColor(progress + 1);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			});

		statusText = (TextView) findViewById(R.id.status);
		openSocket();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeSocket();
	}

	public class connectTask extends AsyncTask<String,String,TCPClient> {
		@Override
		protected TCPClient doInBackground(String... message) {
			//we create a TCPClient object and
			mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
				@Override
				//here the messageReceived method is implemented
				public void messageReceived(String message) {
					//this method calls the onProgressUpdate
					publishProgress(message);
				}
			});
			mTcpClient.run();

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			statusText.setText(values[0]);
		}
	}

	private void openSocket() {
		if (mTcpClient == null || !mTcpClient.isConnected()) {
			statusText.setText("Connecting...");
			new connectTask().execute("");
		}
	}

	private void closeSocket() {
		if (mTcpClient != null) {
			mTcpClient.stopClient();
			mTcpClient = null;
		}
	}

	private void sendPacket(int[] msg) {
		if (msg.length == 12) {
			String text = String.format("0x%02X, 0x%02X, 0x%02X, 0x%02X", msg[6], msg[7], msg[8], msg[9]);
			statusText.setText(text);
		}

		if (mTcpClient != null && mTcpClient.isConnected())
			mTcpClient.sendMessage(msg);
	}

	private void sendMessage(int[] msg) {
		if (msg.length == 1)
			sendPacket(msg);
		else if (msg.length == 4) {
			int[] newMsg = new int[] { 0x55, 0x34, 0x33, 0x39, 0x02, 0x00, 0, 0, 0, 0, 0xAA, 0xAA };
			newMsg[6] = msg[0];
			newMsg[7] = msg[1];
			newMsg[8] = msg[2];
			newMsg[9] = msg[3];
			sendPacket(newMsg);
		}
	}

	private void setColor(int color)
	{
		if (color >= 1 && color <= 96) {
			int[] msg = new int[] { 0x01, 0x01, color, color + 4 };
			if (seekBar != null && seekBar.getProgress() != color - 1)
				seekBar.setProgress(color - 1);
			sendMessage(msg);
		}
	}

	public void onClickToggle(View view) {
		int[] msg = null;

		switch (view.getId()) {
			case R.id.toggle_off:   msg = new int[] { 0x02, 0x12, 0xA9, 0xBF }; break;
			case R.id.toggle_on:    msg = new int[] { 0x02, 0x12, 0xAB, 0xC1 }; break;
		}

		if (msg != null)
			sendMessage(msg);
	}

	public void onClickRGBW(View view) {
		int[] msg = null;

		switch (view.getId()) {
			case R.id.red_on:       msg = new int[] { 0x02, 0x02, 0x81, 0x87 }; break;
			case R.id.red_off:      msg = new int[] { 0x02, 0x02, 0x82, 0x88 }; break;
			case R.id.green_on:     msg = new int[] { 0x02, 0x03, 0x84, 0x8B }; break;
			case R.id.green_off:    msg = new int[] { 0x02, 0x03, 0x85, 0x8C }; break;
			case R.id.blue_on:      msg = new int[] { 0x02, 0x04, 0x87, 0x8F }; break;
			case R.id.blue_off:     msg = new int[] { 0x02, 0x04, 0x88, 0x90 }; break;
			case R.id.white_off:    msg = new int[] { 0x02, 0x05, 0x8A, 0x93 }; break;
			case R.id.white_on:     msg = new int[] { 0x02, 0x05, 0x8B, 0x94 }; break;
		}

		if (msg != null)
			sendMessage(msg);
	}

	public void onClickColor(View view) {
		switch (view.getId()) {
			case R.id.cyan:     setColor(0x02); break;
			case R.id.green:    setColor(0x0D); break;
			case R.id.yellow:   setColor(0x1E); break;
			case R.id.red:      setColor(0x2C); break;
			case R.id.magenta:  setColor(0x3C); break;
			case R.id.blue:     setColor(0x4C); break;
		}
	}
}
