package micropolis.android;

import micropolisj.engine.ZoneStatus;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.util.ResourceBundle;

public class InspectLocationFragment extends DialogFragment
{
	ZoneStatus zone;

	InspectLocationFragment(ZoneStatus zone)
	{
		this.zone = zone;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.inspect_location_dlg, null);
		builder.setView(content);

		setupContent(content);

		builder.setTitle(R.string.inspect_location_caption);
		builder.setNegativeButton(
			R.string.dismiss,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id) {
				}
			});

		return builder.create();
	}

	static ResourceBundle s_strings = ResourceBundle.getBundle("micropolisj.StatusMessages");

	private void setupContent(View c)
	{
		String buildingStr = zone.building != -1 ? s_strings.getString("zone."+zone.building) : "";
		String popDensityStr = s_strings.getString("status."+zone.popDensity);
		String landValueStr = s_strings.getString("status."+zone.landValue);
		String crimeLevelStr = s_strings.getString("status."+zone.crimeLevel);
		String pollutionStr = s_strings.getString("status."+zone.pollution);
		String growthRateStr = s_strings.getString("status."+zone.growthRate);

		setLabel(c, R.id.inspectdlg_zone_lbl, buildingStr);
		setLabel(c, R.id.inspectdlg_density_lbl, popDensityStr);
		setLabel(c, R.id.inspectdlg_value_lbl, landValueStr);
		setLabel(c, R.id.inspectdlg_crime_lbl, crimeLevelStr);
		setLabel(c, R.id.inspectdlg_pollution_lbl, pollutionStr);
		setLabel(c, R.id.inspectdlg_growth_lbl, growthRateStr);
	}

	private void setLabel(View c, int lbl_id, String s)
	{
		TextView v = (TextView) c.findViewById(lbl_id);
		v.setText(s);
	}
}
