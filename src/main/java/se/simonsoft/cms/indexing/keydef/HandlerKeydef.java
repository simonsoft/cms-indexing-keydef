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

import java.util.Set;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingItemHandler;
import se.repos.indexing.item.IndexingItemProgress;

public class HandlerKeydef implements IndexingItemHandler {
	
	private static final String FIELD_KEYDEF = "rel_tf_keydef"; // Temporary field until changing the schema.

	public HandlerKeydef() {

	}

	
	@Override
	public void handle(IndexingItemProgress progress) {
		
		IndexingDoc f = progress.getFields();
		String result = null;
		
		String ext = (String) f.getFieldValue("pathext");
		String itemClass = (String) f.getFieldValue("prop_cms.class");
		if (itemClass == null || !itemClass.contains("keydef")) {
			return;
		}
		
		if (/*"xls".equals(ext) || */"xlsx".equals(ext)) {
			KeydefExcel xl = new KeydefExcel(progress.getContents());
			
			result = xl.transform();
			
		}
		if (result != null) {
			progress.getFields().addField(FIELD_KEYDEF, result);
		}
		
		
	}
	
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
}
