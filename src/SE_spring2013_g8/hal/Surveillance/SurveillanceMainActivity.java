package SE_spring2013_g8.hal.Surveillance;

import SE_spring2013_g8.hal.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class SurveillanceMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.surveillance_activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickClient(View view) {
		startActivity(new Intent(getBaseContext(), ClientActivity.class));
	}
	
	public void onClickServer(View view) {
		startActivity(new Intent(getBaseContext(), ServerActivity.class));
	}

}
