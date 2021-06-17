package cs.dal.ca.dalscholaris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs.dal.ca.dalscholaris.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

//NOTE TO SELF: When running with dpi more than 7in for emulator - error occurs bcos we  haven't catered a layout for gingerbread avd with x-large dpi
public class Main extends Activity   {
	private String chosenKey;
	private String chosenFile;	
	static final int onlyLink = 1;
	static final int freePDF = 2;	
	private String downloadPath = "/ScholarDroid/";	
	private SharedPreferences savedHistory;	
	private SharedPreferences.Editor historyEditor;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		    
        ActionBar bar = getActionBar();
        bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayShowTitleEnabled(true);

		savedHistory = getApplicationContext().getSharedPreferences("History", MODE_PRIVATE);
		historyEditor = savedHistory.edit();
		setAutoComplete();		
		
		final AutoCompleteTextView searchbox =  (AutoCompleteTextView) findViewById(R.id.txtsearchbox);
		
		Button btnsearch = (Button) findViewById(R.id.btnsearch);
		btnsearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, Result.class);
				intent.putExtra("thesearchstring", searchbox.getText().toString());
				
				historyEditor.putString(searchbox.getText().toString(), searchbox.getText().toString());
				historyEditor.commit();
				setAutoComplete();
				
				startActivity(intent);
				//startActivity(new Intent(Main.this, Result.class));
				
			}
		});
	}
	
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_menu, menu);
		return true;
	}
	
	public void setAutoComplete() {
		//SharedPreferences savedHistory = getApplicationContext().getSharedPreferences("History", MODE_PRIVATE);
		//SharedPreferences.Editor historyEditor = savedHistory.edit();
		final Map<String, ?> items = savedHistory.getAll();
		final String[] history = new String[items.keySet().size()];
		int count = 0;
		for(String s : items.keySet()){
			history[count] = s;
			count++;		    
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, history);
        AutoCompleteTextView textSearch = (AutoCompleteTextView) findViewById(R.id.txtsearchbox);
        textSearch.setAdapter(adapter);

	}	
	
	/** What to do when user clicks on menu options */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
			case R.id.advancesearch:    Intent showadvancesearch = new Intent(Main.this, SetSearchOptions.class);         
										startActivity(showadvancesearch);	
										break;		
										
			case R.id.ezproxy:			Intent showezproxy = new Intent(Main.this, SetEzproxy.class);         
										startActivity(showezproxy);
										break;
												
			case R.id.view_bookmark: 	bookmarkChooser();										
										break;

			case R.id.view_downloads:	fileChooser();
										break;

			/*case R.id.refresh: 	    refreshSearch();
										break;

			case R.id.advanced: 		Intent showprefs = new Intent(Result.this, SetPrefsActivity.class);         
										startActivity(showprefs);
										break;*/
			
			case R.id.appsettings:      Intent appsettings = new Intent(Main.this, SetAppSettings.class); 
										startActivity(appsettings);
										break;

			case R.id.about: 			showAbout();
										break;
																				

			/*case R.id.exit:				purgeCache(Result.this);
										finish();
										android.os.Process.killProcess(android.os.Process.myPid());*/
	
		}

		return true;
	}	

	/** Called by About menu option. Text has been hard coded instead of using /res/values/strings.xml */
	public void showAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
		builder.setTitle("About DAL scholaris");
		builder.setMessage("DALscholaris is an android app that allows students or faculty to conveniently  " +
				"browse through search results returned by Google Scholar while at the same " +
				"time leverage on Dalhousie libraries’ subscribed databases of conference proceedings " +
				"and journals for their research purposes. Users can use the app both on campus " +
				"and off campus without having to use a vpn connection. Furthermore one can also " +
				"view the research paper in pdf or word format with added support of zooming into " +
				"specific paragraphs to enlarge the view for easier reading. The app also allows the user " +
				"to save or bookmark the document on the phone for later viewing. We find that this will " +
				"be a useful app for many students and faculty who wants to do their research quick while " +
				"on the move since most people would have access to a mobile device.\n\n"+"Powered by Google Scholar\nVersion - 0.1\n" +
				"Dalscholaris team, 2013\ndalscholaris@outlook.com");

		builder.setPositiveButton("Website", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Main.this,OpenURLActivity.class);											
				i.putExtra("URL","http://dalscholaris.philipyeo.net/");
				i.putExtra("type",onlyLink);
				Main.this.startActivity(i);

			}
		});
		
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});

		builder.create().show();
	}
	
	
	/** Called by Downloads menu option. Adapted from an example on Stackoverflow. */
	public void fileChooser() {
		final String[] mFileList;
		final File mPath = new File(Environment.getExternalStorageDirectory()+downloadPath);
		final String FTYPE = ".pdf";     

		/** Show only PDF and text files */
		if(mPath.exists()){      
			FilenameFilter filter = new FilenameFilter(){          
				public boolean accept(File dir, String filename){              
					File sel = new File(dir, filename);              
					return (filename.contains(FTYPE) || filename.contains(".txt")) && (!sel.isDirectory());  
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
		builder.setTitle("Files");    
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
						Uri targetUri = Uri.fromFile(targetFile);
						Intent i = new Intent(Intent.ACTION_VIEW); 
						if(chosenFile.contains(".txt"))
							i.setDataAndType(targetUri,"text/plain");
						else
							i.setDataAndType(targetUri,"application/pdf");
						Main.this.startActivity(i);
					}
					catch(Exception e) {
						Toast.makeText(Main.this, "Oops! It looks like there's no application installed to view this type of file.", Toast.LENGTH_LONG).show();
					}
				}

			}
		});

		builder.setNeutralButton("Import", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase(""))
						chosenFile = mFileList[0];		

					if (chosenFile.contains(".txt")) {
						File targetFile = new File(mPath,chosenFile);
						SharedPreferences bookmarkPref = getApplicationContext().getSharedPreferences("Bookmarks", MODE_PRIVATE);
						final SharedPreferences.Editor bookmarkEditor = bookmarkPref.edit();
						try {
							FileReader bookmarkRead = new FileReader(targetFile);
							BufferedReader bfr = new BufferedReader(bookmarkRead);
							String line=null;
							String[] row;

							/** "http://" is used as a delimiter for each line in the bookmarks file */
							while((line=bfr.readLine())!=null) {
								row = line.split("http://");

								if(line.trim().replaceAll(" ","").equalsIgnoreCase("IMPORT_MODE=OVERWRITE")) {
									bookmarkEditor.clear();
								}
								if(row.length==2) {
									Pattern pdflink = Pattern.compile("(.*?\\.pdf)",Pattern.MULTILINE);
									Matcher pdflinkmatch = pdflink.matcher(row[1].trim());
									if (pdflinkmatch.find()) {          
										try {
											String query = URLEncoder.encode("http://"+pdflinkmatch.group(1),"utf-8");
											String strGSurl = "http://docs.google.com/viewer?url="+query;						        			
											bookmarkEditor.putString(row[0].trim(), strGSurl);
											//if(D) Log.d("MyApp","Name="+row[0].trim()+" Link="+strGSurl);
										} 
										catch (UnsupportedEncodingException e) {
											e.printStackTrace();
										}
									}
									else {
										bookmarkEditor.putString(row[0].trim(), "http://"+row[1].trim());
										//if(D) Log.d("MyApp","Name="+row[0].trim()+" Link="+"http://"+row[1].trim());
									}
								}
							}
							bookmarkEditor.commit();
							Toast.makeText(Main.this, "Bookmarks have been imported from \""+chosenFile+"\".", Toast.LENGTH_LONG).show();

						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//if(D) Log.d("FileBookmark","name="+targetFile);

					}
					else
						Toast.makeText(Main.this, "Invalid bookmark file.", Toast.LENGTH_LONG).show();
				}

			}
		});

		builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {			
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase(""))
						chosenFile = mFileList[0];
					File targetFile = new File(mPath,chosenFile);
					//if(D) Log.d("FileDelete","name="+targetFile);
					targetFile.delete();
					Toast.makeText(Main.this, "File \""+chosenFile+"\" has been deleted", Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.show();

	}
	
	
	/** Called by Bookmarks menu option */
	public void bookmarkChooser() {
		SharedPreferences bookmarkPref = getApplicationContext().getSharedPreferences("Bookmarks", MODE_PRIVATE);
		final SharedPreferences.Editor bookmarkEditor = bookmarkPref.edit();
		final Map<String, ?> items = bookmarkPref.getAll();
		//if(D) Log.d("File","file="+bookmarkPref.getString("example", "0"));

		final String[] mFileList = new String[items.keySet().size()];
		int count = 0;
		for(String s : items.keySet()){
			mFileList[count] = s;
			//if(D) Log.d("File","file="+s);
			count++;		    
		}

		chosenFile = "";

		AlertDialog.Builder builder = new Builder(this);    
		builder.setTitle("Favourites");  

		builder.setSingleChoiceItems(mFileList, 0, new DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which){ 
				if (mFileList.length>0) {
					chosenFile = items.get(mFileList[which]).toString();
					chosenKey = mFileList[which].toString();
					//if(D) Log.d("File","key="+mFileList[which]+" file="+chosenFile);
				}
			}       
		}); 

		builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase("")) {
						chosenFile = items.get(mFileList[0]).toString();
						chosenKey = mFileList[0].toString();
					}
					//if(D) Log.d("File","key="+mFileList[0]+" file="+chosenFile);

					Intent i = new Intent(Main.this,OpenURLActivity.class);											
					i.putExtra("URL",chosenFile);
					try {
						URI query = new URI(chosenFile);
						//if(D) Log.d("Query","host="+query.getHost());
						if (query.getHost().equalsIgnoreCase("docs.google.com")) {
							i.putExtra("type",freePDF);							
							i.putExtra("downloadlink",query.getQuery().replace("url=", ""));
						}
						else
							i.putExtra("type",onlyLink);

						Main.this.startActivity(i);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}
		});

		builder.setNeutralButton("Export All", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					final File mPath = new File(Environment.getExternalStorageDirectory()+downloadPath);
					File targetFile = new File(mPath,"Bookmarks.txt");
					StringBuilder textToWrite = new StringBuilder();
					for(String s : items.keySet()){
						textToWrite.append(s+"\t"+items.get(s)+"\n");
					}
					try {
						FileWriter bookmarkFile = new FileWriter(targetFile);

						bookmarkFile.append(textToWrite.toString());
						bookmarkFile.flush();
						bookmarkFile.close();
						Toast.makeText(Main.this, "Bookmarks have been exported to Bookmarks.txt", Toast.LENGTH_LONG).show();

					}
					catch(Exception e) {}
				}
			}
		});

		builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mFileList.length>0) {
					if (chosenFile.equalsIgnoreCase("")) {
						chosenFile = items.get(mFileList[0]).toString();
						chosenKey = mFileList[0].toString();
					}
					bookmarkEditor.remove(chosenKey);
					bookmarkEditor.commit();
					Toast.makeText(Main.this, "Bookmark \""+chosenKey+"\" has been deleted", Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.show();
		return;

	}	

}
