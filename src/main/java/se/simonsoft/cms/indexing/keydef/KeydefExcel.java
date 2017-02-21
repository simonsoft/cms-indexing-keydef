/**
 * Copyright (C) 2009-2016 Simonsoft Nordic AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.simonsoft.cms.indexing.keydef;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class KeydefExcel {

	private final InputStream is;
	public KeydefExcel(InputStream is) {
		this.is = is;
	}

	
	public String transform() {
		
		TikaInputStream resource = TikaInputStream.get(is);
		String filename = null;
        String relationshipID = null;
        String mediaType = null;
		//XHTMLContentHandler xhtml = null;
		boolean outputHtml = true;
		
		//handleEmbeddedResource(resource, filename, relationshipID, mediaType, xhtml, outputHtml);
		
		try {
			String xhtml = parseToHTML();
			
			System.out.println(xhtml);
			return xhtml;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String parseToHTML() throws IOException, SAXException, TikaException {
	    ContentHandler handler = new ToXMLContentHandler();
	 
	    AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    
	        parser.parse(is, handler, metadata);
	        return handler.toString();
	}
}
