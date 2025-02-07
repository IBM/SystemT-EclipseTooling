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
package com.ibm.biginsights.textanalytics.resultdifferences;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {



	// The plug-in ID
	public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.resultdifferences"; //$NON-NLS-1$

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

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, "icons/"+path); //$NON-NLS-1$
	}
	
	  /**
	   * Returns an image from the icons/ subdirectory of the plugin. The images are cached in the
	   * ImageRegistry of the plugin, so that their disposal is not necessary.
	   * 
	   * @param imageName
	   *          the name of the icon (without path)
	   * @return the image
	   */
	  public static Image getImage(String imageName) {
	    ImageRegistry imageRegistry = getDefault().getImageRegistry();
	    Image image = imageRegistry.get(imageName);
	    if (image != null) {
	      return image;
	    }

	    imageRegistry.put(imageName, getImageDescriptor(imageName));
	    return imageRegistry.get(imageName);
	  }

	  public static ImageDescriptor getImageDescriptor(String pluginName, String imageName) {
		    return AbstractUIPlugin.imageDescriptorFromPlugin(pluginName, imageName);
		  }

}
