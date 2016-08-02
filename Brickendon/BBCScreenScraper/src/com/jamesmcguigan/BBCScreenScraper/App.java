package com.jamesmcguigan.BBCScreenScraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.jamesmcguigan.BBCScreenScraper.BBCPageFactory.BBCPageClass;

/**
 * Part 3
 *
 * Ensure your code is threadsafe so that I can create a multi-threaded application that
 * could use your class to read the main headline details (Part 1) and find the words (Part 2)
 * simultaneously using only one connection to the website.
 *
 * Include comments explaining how you have ensured thread safety and any assumptions made.
 *
 * 
 *  3rd Party Libraries: 
 *    Commons Lang 3.3.1 - Apache 2.0  - http://mirror.ox.ac.uk/sites/rsync.apache.org/commons/lang/binaries/commons-lang3-3.1-bin.zip
 *    Commons IO 2.1     - Apache 2.0  - http://mirror.ox.ac.uk/sites/rsync.apache.org/commons/io/binaries/commons-io-2.1-bin.zip
 *    HTML Cleaner       - BSD Licence - http://sourceforge.net/projects/htmlcleaner/files/htmlcleaner-2.2.jar/download)
 *   
 *  Install:
 *   Unzip and copy extracted .jar files to ./lib/
 *   This zip file includes these .jar files in the correct location
 *   Ensure this directory in in your classpath
 *
 *  Project Compile and Run: 
 *    cd BBCScreenScraper/src;
 *    javac -cp ".:../lib/*" com/jamesmcguigan/BBCScreenScraper/*.java 
 *    java  -cp ".:../lib/*" com.jamesmcguigan.BBCScreenScraper.App
 *    java  -cp ".:../lib/*" com.jamesmcguigan.BBCScreenScraper.App Euro European Europe
 *    java  -cp ".:../lib/*" com.jamesmcguigan.BBCScreenScraper.App http://www.bbc.co.uk/news/ http://www.bbc.co.uk/news/world/ http://www.bbc.co.uk/news/uk/ http://www.bbc.co.uk/news/england/ http://www.bbc.co.uk/news/northern_ireland/ http://www.bbc.co.uk/news/scotland/ http://www.bbc.co.uk/news/wales/ http://www.bbc.co.uk/news/business/ http://www.bbc.co.uk/news/politics/ http://www.bbc.co.uk/news/health/  http://www.bbc.co.uk/news/education/  http://www.bbc.co.uk/news/science_and_environment/ http://www.bbc.co.uk/news/technology/ http://www.bbc.co.uk/news/entertainment_and_arts/ bank 2011 -tax
 *    
 * @author James McGuigan    
 */
public class App {
	
	/**
	 * This is the command line interface to the class 
	 * @param args [urls] [words]
	 */
	public static void main(String[] args) {
		if( args.length == 0 ) {
			System.out.println("Usage: java  -cp '.:../lib/*' com.jamesmcguigan.BBCScreenScraper.App [urls] [words] [-exclude]");
			return;
		}
		
		List<URL>    urls  = new ArrayList<URL>();
		List<String> words = new ArrayList<String>();
		
		// Duck typing, if its validates a URL then it was meant as a URL, else it was meant as a word
		for( int i=0; i<args.length; i++ ) {
			try {
				URL url = new URL(args[i]);
				urls.add(url);    
			} catch (MalformedURLException e) {
				words.add(args[i]);
			}
		}
		
		if( urls.size() == 0 ) {
			try {
				urls.add(new URL("http://www.bbc.co.uk/news/")); // BBC Homepage is default
			} catch(MalformedURLException e) {
				// This should never happen
				System.out.println("System Error: Invalid URL: http://www.bbc.co.uk/news/"); // But log it anyway
				return;
			}
		}	
		
		//App.singleThreaded(urls, words, BBCPageClass.BBCRegexPage);
		//App.singleThreaded(urls, words, BBCPageClass.BBCXMLPage);

		App.multiThreaded(urls, words, BBCPageClass.BBCRegexPage);
		App.multiThreaded(urls, words, BBCPageClass.BBCXMLPage);
	}
	
	public static void singleThreaded( List<URL> urls, List<String> words, BBCPageClass bbcPageClass ) {
		for( URL url : urls ) {
			try {
				BBCPage page = BBCPageFactory.newBBCPage(url, bbcPageClass);
				
				page.parseMainStory();
	
				System.out.println("*** URL: " + url.toString() + " ***");
				System.out.println("*** Main Story ***");
				//System.out.println("Page HTML:     " + page.getHtml());
				//System.out.println("Story HTML:    " + page.getStoryHtml());
				System.out.println("Main Headline: " + page.getStoryHeadline());
				System.out.println("Main Summary:  " + page.getStorySummary());
				System.out.println("Main Href:     " + page.getStoryHref());
				
				if( words.size() > 0 ) {
					List<String> paragraphs = page.parseWordParagraphs(words);
					System.out.println("");
					System.out.println("*** " + paragraphs.size() + " paragraphs containing: " + StringUtils.join(words, " ") + " ***");
					System.out.println( StringUtils.join(paragraphs, "\n") );
				}
				System.out.println("");
				System.out.println("");
				
			} catch (MalformedURLException e) {
				System.out.println(url.toString() + ": Malformed URL");
			} catch (IOException e) {
				System.out.println(url.toString() + ": Unable to fetch page");
			} catch (XPathExpressionException e) {
				System.out.println(url.toString() + ": " + e.getMessage());
			} catch (SAXException e) {
				System.out.println(url.toString() + ": " + e.getMessage());
			} catch (ParseException e) {
				System.out.println(url.toString() + ": " + e.getMessage());
			}
		}
	}
	
