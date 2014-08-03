package micropolis.android;

import micropolisj.engine.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.*;

public class NewCityActivity extends Activity
{
	Micropolis curCity;
	ArrayList<Micropolis> previousCities = new ArrayList<Micropolis>();
	ArrayList<Micropolis> futureCities = new ArrayList<Micropolis>();

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

		generateCity();

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
		try
		{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		curCity.save(bytes);

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_SIM_DATA, bytes.toByteArray());
		startActivity(intent);
		finish();
		}
		catch (IOException e)
		{
			// unexpected for writing to a byte array
			throw new RuntimeException(e);
		}
	}

	void generateCity()
	{
		curCity = new Micropolis();
		new MapGenerator(curCity).generateNewCity();
		curCity.setFunds(20000);
	}

	public void nextMapClicked(View btn)
	{
		MicropolisView v = getNewCityPreview();
		previousCities.add(v.getCity());

		if (futureCities.isEmpty()) {
			generateCity();
		}
		else {
			curCity = futureCities.remove(futureCities.size()-1);
		}
		v.setCity(curCity);
	}

	public void previousMapClicked(View btn)
	{
		MicropolisView v = getNewCityPreview();
		futureCities.add(v.getCity());

		if (previousCities.isEmpty()) {
			generateCity();
		}
		else {
			curCity = previousCities.remove(previousCities.size()-1);
		}
		v.setCity(curCity);
	}
}
