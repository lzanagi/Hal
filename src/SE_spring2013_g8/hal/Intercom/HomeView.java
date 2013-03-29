package SE_spring2013_g8.hal.Intercom;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeView extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.intercom_homeview);
	
	}
	
	
	public void gotoBroadcastView(View view) {
	   
		Intent intent = new Intent(this, BroadcastView.class);
	    startActivity(intent);
	}
	
	
	public void gotoP2PView(View view) {
		Intent intent = new Intent(this, P2PView.class);
	    startActivity(intent);
	}
	
	
	public void gotoConferenceView(View view) {
		
		Intent intent = new Intent(this, ConferenceView.class);
	    startActivity(intent);
	}
}
