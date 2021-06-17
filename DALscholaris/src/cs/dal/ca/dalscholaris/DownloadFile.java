/** 
 * ***********************************************
 * File		   - DownloadFile.java
 * Description - AsyncTask for downloading PDF files
 * 				 Adapted from example on Stackoverflow
 * 				 Path to save downloaded file is passed
 * 				 as an argument to the constructor.
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
//import android.util.Log;
import android.widget.Toast;

public class DownloadFile extends AsyncTask<String, Integer, String>{ 
	private Context mcontext;
	private ProgressDialog mProgressDialog;
	private InputStream input;
	private OutputStream output;
	String filename;
	private boolean fileexists;
	private boolean downloadDone;
	private boolean cantDownload;
	private String downloadPath;
	//private boolean D = false;

	protected DownloadFile(Context mcontext, String downloadPath) {
		this.mcontext = mcontext;
		this.downloadPath = downloadPath;
		this.mProgressDialog = new ProgressDialog(this.mcontext);
		//if(D) Log.d("InDownload","Running...");
		this.fileexists = false;
		this.downloadDone = false;
		this.cantDownload = false;
	}

	@Override
	protected String doInBackground(String... path) {         
		int count;         
		try {             
			URL url = new URL(path[0]);
			filename = path[1];
			//if(D) Log.d("Download","url="+url+"\nfilename="+filename);
			//long startTime = System.currentTimeMillis();
			URLConnection conexion = url.openConnection();             
			conexion.connect();             
			// this will be useful so that you can show a typical 0-100% progress bar             
			int lengthOfFile = conexion.getContentLength();              
			// download the file             
			input = new BufferedInputStream(url.openStream());
			String PATH = Environment.getExternalStorageDirectory()+downloadPath;
			File filelocation = new File(PATH);
			if (!filelocation.exists()) 
				filelocation.mkdirs();

			File outputFile = new File(filelocation,filename);
			if (outputFile.exists()&&!filename.equalsIgnoreCase("temporary.pdf")) {
				this.fileexists = true; 
				return null;
			}

			output = new FileOutputStream(outputFile);              
			byte data[] = new byte[1024];              
			long total = 0;              
			while (((count = input.read(data)) != -1)&&(!this.isCancelled())) {                 
				total += count;  
				// publishing the progress....                 
				publishProgress((int)(total*100/lengthOfFile));                 
				output.write(data, 0, count);             
			}              
			output.flush();             
			output.close();             
			input.close();
			if (!this.isCancelled())
				this.downloadDone = true;
			if (total<lengthOfFile)
				outputFile.delete();
			//if(D) Log.d("MyApp", "Download completed in "+ ((System.currentTimeMillis() - startTime) / 1000) + " sec");
		} 
		catch (Exception e) {
			//Toast.makeText(this.mcontext, "File download failed.", Toast.LENGTH_LONG).show();
			this.downloadDone = false;
			this.cantDownload = true;
		}         
		return null;     
	} 

	@Override
	protected void onPreExecute() {
		//if(D) Log.d("InDownload","Preexecute...");
		this.mProgressDialog.setMessage("Downloading file"); 
		this.mProgressDialog.setIndeterminate(false); 
		this.mProgressDialog.setMax(100); 
		this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); 
		this.mProgressDialog.setCancelable(true); 
		this.mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {             
			public void onCancel(DialogInterface dialog) {                 

				DownloadFile.this.cancel(true);
			}         
		}); 
		this.mProgressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {             

			public void onClick(DialogInterface dialog, int which) {                 

				DownloadFile.this.cancel(true);
			}         
		}); 
		this.mProgressDialog.show();

	}

	@Override
	protected void onProgressUpdate(Integer... progress){         

		this.mProgressDialog.setProgress(progress[0]);     	 
	}

	@Override
	protected void onPostExecute(String result) {

		if (this.mProgressDialog.isShowing())
			this.mProgressDialog.dismiss();
		if (fileexists)
			Toast.makeText(this.mcontext, "The file \""+filename+"\" already exists.", Toast.LENGTH_LONG).show();
		else if (this.downloadDone&&!filename.equalsIgnoreCase("temporary.pdf"))
			Toast.makeText(this.mcontext, "The file \""+filename+"\" has been saved.", Toast.LENGTH_LONG).show();
		else if (this.downloadDone&&filename.equalsIgnoreCase("temporary.pdf")) {
			try {
				File tempFile = new File(Environment.getExternalStorageDirectory()+"/ScholarDroid","temporary.pdf");
				//if(D) Log.d("MyApp","file="+tempFile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(tempFile),"application/pdf");
				this.mcontext.startActivity(intent);
			}
			catch (Exception e) {
				Toast.makeText(this.mcontext, "Couldn't open the downloaded temporary file.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}			
		}

		if (this.cantDownload)
			Toast.makeText(this.mcontext, "File download failed.", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onCancelled() {
		if (!this.downloadDone)
			Toast.makeText(this.mcontext, "File download has been cancelled.", Toast.LENGTH_LONG).show();
	}
}
