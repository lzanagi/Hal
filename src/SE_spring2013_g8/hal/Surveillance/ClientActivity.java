package SE_spring2013_g8.hal.Surveillance;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ClientActivity extends Activity {

    //private EditText serverIp;
	private String serverIp = "192.168.1.148";
    private Button connectPhones;
    private String serverIpAddress = "";
    private boolean connected = false;
    private Handler handler = new Handler();
    Socket socket;
    
	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	String TAG = "MainActivity";
	private int mCameraIndex = 0;
	private boolean isRecording = false;
	private Button captureButton;
	byte[] callbackImage;
	int callbackImageWidth = 0;
	int callbackImageHeight = 0;
	
    PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
        	callbackImage = data;
        }
    };    
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.surveillance_client_activity);
        
        //serverIp = (EditText) findViewById(R.id.server_ip);
        connectPhones = (Button) findViewById(R.id.connect_phones);
        connectPhones.setOnClickListener(connectListener);
        
        // Create our Preview view and set it as the content of our activity.
        mCamera = openFrontFacingCameraGingerbread();	    
        mPreview = new CameraPreview(this, mCamera, previewCallback);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
	    
		/*
        // Add a listener to the Capture button
		final Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
		    new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		            if (isRecording) {
		            	// stop recording and release camera
		                mMediaRecorder.stop();  // stop the recording
		                releaseMediaRecorder(); // release the MediaRecorder object
		                mCamera.lock();         // take camera access back from MediaRecorder
		                
		                // inform the user that recording has stopped
		                captureButton.setText("Start video capture storage");
		                isRecording = false;
		            } else {
		                // initialize video camera
		                if (prepareVideoRecorder()) {
		                    // Camera is available and unlocked, MediaRecorder is prepared,
		                    // now you can start recording
		                    mMediaRecorder.start();

		                    // inform the user that recording has started
		                    captureButton.setText("Stop video capture storage");
		                    isRecording = true;
		                } else {
		                    // prepare didn't work, release the camera
		                    releaseMediaRecorder();
		                    // inform user
		                }
		            }
		        }
		    }
		); */
    }
    
    private OnClickListener connectListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!connected) {
                serverIpAddress = serverIp;
                if (!serverIpAddress.equals("")) {
                    Thread cThread = new Thread(new ClientThread());
                    cThread.start();
                }
            }
        }
    };

    public class ClientThread implements Runnable {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                socket = new Socket(serverAddr, ServerActivity.SERVERPORT);
                connected = true;
                
                while (connected) {
                    try {
                    	Thread.sleep(200);
                    	Log.d("ClientActivity", "C: Sending command.");
                    	DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        final byte[] imageToSend = callbackImage;

                        // Toast to see if the byte array containing the image is created properly
                        /*handler.post(new Runnable() {
                            @Override
                            public void run() {
                            	Toast.makeText(getBaseContext(), arrayDataToString(imageToSend), Toast.LENGTH_LONG).show();
                            }
                        });*/

                        int callbackImageWidth =  mCamera.getParameters().getPreviewSize().width;
                    	int callbackImageHeight =  mCamera.getParameters().getPreviewSize().height;
                        
                    	out.writeInt(callbackImageWidth);
                    	out.writeInt(callbackImageHeight);
                        out.writeInt(imageToSend.length);
                        out.write(imageToSend);
                        
                        Log.i("ClientActivity", "C: Sent.");
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
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
    
	public void setCaptureButtonText(String s) {
	    captureButton.setText(s);
	}
	

	private boolean prepareVideoRecorder(){

	    //mCamera = openFrontFacingCameraGingerbread();
	    mMediaRecorder = new MediaRecorder();

	    // Step 1: Unlock and set camera to MediaRecorder
	    mCamera.unlock();
	    mMediaRecorder.setCamera(mCamera);

	    // Step 2: Set sources
	    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

	    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
	    //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
	    mMediaRecorder.setProfile(CamcorderProfile.get(mCameraIndex, CamcorderProfile.QUALITY_LOW));

	    // Step 4: Set output file stored as file
	    //mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
	    
	    // Step 4: Set output file for broadcast
	    mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
	    Toast.makeText(getBaseContext(), "File Output location:" + getOutputMediaFile(MEDIA_TYPE_VIDEO).toString(), Toast.LENGTH_LONG).show();
	    
	    //perhaps change this to a way that stores a different video format?
	    //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
					        
	    // Step 5: Set the preview output
	    mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

	    // Step 6: Prepare configured MediaRecorder
	    try {
	        mMediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
	        releaseMediaRecorder();
	        return false;
	    }
	    return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private Camera openFrontFacingCameraGingerbread() {
		String TAG = "openFrontFacingCameraGingerbread";
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo );
			if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
				mCameraIndex = camIdx;
				try {
					cam = Camera.open( camIdx );
				} catch (RuntimeException e) {
					Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}
		
		return cam;
	}
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    
	    if (mediaFile.exists()) {
	        // Set to Readable and MODE_WORLD_READABLE
	        mediaFile.setReadable(true, false);
	    }
	    
	    return mediaFile;
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
