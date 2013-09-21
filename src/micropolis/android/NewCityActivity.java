package micropolis.android;

import micropolisj.engine.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class NewCityActivity extends Activity
{
	Micropolis curCity;

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

		curCity = new Micropolis();
		new MapGenerator(curCity).generateNewCity();

		MicropolisView v = getNewCityPreview();
		v.setCity(curCity);
		v.setTileSize(8);
		v.allowTouchMotion = false;
		v.scaleFactor = 0.5f;
	}

	MicropolisView getNewCityPreview()
	{
		return (MicropolisView) findViewById(R.id.new_city_preview);
	}

	public void playThisMapClicked(View btn)
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
