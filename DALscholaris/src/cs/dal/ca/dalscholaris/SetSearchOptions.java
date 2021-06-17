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

public class SetSearchOptions extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private EditTextPreference startYearPref;
	private EditTextPreference endYearPref;
	//private EditTextPreference downloadPathPref;
	private EditTextPreference journalPref;
	private EditTextPreference authorPref;
	String chosenYear = ""; 
	String chosenJournal = "";
	String chosenFile = "";

	//private boolean D = false;

	/** Populate the advanced search options with pre user-defined values */
	@Override    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.advancesearch);

		startYearPref = (EditTextPreference)getPreferenceScreen().findPreference("startYear");
		endYearPref = (EditTextPreference)getPreferenceScreen().findPreference("endYear");
		journalPref = (EditTextPreference)getPreferenceScreen().findPreference("journal");
		authorPref = (EditTextPreference)getPreferenceScreen().findPreference("author");

	}

	/** Whenever the user changes an option, it'll be displayed as a summary */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {     
		//if(D) Log.d("MyApp","Changed="+key);
		if (key.equalsIgnoreCase("startYear")) {         			       
			startYearPref.setSummary(startYearPref.getText());  			
		} 
		else if (key.equalsIgnoreCase("endYear")) {         			       
			endYearPref.setSummary(endYearPref.getText());  			
		}
		else if (key.equalsIgnoreCase("journal")) {         			       
			journalPref.setSummary(journalPref.getText());  			
		}
		else if (key.equalsIgnoreCase("author")) {         			       
			authorPref.setSummary(authorPref.getText());  			
		}
	} 

	/** When brought to the foreground, display selected values as summary */
	@Override     
	protected void onResume() {         
		super.onResume();          
		// Setup the initial values         
		startYearPref.setSummary(startYearPref.getText()); 
		endYearPref.setSummary(endYearPref.getText());
		journalPref.setSummary(journalPref.getText());
		authorPref.setSummary(authorPref.getText());
	
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
	//CHANGE!!!!
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.preference_menu,menu);
		return true;
	}

	//CHANGE!!!!	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.startover: 	startYearPref.setText("");
									endYearPref.setText("");
									journalPref.setText("");
									authorPref.setText("");
									//bioPref.setChecked(false);
									//busPref.setChecked(false);
									//chmPref.setChecked(false);
									//engPref.setChecked(false);
									//medPref.setChecked(false);
									//phyPref.setChecked(false);
									//socPref.setChecked(false);
									break;
			case R.id.chooseyear:	yearpicker();
									break;

			case R.id.choosejournal: 	journalpicker();
										break;

			case R.id.setpath: 		fileexplorer(Environment.getExternalStorageDirectory());
									break;
									
			case R.id.clearhistory:	AlertDialog.Builder builder = new AlertDialog.Builder(SetSearchOptions.this);
									builder.setTitle("Clear History");
									builder.setMessage("Are you sure you want to clear the entire history of search queries?");

									builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											SharedPreferences savedHistory = getApplicationContext().getSharedPreferences("History", MODE_PRIVATE);
											SharedPreferences.Editor historyEditor = savedHistory.edit();
											historyEditor.clear();
											historyEditor.commit();
											Toast.makeText(SetSearchOptions.this, "Search history has been cleared.", Toast.LENGTH_LONG).show();
										}
									});
									
									builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											Toast.makeText(SetSearchOptions.this, "No changes were made to search history.", Toast.LENGTH_LONG).show();
											dialog.cancel();
										}
									});

									builder.create().show();
									
									break;
									
			case R.id.returntosearch:	SetSearchOptions.this.finish();

		}

		return true;
	}

	/** Shows dialog box with a list showing the last 30 years */
	public void yearpicker() {
		int numYears = 30;

		final String[] mYearList = new String[numYears];

		Calendar cal = Calendar.getInstance();  
		int current_year = cal.get(Calendar.YEAR);
		chosenYear = Integer.toString(current_year);

		int count_year;
		for(count_year=0;count_year<numYears;count_year++)
			mYearList[count_year] = Integer.toString(current_year-count_year);

		AlertDialog.Builder builder = new Builder(this);    
		builder.setTitle("Choose year");    
		builder.setSingleChoiceItems(mYearList, 0, new DialogInterface.OnClickListener(){ 
			public void onClick(DialogInterface dialog, int which){ 
				chosenYear = mYearList[which];   			
			}       
		});

		builder.setPositiveButton("From year", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				startYearPref.setText(chosenYear);

			}
		});

		builder.setNegativeButton("To year", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				endYearPref.setText(chosenYear);

			}
		});

		builder.show();
	}

	public void journalpicker() {
		SharedPreferences journalPreference = getApplicationContext().getSharedPreferences("Journals", MODE_PRIVATE);
		final SharedPreferences.Editor journalEditor = journalPreference.edit();
		final Map<String, ?> items = journalPreference.getAll();

		final String[] mJournalList = new String[items.keySet().size()];
		int count = 0;
		for(String s : items.keySet()){
			mJournalList[count] = s;
			//if(D) Log.d("File","file="+s);
			count++;		    
		}

		chosenJournal = "";

		AlertDialog.Builder builder = new Builder(this);    
		builder.setTitle("Choose journal");    
		builder.setSingleChoiceItems(mJournalList, 0, new DialogInterface.OnClickListener(){ 
			public void onClick(DialogInterface dialog, int which){ 
				if (mJournalList.length>0) {
					chosenJournal = mJournalList[which]; 
				}
			}       
		});

		builder.setPositiveButton("Choose", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mJournalList.length>0) {
					if (chosenJournal.equalsIgnoreCase("")) {
						chosenJournal = items.get(mJournalList[0]).toString();
					}
					journalPref.setText(chosenJournal);
				}

			}
		});

		builder.setNeutralButton("Add", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				addjournal();

			}
		});

		builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mJournalList.length>0) {
					if (chosenJournal.equalsIgnoreCase("")) {
						chosenJournal = items.get(mJournalList[0]).toString();
					}
					journalEditor.remove(chosenJournal);
					journalEditor.commit();
					Toast.makeText(SetSearchOptions.this, "Journal name \""+chosenJournal+"\" has been deleted", Toast.LENGTH_SHORT).show();
				}

			}
		});

		builder.show();
	}

	public void addjournal() {
		AlertDialog.Builder addJournal = new Builder(this);    
		addJournal.setTitle("Choose journal");    
		addJournal.setMessage("Enter journal name");  
		// Set an EditText view to get user input  
		final EditText inputJournal = new EditText(this);
		inputJournal.setText("Nature");
		inputJournal.setSingleLine();
		addJournal.setView(inputJournal);  
		addJournal.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Editable journal = inputJournal.getText();  // Do something with value!
				if (!journal.toString().equalsIgnoreCase("")) {
					SharedPreferences journalPreference = getApplicationContext().getSharedPreferences("Journals", MODE_PRIVATE);
					SharedPreferences.Editor journalEditor = journalPreference.edit();
					if(!journalPreference.contains(journal.toString())) {
						journalEditor.putString(journal.toString(), journal.toString());
						journalEditor.commit();
						//if(D) Log.d("SPref","bm="+bookmarkPref.getString(bookmark.toString(), webview.getUrl()));
						Toast.makeText(SetSearchOptions.this, "The journal name "+"\""+journal.toString().toString()+"\" has been saved.", Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(SetSearchOptions.this, "The journal name "+"\""+journal.toString().toString()+"\" already exists.", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(SetSearchOptions.this, "Invalid name.", Toast.LENGTH_SHORT).show();

				journalpicker();
			}
		});

		addJournal.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				journalpicker();
			}
		});

		addJournal.show();

	}

	public void fileexplorer(File setPath) {
		final String[] mFileList;
		final File mPath = setPath;

		if(mPath.exists()){      
			FilenameFilter filter = new FilenameFilter(){          
				public boolean accept(File dir, String filename){              
					File sel = new File(dir, filename);              
					return (sel.isDirectory());  
				}      
			};      
			mFileList = mPath.list(filter);

		}   
		else{     
			mFileList= new String[0];   
		} 
		//if(D) Log.d("MyFile","size="+mFileList.length);
		chosenFile = "";

		AlertDialog.Builder builder = new Builder(this);    
		builder.setTitle("/"+Environment.getExternalStorageDirectory().toURI().relativize(mPath.toURI()).getPath());    
		builder.setSingleChoiceItems(mFileList, 0, new DialogInterface.OnClickListener(){ 
			public void onClick(DialogInterface dialog, int which){ 
				if (mFileList.length>0) {
					chosenFile = mFileList[which];
					//if(D) Log.d("MyFile","file="+chosenFile+"index="+which);
				}
			}       
		});

		builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase(""))
						chosenFile = mFileList[0];
					File targetFile = new File(mPath,chosenFile);
					//if(D) Log.d("File","name="+targetFile);
					try {
						fileexplorer(new File(targetFile.getCanonicalPath()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				

				}

			}
		});

		builder.setNegativeButton("Up", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				File targetFile = new File(mPath,"..");
				if (targetFile.exists()&&!mPath.equals(Environment.getExternalStorageDirectory())) {					
					//if(D) Log.d("File","name="+targetFile);
					try {
						fileexplorer(new File(targetFile.getCanonicalPath()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}							
				}
				else
					fileexplorer(Environment.getExternalStorageDirectory());

			}
		});

		builder.setNeutralButton("Choose", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase(""))
						chosenFile = mFileList[0];
					File targetFile = new File(mPath,chosenFile);
					//if(D) Log.d("File","Chosen="+targetFile);

					String relative = Environment.getExternalStorageDirectory().toURI().relativize(targetFile.toURI()).getPath();
					//if(D) Log.d("File","Chosen1="+"/"+relative);
					//downloadPathPref.setText("/"+relative);
				}
			}
		});

		builder.show();
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
	
	public void showAmbilWarnaDialog(String key) {
		final Preference changePref = findPreference(key);
		final String keyChange = key;
		int initialColor;
		SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor appPrefEditor = appPref.edit();
		
		initialColor = Color.parseColor(changePref.getSummary().toString());
		
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, new OnAmbilWarnaListener() {
			public void onOk(AmbilWarnaDialog dialog, int selectedColor) {
				String strselectedColor = "#"+colortoString(selectedColor);
				appPrefEditor.putString(keyChange, strselectedColor);
				appPrefEditor.commit();
 				
			}     
			public void onCancel(AmbilWarnaDialog dialog) {
				// cancel was selected by the user
			}
			
		});			
		dialog.show();
	}
	
    static String colortoString(int color){     
    	return Integer.toHexString(Color.rgb(Color.red(color), Color.green(color), Color.blue(color))); 
    } 
  
}
