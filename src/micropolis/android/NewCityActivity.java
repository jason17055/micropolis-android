package micropolis.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class NewCityActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_city_activity);

		Spinner levelSpinner = (Spinner)
			findViewById(R.id.level_spinner);
		ArrayAdapter<CharSequence> adapter =
			ArrayAdapter.createFromResource(this,
				R.array.levels_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		levelSpinner.setAdapter(adapter);
	}

	public void playThisMapClicked(View btn)
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
