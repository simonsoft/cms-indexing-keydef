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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

import net.sf.saxon.s9api.DocumentBuilder;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
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
import se.simonsoft.cms.xmlsource.handler.XmlNotWellFormedException;
import se.simonsoft.cms.xmlsource.handler.s9api.XmlSourceDocumentS9api;
import se.simonsoft.cms.xmlsource.handler.s9api.XmlSourceReaderS9api;
import se.simonsoft.cms.xmlsource.transform.TransformOptions;
import se.simonsoft.cms.xmlsource.transform.TransformerService;

public class HandlerKeydefExcel implements IndexingItemHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerKeydefExcel.class);
	
	//private final XmlSourceReaderS9api sourceReader;
	//private final TransformerService transformerServiceXliff12;


	@Override
	public void handle(IndexingItemProgress progress) {
		
		IndexingDoc f = progress.getFields();
		
		if (!HandlerKeydef.isCmsClass(f, "keydefmap")) {
			return;
		}
		
		String name = (String) f.getFieldValue("pathname");
		String ext = (String) f.getFieldValue("pathext");
		if (!("xlsx".equals(ext))) {
			return;
		}
		
		logger.info("Transforming into keydefmap: {}", name);
		
		InputStream is = progress.getContents();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		
		/*
		TransformOptions transformOptions = HandlerKeydef.getTransformOptions(f);
		try {
			XmlSourceDocumentS9api xmlDoc = sourceReader.read(is);
			transformerServiceXliff12.transform(xmlDoc, new OutputStreamWriter(result), transformOptions);
			
		} catch (XmlNotWellFormedException e) { 
			String msg = MessageFormatter.format("Invalid XML {} skipped. {}", progress.getFields().getFieldValue("path"), e.getCause()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		}
		*/
		
		//f.addField(HandlerKeydef.FIELD_KEYDEF, result.toString());
		
		f.addField(HandlerKeydef.FIELD_KEYDEF, transform(is));
	}
	
	public String transform(InputStream is) {
		
		TikaInputStream resource = TikaInputStream.get(is);
		String filename = null;
        String relationshipID = null;
        String mediaType = null;
		//XHTMLContentHandler xhtml = null;
		boolean outputHtml = true;
		
		//handleEmbeddedResource(resource, filename, relationshipID, mediaType, xhtml, outputHtml);
		
		try {
			String xhtml = parseToHTML(is);
			
			System.out.println(xhtml);
			return xhtml;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void parseToSaxonTree() {
		
		//ContentHandler handler = DocumentBuilder.newBuildingContentHandler();
		
	}
	
	public String parseToHTML(InputStream is) throws IOException, SAXException, TikaException {
	    ContentHandler handler = new ToXMLContentHandler();
	 
	    AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    
	        parser.parse(is, handler, metadata);
	        return handler.toString();
	}
	
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
