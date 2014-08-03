package micropolis.android;

import micropolisj.engine.Micropolis;
import micropolisj.engine.CityLocation;
import micropolisj.engine.MicropolisTool;
import micropolisj.engine.ZoneStatus;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ResourceBundle;

public class BudgetFragment extends DialogFragment
{
	Micropolis city;
	CheckBox demolishCtrl;

	BudgetFragment(Micropolis city)
	{
		this.city = city;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.budget_dlg, null);
		builder.setView(content);

		setupContent(content);

		// determine title
		builder.setTitle("Budget"); //TODO- translate

		builder.setNegativeButton(
			R.string.dismiss,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id) {
					onDismissClicked();
				}
			});

		return builder.create();
	}

	private void onDismissClicked()
	{
	}

	private void setupContent(View c)
	{
	}
}
