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
package com.ibm.biginsights.project.templates;

public class TemplateFactory {

	public enum TemplateFactoryKeys {
		BASE_SRC_FOLDER, 			// type: IPackageFragmentRoot 
		BASE_PACKAGE,	 		 	// type: IPackageFragment 		
		
		MR_KEYIN_TYPE_MAPPER, 		// type: String
		MR_VALUEIN_TYPE_MAPPER,		// type: String
		MR_KEYOUT_TYPE_MAPPER, 		// type: String 
		MR_VALUEOUT_TYPE_MAPPER,	// type: String 
		MR_KEYOUT_TYPE_REDUCER, 	// type: String 
		MR_VALUEOUT_TYPE_REDUCER, 	// type: String 
		MR_MAPPER_PACKAGE, 			// type: String 
		MR_REDUCER_PACKAGE,			// type: String 
		MR_DRIVER_PACKAGE, 			// type: String
		MR_MAPPER_CLASSNAME, 		// type: String
		MR_REDUCER_CLASSNAME, 		// type: String
		MR_DRIVER_CLASSNAME,		// type: String 

		BS_MACRO_RETURN_TYPE, 		// type: String
		BS_READER_SUPER_CLASS,		// type: String
		BS_READER_KEY_TYPE, 		// type: String
		BS_READER_VALUE_TYPE		// type: String
	}	
	
	public static ITemplateCreator getDefaultTemplateCreator() {
		return new TemplateCreatorV13();
	}
	
	public static ITemplateCreator getTemplateCreator(String version)
	{
		ITemplateCreator creator = null;	
		/* future: Since we don't generate different code for different BI versions, can always return the 1.3 template creator.
		 *         If we need to differentiate, can check for the passed in project version
		 */
		creator = new TemplateCreatorV13();
		return creator;
	}
	
	public static String extractPackageName(String fullyQualifiedName) {
		String result = null;
		if (fullyQualifiedName.lastIndexOf('.')>-1)
			result = fullyQualifiedName.substring(0, fullyQualifiedName.lastIndexOf('.'));
		return result;
	}
	
	public static String extractClassName(String fullyQualifiedName) {
		return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.')+1);
	}
}
