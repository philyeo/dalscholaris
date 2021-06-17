/** 
 * ***********************************************
 * File		   - OpenURLActivity.java
 * Description - Handles opening of webpages using WebView
 * 				 Gets intent-bundle from Result activity
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs.dal.ca.dalscholaris.R;

import android.content.SharedPreferences; 

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
//import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class OpenURLActivity extends Activity {

	private String strGSurl = "";
	private WebView webview;
	static final int onlyLink = 1;
	static final int freePDF = 2;
	static final int book = 3;
	int type;
	private String downloadlink = "";
	private String downloadPath = "";
	//private boolean D = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
        //ActionBar bar = getActionBar();
        //bar.setDisplayShowHomeEnabled(true);
		//bar.setDisplayShowTitleEnabled(true);		

		SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
		downloadPath = appPref.getString("downloadPath", "/ScholarDroid/");

		webview = (WebView)findViewById(R.id.webview);    
		webview.getSettings().setJavaScriptEnabled(true);

		/** Removed zoom controls since it crashes the app when the user clicks on 
		 * back key before the controls have disappeared from the screen */
		//webview.getSettings().setSupportZoom(true); 
		//webview.getSettings().setBuiltInZoomControls(true);
		//webview.getSettings().setDisplayZoomControls(false);

		webview.setWebViewClient(new GSWebViewClient());

		/** Look for links that the user clicked */
		webview.setDownloadListener(new DownloadListener() {         
			public void onDownloadStart(String url, String userAgent,                 
					String contentDisposition, String mimetype,                 
					long contentLength) { 
				downloadlink = url;
				AlertDialog.Builder alert = new AlertDialog.Builder(OpenURLActivity.this);  
				alert.setTitle("File Access"); 
				alert.setMessage("How do you want to open this link?\nIf download, enter file name"); 
				final EditText input = new EditText(OpenURLActivity.this);
				input.setText("temporary.pdf");
				input.setSingleLine();
				alert.setView(input); 
				alert.setPositiveButton("View", new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int whichButton) { 

						String linkquery;
						try {
							linkquery = URLEncoder.encode(downloadlink,"utf-8");
							type = onlyLink;
							webview.loadUrl("http://docs.google.com/viewer?url="+linkquery);			        			

						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							Toast.makeText(OpenURLActivity.this, "Problem loading URL.", Toast.LENGTH_LONG).show();
						}
					} 
				}); 
				alert.setNeutralButton("Download", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {
						Editable filename = input.getText();
						DownloadFile downloadFile = new DownloadFile(OpenURLActivity.this, downloadPath); 
						downloadFile.execute(downloadlink,filename.toString()); 
					} 
				});
				alert.setNegativeButton("Browser", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							Intent browserIntent = new Intent();
							ComponentName comp = new ComponentName("com.android.browser","com.android.browser.BrowserActivity");
							browserIntent.setComponent(comp);
							browserIntent.setAction(Intent.ACTION_VIEW);
							browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
							browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							browserIntent.setData(Uri.parse(webview.getUrl()));
							startActivity(browserIntent);

						}
						catch(Exception e) {
							Toast.makeText(OpenURLActivity.this, "Problem loading URL.", Toast.LENGTH_LONG).show();
						}
					} 
				});   
				alert.show(); 



			}     
		}); 

		/** Get URL that was passed in and type of file, whether PDF or a webpage */
		Intent i = getIntent();
		Bundle extras = i.getExtras();

		strGSurl = extras.getString("URL");
		type = extras.getInt("type");
		//if(D) Log.d("UrlIntent","type="+type);

		/** Sometimes ViewLink button on Main screen also point to a PDF file instead of webpage. 
		 * So check if really not PDF */
		if(type!=freePDF) {
			Pattern pdflinktag = Pattern.compile("(.*?\\.pdf)",Pattern.MULTILINE);
			Matcher pdflinkmatch = pdflinktag.matcher(strGSurl);
			if (pdflinkmatch.find()) {          
				try {
					String query = URLEncoder.encode(pdflinkmatch.group(1),"utf-8");
					strGSurl = "http://docs.google.com/viewer?url="+query;
					type = freePDF;
					extras.putString("downloadlink", pdflinkmatch.group(1));
					//if(D) Log.d("UrlIntent","download="+pdflinkmatch.group(1));
				} 
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		/** If really PDF, then set variable so that we can download file using it */
		if(type==freePDF)
			downloadlink = extras.getString("downloadlink");
		webview.loadUrl(strGSurl);
		//if(D) Log.d("UrlIntent","url="+strGSurl);
		//webview.loadUrl("http://docs.google.com/viewer?url=http%3A%2F%2Fciteseerx.ist.psu.edu%2Fviewdoc%2Fdownload%3Fdoi%3D10.1.1.81.7673%26rep%3Drep1%26type%3Dpdf");

	}

	/** Clicking links in webview will start a new browser. So the following is needed. */
	private class GSWebViewClient extends WebViewClient {    
		@Override    
		public boolean shouldOverrideUrlLoading(WebView view, String url) {        
			view.loadUrl(url);        
			return true;    
		}
	}

	/** When back physical key is pressed */
	@Override       
	public boolean onKeyDown(int keyCode, KeyEvent event) {           
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {                              
			webview.goBack();               
			return true;           
		}           
		return super.onKeyDown(keyCode, event);      
	}

	/**  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater mi = getMenuInflater();
    	mi.inflate(R.menu.webview_menu,menu);
    	return true;
    }*/

	/** Save to SD card menu option is only enabled if it's a PDF file and not a webpage.
	 * Layout is defined in /res/menu/webview_menu.xml */
	@Override 
	public boolean onPrepareOptionsMenu (Menu menu) { 
		menu.clear();
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.webview_menu,menu);
		if (type!=freePDF)         
			menu.getItem(4).setEnabled(false);     
		return true; 
	} 

	/** What to do when menu options are selected. */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()) {
			case R.id.bookmark: AlertDialog.Builder addBookmark = new AlertDialog.Builder(this);  
								addBookmark.setTitle("Add Favourite"); 
								addBookmark.setMessage("Enter a name for the favourite");  
								// Set an EditText view to get user input  
								final EditText inputBookmark = new EditText(this);
								inputBookmark.setText("example");
								inputBookmark.setSingleLine();
								addBookmark.setView(inputBookmark);  
								addBookmark.setPositiveButton("Save", new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int whichButton) { 
										Editable bookmark = inputBookmark.getText();  // Do something with value!
										if (!bookmark.toString().equalsIgnoreCase("")) {
											SharedPreferences bookmarkPref = getApplicationContext().getSharedPreferences("Bookmarks", MODE_PRIVATE);
											SharedPreferences.Editor bookmarkEditor = bookmarkPref.edit();
											if(!bookmarkPref.contains(bookmark.toString())) {
												bookmarkEditor.putString(bookmark.toString(), webview.getUrl());
												bookmarkEditor.commit();
												//if(D) Log.d("SPref","bm="+bookmarkPref.getString(bookmark.toString(), webview.getUrl()));
												Toast.makeText(OpenURLActivity.this, "The bookmark "+"\""+bookmark.toString()+"\" has been saved.", Toast.LENGTH_LONG).show();
											}
											else
												Toast.makeText(OpenURLActivity.this, "The bookmark "+"\""+bookmark.toString()+"\" already exists.", Toast.LENGTH_LONG).show();
										}
										else
											Toast.makeText(OpenURLActivity.this, "Invalid name.", Toast.LENGTH_LONG).show();

									} 
								});  
								addBookmark.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
									public void onClick(DialogInterface dialog, int whichButton) {      
										// Do nothing
									} 
								});   
								addBookmark.show(); 

								break;

			case R.id.save:		AlertDialog.Builder alert = new AlertDialog.Builder(this);  
								alert.setTitle("File Download and Save"); 
								alert.setMessage("Enter a file name");  
								// Set an EditText view to get user input  
								final EditText input = new EditText(this);
								input.setText("example.pdf");
								input.setSingleLine();
								alert.setView(input);  
								alert.setPositiveButton("Save", new DialogInterface.OnClickListener() { 
									public void onClick(DialogInterface dialog, int whichButton) { 
										Editable filename = input.getText();  // Do something with value!
										if (!filename.toString().equalsIgnoreCase("")) {
											//if(D) Log.d("Download","filename="+filename.toString()+"\nlink="+downloadlink);

											DownloadFile downloadFile = new DownloadFile(OpenURLActivity.this,downloadPath); 
											downloadFile.execute(downloadlink,filename.toString()); 
										}

									} 
								});  
								alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
									public void onClick(DialogInterface dialog, int whichButton) {      
										// Do nothing
									} 
								});   
								alert.show(); 

								break;

			case R.id.email:	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
								emailIntent.setType("text/plain");                 
								emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Scholar Droid link"); 
								if (type==freePDF)
									emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "PDF - "+downloadlink+"\nGoogle Docs Viewer - "+strGSurl);
								else
									emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Link - "+webview.getUrl());
								startActivity(Intent.createChooser(emailIntent, "Send email...")); 
								break;	

			case R.id.homepage:	SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(this);
								String homepage = appPref.getString("homePage", "");
								if(!homepage.equalsIgnoreCase(""))
									webview.loadUrl(homepage);
								break;
								
			case R.id.notes:	AlertDialog.Builder notes = new AlertDialog.Builder(this); 
								//notes.setMessage("Enter a file name");  
								// Set an EditText view to get user input  
								final EditText inputNotes = new EditText(this);
								final File mPath = new File(Environment.getExternalStorageDirectory()+downloadPath);
								File targetFile = new File(mPath,"Notes.txt");
								notes.setTitle(targetFile.toString()); 
								try {
									FileReader notesFile = new FileReader(targetFile);
									BufferedReader bfr = new BufferedReader(notesFile);
									String line = null;
									while((line=bfr.readLine())!=null) {
										inputNotes.append(line);
										inputNotes.append("\n");
									}
									
								} catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									
								}
								//inputNotes.setText("example.pdf");
								//inputNotes.setSingleLine();
								inputNotes.setMinLines(10);
								inputNotes.setGravity(Gravity.TOP);
								notes.setView(inputNotes);  
								notes.setNegativeButton("Save and Close", new DialogInterface.OnClickListener() { 
									public void onClick(DialogInterface dialog, int whichButton) { 
										Editable notesSave = inputNotes.getText();  // Do something with value!
										if (!notesSave.toString().equalsIgnoreCase("")) {
											final File mPath = new File(Environment.getExternalStorageDirectory()+downloadPath);
											File targetFile = new File(mPath,"Notes.txt");
											try {
												FileWriter notesFile = new FileWriter(targetFile,false);

												notesFile.append(notesSave.toString());
												notesFile.flush();
												notesFile.close();				

											}
											catch(Exception e) {}
										}
									} 
								});  
								  
								notes.show(); 

								break;
			
			case R.id.return_search:	OpenURLActivity.this.finish();

		}

		return true;
	}

	/** Following is based on discussions on Stackoverflow. */
	@Override
	protected void onPause() {
		super.onPause();
		webview.clearHistory();
		webview.clearCache(false);
		callHiddenWebViewMethod("onPause");

	}

	@Override     
	protected void onDestroy() {         
		super.onDestroy();                  
		webview.destroy();
		webview = null;
	} 

	/** Webview's page load operation is not really stopped when back key is pressed. 
	 * If a download was initiated, it'll continue in the background.
	 * Uses reflection to call hidden onPause method */
	private void callHiddenWebViewMethod(String name) {     
		if( webview != null ) {         
			try {             
				Method method = WebView.class.getMethod(name);             
				method.invoke(webview);
				//if(D) Log.d("MyApp","Stopping webview");
			} 
			catch (NoSuchMethodException e) {             
				//if(D) Log.d("MyApp", "No such method: " + name + " " +  e);         
			} 
			catch (IllegalAccessException e) {             
				//if(D) Log.d("MyApp", "Illegal Access: " + name + " " +  e);         
			} 
			catch (InvocationTargetException e) {             
				//if(D) Log.d("MyApp", "Invocation Target Exception: " + name + " " +  e);         
			}     
		} 
	} 	

}
