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

import static org.junit.Assert.assertTrue;
import net.sf.saxon.s9api.Processor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.item.IndexingItemStandalone;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.xmlsource.SaxonConfiguration;
import se.simonsoft.cms.xmlsource.handler.s9api.XmlSourceReaderS9api;
import se.simonsoft.cms.xmlsource.transform.TransformerServiceFactory;

public class HandlerKeydefTest {
	
	private static CmsRepository repo = new CmsRepository("http://cmshost/svn/repo");
	private static Processor processor;
	private static XmlSourceReaderS9api sourceReader;
	private static TransformerServiceFactory tsf;

	public HandlerKeydefTest() {
		// TODO Auto-generated constructor stub
	}
	
	
	@BeforeClass
	public static void setUp() {
		
		// NOTE: The processor sets unique IDs when using fn:generate-id in XSL. The document counter is increased.
		processor = SaxonConfiguration.getForTesting();
		sourceReader = new XmlSourceReaderS9api(processor);
		tsf = new TransformerServiceFactory(processor, sourceReader);
		
	}

	
	@Test
	public void testExcel1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		
		keydef.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("rel_tf_keydefmap"));

		
		assertTrue("Calculated cell", ((String)fields.getFieldValue("rel_tf_keydefmap")).contains("<td>10.24</td>"));
	}
	
	
}
