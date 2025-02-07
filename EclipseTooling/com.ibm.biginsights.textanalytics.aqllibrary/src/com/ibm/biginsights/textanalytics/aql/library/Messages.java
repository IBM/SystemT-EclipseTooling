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
package com.ibm.biginsights.textanalytics.aql.library;

import org.eclipse.osgi.util.NLS;

import com.ibm.biginsights.textanalytics.aqllibrary.Activator;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class Messages extends NLS {


  private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.aql.library.messages"; //$NON-NLS-1$

  public static void LogErrorMessage (String message, Throwable t)
  {
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (message, t);
  }

  public static void ShowErrorMessage (String message, Throwable t)
  {
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (message, t);
  }


  public static String ERROR_CANT_DELETE_MARKERS;
  public static String ERROR_CANT_ADD_MARKERS;

  public static String WRN_AQL_FILE_NOT_COMPILED;

  public static String ERROR_REQ_MODULES_GEN_ERROR;
  public static String ERROR_ERR_MODULE_METADATA;

  public static String ERROR_AQLFILE_READ_ERROR;
  public static String ERROR_PROJECT_NATURE_ERROR; 


	static
	{
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
