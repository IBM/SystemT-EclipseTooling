/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.biginsights.textanalytics.resourcechange;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {


	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.resourcechange.messages"; //$NON-NLS-1$
	public static String AQLResourceChangeActionDelegate_ATTRIBS_OF_LAUNCH_CONFIGS_UPDATED;
	public static String AQLResourceChangeActionDelegate_MANUALLY_MIGRATE;
	public static String AQLResourceChangeActionDelegate_PROPERTIES_OF_PROJECTS_UPDATED;
	public static String AQLResourceChangeActionDelegate_VISIT_RESOURCES_TO_ENSURE_CORRECTNESS;
	public static String AQLResourceChangeActionDelegate_WARNING;
	public static String GoldStandardResourceChangeActionDelegate_CANNOT_COPY_DIRECTLY_UNDER_PROJECT;
	public static String GoldStandardResourceChangeActionDelegate_CANNOT_COPY_TO_NON_LC_ROOT_DIR;
	public static String GoldStandardResourceChangeActionDelegate_CANNOT_COPY_TO_RESTRICTED_FOLDER;
	public static String ResultResourceChangeActionDelegate_CANNOT_COPY_DIRECTLY_UNDER_PROJECT;
	public static String ResultResourceChangeActionDelegate_CANNOT_COPY_TO_NON_RESULT_ROOT_DIR;
	public static String ResultResourceChangeActionDelegate_CANNOT_COPY_UNDER_RESTRICTED_FOLDER;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
