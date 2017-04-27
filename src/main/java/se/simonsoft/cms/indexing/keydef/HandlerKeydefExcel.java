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
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.BuildingContentHandler;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
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
import se.simonsoft.cms.xmlsource.transform.TransformerServiceFactory;

public class HandlerKeydefExcel implements IndexingItemHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerKeydefExcel.class);
	
	private final XmlSourceReaderS9api sourceReader;
	private final DocumentBuilder db;
	private final TransformerService transformerService;

	@Inject
	public HandlerKeydefExcel(XmlSourceReaderS9api sourceReader, TransformerServiceFactory transformerServiceFactory) {
		
		this.sourceReader = sourceReader;
		this.db = sourceReader.getProcessor().newDocumentBuilder();
		this.transformerService = transformerServiceFactory.buildTransformerService(new StreamSource(this.getClass().getClassLoader().getResourceAsStream("se/simonsoft/cms/indexing/keydef/excel.xsl")));
	}


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
		
		Locale locale = Locale.US;
		if (HandlerKeydef.getLocale(f) != null) {
			locale = Locale.forLanguageTag(HandlerKeydef.getLocale(f));
			logger.debug("Parsing Excel file with Java locale: {}", locale);
		}
		TransformOptions transformOptions = HandlerKeydef.getTransformOptions(f);
		try {
			boolean debug = false;
			if (debug) {
				String test = parseToHTML(is, locale);
				System.out.println(test);
				is = progress.getContents();
			}
			
			XmlSourceDocumentS9api xmlDoc = parseToSaxonTree(is, locale);
			
			transformerService.transform(xmlDoc, new OutputStreamWriter(result), transformOptions);
			System.out.println("Transformed result:");
			System.out.println(result.toString());
			
		} catch (XmlNotWellFormedException e) { 
			String msg = MessageFormatter.format("Excel keydefmap extraction provided invalid XHTML {} - {}", progress.getFields().getFieldValue("path"), e.getCause()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (SaxonApiException e) {
			String msg = MessageFormatter.format("Excel keydefmap extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (IOException e) {
			String msg = MessageFormatter.format("Excel keydefmap extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (SAXException e) {
			String msg = MessageFormatter.format("Excel keydefmap extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		} catch (TikaException e) {
			String msg = MessageFormatter.format("Excel keydefmap extraction failed {} - {}", progress.getFields().getFieldValue("path"), e.getMessage()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		}
		
		f.addField(HandlerKeydef.FIELD_KEYDEF, result.toString());
	}
	
	
	public XmlSourceDocumentS9api parseToSaxonTree(InputStream is, Locale locale) throws SaxonApiException, IOException, SAXException, TikaException {
		
		BuildingContentHandler handler = this.db.newBuildingContentHandler();
		AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    ParseContext context = new ParseContext();
	    context.set(Locale.class, locale);
	    
	    parser.parse(is, handler, metadata, context);
		
	    XmlSourceDocumentS9api xmlDoc = new XmlSourceDocumentS9api(handler.getDocumentNode(), this.sourceReader.buildSourceElement(XmlSourceReaderS9api.getDocumentElement(handler.getDocumentNode())), null);
	    return xmlDoc;
	}
	
	public String parseToHTML(InputStream is, Locale locale) throws IOException, SAXException, TikaException {
	    ContentHandler handler = new ToXMLContentHandler();
	 
	    AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    ParseContext context = new ParseContext();
	    context.set(Locale.class, locale);
	    
	    parser.parse(is, handler, metadata, context);
	    return handler.toString();
	}
	
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
