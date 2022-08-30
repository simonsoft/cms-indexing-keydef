/**
 * Copyright (C) 2009-2017 Simonsoft Nordic AB
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
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.sax.ToXMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingHandlerException;
import se.repos.indexing.IndexingItemHandler;
import se.repos.indexing.item.IndexingItemProgress;

public class HandlerTransformTika implements IndexingItemHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerTransformTika.class);
	
	@Inject
	public HandlerTransformTika() {
		
	}


	@Override
	public void handle(IndexingItemProgress progress) {
		
		IndexingDoc f = progress.getFields();
		
		// Conservative for now, only extract items with class 'tikahtml'
		// There might be a security aspect of having the whole content in SolR (no authz).
		if (!HandlerKeydef.isCmsClass(f, "tikahtml")) {
			return;
		}
		
		String name = (String) f.getFieldValue("pathname");
		/* Can process all file types as long as we require the class attribute.
		String ext = (String) f.getFieldValue("pathext");
		if (!("xlsx".equals(ext))) {
			return;
		}
		*/
		
		logger.info("Extracting Tika HTML: {}", name);
		
		InputStream is = progress.getContents();
		String result;
		
		Locale locale = Locale.US;
		if (HandlerKeydef.getLocaleRfc(f) != null) {
			locale = Locale.forLanguageTag(HandlerKeydef.getLocaleRfc(f));
			logger.debug("Parsing Excel file with Java locale: {}", locale);
		}
		try {
			result = parseToHTML(is, locale);
			//System.out.println(test);
			
			logger.trace("Tika HTML extraction result:\n{}", result);
			
		} catch (IOException e) {
			String msg = MessageFormatter.format("Tika HTML extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (SAXException e) {
			String msg = MessageFormatter.format("Tika HTML extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (TikaException e) {
			String msg = MessageFormatter.format("Tika HTML extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		}
		
		f.addField(HandlerKeydef.FIELD_TIKAHTML, result);
	}
	
	
	public String parseToHTML(InputStream is, Locale locale) throws IOException, SAXException, TikaException {
	    ContentHandler handler = new ToXMLContentHandler();
	 
	    AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    ParseContext context = new ParseContext();
	    
	    context.set(Locale.class, locale);
	    
	    // Experimenting with the new SAX parser for DOCX / PPTX (should not impact search indexing).
	    OfficeParserConfig officeParserConfig = new OfficeParserConfig();
	    officeParserConfig.setUseSAXDocxExtractor(true);
	    officeParserConfig.setUseSAXPptxExtractor(true);
	    context.set(OfficeParserConfig.class, officeParserConfig);
	    
	    parser.parse(is, handler, metadata, context);
	    return handler.toString();
	}
	
	
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
