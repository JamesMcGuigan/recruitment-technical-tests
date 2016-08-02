package com.jamesmcguigan.BBCScreenScraper;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;
public interface BBCPage {

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
	 * Parses the HTML via mainStoryPattern regexp or xpath
	 * Results accessable via getStoryHref(), getStoryImgSrc(), getStoryImgAlt(), getStoryHeadline(), getStorySummary()
	 * 
	 * Method doesn't need to be syncronized as matcher is fully deterministic based on final this.html
	 * None of the other instance variable depend on each other, or are accessed more than once 
	 * 
	 * @throws ParseException                if unable to match main story 
	 * @throws IOException                   if unable to download URL
	 */
	public abstract void parseMainStory() throws ParseException, IOException;
	
	/**
	 * Part 2
	 * 
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
	 * Parses the downloaded html for paragraphs containing one or more of a list of whole words
	 * A paragraph is defined as a single block of text not interrupted by HTML tags
	 * Words prefixed with -minus will be used as an exclusion filter to remove paragraphs from the list   
	 *
	 * @param words
	 * @return
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public abstract List<String> parseWordParagraphs(List<String> words) throws IOException, XPathExpressionException;

	/**
	 * @return HTML for the entire page
	 */
	public abstract String getHtml();

	/**
	 * @return The URL object for the BBC Page
	 */
	public abstract URL getUrl();
	
	/**
	 * @return The extracted main story headline as requested in part 1
	 */
	public abstract String getStoryHeadline();

	/**
	 * @return The extracted main story summary as requested in part 1
	 */
	public abstract String getStorySummary();

	/**
	 * @return The extracted href from the main story as requested in part 1
	 */
	public abstract String getStoryHref();

	/**
	 * @return HTML for the main story
	 */
	public abstract String getStoryHtml();

}