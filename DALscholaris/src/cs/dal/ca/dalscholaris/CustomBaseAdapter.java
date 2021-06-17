/** 
 * ***********************************************
 * File		   - CustomBaseAdapter.java
 * Description - Adapter for the listview on main screen
 * 				 Instantiated in ResultActivity after parsing completes
 * 				 Resulting ArrayList from parsing is passed
 * 				 to the addResultList function in this class
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cs.dal.ca.dalscholaris.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class CustomBaseAdapter extends BaseAdapter {
	private ArrayList<GSresult> results;	
	private ArrayList<String> dbaselinks;
	private LayoutInflater mInflater;
	Context context;
	int type;
	static final int onlyLink = 1;
	static final int freePDF = 2;
	static final int book = 3;
	static final int bookandPDF = 4;
	private boolean showAuthor;
	private boolean showSummary;
	private boolean showCitation;
	//private boolean D = false;

	public CustomBaseAdapter(Context context, boolean Author, boolean Summary, boolean Citation) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.showAuthor = Author;
		this.showSummary = Summary;
		this.showCitation = Citation;
		dbaselinks = new ArrayList<String>();
		dbaselinks.add("dl.acm.org");
		dbaselinks.add("www.sciencedirect.com");
		dbaselinks.add("www.springerlink.com");
		dbaselinks.add("link.springer.com");
		dbaselinks.add("ieeexplore.ieee.org");
		dbaselinks.add("jstor.org");
				
		//if(D) Log.d("MyApp","Author="+this.showAuthor+"Summary="+this.showSummary+"Cite="+this.showCitation);
	}

	/** Store the ArrayList obtained from parsing in results */
	public void addResultList(ArrayList<GSresult> gsresultlist) {

		results = gsresultlist;
		this.notifyDataSetChanged();
		//refresh();
	}

	public void clear() {
		results.clear();
	}

	public int getCount() {
		return results.size();
	}

	public Object getItem(int position) {
		return results.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	private boolean found(String link) {
		boolean fnd = false;
		for (int i = 0; i < dbaselinks.size(); i++) {
			if(link.contains(dbaselinks.get(i))) {
				fnd = true;
			}		
		}
		return fnd;		
	}


	/* Populates each row of the listview which will be displayed on the main screen.
	 * Decides on which buttons (Amazon/View PDF/View Link) should be shown.
	 * Show/hide author/summary/citedby is used here. */
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row, null);

			convertView.setClickable(false);
			/**convertView.setClickable(false); 
			 convertView.setOnLongClickListener(new View.OnLongClickListener() {              
				 public boolean onLongClick(final View v) {                 
					 // return false to let list's context menu show 
					 //if(D) Log.d("MyApp","Long click Text="+((TextView) v.findViewById(R.id.title)).getText());
					 String copytitle = ((TextView) v.findViewById(R.id.title)).getText().toString();
					 String copyauthor = ((TextView) v.findViewById(R.id.author)).getText().toString();
					 String copylink = ((TextView) v.findViewById(R.id.link)).getText().toString();
					 String copytext = ((TextView) v.findViewById(R.id.text)).getText().toString();
					 String copycites = ((TextView) v.findViewById(R.id.cites)).getText().toString();

					 AlertDialog.Builder copyResult = new AlertDialog.Builder(context);
					 copyResult.setTitle("Copy Result"); 
					 //copyResult.setMessage("Enter a file name");  
					 // Set an EditText view to get user input  
					 final EditText disp = new EditText(context);
					 disp.setText(copytitle+"\n\n"+copyauthor+"\n\n"+copylink+"\n\n"+copytext+"\n\n"+copycites);
					 copyResult.setView(disp);  

					 copyResult.setPositiveButton("Copy", new DialogInterface.OnClickListener() { 
						 public void onClick(DialogInterface dialog, int whichButton) { 
							 // Copy    
							 ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE); 
							 clipboard.setText(disp.getText().toString()); 
							 Toast.makeText(context, "Text has been copied to clipboard.",Toast.LENGTH_SHORT).show(); 
						 }
					 });  

					 copyResult.setNeutralButton("Email", new DialogInterface.OnClickListener() { 
						 public void onClick(DialogInterface dialog, int whichButton) { 
							 // Email    
								Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
								emailIntent.setType("text/plain"); 
								emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Google Scholar search result"); 
								emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, disp.getText().toString());
								context.startActivity(Intent.createChooser(emailIntent, "Send email...")); 
						 }
					 }); 

					 copyResult.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
						 public void onClick(DialogInterface dialog, int whichButton) {      
								// Do nothing
						 } 
					 });   
					 copyResult.show(); 
					 return false;             
				 }         
			 });*/


			holder = new ViewHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			holder.txtAuthor = (TextView) convertView.findViewById(R.id.author);
			holder.txtLink = (TextView) convertView.findViewById(R.id.link);
			holder.txtText = (TextView) convertView.findViewById(R.id.text);
			holder.txtCites = (TextView) convertView.findViewById(R.id.cites);
			holder.btnView = (Button) convertView.findViewById(R.id.viewbutton);
			holder.btnLinkonly = (Button) convertView.findViewById(R.id.linkonlybutton);
			holder.btnAmazon = (Button) convertView.findViewById(R.id.amazonbutton);
			holder.type = onlyLink;
			holder.pdf = null;
			convertView.setTag(holder);
		} 
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtTitle.setText(results.get(position).getTitle());
		if(showAuthor) {
			holder.txtAuthor.setText(results.get(position).getAuthor());
			holder.txtAuthor.setVisibility(View.VISIBLE);
		}
		else {
			holder.txtAuthor.setText("");
			holder.txtAuthor.setVisibility(View.GONE);
		}

		holder.txtLink.setText(results.get(position).getLink());
		if(showSummary) {
			holder.txtText.setText(results.get(position).getText());
			holder.txtText.setVisibility(View.VISIBLE);
		}
		else {
			holder.txtText.setText("");
			holder.txtText.setVisibility(View.GONE);
		}
		//if(results.get(position).getLink().contains("www.sciencedirect.com")) {
		//	convertView.setBackgroundColor(Color.rgb(255, 254, 159));
		//}
		if(found(results.get(position).getLink())) {
			convertView.setBackgroundColor(Color.rgb(255, 254, 159));
		}
		

		if(showCitation) {
			holder.txtCites.setText(results.get(position).getCites());
			holder.txtCites.setVisibility(View.VISIBLE);
		}
		else {
			holder.txtCites.setText("");
			holder.txtCites.setVisibility(View.GONE);
		}

		holder.pdf = results.get(position).getPDF();
		holder.type = results.get(position).getType();

		//if(D) Log.d("CBA","Type="+holder.type+" type="+holder.pdf);
		if (holder.type==onlyLink) {
			holder.btnLinkonly.setText("View Link");
			holder.btnView.setText("View PDF");
			holder.btnAmazon.setText("Amazon");
			/**holder.btnView.setEnabled(false);
			 holder.btnAmazon.setEnabled(false);
			 holder.btnLinkonly.setEnabled(true);*/

			holder.btnAmazon.setVisibility(View.GONE);
			holder.btnView.setVisibility(View.GONE);
			holder.btnLinkonly.setVisibility(View.VISIBLE);

		}
		else if (holder.type==freePDF) {
			holder.btnLinkonly.setText("View Link");
			holder.btnView.setText("View PDF");

			/**holder.btnAmazon.setEnabled(false);
			 holder.btnLinkonly.setEnabled(true);
			 holder.btnView.setEnabled(true);*/

			holder.btnView.setVisibility(View.VISIBLE);
			holder.btnAmazon.setVisibility(View.GONE);
			holder.btnLinkonly.setVisibility(View.VISIBLE);
		}
		else if (holder.type==book) {
			holder.btnLinkonly.setText("View Link");
			holder.btnAmazon.setText("Amazon");
			/**holder.btnView.setEnabled(false);
			 holder.btnLinkonly.setEnabled(true);
			 holder.btnAmazon.setEnabled(true);*/
			holder.btnAmazon.setVisibility(View.VISIBLE);
			holder.btnView.setVisibility(View.GONE);
			holder.btnLinkonly.setVisibility(View.VISIBLE);
		}
		else if (holder.type==bookandPDF)  {			 
			holder.btnLinkonly.setText("View Link");
			holder.btnView.setText("View PDF");
			holder.btnAmazon.setText("Amazon");
			/**holder.btnLinkonly.setEnabled(true);
			 holder.btnView.setEnabled(true);
			 holder.btnAmazon.setEnabled(true);*/
			holder.btnView.setVisibility(View.VISIBLE);
			holder.btnAmazon.setVisibility(View.VISIBLE);
			holder.btnLinkonly.setVisibility(View.VISIBLE);
		}

		holder.btnView.setTag(holder);
		holder.btnView.setOnClickListener(new View.OnClickListener() { 

			public void onClick(View v) {
				ViewHolder vh = new ViewHolder();
				vh = (ViewHolder) v.getTag();
				String strGSurl = vh.pdf;
				String query = "";
				try {
					query = URLEncoder.encode(strGSurl,"utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				String url = "http://docs.google.com/viewer?url="+query;

				//if(D) Log.d("CBA","type="+vh.type+" url="+url);
				Intent i = new Intent(v.getContext(),OpenURLActivity.class);
				i.putExtra("URL",url);
				if((vh.type==freePDF)||(vh.type==bookandPDF))
					i.putExtra("type", freePDF);
				else
					i.putExtra("type", vh.type);
				if((vh.type==freePDF)||(vh.type==bookandPDF))
					i.putExtra("downloadlink", vh.pdf);
				v.getContext().startActivity(i);				 

			}     
		}); 

		holder.btnAmazon.setTag(holder);
		holder.btnAmazon.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View v) {
				ViewHolder vh = new ViewHolder();
				vh = (ViewHolder) v.getTag();

				String url = "http://www.amazon.com/gp/aw/s/ref=is_box_?k="+vh.txtTitle.getText().toString().replaceAll(" ","+");
				//if(D) Log.d("CBA","url="+url);
				Intent i = new Intent(v.getContext(),OpenURLActivity.class);
				i.putExtra("URL",url);
				i.putExtra("type", onlyLink);
				v.getContext().startActivity(i);
			}
		});

		holder.btnLinkonly.setTag(holder);
		holder.btnLinkonly.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View v) {
				ViewHolder vh = new ViewHolder();
				vh = (ViewHolder) v.getTag();
				//if(D) Log.d("CBA","url="+(String) vh.txtLink.getText());
				Intent i = new Intent(v.getContext(),OpenURLActivity.class);
				i.putExtra("URL",(String) vh.txtLink.getText());
				i.putExtra("type", onlyLink);
				v.getContext().startActivity(i);
			}
		});
		return convertView;
	}

	static class ViewHolder {
		TextView txtTitle;
		TextView txtAuthor;
		TextView txtLink;
		TextView txtText;
		TextView txtCites;
		Button btnView;
		Button btnLinkonly;
		Button btnAmazon;
		String pdf;
		int type;
	}

	public void refresh() {
		notifyDataSetChanged();
	}

}
