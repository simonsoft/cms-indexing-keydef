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

import java.util.Arrays;
import java.util.Set;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingItemHandler;
import se.repos.indexing.item.IndexingItemProgress;
import se.simonsoft.cms.xmlsource.transform.TransformOptions;

public abstract class HandlerKeydef {
	
	public static final String FIELD_KEYDEF = "rel_tf_keydefmap"; // Temporary field until changing the schema.

	public HandlerKeydef() {

	}

	
	public static boolean isClass(IndexingDoc f, String name) {
		
		String itemClass = (String) f.getFieldValue("prop_cms.class");
		if (itemClass == null) {
			return false;
		}
		String[] a = itemClass.split(" ");
		
		return Arrays.asList(a).contains(name);
	}
	
	public static TransformOptions getTransformOptions(IndexingDoc f) {
		
		TransformOptions o = new TransformOptions();
		
		if (f.containsKey("patharea")) {
			String patharea = (String) f.getFieldValue("patharea");
			o.setParameter("patharea", patharea);
		}
		if (f.containsKey("prop_abx.TranslationLocale")) {
			String locale = (String) f.getFieldValue("prop_abx.TranslationLocale");
			o.setParameter("locale", locale);
		}
		
		
		if (f.containsKey("prop_keydefmap.prefix")) {
			String prefix = (String) f.getFieldValue("prop_keydefmap.prefix");
			o.setParameter("prefix", prefix);
		}
			
		return o;
	}
	
	//@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
