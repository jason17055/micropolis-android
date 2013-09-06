package micropolis.android;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import micropolisj.engine.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.action_build_roads:
			doRoadTool();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void doRoadTool()
	{
		MicropolisView view = (MicropolisView) findViewById(R.id.main_view);
		view.setTool(MicropolisTool.ROADS);
	}
}
