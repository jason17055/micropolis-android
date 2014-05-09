package micropolis.android;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class InspectLocationFragment extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.inspect_location_dlg, null);
		builder.setView(content);

		builder.setTitle(R.string.inspect_location_caption);
		builder.setNegativeButton(
			R.string.dismiss,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id) {
				}
			});

		return builder.create();
	}
}
