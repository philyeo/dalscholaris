/**  
* ***********************************************
 * File		   - GSresult.java
 * Description - Class definition for object that contains
 * 				 the parser's results.
 * 				 ArrayList gsresultlist in GSXMLHandler 
 * 				 is constructed out of these objects.
 * ***********************************************
 */
package cs.dal.ca.dalscholaris;

public class GSresult {
	
	private String title;
	private String author;
	private String text;
	private String link;
	private String pdf;
	private String cites;
	private String fulltextlink = "";
	private int type;

	public GSresult() {

	}

	public GSresult(GSresult copy) {
		this.title = copy.title;
		this.author = copy.author;
		this.text = copy.text;
		this.link = copy.link;
		this.cites = copy.cites;
		this.pdf = copy.pdf;
		this.fulltextlink = copy.fulltextlink;
		this.type = copy.type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPDF() {
		return pdf;
	}

	public void setPDF(String pdf) {
		this.pdf = pdf;
	}

	public String getCites() {
		return cites;
	}

	public void setCites(String cites) {
		this.cites = cites;
	}

	public String getFullTextLink() {
		return fulltextlink;
	}

	public void setFullTextLink(String fulltextlink) {
		this.fulltextlink = fulltextlink;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}	

}
