package com.jamesmcguigan.BBCScreenScraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

// Start 00:45
// End:  04:15 - 3:30min (Part 1)
// End:  07:45 - 7 hours (Part 1+2)
/**
 * WebScraping Implementation for the BBC website, using regexs
 * @author James McGuigan
 */
public class BBCRegexPage implements BBCPage {
	
	// All final fields are inherently thread safe, written in the constructor and read-only afterwards
	private final URL url;
	private final String html;
	
	// Volatile ensures we have full visibility on these variables as soon as they are written
	private volatile String storyHeadline = "";
	private volatile String storySummary  = "";
	private volatile String storyHref     = "";
	private volatile String storyHTML     = "";
		
	/**
		<div class="large-image" id="top-story">
	   					
			<h2 class="top-story-header ">
				<a href="/news/world-middle-east-17136037" rel="published-1329958690268" class="story">Homs reporters' deaths condemned<img alt="Marie Colvin and Remi Ochlik (file)" src="http://news.bbcimg.co.uk/media/images/58664000/jpg/_58664913_c9dbf679-7705-4741-b15a-ed19a0821a63.jpg"></a>
			</h2>
		   	
		   	<p>The killings of two Western reporters in Homs and the reported deaths of 60 people across Syria trigger Western condemnation of Bashar al'Assad's regime. 	 
		   	<span class="gvl3-icon gvl3-icon-comment comment-count dna-comment-count-show" id="dna-comment-count___CPS__17136037">21</span></p>
	
			<ul class="see-also">
				<li class=" first-child column-1"><a href="/news/world-middle-east-17136427" rel="published-1329958152414" class="story">'Top of the scale of courage'</a></li>
				<li class="has-icon-boxedwatch column-1"><a href="/news/world-middle-east-17120484" rel="published-1329847082579" class="story">Colvin's last BBC TV interview<span class="gvl3-icon gvl3-icon-boxedwatch"> Watch</span></a></li>
				<li class="has-icon-boxedwatch column-1"><a href="/news/uk-17132302" rel="published-1329936526393" class="story">'She may have been targeted'<span class="gvl3-icon gvl3-icon-boxedwatch"> Watch</span></a></li>
				<li class=" column-2"><a href="/news/world-middle-east-17124645" rel="published-1329908416476" class="story">Profiles: Marie Colvin and Remi Ochlik</a></li>
				<li class=" column-2"><a href="/news/uk-17127722" rel="published-1329914183677" class="story">Tributes to reporter Marie Colvin</a></li>
				<li class=" column-2"><a href="/news/world-middle-east-17131958" rel="published-1329928650030" class="story">Obituary: Rami al-Sayed</a></li>
			</ul>
			<hr>
   		</div>
	*/
	private static final Pattern mainStoryPattern  = Pattern.compile(
			"<div[^>]*id=\"[^\">]*top-story[^>]*>" +
			".*?" +
			"<h2[^>]*class=\"[^\">]*top-story-header[^>]*>" + 
				"(?:" +
					 "\\s+"                             + // ignore starting whitespace
					"|<span.*?</span>"                  + // ignore spans (used for new/comment logos)
					"|<ul.*?</ul>"                      + // ul ignore (used for see-also)
					"|<img[^>]*/?>" +
					"|</a>" +
					"|<a[^>]*href=\"([^\">]*)\"[^>]*>"  + // $1 = href
					"|([^<>]+)"                         + // $2 = headline text, in h2 but outside tags
					"|.*?"                              + // failsafe
				")+" +
			"</h2>" +
			".*?" +	
			"<p>([^<]*)"                                + // $3 = summary text
			".*?" +
			"</div>" +
			"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE );
	
	/**
	 * Creates a data model representation of a given URL
	 * Raw HTML is downloaded and cached as final in the constructor to keep things threadsafe 
	 *  
	 * @param  url                     URL of the page to be visited
	 * @throws MalformedURLException   exception throw if the URL is malformed
	 * @throws IOException             exception throw in the URL is inaccessable
	 * @Threadsafe
	 */
	public BBCRegexPage(URL url) throws IOException {
		this.url  = url;
		this.html = fetchHTML(url);
	}
	
	/**
	 * Part 1
	 * 
	 * The frontpage of the BBC News website has a main headline with a brief summary
     * underneath. The task is to write a class to extract the main headline, the summary text and
 	 * the URL linking to the main article from the frontpage.
 	 *
 	 * Write an additional class that is executable from the command line and uses your class to
 	 * display the headline, summary and URL. Include an example of the command I need to enter
 	 * at the command prompt in order to run your code.
 	 *  
	 * Parses the HTML via mainStoryPattern regexp, sets storyHref, storyImgSrc, storyImgAlt, storyHeadline, storySummary
	 * 
	 * Method doesn't need to be syncronized as matcher is fully deterministic based on final this.html
	 * None of the other instance variable depend on each other, or are accessed more than once
	 * 
	 * @throws ParseException  if unable to match main story
	 */
	public void parseMainStory() throws ParseException, IOException {
		Matcher matcher = mainStoryPattern.matcher( this.getHtml() );
		if( matcher.find() ) {
			storyHTML     = matcher.group(0);
			storyHeadline = StringEscapeUtils.unescapeHtml4( matcher.group(2) );
			storySummary  = StringEscapeUtils.unescapeHtml4( matcher.group(3) );
			
			try {
				storyHref = (new URL( url, matcher.group(1) )).toString();
			} catch (MalformedURLException e) {
				storyHref = "";
			}
		} else {
			throw new ParseException("Unable to extract main story", 0);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jamesmcguigan.BBCScreenScraper.BBCPage#parseWordParagraphs(java.util.List)
	 */
	@Override
	public List<String> parseWordParagraphs(List<String> words) throws IOException {
		List<String> quoteWords = new ArrayList<String>();
		List<String> minusWords = new ArrayList<String>();
		
		// Don't trust raw user input - ever
		for( String word : words ) {
			if( word == null || word.equals("") ) {
				continue; // filter, these could mess up the regexp
			} else if( word.startsWith("-") ) {
				minusWords.add(Pattern.quote(word.substring(1)));
			} else {
				quoteWords.add(Pattern.quote(word));
			}
		}
		
		List<String> paragraphs = new ArrayList<String>();
		if( quoteWords.size() > 0 ) { 
			
			// All we need is one whole word out of the set
			// We assume that the paragraph is the space between two HTML tags (this also includes titles)
			String wordRegexp    = "<(?:a|p|span|h2|h3)[^>]*>([^<>]*\\b(?:" + StringUtils.join(quoteWords,"|")  + ")\\b[^<>]*)<[^>]*>";
			String minusRegexp   = "\\b(?:" + StringUtils.join(minusWords,"|")  + ")\\b";
			Pattern wordPattern  = Pattern.compile(wordRegexp,  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Pattern minusPattern = Pattern.compile(minusRegexp, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			
			Matcher matcher = wordPattern.matcher( getHtml() );
			while( matcher.find() ) {
				String paragraph = matcher.group(1);
				if( minusPattern.matcher(paragraph).find() == false ) {
					paragraph = StringEscapeUtils.unescapeHtml4(paragraph); 
					paragraphs.add( paragraph );
				}
			}
		}
		
		return paragraphs;
	}


	/**
	 * Alternative implementation would be to make this.html non-final and then lazy load
	 * via getHtml() when parseMainStory() or parseWordParagraphs() was called.
	 * 
	 * fetchHTML() would be synchronized to ensure only a single connection is made
	 * getHtml() may be synchronized to delay if any concurrent fetchHtml() requests are in progress 
	 * parseMainStory() would be synchronized to ensure the four instance variables are self-consistant 
	 * parseWordParagraphs() would not be synchronized as its stateless and only reads this.html once 
	 * 
	 * @return html
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private String fetchHTML(URL url) throws IOException {
		if( url == null ) {
			throw new IOException("Invalid URL: null");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuffer html = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
		    html.append(line);
		}
		return html.toString();
	}
	
	public String getHtml() {
		return html;
	}
	public URL getUrl() {
		return url;
	}
	public String getStoryHeadline() {
		return storyHeadline;
	}
	public String getStorySummary() {
		return storySummary;
	}
	public String getStoryHref() {
		return storyHref;
	}
	public String getStoryHtml() {
		return storyHTML;
	}	

	public boolean equals(Object o) {
		if( o instanceof BBCPage ) {
			if( getUrl().equals( ((BBCPage) o).getUrl() )
			&&  getHtml().equals( ((BBCPage) o).getHtml() ) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * As we cannot call fetchHTML() a second time after the constructor has completed, 
	 * if we want to reload the HTML from the server, we must clone the BBCPage object
	 */
	public BBCPage clone() {
		try {
			return new BBCRegexPage(url);
		} catch (IOException e) {
			return null;
		}
	}
}



