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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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

public class HandlerKeydefXliff implements IndexingItemHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(HandlerKeydefXliff.class);
	
	private final XmlSourceReaderS9api sourceReader;
	private final TransformerService transformerServiceXliff12;
	
	
	

	@Inject
	public HandlerKeydefXliff(XmlSourceReaderS9api sourceReader, TransformerServiceFactory transformerServiceFactory) {
		
		this.sourceReader = sourceReader;
		this.transformerServiceXliff12 = transformerServiceFactory.buildTransformerService(new StreamSource(this.getClass().getClassLoader().getResourceAsStream("se/simonsoft/cms/indexing/keydef/xliff12.xsl")));
	}


	@Override
	public void handle(IndexingItemProgress progress) {
		
		IndexingDoc f = progress.getFields();
		
		if (!HandlerKeydef.isClass(f, "keydefmap")) {
			return;
		}
		
		String name = (String) f.getFieldValue("pathname");
		String ext = (String) f.getFieldValue("pathext");
		if (!("xlf".equals(ext) || "xliff".equals(ext))) {
			return;
		}
		
		logger.info("Transforming into keydefmap: {}", name);
		// Transforming from inputstream makes SAX parser fail when file has a BOM.
		/*
		try {
			logger.debug("{}", new BufferedReader(new InputStreamReader(progress.getContents())).readLine());
		} catch (Exception e) {}
		*/
		
		InputStream is = progress.getContents();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		TransformOptions transformOptions = HandlerKeydef.getTransformOptions(f);
		try {
			XmlSourceDocumentS9api xmlDoc = sourceReader.read(is);
			transformerServiceXliff12.transform(xmlDoc, new OutputStreamWriter(result), transformOptions);
			
		} catch (XmlNotWellFormedException e) { 
			String msg = MessageFormatter.format("Invalid XML {} skipped. {}", progress.getFields().getFieldValue("path"), e.getCause()).getMessage();
			logger.error(msg, e);
			throw new IndexingHandlerException(msg, e);
		}
		
		
		f.addField(HandlerKeydef.FIELD_KEYDEF, result.toString());
	}


	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

}
