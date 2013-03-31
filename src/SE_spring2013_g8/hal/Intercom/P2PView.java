package SE_spring2013_g8.hal.Intercom;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class P2PView extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intercom_p2p);
		
		}

	public void gotoHome(View view) {
		Intent intent = new Intent(this, HomeView.class);
	    startActivity(intent);
	}
	
	public void gotoConnCall(View view) {
		Intent intent = new Intent(this, ConnCallView.class);
	    startActivity(intent);
	}
	
	public void gotoDail(View view) {
		Intent intent = new Intent(this, DailView.class);
	    startActivity(intent);
	}
	
}
