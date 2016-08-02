package com.jamesmcguigan.BBCScreenScraper;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;

/**
 * Factory for creating new BBCPage instances
 * @author James McGuigan
 */
public class BBCPageFactory {
	public static enum BBCPageClass {
		BBCRegexPage,
		BBCXMLPage,
	}

	public static BBCPage newBBCPage(URL url, BBCPageClass type) throws IOException, SAXException {
		switch (type) {
		case BBCRegexPage:
			return new BBCRegexPage(url);
		case BBCXMLPage:
			return new BBCXMLPage(url);
		default:
			return null;
		}
	}
}
