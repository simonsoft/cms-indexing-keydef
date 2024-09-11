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

import java.util.Arrays;
import java.util.Set;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingItemHandler;
import se.simonsoft.cms.xmlsource.transform.TransformOptions;

public abstract class HandlerKeydef {
	
	public static final String FIELD_KEYDEF = "tf_keydefmap";
	public static final String FIELD_TIKAHTML = "tf_tikahtml";

	public HandlerKeydef() {

	}

	
	public static boolean isCmsClass(IndexingDoc f, String name) {
		
		String itemClass = (String) f.getFieldValue("prop_cms.class");
		if (itemClass == null) {
			return false;
		}
		String[] a = itemClass.toLowerCase().split(" ");
		
		return Arrays.asList(a).contains(name.toLowerCase());
	}
	
	public static String getLocale(IndexingDoc f) {
		
		String locale = null;
		
		// TranslationLocale with fallback to abx:lang where the master locale can be set.
		if (f.containsKey("prop_abx.TranslationLocale")) {
			locale = (String) f.getFieldValue("prop_abx.TranslationLocale");
		} else if (f.containsKey("prop_abx.lang")) {
			locale = (String) f.getFieldValue("prop_abx.lang");
		}
		
		return locale;
	}
	
	public static String getLocaleRfc(IndexingDoc f) {
		
		String locale = null;
		
		// TranslationLocale with fallback to abx:lang where the master locale can be set.
		// The RFC variant of the properties take precedence.
		if (f.containsKey("prop_abx.TranslationLocaleRfc")) {
			locale = (String) f.getFieldValue("prop_abx.TranslationLocaleRfc");
		} else if (f.containsKey("prop_abx.TranslationLocale")) {
			locale = (String) f.getFieldValue("prop_abx.TranslationLocale");
			
		} else if (f.containsKey("prop_abx.ReleaseLocaleRfc")) {
			locale = (String) f.getFieldValue("prop_abx.ReleaseLocaleRfc");
		} else if (f.containsKey("prop_abx.ReleaseLocale")) {
			locale = (String) f.getFieldValue("prop_abx.ReleaseLocale");
			
		} else if (f.containsKey("prop_cms.locale")) {
			// Also looking at cms:locale as an RFC compliant override of abx:lang.
			locale = (String) f.getFieldValue("prop_cms.locale");	
		} else if (f.containsKey("prop_abx.lang")) {
			locale = (String) f.getFieldValue("prop_abx.lang");
		}
		
		return locale;
	}
	
	
	public static TransformOptions getTransformOptions(IndexingDoc f) {
		
		TransformOptions o = new TransformOptions();
		
		if (f.containsKey("patharea")) {
			String patharea = (String) f.getFieldValue("patharea");
			o.setParameter("patharea", patharea);
		}
		
		if (f.containsKey("prop_keydefmap.prefix")) {
			String prefix = (String) f.getFieldValue("prop_keydefmap.prefix");
			o.setParameter("prefix", prefix);
		}
		
		String locale = getLocale(f);
		if (locale != null) {
			o.setParameter("locale", locale);
		}
		
		if (HandlerKeydef.isCmsClass(f, "dita1x")) {
			o.setParameter("enableDita1X", true);
		} 
		if (HandlerKeydef.isCmsClass(f, "dita20")) {
			o.setParameter("enableDita20", true);
		}
		if (!HandlerKeydef.isCmsClass(f, "dita1x") && !HandlerKeydef.isCmsClass(f, "dita20")) {
			o.setParameter("enableDita1X", true); // Default remains Dita 1.X during CMS 5.2.
		}
			
		return o;
	}
	
	//@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