	public static void multiThreaded( final List<URL> urls, final List<String> words, final BBCPageClass bbcPageClass ) {
	    final Map<URL,String>       outputErrorURL   = Collections.synchronizedMap(new HashMap<URL,String>());
	    final Map<URL,String>       outputMainStory  = Collections.synchronizedMap(new HashMap<URL,String>());
	    final Map<URL,List<String>> outputParagraphs = Collections.synchronizedMap(new HashMap<URL,List<String>>());
	    final List<Thread> outerThreads              = Collections.synchronizedList(new ArrayList<Thread>());
	    final List<Thread> innerThreads              = Collections.synchronizedList(new ArrayList<Thread>());
	    
		for( final URL url: urls ) {
			Thread loadUrlThread = new Thread(){
				public void run() {
					try {
						final BBCPage page = BBCPageFactory.newBBCPage(url, bbcPageClass);
						System.out.println( "Loaded: " + url.toString() );

						Thread parseMainStoryThread = new Thread() {
							public void run() {
								try {
									page.parseMainStory();
	
									String output = new StringBuilder()
										//.append("Page HTML:     ").append(page.getHtml()).append("\n")
										//.append("Story HTML:    ").append(page.getStoryHtml()).append("\n")
										.append("Main Headline: ").append(page.getStoryHeadline()).append("\n")
										.append("Main Summary:  ").append(page.getStorySummary()).append("\n")
										.append("Main Href:     ").append(page.getStoryHref()).append("\n")
										.toString();
		
									outputMainStory.put(url, output);
									
								} catch (ParseException e) {
									outputErrorURL.put(url, "Unable to parse main story");
								} catch (IOException e) {
									outputErrorURL.put(url, "Unable to fetch page");
								}
							}
						};
						
						Thread parseWordParagraphsThread = new Thread() {
							public void run() {
								try {
									outputParagraphs.put(url, page.parseWordParagraphs(words));
									
								} catch (IOException e) {
									outputErrorURL.put(url, "Unable to fetch page");
								} catch (XPathExpressionException e) {
									outputErrorURL.put(url, e.getMessage());
								}								
							}
						};
						
						innerThreads.add( parseMainStoryThread );
						innerThreads.add( parseWordParagraphsThread );
						
						parseMainStoryThread.start();
						parseWordParagraphsThread.start();
						
					} catch (IOException e) {
						outputErrorURL.put(url, "Unable to fetch page");
					} catch (SAXException e) {
						outputErrorURL.put(url, e.getMessage());
					}
				}
			};
			outerThreads.add( loadUrlThread );
			loadUrlThread.start();
		}
		
		// Wait for all of the above to complete
		synchronized (outerThreads) {
			for( Thread thread : outerThreads ) {
				try {
					thread.join();
				} catch (InterruptedException e) {} // Do nothing
			}
		}
		
		// We started some parseMainStoryThread and parseWordParagraphsThread inside of loadUrlThread
		// These will not have been added until the outerThreads have returned		
		synchronized (innerThreads) {
			for( Thread thread : innerThreads ) {
				try {
					thread.join();
				} catch (InterruptedException e) {} // Do nothing
			}
		}
		
		
		// Now output the results
		if( outputErrorURL.size() > 0 ) {
			System.out.println("*** Errors ***");
			for( URL url : urls ) { // Keep original order
				if( outputErrorURL.containsKey(url) ) {
					System.out.println( url.toString() + " " + outputErrorURL.get(url) );
				}
			}
			System.out.println("");
		}

		if( outputMainStory.size() > 0 ) {
			System.out.println("*** Main Stories ***");
			for( URL url : urls ) { // Keep original order
				if( outputMainStory.containsKey(url) ) {
					System.out.println( "+++ " + url.toString() );
					System.out.println( outputMainStory.get(url) );
					System.out.println("");
				}
			}
			System.out.println("");
		}
		
		if( outputParagraphs.size() > 0 ) {
			System.out.println("*** Paragraphs containing: " + StringUtils.join(words, " ") + " ***");
			for( URL url : urls ) { // Keep original order
				if( outputParagraphs.containsKey(url) ) {
					System.out.println( "+++ " + url.toString() + " | " + outputParagraphs.get(url).size() + " results" );
					System.out.println( StringUtils.join(outputParagraphs.get(url), "\n") );
					System.out.println("");
				}
			}
		}
	}
}
