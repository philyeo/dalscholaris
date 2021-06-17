/** 
 * ***********************************************
 * File		   - SetPrefsActivity.java
 * Description - Preferences/Advanced search activity
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class SetAppSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private CheckBoxPreference showAuthorPref;
	private CheckBoxPreference showSummaryPref;
	private CheckBoxPreference showCitationPref;
	private EditTextPreference downloadPathPref;
	

	//private boolean D = false;

	/** Populate the advanced search options with pre user-defined values */
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.appsettings);
		
		downloadPathPref = (EditTextPreference)getPreferenceScreen().findPreference("downloadPath");
		
	}

	/** Whenever the user changes an option, it'll be displayed as a summary */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {     
		//if(D) Log.d("MyApp","Changed="+key);
		if (key.equalsIgnoreCase("downloadPath")) {
			String PATH = Environment.getExternalStorageDirectory()+downloadPathPref.getText();
			File filelocation = new File(PATH);
			//if(D) Log.d("MyApp","Folder="+filelocation.toString());

			if(checkValidFoldername(filelocation)) {
				//if(D) Log.d("MyApp","Folderyes="+filelocation.toString());
				if (!filelocation.exists()) 
					filelocation.mkdirs();
				downloadPathPref.setSummary(downloadPathPref.getText());
			}
			else {
				Toast.makeText(SetAppSettings.this, "Invalid folder name.", Toast.LENGTH_LONG).show();
			}
		}
		
		
	} 

	/** When brought to the foreground, display selected values as summary */
	@Override     
	protected void onResume() {         
		super.onResume();          
		downloadPathPref.setSummary(downloadPathPref.getText());
                    
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);     
	}      

	@Override     
	protected void onPause() {         
		super.onPause();         
		downloadPathPref.setSummary(downloadPathPref.getText());
		
		// Unregister the listener whenever a key changes                     
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);         

	}


	/** Checks if downloads folder path that the user manually entered is valid or not. */
	public boolean checkValidFoldername(File f) {  
		try {     
			f.getCanonicalPath();     
			return true;   
		} catch (IOException e) {    
			return false;   
		} 
	}
  
}
