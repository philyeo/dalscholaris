/** 
 * ***********************************************
 * File		   - GSXMLHandler.java
 * Description - DAL Scholar SAX Parser
 * 				 Called within Result activity
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GSXMLHandler extends DefaultHandler {

	static ArrayList<GSresult> gsresultlist = new ArrayList<GSresult>();
	GSresult gsresult = new GSresult();
	StringBuilder builder = new StringBuilder();
	static String currenttext = null;
	static int pageresultcount = 0;
	static Pattern resulttag;
	static Matcher resultmatch;
	static Pattern citestag;
	static Matcher citesmatch;
	static int numresults = 0;
	static int level = 0;

	/** The following variables act as flags to keep track of state during parsing */
	static final int inGSR = 1;
	static final int inPDF = 2;
	static final int incheckPDF = 3;
	static final int outcheckPDF = 4;
	static final int outPDF = 5; 
	static final int inGSRI = 6;
	static final int inGSRT = 7;
	static final int incheckBOOK = 8;
	static final int outcheckBOOK = 9;
	static final int inFULLTEXT = 10;
	static final int outFULLTEXT = 10;
	static final int inLINK = 10;
	static final int inTITLE = 11;
	static final int outTITLE = 12;
	static final int outLINK = 13;
	static final int outGSRT = 14;

	//static int inBOOK = 3;


	static final int inAUTHOR = 15;
	static final int outAUTHOR = 16;
	static final int inTEXT = 17;
	static final int outTEXT = 18;
	static final int inCITE = 19;
	static final int outCITE = 20;
	static final int outGSRI = 21;	
	static final int outGSR = 0;

	static int type = 1;
	static final int onlyLink = 1;
	static final int freePDF = 2;
	static final int book = 3;
	static final int bookandPDF = 4;
	private boolean isfreePDF = false;
	private boolean isBOOK = false;
	//private boolean D = false;

	//public static GSresultList gsresultlist = null;
	//private static final String TAG = "MyActivity";

	/** ArrayList that contains numresults number of results */
	public ArrayList<GSresult> getGSresultList() {
		return gsresultlist;
	}

	public void clearGSresultList() {
		gsresultlist.clear();
	}

	/** Number of results returned at the end of parsing. Usually 10. */
	public int getNumResults() {
		return numresults;
	}

	/** The parsing flow is as below. Terms in parentheses may not always appear during parsing. 
	 * inGSR->(inPDF->check if PDF/HTML link (incheckPDF->outcheckPDF)->outPDF)->
	 * inGSRI->inGSRT->(check if [BOOK] appears->)inLINK->inTITLE->outTITLE->outLINK->outGSRT->
	 * inAUTHOR->outAUTHOR->inTEXT->outTEXT->(inCITE->outCITE)->outGSRI->outGSR */

	/** State setting begins once "<div>","<span>","<a>", etc appear in the XML */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (localName.equals("div")&&(attributes.getLength()>=1)) {

			currenttext = attributes.getValue(0);
			if (currenttext.equalsIgnoreCase("gs_r")) {
				level = inGSR;
				pageresultcount++;
				//if(D) Log.d(TAG,"Found");
			} //#3
			// Previously "gs_ggs gs_fl"
			else if ((level==inGSR)&&(currenttext.equalsIgnoreCase("gs_md_wp"))) {
				level = inPDF;
				//if(D) Log.d(TAG,"pdf=yes");
			} //#3
			else if (((level==inGSR)||(level==outPDF))&&(currenttext.equalsIgnoreCase("gs_ri"))) {
				level = inGSRI;
			} //#3
			else if ((level==outGSRT)&&(currenttext.equalsIgnoreCase("gs_a"))) {
				level = inAUTHOR;
				builder.setLength(0);
			} //#3
			else if ((level==outAUTHOR)&&currenttext.equalsIgnoreCase("gs_rs")) {
				level = inTEXT;
				builder.setLength(0);
			} //#3
			else if ((level==outTEXT)&&currenttext.equalsIgnoreCase("gs_fl")) {
				level = inCITE;
				builder.setLength(0);

			} //#3
			else if ((level==outcheckPDF)&&currenttext.equalsIgnoreCase("gs_br")) {
				level = inFULLTEXT;

			} //#3

			/** Get total number of results that Google Scholar displays on upper right corner. */
			if (pageresultcount==1) {
				/** New Google Scholar shows it on upper left */
				resulttag = Pattern.compile("[\\w]*\\s?([0-9,]+) results",Pattern.MULTILINE);			
				//if(D) Log.d(TAG,"\nString="+builder.toString());
				resultmatch = resulttag.matcher(builder.toString());
				if (resultmatch.find()) {
					NumberFormat format = NumberFormat.getInstance(Locale.US);          
					try {
						//if(D) Log.d(TAG,"Match="+resultmatch.group(1).toString());
						numresults = format.parse(resultmatch.group(1).replaceAll("About", "").trim()).intValue();
						//if(D) Log.d(TAG,"Match="+numresults);
					} catch (ParseException e) {
						e.printStackTrace();
					} 

				}
			}

		}
		else if (localName.equals("h3")&&(level==inGSRI)) {
			currenttext = attributes.getValue("class");			
			if(currenttext.equalsIgnoreCase("gs_rt"))
				level = inGSRT;
		} // #3

		else if (localName.equals("a")) {
			if (level==inPDF) {
				//if(D) Log.d(TAG,"pdf="+attributes.getValue("href"));
				gsresult.setPDF(attributes.getValue("href"));
			} // #3
			else if (level==inFULLTEXT) {
				//if(D) Log.d(TAG,"pdf="+attributes.getValue("href"));
				gsresult.setFullTextLink(attributes.getValue("href"));
				level = outFULLTEXT;
			} // #3
			else if ((level==inGSRT)||(level==outcheckBOOK)) {
				//if(D) Log.d(TAG,"link="+attributes.getValue("href"));
				gsresult.setLink(attributes.getValue("href"));
				builder.setLength(0);
				level = inTITLE;
				if((!isfreePDF)&&(!isBOOK))
					gsresult.setType(onlyLink);

			} // #3

		}
		else if (localName.equals("span")&&(attributes.getLength()>=1)) {
			currenttext = attributes.getValue("class");

			if ((level == inPDF) && currenttext.equalsIgnoreCase("gs_ctg2")) {
				level = incheckPDF;
				builder.setLength(0);

			} //#3
			else if ((level == inGSRT) && currenttext.equalsIgnoreCase("gs_ctc")) {
				level = incheckBOOK;
				builder.setLength(0);

			} //#3

		} 

	}

	/** State resetting once once end of "<div>","<span>","<a>", etc appear in the XML */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equalsIgnoreCase("div")) {
			// Only "Full text" appears
			if (level==inPDF) {
				level = outPDF;
			} // #3
			else if ((level==outcheckPDF)||(level==outFULLTEXT)) {
				level = outPDF;
			} // #3
			else if (level==inAUTHOR) {
				//if(D) Log.d(TAG,"author="+builder.toString());
				gsresult.setAuthor(builder.toString());

				level = outAUTHOR;
			} //#3
			else if (level==inTEXT) {
				//if(D) Log.d(TAG,"text="+builder.toString());
				gsresult.setText(builder.toString().replaceAll("\n", " "));
				builder.setLength(0);
				level = outTEXT;
			} //#3
			else if (level==inCITE) {
				level = outCITE;
				citestag = Pattern.compile("Cited by (.*?) .*?",Pattern.MULTILINE);
				citesmatch = citestag.matcher(builder.toString());
				if (citesmatch.find()) {          
					try {						
						//if(D) Log.d(TAG,"Cites="+citesmatch.group(1));
						gsresult.setCites("Cited by - "+citesmatch.group(1));
					} 
					catch (Exception e) {
						e.printStackTrace();
					} 

				}
				else
					gsresult.setCites("Cited by - NA");

			} //#3
			else if (level==outCITE) {
				level = outGSRI;
				//if(D) Log.d(TAG,"type="+gsresult.getType());

			} //#3
			else if (level==outGSRI) {
				level = outGSR;
				gsresultlist.add(new GSresult(gsresult));
				pageresultcount = 0;
				isfreePDF = false;
				isBOOK = false;
			} //#3
			else if (level==outTEXT) {
				level = outGSRI;
				gsresult.setCites("Cited by - NA");
			} //#3


		}
		else if (localName.equalsIgnoreCase("h3")) {
			if(level==outTITLE) {
				level = outGSRT;
			}
		} //#3
		else if (localName.equalsIgnoreCase("a")) {
			if (level==inTITLE) {
				level = outTITLE;
				//if(D) Log.d(TAG,"title="+builder.toString());
				gsresult.setTitle(builder.toString());

			} //#3

		}
		else if (localName.equalsIgnoreCase("span")) {
			if(level==inPDF) {
				level = outcheckPDF;
			} //#3
			else if (level==incheckPDF) {
				//if(D) Log.d("MyApp","Type="+builder.toString());
				if (builder.toString().equalsIgnoreCase("[PDF]")) { 
					isfreePDF = true;
					gsresult.setType(freePDF);
				}
				level = outcheckPDF;
			} //#3
			else if (level==incheckBOOK) {
				if (builder.toString().equalsIgnoreCase("[BOOK]")) { 
					isBOOK = true;
					if (isfreePDF)
						gsresult.setType(bookandPDF);
					else
						gsresult.setType(book);	
					//if(D) Log.d("MyApp","isPDF="+gsresult.getType());
				}
				level = outcheckBOOK;
			} //#3
		} 


	}

	/** Save data that appears between relevant start and end tags in the XML */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		currenttext = new String(ch, start, length);
		builder.append(currenttext);

	}
}
