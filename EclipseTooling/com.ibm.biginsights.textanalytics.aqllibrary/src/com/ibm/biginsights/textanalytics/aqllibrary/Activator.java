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
package com.ibm.biginsights.textanalytics.aqllibrary;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ibm.biginsights.textanalytics.aql.library.ModularAQLModel;
import com.ibm.biginsights.textanalytics.aql.library.AQLLibrary;
import com.ibm.biginsights.textanalytics.aql.library.AQLModel;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleLibrary;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {



	// The plug-in ID
	public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.aqllibrary"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public AQLModel model;
  public ModularAQLModel model15;  // AQl Library model for 15 or modular projects
  
  private static AQLLibrary aqlLibrary;
  private static AQLModuleLibrary aql15Library;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		aqlLibrary = AQLLibrary.getInstance ();
    aql15Library = AQLModuleLibrary.getInstance ();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
//Returns the AQL library for 1.3 / non-modular projects
  public static IAQLLibrary getLibrary() {
    return aqlLibrary;
  }
  //Returns the AQL library for 1.5 / modular projects  
  public static IAQLLibrary getModularLibrary() {
    return aql15Library;
  }

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
