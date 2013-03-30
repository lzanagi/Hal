package SE_spring2013_g8.hal.Surveillance;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerActivity extends Activity {

    private TextView serverStatus;
    
    public static String SERVERIP = "10.0.2.15"; // default ip
    public static final int SERVERPORT = 8080; // designate a port
    private Handler handler = new Handler();
    private ServerSocket serverSocket;
    MediaPlayer mMediaPlayer;
    ImageView incomingImages;
    Bitmap incomingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surveillance_server_activity);
        serverStatus = (TextView) findViewById(R.id.server_status);
        mMediaPlayer = new MediaPlayer();    
        SERVERIP = getLocalIpAddress();

        //create view to hold images being sent from client
        incomingImages = (ImageView) findViewById(R.id.incoming_images);
        
        
    	//start server thread
        Thread fst = new Thread(new ServerThread());
        fst.start();
        

    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // listen for incoming clients
                        Socket client = serverSocket.accept();
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                serverStatus.setText("Connected.");
                            }
                        });

                        try {
                        	DataInputStream in = new DataInputStream(client.getInputStream());
	                        while (true) {
	                        	final int callbackImageWidth = in.readInt();
	                        	final int callbackImageHeight = in.readInt();
	                        	final int imageArrayLength = in.readInt();
	                        	byte[] imageArray = new byte[imageArrayLength];
	                        	in.readFully(imageArray);
	                        	final byte[] finalImageArray = imageArray;
	                        	Log.d("ServerActivity", "imageArray Received!");                        	
                            	
	                        	//toasts to make sure the byte array is being sent correctly
	                        	/*
	                        	handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(), arrayDataToString(finalImageArray), Toast.LENGTH_SHORT).show();
                                        Toast.makeText(getBaseContext(), "Width=" + callbackImageWidth + "Height=" + callbackImageHeight, Toast.LENGTH_SHORT).show();
                                    }
                                });*/                            	

                            	// Convert the array of bytes stored as NV21 to a bitmap
                            	incomingImage = getBitmapFromNV21(callbackImageWidth, callbackImageHeight, imageArray);
                            	
                            	// update the image!
                            	handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        incomingImages.setImageBitmap(incomingImage);                                        
                                    }
                                }); 
                            	
                            }
	                        //break;
                            
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            }
        }
    }
    
    private String arrayDataToString (byte[] byteArray) {
    	String arrayData = "";
    	arrayData = arrayData + "Array Length = " + byteArray.length;
    	arrayData = arrayData + "  **  " + "First Element = " + byteArray[0];
    	arrayData = arrayData + "  **  " + "Second Element = " + byteArray[1];
    	arrayData = arrayData + "  **  " + "Last Element = " + byteArray[byteArray.length-1];
    	return arrayData;
    }
    
    // gets the ip address of your phone's network
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
             // make sure you close the socket upon exiting
             serverSocket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    
    private Bitmap getBitmapFromNV21 (int callbackImageWidth, int callbackImageHeight, byte[] imageArray) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuvImage = new YuvImage(imageArray, ImageFormat.NV21, callbackImageWidth, callbackImageHeight, null);
		yuvImage.compressToJpeg(new Rect(0, 0, callbackImageWidth, callbackImageHeight), 50, out);
		byte[] imageBytes = out.toByteArray();
		Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		return image;
    }
}