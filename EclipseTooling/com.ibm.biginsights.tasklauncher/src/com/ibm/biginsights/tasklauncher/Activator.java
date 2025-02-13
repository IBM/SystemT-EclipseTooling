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
package com.ibm.biginsights.tasklauncher;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
public static final String IBM_COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	// The plug-in ID
	public static final String PLUGIN_ID = "com.ibm.bigInsights.quick.launch.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
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

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
