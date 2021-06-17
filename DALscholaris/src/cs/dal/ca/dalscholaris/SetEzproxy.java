package cs.dal.ca.dalscholaris;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;

public class SetEzproxy extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private CheckBoxPreference enableoffcampus;
	private EditTextPreference ezproxyurl;
	private EditTextPreference netid;
	private EditTextPreference netidpass;

	/** Populate the advanced search options with pre user-defined values */
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		addPreferencesFromResource(R.xml.ezproxy);

		enableoffcampus = (CheckBoxPreference)getPreferenceScreen().findPreference("enableOffCampus");
		ezproxyurl = (EditTextPreference)getPreferenceScreen().findPreference("ezproxyURL");
		netid = (EditTextPreference)getPreferenceScreen().findPreference("netID");
		netidpass = (EditTextPreference)getPreferenceScreen().findPreference("netIDpass");		
	}
	
	/** When brought to the foreground, display selected values as summary */
	@Override     
	protected void onResume() {         
		super.onResume();          
		// Setup the initial values         
		//enableoffcampus.setSummary(enableoffcampus.getText()); 
		ezproxyurl.setSummary(ezproxyurl.getText());
		netid.setSummary(netid.getText());
		//netidpass.setSummary(netidpass.getText());

		if(enableoffcampus.isChecked()) {
			ezproxyurl.setEnabled(true);
			netid.setEnabled(true);
			netidpass.setEnabled(true);
		}
		else {
			ezproxyurl.setEnabled(false);
			netid.setEnabled(false);
			netidpass.setEnabled(false);
		}
		
		// Set up a listener whenever a key changes                     
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);     
	}


	@Override     
	protected void onPause() {         
		super.onPause();          
		// Unregister the listener whenever a key changes                     
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);         

	}
	
	/** Menu layout defined in /res/menu/prefernce_menu.xml */
	//not necessary for setezproxy
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.preference_menu,menu);
		return true;
	}*/

	/** Whenever the user changes an option, it'll be displayed as a summary */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {     
		 if (key.equalsIgnoreCase("enableOffCampus")) {
			 if(enableoffcampus.isChecked()) {
				 //enableoffcampus.setEnabled(true);
				 //enableoffcampus.setSummary(enableoffcampus.getText());
				 ezproxyurl.setEnabled(true);
				 ezproxyurl.setSummary(ezproxyurl.getText());
				 netid.setEnabled(true);
				 netid.setSummary(netid.getText());
				 netidpass.setEnabled(true);			 
			 } else {
				 //enableoffcampus.setEnabled(false);
				 ezproxyurl.setEnabled(false);
				 netid.setEnabled(false);
				 netidpass.setEnabled(false);
			 }
		 } else if (key.equalsIgnoreCase("ezproxyURL")) {
			 ezproxyurl.setSummary(ezproxyurl.getText());
		 } else if (key.equalsIgnoreCase("netID")) {
			 netid.setSummary(netid.getText());
		 } 
		 
	}	
	
}
