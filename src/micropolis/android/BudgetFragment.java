package micropolis.android;

import micropolisj.engine.BudgetNumbers;
import micropolisj.engine.Micropolis;
import micropolisj.engine.CityLocation;
import micropolisj.engine.MicropolisTool;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;;
import android.widget.TextView;
import java.util.ResourceBundle;

public class BudgetFragment extends DialogFragment
{
	Micropolis city;
	EditText taxRateCtrl;
	EditText roadFundCtrl;
	EditText policeFundCtrl;
	EditText fireFundCtrl;

	static final int MIN_TAX_RATE = 0;
	static final int MAX_TAX_RATE = 20;
	static final int MIN_PERCENT = 0;
	static final int MAX_PERCENT = 100;

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
		int newTaxRate = Integer.parseInt(taxRateCtrl.getText().toString());
		city.cityTax = Math.min(Math.max(newTaxRate, MIN_TAX_RATE), MAX_TAX_RATE);

		int newRoadPct = Integer.parseInt(roadFundCtrl.getText().toString());
		city.roadPercent = (double)Math.min(Math.max(newRoadPct, MIN_PERCENT), MAX_PERCENT) / 100.0;

		int newPolicePct = Integer.parseInt(policeFundCtrl.getText().toString());
		city.policePercent = (double)Math.min(Math.max(newPolicePct, MIN_PERCENT), MAX_PERCENT) / 100.0;

		int newFirePct = Integer.parseInt(fireFundCtrl.getText().toString());
		city.firePercent = (double)Math.min(Math.max(newFirePct, MIN_PERCENT), MAX_PERCENT) / 100.0;
	}

	private void setupContent(View c)
	{
		BudgetNumbers b = city.generateBudget();

		taxRateCtrl = (EditText) c.findViewById(R.id.budgetdlg_taxrate);
		taxRateCtrl.setText(Long.toString(b.taxRate));

		roadFundCtrl = (EditText) c.findViewById(R.id.budgetdlg_roadfund);
		roadFundCtrl.setText(Long.toString(Math.round(b.roadPercent*100.0)));

		policeFundCtrl = (EditText) c.findViewById(R.id.budgetdlg_policefund);
		policeFundCtrl.setText(Long.toString(Math.round(b.policePercent*100.0)));

		fireFundCtrl = (EditText) c.findViewById(R.id.budgetdlg_firefund);
		fireFundCtrl.setText(Long.toString(Math.round(b.firePercent*100.0)));
	}
}
