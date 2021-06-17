/** 
 * ***********************************************
 * File		   - Result.java
 * Description - Main activity for the app 
 * ***********************************************
 */
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import cs.dal.ca.dalscholaris.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
//import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Result extends Activity {
	
	private int radioChecked=1;
	private final int raisedBySearch = 0;
	private final int raisedByBack = 1;
	private final int raisedByNext = 2;
	private final int raisedByPage = 3;
	private int pagenum = 1;
	private boolean searchRunning = false;
	private String searchreport;
	private String chosenKey;
	private String chosenFile;
	static final int onlyLink = 1;
	static final int freePDF = 2;
	static final int book = 3;
	//ImageView imgScholarDroid;
	final Handler mHandler = new Handler();
	private String startYear = "";
	private String endYear = "";
	private String journalName = "";
	private String authorName = "";
	private boolean bio;
	private boolean bus;
	private boolean chm;
	private boolean eng;
	private boolean med;
	private boolean phy;
	private boolean soc;
	private boolean showAuthor;
	private boolean showSummary;
	private boolean showCitation;
	private boolean useCustomColor;
	private String bgColor;
	private boolean showImage;
	//private String downloadPath = "/ScholarDroid/";
	private String downloadPath = "/DalScholaris/";
	private String subSearch = "";
	private String advancedSearch = "";
	private String authorSearch = "";
	private boolean useProxy = false;
	private String hostname = "";
	private String port = "";
	private String username = "";
	private String password = "";
	private boolean onStart = false;
	private SharedPreferences savedHistory;
	private SharedPreferences.Editor historyEditor;
	//private boolean D = false;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		
		/** Takes care of NetworkOnMainThread Exception. 
		 * Obsolete since parsing is now done in a separate thread but just in case. */
		// Version check
		if(Build.VERSION.SDK_INT >= 11) {
			try { 
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy); 
			} catch(Exception e) {
				//System.out.println("StrictMode is not available = " + e);
				//e.printStackTrace();
			}
		}
		radioChecked = 1;
		//imgScholarDroid = (ImageView) findViewById(R.id.icon);
		onStart = true;
		
		savedHistory = getApplicationContext().getSharedPreferences("History", MODE_PRIVATE);
		historyEditor = savedHistory.edit();
		setAutoComplete();
		readPrefs();
		setListeners();
		
		AutoCompleteTextView searchbox = (AutoCompleteTextView) findViewById(R.id.txtsearchbox);
		if(getIntent().getExtras().getString("thesearchstring").length() > 0) {
			searchbox.setText(getIntent().getExtras().getString("thesearchstring"));
			Button btn = (Button) findViewById(R.id.btnsearch);
			btn.performClick();
		} 		


		//if(D) Log.d("MyApp","Rotated");
		//lv.setAdapter(mCustomBaseAdapter);
		/* *setEnterPressListener();
        setSearchButtonClickListener();
        setBackNextClickListener();
        setPageEnterListener();*/

		/** Restore state after restart */
		if (savedInstanceState != null) {
			String search = savedInstanceState.getString("search");
			String page = savedInstanceState.getString("page");
			String pagemax = savedInstanceState.getString("pagemax");
			radioChecked = savedInstanceState.getInt("radiochecked");

			//RadioButton radioArticle = (RadioButton)findViewById(R.id.articlechoose);    	
			//RadioButton radioBook = (RadioButton)findViewById(R.id.bookchoose);    	
			//RadioButton radioBoth = (RadioButton)findViewById(R.id.bothchoose);  
			
			/* if(radioChecked==1)
				radioArticle.setChecked(true);
			else if(radioChecked==2)
				radioBook.setChecked(true);
			else if(radioChecked==3)
				radioBoth.setChecked(true); 
			*/
			
			/** If already has text, then execute Search */
			
			if (!search.equalsIgnoreCase("")) {

				Button buttonSearch = (Button) findViewById(R.id.btnsearch);
				final AutoCompleteTextView textSearch = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);
				final TextView textPage = (TextView) findViewById(R.id.pagestring);
				final TextView textPageMax = (TextView) findViewById(R.id.pagemax);
				Button buttonBack = (Button) findViewById(R.id.backbutton);
				Button buttonNext = (Button) findViewById(R.id.nextbutton);

				textSearch.setText(search);
				textPage.setText(page);
				textPageMax.setText("/"+pagemax);

				buttonSearch.setTag(raisedByPage);
				buttonSearch.performClick();

				//if(D) Toast.makeText(GSsearchActivity.this, search+" "+page+" "+pagemax, Toast.LENGTH_LONG).show();
				if (Integer.parseInt(page)==1) {
					if (Integer.parseInt(pagemax)==1) {
						buttonBack.setEnabled(false);
						buttonNext.setEnabled(false);
					} 
					else {
						buttonBack.setEnabled(false);
						buttonNext.setEnabled(true);
					}
				}
				else if (Integer.parseInt(page)==Integer.parseInt(pagemax)) {
					buttonBack.setEnabled(true);
					buttonNext.setEnabled(false);
				}
				else if ((Integer.parseInt(page)>1)&&(Integer.parseInt(page)<Integer.parseInt(pagemax))) {
					buttonBack.setEnabled(true);
					buttonNext.setEnabled(true);
				}

			}					
			
		}
		
	}

	private void setSearchButtonClickListener() {

		/* RadioButton radioArticle = (RadioButton)findViewById(R.id.articlechoose);
		radioArticle.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				radioChecked=1;
			}
		});

		RadioButton radioBook = (RadioButton)findViewById(R.id.bookchoose);    	
		radioBook.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				radioChecked=2;
			}
		});

		RadioButton radioBoth = (RadioButton)findViewById(R.id.bothchoose);    	
		radioBoth.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				radioChecked=3;
			}
		}); */

		final AutoCompleteTextView textSearch = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);
		Button buttonSearch = (Button)findViewById(R.id.btnsearch);
		buttonSearch.setTag(raisedBySearch);

		buttonSearch.setOnClickListener(new View.OnClickListener() {

			ListView lv = (ListView)findViewById(R.id.listresults);
			CustomBaseAdapter mCustomBaseAdapter = new CustomBaseAdapter(Result.this,showAuthor,showSummary,showCitation);

			Button buttonSearch = (Button) findViewById(R.id.btnsearch);
			Button buttonBack = (Button) findViewById(R.id.backbutton);
			Button buttonNext = (Button) findViewById(R.id.nextbutton);
			final TextView textPage = (TextView) findViewById(R.id.pagestring);
			final TextView textPageMax = (TextView) findViewById(R.id.pagemax);

			int raisedBy = (Integer) buttonSearch.getTag();
			//int pagenum = 1;
			int pagetotal = 1;
			String pagenumquery = "";

			public void onClick(View v) {

				/** If no text has been entered */
				if (textSearch.getText().toString().equalsIgnoreCase("")) {
					textPage.setText("0");
					textPage.setEnabled(false);
					textPageMax.setText(" /0");
					buttonBack.setEnabled(false);
					buttonNext.setEnabled(false);
					Toast.makeText(Result.this, "Please enter a search term", Toast.LENGTH_LONG).show();
					buttonSearch.setTag(raisedBySearch);
					return;
				}	

				if(searchRunning) {
					Toast.makeText(Result.this, "A previous search is still running. Please wait.", Toast.LENGTH_LONG).show();
					return;
				}
				searchRunning = true;
				lv.setEnabled(false);
				
				/** Adjust page number and next/back button state */
				raisedBy = (Integer) buttonSearch.getTag();
				if (raisedBy==raisedBySearch) {
					pagenumquery = "";
					pagenum = 1;
				}
				else if (raisedBy==raisedByNext) {
					pagenumquery = "start="+pagenum*10+"&";
					pagenum ++;
					buttonBack.setEnabled(true);
					if (pagenum==pagetotal)	
						buttonNext.setEnabled(false);
				}
				else if (raisedBy==raisedByBack) {
					pagenum = pagenum - 2;
					if (pagenum==0)
						pagenumquery = "";
					else
						pagenumquery = "start="+pagenum*10+"&";

					pagenum ++;
					if (pagenum==1)	
						buttonBack.setEnabled(false);
				}
				else if (raisedBy==raisedByPage) {
					if (textPage.getText().toString().equalsIgnoreCase("")) {
						Toast.makeText(Result.this, "Invalid page number", Toast.LENGTH_LONG).show();
						buttonSearch.setTag(raisedBySearch);
						return;						
					}

					try {
						if (Integer.parseInt(textPage.getText().toString())<=Integer.parseInt(textPageMax.getText().toString().trim().replace("/", ""))) {

							pagenum = Integer.parseInt(textPage.getText().toString());

							if (pagenum==0) {
								Toast.makeText(Result.this, "Invalid page number", Toast.LENGTH_LONG).show();
								buttonBack.setEnabled(false);
								buttonNext.setEnabled(false);
								buttonSearch.setTag(raisedBySearch);
								return;
							}

							if (Integer.parseInt(textPageMax.getText().toString().trim().replace("/", ""))==1) {
								buttonBack.setEnabled(false);
								buttonNext.setEnabled(false);
							}
							else if (pagenum == 1) {
								pagenumquery = "";
								buttonBack.setEnabled(false);
								buttonNext.setEnabled(true);
							}
							else if (pagenum==pagetotal) {
								pagenumquery = "start="+(pagenum-1)*10+"&";
								buttonBack.setEnabled(true);
								buttonNext.setEnabled(false);
							}
							else {
								pagenumquery = "start="+(pagenum-1)*10+"&";
								buttonBack.setEnabled(true);
								buttonNext.setEnabled(true);
							}
						}
						else {
							Toast.makeText(Result.this, "Invalid page number", Toast.LENGTH_LONG).show();
							buttonBack.setEnabled(false);
							buttonNext.setEnabled(false);
							buttonSearch.setTag(raisedBySearch);
							return;
						}
					} catch (NumberFormatException nfe) {
						sendReport(textSearch.getText().toString());
					}
				}

				/** Construct Google Scholar query */
				String textToSearch = textSearch.getText().toString().replaceAll(" ", "+");
				String strGSurl = null;
				if(radioChecked==1)
					strGSurl = "http://scholar.google.com/scholar?"+pagenumquery+"q="+textToSearch+"+article"+authorSearch+"&hl=en&as_sdt=1%2C15&as_vis=1"+advancedSearch;
				else if(radioChecked==2)
					strGSurl = "http://scholar.google.com/scholar?"+pagenumquery+"q="+textToSearch+"+book"+authorSearch+"&hl=en&as_sdt=1%2C15&as_vis=1"+advancedSearch;	
				else if(radioChecked==3) 
					strGSurl = "http://scholar.google.com/scholar?"+pagenumquery+"q="+textToSearch+authorSearch+"&hl=en&as_sdt=1%2C15&as_vis=1"+advancedSearch;	

				//if(D) Log.d("MyApp","Query="+strGSurl);
				/** Run parser in separate thread to prevent GUI lock-up */
				final GSXMLHandler gsXMLHandler = new GSXMLHandler();
				gsXMLHandler.clearGSresultList();
				final String strGSurlthread = strGSurl;
				Thread t = new Thread() {            
					public void run() {
						try {
							/** Uses TagSoup */
							XMLReader xr = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser"); 
							URL sourceUrl = new URL(strGSurlthread);  
							xr.setContentHandler(gsXMLHandler);

							InputSource gsxml = new InputSource(sourceUrl.openStream());
							gsxml.setEncoding("iso-8859-1");
							xr.parse(gsxml); 

						} catch (Exception e) {
							//System.out.println("XML Parsing Exception = " + e);
							//e.printStackTrace();
						} 

						/** Store returned search results */
						final ArrayList<GSresult>gsresultlist =  gsXMLHandler.getGSresultList();

						/** Post back on main thread */
						mHandler.post(new Runnable() {        
							public void run() { 

								mCustomBaseAdapter.addResultList(gsresultlist);
								lv.setAdapter(mCustomBaseAdapter);

								/** When no results are returned */
								if (mCustomBaseAdapter.getCount()==0) {
									if(showImage)
										//imgScholarDroid.setVisibility(View.VISIBLE);
									if (pagenum>100)
										Toast.makeText(Result.this, "Sorry, Google Scholar doesn't serve more than 1000 results (100 pages) per query", Toast.LENGTH_LONG).show();
									else
										Toast.makeText(Result.this, "No results were found.", Toast.LENGTH_LONG).show();
									//sendReport(textSearch.getText().toString());
								}
								else
									//imgScholarDroid.setVisibility(View.INVISIBLE);

								/** Update page count and enable/disable next/back buttons */
								pagetotal = (int) (((double) gsXMLHandler.getNumResults())/10 + 0.9);
								textPageMax.setText("/"+pagetotal);

								textPage.setText(Integer.toString(pagenum));


								if (raisedBy==raisedBySearch) {
									pagenumquery = "";
									pagenum = 1;
									buttonBack.setEnabled(false);
									if (pagetotal>1)
										buttonNext.setEnabled(true);
									else
										buttonNext.setEnabled(false);

								}

								/** This is needed since it'll be checked to find out what initiated the search */
								buttonSearch.setTag(raisedBySearch);
								textPage.setEnabled(true);
								searchRunning = false;
								lv.setEnabled(true);
								
								historyEditor.putString(textSearch.getText().toString(), textSearch.getText().toString());
								historyEditor.commit();
								setAutoComplete();

							} 
						}); // End of mHandler

					}
				}; // End of thread
				t.start();

			}
		});
	}

	/** Enter key should trigger search */
	public void setEnterPressListener() { 

		final AutoCompleteTextView textSearch = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);

		textSearch.setOnKeyListener(new View.OnKeyListener() {          
			public boolean onKey(View v, int keyCode, KeyEvent event) {  
				Button buttonSearch = (Button)findViewById(R.id.btnsearch);
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {                 
						// perform search   
						buttonSearch.performClick();
						return true;             
					}
				}
				return false;         
			}     
		}); 

	} 

	/** Listeners for Next/Back button press */
	private void setBackNextClickListener() {
		Button buttonBack = (Button) findViewById(R.id.backbutton);
		Button buttonNext = (Button) findViewById(R.id.nextbutton);

		buttonBack.setOnClickListener(new View.OnClickListener() {

			Button buttonSearch = (Button) findViewById(R.id.btnsearch);

			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonSearch.setTag(raisedByBack);
				buttonSearch.performClick();

			}
		});

		buttonNext.setOnClickListener(new View.OnClickListener() {

			Button buttonSearch = (Button) findViewById(R.id.btnsearch);

			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonSearch.setTag(raisedByNext);
				buttonSearch.performClick();

			}
		});

	}

	/** When enter key is pressed while focus is with page number text view */
	public void setPageEnterListener() { 

		final TextView textPage = (TextView)findViewById(R.id.pagestring);

		textPage.setOnKeyListener(new View.OnKeyListener() {          
			public boolean onKey(View v, int keyCode, KeyEvent event) {  
				Button buttonSearch = (Button)findViewById(R.id.btnsearch);
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {                 
						// perform search  
						buttonSearch.setTag(raisedByPage);
						buttonSearch.performClick();
						return true;             
					}  
				}
				return false;         
			}     
		}); 

	} 

	/** Save state so that it can be restored (when user presses home key and then returns to the app) */
	protected void onSaveInstanceState(Bundle savedInstanceState) {

		final AutoCompleteTextView textSearch = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);
		final TextView textPage = (TextView) findViewById(R.id.pagestring);
		final TextView textPageMax = (TextView) findViewById(R.id.pagemax);

		savedInstanceState.putString("search", textSearch.getText().toString());
		savedInstanceState.putString("page", textPage.getText().toString());
		savedInstanceState.putString("pagemax", textPageMax.getText().toString().trim().replace("/", ""));
		savedInstanceState.putInt("radiochecked", radioChecked);
		super.onSaveInstanceState(savedInstanceState);

	}

	/** Menu layout in /res/menu/gssearch_menu.xml */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.search_menu,menu);
		return true;
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.search_menu, menu);
        //return true;		
	}

	/** What to do when user clicks on menu options */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
			case R.id.advancesearch:    Intent showadvancesearch = new Intent(Result.this, SetSearchOptions.class);         
										startActivity(showadvancesearch);	
										break;		
										
			case R.id.ezproxy:			Intent showezproxy = new Intent(Result.this, SetEzproxy.class);         
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
			
			case R.id.appsettings:      Intent appsettings = new Intent(Result.this, SetAppSettings.class); 
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
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		//Toast.makeText(Result.this, "Aloha!", Toast.LENGTH_LONG).show();
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
        	if(!searchRunning) {
            	refreshSearch();
        	}

        } 
        
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
						Result.this.startActivity(i);
					}
					catch(Exception e) {
						Toast.makeText(Result.this, "Oops! It looks like there's no application installed to view this type of file.", Toast.LENGTH_LONG).show();
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
							Toast.makeText(Result.this, "Bookmarks have been imported from \""+chosenFile+"\".", Toast.LENGTH_LONG).show();

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
						Toast.makeText(Result.this, "Invalid bookmark file.", Toast.LENGTH_LONG).show();
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
					Toast.makeText(Result.this, "File \""+chosenFile+"\" has been deleted", Toast.LENGTH_LONG).show();
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

					Intent i = new Intent(Result.this,OpenURLActivity.class);											
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

						Result.this.startActivity(i);
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
						Toast.makeText(Result.this, "Bookmarks have been exported to Bookmarks.txt", Toast.LENGTH_LONG).show();

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
					Toast.makeText(Result.this, "Bookmark \""+chosenKey+"\" has been deleted", Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.show();
		return;

	}

	/** Called by About menu option. Text has been hard coded instead of using /res/values/strings.xml */
	public void showAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Result.this);
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
				Intent i = new Intent(Result.this,OpenURLActivity.class);											
				i.putExtra("URL","http://dalscholaris.philipyeo.net/");
				i.putExtra("type",onlyLink);
				Result.this.startActivity(i);

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

	/** As of v2.0, not called anymore. */
	public void sendReport(String searchterm) {
		AlertDialog.Builder report = new AlertDialog.Builder(Result.this);
		searchreport = searchterm;
		report.setTitle("Scholar Droid - Error");
		report.setMessage("Oops! Scholar Droid has encountered an error.\n\n " +
				"Possible reasons could be -\n-No Internet connection\n" +
				"-Invalid search term\n-Google Scholar is down\n\n" +
				"If it's none of the above then please choose Menu->Exit " +
				"and restart the app. If the problem persists then either submit an error report " +
				"or wait for an updated version of Scholar Droid with an improved SAX parser. " +
				"Thanks for your patience!");

		report.setPositiveButton("Send report", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
				emailIntent.setType("text/plain"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,"microbuff@hotmail.com");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Scholar Droid bug report"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hi DiodeDroid,\n   " +
						"Scholar Droid is unable to return search results for \""+searchreport+"\". "+
						"Please look into it.\n<Provide details like page number, previous search term, etc. " +
						"so that I can replicate the problem.>\n\n" +
						"Regards\n<Your name>\n\n");
				startActivity(Intent.createChooser(emailIntent, "Send email...")); 
			}
		});

		report.setNegativeButton("Don't send", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		report.create().show();
	}

	/** Clear cache before exiting app. Borrowed code from a discussion on Google Groups. */
	public void purgeCache(Context context) { 
		try { 
			File dir = context.getCacheDir(); 
			if (dir != null && dir.isDirectory()) { 
				deleteDir(dir); 

			} 
		} catch (Exception e) { 
			// TODO: handle exception 
		} 
	} 


	public boolean deleteDir(File dir) { 
		if (dir!=null && dir.isDirectory()) { 
			String[] children = dir.list(); 
			for (int i = 0; i < children.length; i++) { 
				boolean success = deleteDir(new File(dir, children[i])); 
				if (!success) { 
					return false; 
				} 
			} 
		} 
		// The directory is now empty so delete it 
		return dir.delete(); 
	}


	@Override
	protected void onPause() {
		//finish();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		readPrefs();
		setAutoComplete();
		if(!onStart)
			setListeners();
		onStart = false;

	}


	public void setListeners() {
		setEnterPressListener();
		setSearchButtonClickListener();
		setBackNextClickListener();
		setPageEnterListener();

		final ListView lv = (ListView) findViewById(R.id.listresults);
		registerForContextMenu(lv);
		lv.setItemsCanFocus(false);

	}


	@Override 
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {             
		//if(D) Log.d("MyApp","Context menu");     
		menu.setHeaderTitle("Choose Action");  

		menu.add(0, 10, 0, "Select Title");       
		menu.add(0, 11, 0, "Select Author"); 
		menu.add(0, 12, 0, "Select Link");       
		menu.add(0, 13, 0, "Select Text"); 
		menu.add(0, 14, 0, "Select Cited By");       
		menu.add(0, 15, 0, "Select All"); 
		//menu.add(0, 16, 0, "Full text at");       
		//menu.add(0, 17, 0, "All versions"); 

	}     

	@Override 
	public boolean onContextItemSelected(MenuItem item) {     
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		//if(D) Log.d("MyApp","ID=Hello");
		String ctitle = ((TextView) info.targetView.findViewById(R.id.title)).getText().toString();
		String cauthor = ((TextView) info.targetView.findViewById(R.id.author)).getText().toString();
		String clink = ((TextView) info.targetView.findViewById(R.id.link)).getText().toString();
		String ctext = ((TextView) info.targetView.findViewById(R.id.text)).getText().toString();
		String ccites = ((TextView) info.targetView.findViewById(R.id.cites)).getText().toString();
		String call = ctitle+"\n\n"+cauthor+"\n\n"+clink+"\n\n"+ctext+"\n\n"+ccites;

		switch (item.getItemId()) 
		{      
			case 10:    //if(D) Log.d("MyApp","Title="+ctitle); 
						copySearchResult(ctitle);
						break;     
			case 11:    //if(D) Log.d("MyApp","Author="+cauthor); 
						copySearchResult(cauthor);
						break;  
			case 12:    //if(D) Log.d("MyApp","Link="+clink); 
						copySearchResult(clink);
						break;     
			case 13:    //if(D) Log.d("MyApp","Text="+ctext); 
						copySearchResult(ctext);
						break;    		
			case 14:    //if(D) Log.d("MyApp","Cites="+ccites); 
						copySearchResult(ccites);
						break;     
			case 15:    //if(D) Log.d("MyApp","Author="+call); 
						copySearchResult(call);
						break;    
		}     
		return true; 
	}    

	public void copySearchResult(String copydata) {
		AlertDialog.Builder copyResult = new AlertDialog.Builder(Result.this);
		copyResult.setTitle("Copy/Email Result"); 
		// Set an EditText view to get user input  
		final EditText disp = new EditText(Result.this);
		disp.setText(copydata+"\n\n");
		copyResult.setView(disp);  

		copyResult.setPositiveButton("Copy", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				// Copy    
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
				clipboard.setText(disp.getText().toString()); 
				Toast.makeText(Result.this, "Text has been copied to clipboard.",Toast.LENGTH_SHORT).show(); 
			}
		});  

		copyResult.setNeutralButton("Email", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) { 
				// Email    
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
				emailIntent.setType("text/plain"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Google Scholar search result"); 
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, disp.getText().toString());
				startActivity(Intent.createChooser(emailIntent, "Send email...")); 
			}
		}); 

		copyResult.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {      
				// Do nothing
			} 
		});   
		copyResult.show(); 
	}


	/** Read preferences and construct advanced search string */
	public void readPrefs() {
		//SharedPreferences appPref = getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);
		SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		startYear = appPref.getString("startYear", "");
		endYear = appPref.getString("endYear", "");
		journalName = appPref.getString("journal", "");
		authorName = appPref.getString("author", "");
		bio = appPref.getBoolean("bio", false);
		bus = appPref.getBoolean("bus", false);
		chm = appPref.getBoolean("chm", false);
		eng = appPref.getBoolean("eng", false);
		med = appPref.getBoolean("med", false);
		phy = appPref.getBoolean("phy", false);
		soc = appPref.getBoolean("soc", false);
		showAuthor = appPref.getBoolean("showAuthor", true);
		showSummary = appPref.getBoolean("showSummary", true);
		showCitation = appPref.getBoolean("showCitation", true);
		useCustomColor = appPref.getBoolean("useCustomColor", false);
		bgColor = appPref.getString("bgColor","#000000");
		showImage = appPref.getBoolean("showImage", true);
		useProxy = appPref.getBoolean("enableProxy", false);

		if(useProxy) {
			hostname = appPref.getString("proxyHost", "");
			port = appPref.getString("proxyPort", "");
			username = appPref.getString("username", "");
			password = appPref.getString("password", "");

			System.setProperty("http.proxyHost", hostname);  
			System.setProperty("http.proxyPort", port);
			if(!username.equalsIgnoreCase(""))
				System.setProperty("http.proxyUser", username);
			if(!password.equalsIgnoreCase(""))
				System.setProperty("http.proxyPassword", password); 
		}
		else {
			System.setProperty("http.proxyHost", "");  
			System.setProperty("http.proxyPort", ""); 
			System.setProperty("http.proxyUser", "");  
			System.setProperty("http.proxyPassword", "");
		}
		/*
		RelativeLayout imgBackground = (RelativeLayout) findViewById(R.id.mainlayout);
		if(!useCustomColor) {
			//setContentView(R.layout.main);
		}
		else {
			imgBackground.setPadding(0, 10, 0, 0);
			imgBackground.setBackgroundDrawable(null);
			imgBackground.setBackgroundColor(Color.parseColor(bgColor));
		}
		
		imgScholarDroid = (ImageView) findViewById(R.id.icon);
		if(showImage)
			imgScholarDroid.setVisibility(View.VISIBLE);
		else
			imgScholarDroid.setVisibility(View.INVISIBLE);
		downloadPath = appPref.getString("downloadPath", "");
		*/
		//if(D) Log.d("MyApp","Start="+startYear+"\nEnd="+endYear+"\nPath="+downloadPath+"\nEng="+eng);
		//if(D) Log.d("MyApp","Author="+showAuthor+"\nSummary="+showSummary+"\nCite="+showCitation);

		subSearch = "&";
		authorSearch = "";
		if(!startYear.equalsIgnoreCase(""))
			subSearch = subSearch+"as_ylo="+startYear+"&";
		if(!endYear.equalsIgnoreCase(""))
			subSearch = subSearch+"as_yhi="+endYear+"&";
		if(!journalName.equalsIgnoreCase(""))
			subSearch = subSearch+"as_publication="+journalName.replaceAll(" ", "+")+"&";
		if(!authorName.equalsIgnoreCase(""))
			//subSearch = subSearch+"as_sauthors="+authorName.replaceAll(" ", "+")+"&";
			authorSearch = "+author%3A"+authorName.replaceAll(" ", "+");


		if(bio)
			subSearch = subSearch+"as_subj=bio&";
		if(bus)
			subSearch = subSearch+"as_subj=bus&";
		if(chm)
			subSearch = subSearch+"as_subj=chm&";
		if(eng)
			subSearch = subSearch+"as_subj=eng&";
		if(med)
			subSearch = subSearch+"as_subj=med&";
		if(phy)
			subSearch = subSearch+"as_subj=phy&";
		if(soc)
			subSearch = subSearch+"as_subj=soc&";


		advancedSearch = subSearch.substring(0, subSearch.length()-1);
		//if(D) Log.d("MyApp","Subsearch="+advancedSearch); 



	}

	public void refreshSearch() {
		Button mbuttonSearch = (Button)findViewById(R.id.btnsearch);
		mbuttonSearch.setTag(raisedByPage);
		mbuttonSearch.performClick();
	}

	/** Handle screen rotations without killing activity and restore state
	 * Any open Alert dialogs will not disappear by doing it this way
	 * For restarts, SavedInstanceState will be used as before */
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		String searchStrOld = "";
		String pageStrOld = "";
		String pagemaxStrOld = "";

		AutoCompleteTextView searchTextOld = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);
		TextView pageNumOld = (TextView)findViewById(R.id.pagestring);
		TextView pageMaxOld = (TextView)findViewById(R.id.pagemax);

		searchStrOld = searchTextOld.getText().toString();
		pageStrOld = pageNumOld.getText().toString();
		pagemaxStrOld = pageMaxOld.getText().toString();

		//RadioButton radioArticleOld = (RadioButton)findViewById(R.id.articlechoose);    	
		//RadioButton radioBookOld = (RadioButton)findViewById(R.id.bookchoose);    	
		//RadioButton radioBothOld = (RadioButton)findViewById(R.id.bothchoose);
		/*
		if(radioArticleOld.isChecked())
			radioChecked = 1;
		else if(radioBookOld.isChecked())
			radioChecked = 2;
		else if(radioBothOld.isChecked())
			radioChecked = 3;
		*/

		//if(D) Log.d("MyApp","Restore="+searchTextOld.getText().toString()+" "+pageNumOld.getText().toString());
		//if(D) Log.d("MyApp","RadioChecked="+radioChecked);
		super.onConfigurationChanged(newConfig);       
		setContentView(R.layout.main);

		AutoCompleteTextView searchTextNew = (AutoCompleteTextView)findViewById(R.id.txtsearchbox);
		TextView pageNumNew = (TextView)findViewById(R.id.pagestring);
		TextView pageMaxNew = (TextView)findViewById(R.id.pagemax);
		searchTextNew.setText(searchStrOld);
		pageNumNew.setText(pageStrOld);
		pageMaxNew.setText(pagemaxStrOld);

		setListeners();

		readPrefs();
		//RadioButton radioArticleNew = (RadioButton)findViewById(R.id.articlechoose);    	
		//RadioButton radioBookNew = (RadioButton)findViewById(R.id.bookchoose);    	
		//RadioButton radioBothNew = (RadioButton)findViewById(R.id.bothchoose);  

		/*
		if(radioChecked==1)
			radioArticleNew.setChecked(true);
		else if(radioChecked==2)
			radioBookNew.setChecked(true);
		else if(radioChecked==3)
			radioBothNew.setChecked(true); 
		*/
		if(!searchStrOld.equalsIgnoreCase(""))
			refreshSearch();

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

	

}
