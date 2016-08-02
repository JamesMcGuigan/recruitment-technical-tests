package com.jamesmcguigan.BBCScreenScraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Start: 00:00
// End:   08:00 (8 hours)
/**
 * This is an alternative implementation based on HTMLCleaner and Java API for XPath (JAXP) 
 */
public class BBCXMLPage implements BBCPage {
	
	// All final fields are inherently thread safe, written in the constructor and read-only afterwards
	private final URL      url;
	private final String   html;
	private final Document htmlDocument;
	
	private final HtmlCleaner       cleaner           = new HtmlCleaner();
	private final CleanerProperties cleanerProperties = cleaner.getProperties();
	private final XPathFactory      xPathFactory      = XPathFactory.newInstance();
	
	// Volatile ensures we have full visibility on these variables as soon as they are written
	private volatile String storyHeadline = "";
	private volatile String storySummary  = "";
	private volatile String storyHref     = "";
	//private volatile String storyHTML     = "";
	private volatile Node storyNode;
	
	/**
	 * Creates a data model representation of a given URL
	 * All instance variables are final, this avoids any synchronization issues 
	 *  
	 * @param  url                     URL of the page to be visited
	 * @throws MalformedURLException   exception throw if the URL is malformed
	 * @throws IOException             exception throw in the URL is inaccessable
	 * @throws SAXException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerConfigurationException 
	 * @Threadsafe
	 */
	public BBCXMLPage(URL url) throws IOException, SAXException {
		this.url          = url;
		this.html         = fetchHTML(url);
		this.htmlDocument = parseHtmlDocument( this.html );
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
	 * @throws SAXException 
	 */
	public synchronized void parseMainStory()  {
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
		
		    try {
				storyNode     = (Node) xPathFactory.newXPath().compile("//*[@id='top-story']").evaluate(getHtmlDocument(), XPathConstants.NODE);
				storyHeadline =        xPathFactory.newXPath().compile("//*[@id='top-story']//a[@class='story']/text()").evaluate(getHtmlDocument(), XPathConstants.STRING).toString().trim();
				storySummary  =        xPathFactory.newXPath().compile("//*[@id='top-story']//p/text()").evaluate(getHtmlDocument(), XPathConstants.STRING).toString().trim();
				String href   =        xPathFactory.newXPath().compile("//*[@id='top-story']//a[@class='story']/@href").evaluate(getHtmlDocument(), XPathConstants.STRING).toString().trim();
				
				try {
					storyHref = (new URL( url, href )).toString();
				} catch (MalformedURLException e) {
					storyHref = "";
				}
			} catch (XPathExpressionException e) {
				System.out.println("XPathExpressionException: " + e.getMessage());
			}
	}
	

	/**
	 * Part 2
	 * Enhance your original class to add functions that can count the number of times a given
	 * word or words appear on the frontpage of the BBC News site and extract the entire
	 * paragraph in which the word or words appear. For example, I may supply you with the
	 * single word “European” or a list including “Euro”, “European” and “Europe”. In the latter
	 * case a paragraph containing both “Euro” and “European” must only appear once in your
	 * output and a paragraph containing none of those words but including the word “Europa”
	 * would be ignored.
	 *
	 * Enhance your executable class to demonstrate the above enhancements and allow me to
	 * specify the single word or list of words I wish to search for from the command line.
	 *
	 * 
	 * Parses the downloaded html for paragraphs containing one or more of a list of whole words
	 * A paragraph is defined as a single block of text not interrupted by HTML tags
	 * Words prefixed with -minus will be used as an exclusion filter to remove paragraphs from the list   
	 * 
	 * As the Document object is not immutable and not threadsafe, we need any method that parses it synchronized
	 * 
	 * @param words
	 * @return
	 * @throws IOException
	 * @throws XPathExpressionException 
	 */
	public synchronized List<String> parseWordParagraphs(List<String> words) throws IOException, XPathExpressionException {
		List<String> quoteWords = new ArrayList<String>();
		List<String> minusWords = new ArrayList<String>();
		
		// Don't trust raw user input - ever
		for( String word : words ) {
			if( word == null || word.equals("") ) {
				continue; // filter, empty strings could mess up the regexp
			} else if( word.startsWith("-") ) {
				minusWords.add(word.substring(1));
			} else {
				quoteWords.add(word);
			}
		}
	
		
		List<String> paragraphs = new ArrayList<String>();
		if( quoteWords.size() > 0 ) { 
			
			// Use $1, $2, $3 etc to quote xpath variables - don't trust raw user data ever
			final HashMap<String, String> quoteHash = new LinkedHashMap<String, String>();
			for( String word : quoteWords ) {
				quoteHash.put( String.valueOf(quoteHash.size()), word );
			}

			// Xpath variables need to be defined outside of quotes
			String xpathString = "//*[contains(text(), $" 
				       + StringUtils.join(quoteHash.keySet(), ") or contains(text(), $" ) 
			           + ")]";
			
			XPath xpath = xPathFactory.newXPath();
			xpath.setXPathVariableResolver(new XPathVariableResolver() {
				public Object resolveVariable(QName variableName) {
					for( String index : quoteHash.keySet() ) {
						if( variableName.equals(new QName( index.toString() ))) {
							return quoteHash.get(index);
						}
					}
					return null;
				}
			});
			NodeList nodeList = (NodeList) xpath.compile(xpathString).evaluate(getHtmlDocument(), XPathConstants.NODESET);

			
			// XPath can't do word boundries, so lets validate the winning entries by regex 
			String wordRegexp    = "\\b(?:" + StringUtils.join(quoteWords,"|")  + ")\\b"; // WARN: always matches if quoteWords.length == 0
			String minusRegexp   = "\\b(?:" + StringUtils.join(minusWords,"|")  + ")\\b"; // WARN: always matches if minusWords.length == 0
			Pattern wordPattern  = Pattern.compile(wordRegexp,  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Pattern minusPattern = Pattern.compile(minusRegexp, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			
			for( int i = 0; i < nodeList.getLength(); i++ ) {
				Node node = nodeList.item(i);
				String text = xPathFactory.newXPath().compile("text()").evaluate(node, XPathConstants.STRING).toString().trim();

				if( "script".equals(node.getNodeName()) == false   
				&&  (quoteWords.size() == 0 || wordPattern.matcher(text).find()  == true ) 
				&&  (minusWords.size() == 0 || minusPattern.matcher(text).find() == false) ) {
					paragraphs.add(text);
				}
			}
		}
		
		return paragraphs;
	}

	/**
	 * Fetches HTML for a given URL
	 * @return HTML
	 * @throws IOException if cannot download URL
	 */
	private synchronized String fetchHTML(URL url) throws IOException {
		if( url == null ) {
			throw new IOException("Invalid URL: null");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		String html = serializeToHTML( cleaner.clean(reader) );
		
		// There is a nasty bug/feature in JAXP for tags such as this, HTMLCleaner doesn't seem to spot this, so lets fix it manually
		// <img src="http://news.bbcimg.co.uk/media/images/58697000/jpg/_58697378_asili&babyxxx_chessington.jpg" />
		// [Fatal Error] :728:117: The reference to entity "babyxxx_chessington.jpg" must end with the ';' delimiter.
		Pattern tagWithAmpPattern = Pattern.compile("<\\w+ [^>]*&(?!([a-z\\d]{2,7}|#\\d{2,5});)[^>]*>");
		Matcher matcher = tagWithAmpPattern.matcher(html);
		StringBuffer sb = new StringBuffer(html.length());
		if( matcher.find() ) {
			String tag = matcher.group(0);
			tag = tag.replaceAll("&(?!([a-z\\d]{2,7}|#\\d{2,5});)", "&amp;");
		    matcher.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		return html;
	}
	
	
	/**
	 * Converts a HTMLCleaner TagNode into a HTML String
	 * @param tagNode
	 * @return html
	 */
	private synchronized String serializeToHTML( TagNode tagNode ) {
		if( tagNode == null ) { 
			return "<!-- Node Not Found -->";
		}
		try {
			StringWriter stringWriter = new StringWriter();
			tagNode.serialize(new PrettyHtmlSerializer(cleanerProperties), stringWriter);
			String html = stringWriter.toString().trim();
			
			return html;
		} catch (IOException e) {
			return "<!-- HTML Serializion Error -->";
		}	
	}
	/**
	 * Converts a org.w3c.dom.Node into a HTML String
	 * @param tagNode
	 * @return html
	 */
	private synchronized String serializeToHTML( Node node ) {
		try {
			// Transformer instances are not-threadsafe
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,     "yes");
			
	        StringWriter writer = new StringWriter();
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
			String html = writer.toString().trim();
			return html;
			
		} catch (TransformerConfigurationException e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		} catch (TransformerFactoryConfigurationError e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		} catch (TransformerException e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		}
	}
	/**
	 * Converts a org.w3c.dom.Document into a HTML String
	 * @param tagNode
	 * @return html
	 */
	private synchronized String serializeToHTML( Document document ) {
		try {
			// Transformer instances are not-threadsafe
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,     "yes");
	        
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			String html = writer.toString().trim();
			return html;
			
		} catch (TransformerConfigurationException e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		} catch (TransformerFactoryConfigurationError e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		} catch (TransformerException e) {
			return "<!-- HTML Serializion Error: " + e.getMessage() + " -->";
		}
	}

	/**
	 * Converts a html string into a DOM Document
	 * @param  html          HTML to be converted
	 * @return Document      Document representation of HTML
	 * @throws SAXException 
	 * @throws IOException
	 */
	public synchronized Document parseHtmlDocument(String html) throws SAXException, IOException {
		try {
			// DocumentBuilderFactory not guaranteed to be thread-safe
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		    domFactory.setNamespaceAware(true); // never forget this!
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    Document document = builder.parse( IOUtils.toInputStream(html, "UTF-8") );
		    return document;
		    
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	private Document getHtmlDocument() {
		return htmlDocument;
	}
	public String getHtml() {
		return serializeToHTML( getHtmlDocument() );
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
		return serializeToHTML( storyNode );
	}	
		
	public boolean equals(Object o) {
		if( o instanceof BBCXMLPage ) {
			if( getUrl().equals( ((BBCXMLPage) o).getUrl() )
			&&  getHtml().equals( ((BBCXMLPage) o).getHtml() ) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * As we cannot call fetchHTML() a second time after the constructor has completed, 
	 * if we want to reload the HTML from the server, we must clone the BBCPage object
	 */
	public BBCXMLPage clone() {
		try {
			return new BBCXMLPage(url);
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}
	}
}



