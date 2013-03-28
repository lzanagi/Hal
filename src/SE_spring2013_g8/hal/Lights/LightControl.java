package SE_spring2013_g8.hal.Lights;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class LightControl extends Activity {
	static final String NICKNAME = "HAL";
	InetAddress serverAddress;
	Socket socket;
	String ipv4;
	//---all the Views---
	static TextView txtMessagesReceived;
	EditText txtMessage;
	//---thread for communicating on the socket---
	CommsThread commsThread;
	//---used for updating the UI on the main activity---
	static Handler UIupdater = new Handler() {
	};
	
	private class CreateCommThreadTask extends AsyncTask
	<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				//---create a socket---
				serverAddress =
				InetAddress.getByName("192.168.0.117");
				//--remember to change the IP address above to match your own--
				socket = new Socket(serverAddress, 500);
				commsThread = new CommsThread(socket);
				commsThread.start();
				//---sign in for the user; sends the nick name---
				sendToServer(NICKNAME);
			} catch (UnknownHostException e) {
				Log.d("Sockets", e.getLocalizedMessage());
			} catch (IOException e) {
				Log.d("Sockets", e.getLocalizedMessage());
			}
			return null;
		}
	}
	
	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface
	                .getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf
	                    .getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                System.out.println("ip1--:" + inetAddress);
	                System.out.println("ip2--:" + inetAddress.getHostAddress());

	      // for getting IPV4 format
	      if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {

	                    // return inetAddress.getHostAddress().toString();
	                    return ipv4;
	                }
	            }
	        }
	    } catch (Exception ex) {
	        Log.e("IP Address", ex.toString());
	    }
	    return null;
	}
	
	private class WriteToServerTask extends AsyncTask
	<byte[], Void, Void> {
		protected Void doInBackground(byte[]...data) {
			commsThread.write(data[0]);
			return null;
		}
	}
	private class CloseSocketTask extends AsyncTask
	<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				socket.close();
			} catch (IOException e) {
				Log.d("Sockets", e.getLocalizedMessage());
			}
			return null;
		}
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_light_control);
		//---get the views---
	}
	public void onClickSendOn(View view) {
		//---send the message 'on' to the server---
		sendToServer("Light_on");
	}
	public void onClickSendOff(View view) {
		//---send the message 'off' to the server---
		sendToServer("Light_off");
	}
	private void sendToServer(String message) {
		byte[] theByteArray =
		message.getBytes();
		new WriteToServerTask().execute(theByteArray);
	}
	@Override
	public void onResume() {
		super.onResume();
		new CreateCommThreadTask().execute();
	}
	@Override
	public void onPause() {
		super.onPause();
		new CloseSocketTask().execute();
	}
}