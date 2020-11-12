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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.saxon.s9api.Processor;
import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.item.IndexingItemStandalone;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.xmlsource.SaxonConfiguration;
import se.simonsoft.cms.xmlsource.handler.s9api.XmlSourceReaderS9api;
import se.simonsoft.cms.xmlsource.transform.TransformerServiceFactory;

public class HandlerKeydefTest {
	
	@SuppressWarnings("unused")
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
		item.getFields().addField("prop_abx.lang", "sv-SE");
		
		keydef.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("tf_keydefmap"));

		String keydefmap = (String) fields.getFieldValue("tf_keydefmap");
		
		assertEquals("Number of keydef, suppress header row", 7, StringUtils.countMatches(keydefmap, "<keydef keys="));
		
		assertTrue("Number format based on locale - decimal", keydefmap.contains("<keyword>3,2m</keyword>"));
		assertFalse("Number format based on locale - 1000-separator normal space", keydefmap.contains("<keyword>6 000m</keyword>"));
		assertTrue("Number format based on locale - 1000-separator NO-BREAK SPACE", keydefmap.contains("<keyword>6 000m</keyword>")); //Unicode: U+00A0, UTF-8: C2 A0
		
		assertTrue("Date as text is preserved", keydefmap.contains("<keyword>2016-02-01</keyword>"));
		// The date format is not according to Locale with Tika 1.14.
		
		assertTrue("Calculated cell", keydefmap.contains("<keyword>10,24m²</keyword>"));
	}
	
	@Test
	public void testExcelColmissing1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-colmissing1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		item.getFields().addField("prop_abx.lang", "sv-SE");
		
		keydef.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("tf_keydefmap"));

		String keydefmap = (String) fields.getFieldValue("tf_keydefmap");
		
		assertEquals("Number of keydef, validation failure result in none", 0, StringUtils.countMatches(keydefmap, "<keydef keys="));
		
		assertTrue(keydefmap.contains("<!--Sheet failed column count validation.-->"));
		assertTrue(keydefmap.contains("<!--Incorrect column count on row 6: \"Broken\"-->"));
	}
	
	@Test
	public void testExcelColadded1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-coladded1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		item.getFields().addField("prop_abx.lang", "sv-SE");
		
		keydef.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("tf_keydefmap"));

		String keydefmap = (String) fields.getFieldValue("tf_keydefmap");
		
		assertEquals("Number of keydef, validation failure result in none", 0, StringUtils.countMatches(keydefmap, "<keydef keys="));
		
		assertTrue(keydefmap.contains("<!--Sheet failed column count validation.-->"));
		assertTrue(keydefmap.contains("<!--Incorrect column count on row 6: \"BrokenComment|15|m|Some comment.\"-->"));
	}
	
	
	@Test
	public void testExcelComment1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-comment1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		item.getFields().addField("prop_abx.lang", "sv-SE");
		
		keydef.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("tf_keydefmap"));

		String keydefmap = (String) fields.getFieldValue("tf_keydefmap");
		
		//System.out.println(keydefmap);
		
		assertEquals("Number of keydef, now suppressing comments so this can be extracted.", 7, StringUtils.countMatches(keydefmap, "<keydef keys="));
		
		assertTrue(keydefmap.contains("<keydef keys=\"OfficeComment\"><topicmeta><keywords><keyword>15m</keyword></keywords></topicmeta></keydef>"));
		assertTrue(keydefmap.contains("<keydef keys=\"OfficeNote\"><topicmeta><keywords><keyword>10,24m²</keyword></keywords></topicmeta></keydef>"));
	}
	
	@Test
	public void testExcelLang1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-lang1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		
		keydef.handle(item);

		assertTrue("Should extract text", item.getFields().containsKey("tf_keydefmap"));
		String keydefmap = (String) item.getFields().getFieldValue("tf_keydefmap");
		
		//assertEquals("", keydefmap);
		//assertTrue(keydefmap.contains("<!--Sheet failed column count validation.-->")); // Tika 1.14
		assertTrue(keydefmap.contains("<!--Sheet failed empty key validation.-->")); // Tika 1.23
		assertEquals("Number of keydef, validation failure result in none", 0, StringUtils.countMatches(keydefmap, "<keydef keys="));
		
		
		/*IndexingItemProgress*/ item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-lang1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		item.getFields().addField("prop_abx.lang", "sv-SE");
		
		keydef.handle(item);
		assertTrue("Should extract text", item.getFields().containsKey("tf_keydefmap"));
		keydefmap = (String) item.getFields().getFieldValue("tf_keydefmap");
		
		assertTrue(keydefmap.contains("<!--Transforming Excel sheet: \"sv-SE\"-->"));
		assertEquals("Number of keydef, suppress header row", 7, StringUtils.countMatches(keydefmap, "<keydef keys="));
	}
	
	@Test
	public void testExcelKeydefmap1() {
		HandlerKeydefExcel keydef = new HandlerKeydefExcel(sourceReader, tsf);

		IndexingItemProgress item = new IndexingItemStandalone("se/simonsoft/cms/indexing/keydef/Techdata1-keydefmap1.xlsx");
		item.getFields().addField("prop_cms.class", "keydefmap");
		item.getFields().addField("pathext", "xlsx");
		
		
		keydef.handle(item);
		assertTrue("Should extract text", item.getFields().containsKey("tf_keydefmap"));
		String keydefmap = (String) item.getFields().getFieldValue("tf_keydefmap");
		
		assertTrue(keydefmap.contains("<!--Transforming Excel sheet: \"keydefmap\"-->"));
		assertEquals("Number of keydef, suppress header row", 7, StringUtils.countMatches(keydefmap, "<keydef keys="));
	}
	
	
	@Test
	public void testExcelLocaleParser() {
		
		// It is not known which part of Locale is actually used by Tika, see NumberFormatter and POI data formatter.
		// java.text.DecimalFormatSymbols primarily uses getCountry().
		assertEquals("", Locale.ENGLISH, Locale.forLanguageTag("en"));
		assertEquals("", Locale.US, Locale.forLanguageTag("en-US"));
		assertEquals("", new Locale.Builder().setLanguage("en").setRegion("GB").build(), Locale.forLanguageTag("en-GB"));
		
		assertEquals("", "sv", Locale.forLanguageTag("sv").toString());
		assertEquals("Only lang does not work for getCountry()", "", Locale.forLanguageTag("sv").getCountry());
		assertEquals("", "sv_SE", Locale.forLanguageTag("sv-SE").toString());
		assertEquals("Locale seems to prefer", "xyz", Locale.forLanguageTag("sv-XYZ").toString());
		
		assertEquals("", "deu_DE", Locale.forLanguageTag("deu-DE").toString());
		assertEquals("", "DE", Locale.forLanguageTag("deu-DE").getCountry());
		
		assertEquals("Does not convert A-3 Code", "deu", Locale.forLanguageTag("deu").toString());
		assertEquals("", "deu", Locale.forLanguageTag("DEU").toString());
		
		assertEquals("Does not convert A-3 Code", "deu", Locale.forLanguageTag("deu").getLanguage());
		assertEquals("Does not convert A-3 Code", "", Locale.forLanguageTag("deu").getCountry());
		
		assertEquals("", "en_CN", Locale.forLanguageTag("en-CN").toString());
		assertEquals("", "CN", Locale.forLanguageTag("en-CN").getCountry());
		
		assertEquals("", "eng_CN", Locale.forLanguageTag("eng-CN").toString());
		assertEquals("", "CN", Locale.forLanguageTag("eng-CN").getCountry());
		
		assertEquals("", "eng_US", Locale.forLanguageTag("eng-US").toString());
		assertEquals("", "US", Locale.forLanguageTag("eng-US").getCountry());
	}
	

	
}
