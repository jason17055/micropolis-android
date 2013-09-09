package micropolis.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
	}

	/** Called when the user clicks the Start New City button. */
	public void startNewCity(View btn)
	{
		Intent intent = new Intent(this, NewCityActivity.class);
		startActivity(intent);
	}
}
