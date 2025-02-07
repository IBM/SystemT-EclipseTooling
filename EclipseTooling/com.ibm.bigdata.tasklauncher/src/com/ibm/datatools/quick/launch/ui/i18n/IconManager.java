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

package com.ibm.datatools.quick.launch.ui.i18n;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.ibm.datatools.quick.launch.ui.Activator;

/**
 * Manage icons for prototypes
 * 
 * 
 * @version 1.2
 * @since v1r1	
 * Jan 23, 2004	jruggles CHANGE_DESCRIPTION
 * 2007Jul05	jruggles	Add wizard icons
 * 2008Sep05	hlhuang		add decorator icons                                                  
 */
public class IconManager {


	
	public static final String QUICK_LAUNCH_UI_ICONS_FOLDER_URL = "platform:/plugin/com.ibm.bigdata.tasklauncher/icons/"; //$NON-NLS-1$
	
	public static final String CONNECT_TO = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "connect_to.gif"; //$NON-NLS-1$
	public static final String CAT_BROWSE = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "catbrowse.gif"; //$NON-NLS-1$
	public static final String NEW_CONNECTION = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "newconnection.gif"; //$NON-NLS-1$
	public static final String NEW_SCRIPT = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "newscript.gif"; //$NON-NLS-1$
	public static final String BANNER = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "banner01.gif"; //$NON-NLS-1$
	public static final String CAMERA = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "camera.gif"; //$NON-NLS-1$
	public static final String OPTIM_BANNER_OLD = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "optimBanner1.gif"; //$NON-NLS-1$
    public static final String OPTIM_BANNER3 = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "optimbanner03.gif"; //$NON-NLS-1$
    public static final String OPTIM_BANNER = QUICK_LAUNCH_UI_ICONS_FOLDER_URL + "optimBanner.png"; //$NON-NLS-1$
    
	@SuppressWarnings("unchecked")
    public static String[] getIconKeys() {
		ArrayList<String> iconKeys = new ArrayList<String>();

		Class c;
		try {
			c = Class.forName("IconManager"); //$NON-NLS-1$
			Field f[] = c.getDeclaredFields();
			for (int i = 0; i < f.length; i++) {
				String val = null;
				try {
					val = (String) f[i].get(val);
				} catch (IllegalArgumentException e) {
					// Do nothing
				} catch (IllegalAccessException e) {
					 // Do nothing
				}
				if (val != null)
					iconKeys.add(val);
			}
		} catch (ClassNotFoundException e) {
			// Do nothing
		}
		return iconKeys.toArray(
				new String[iconKeys.size()]);
	}
	
	public static Image getImage(String p_key){
		ImageRegistry registry = Activator.getDefault().getImageRegistry(); 
		Image image =  registry.get(p_key);
		if(image==null) {
			try {
				URL url = new URL(p_key);
				ImageDescriptor descr = ImageDescriptor.createFromURL(url);
				image = descr.createImage();
				registry.put(p_key, descr);
			} catch (IOException e) {
				System.err.println("Unable to resolve url"+p_key); //$NON-NLS-1$
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()+" Unable to resolve url"+p_key)); //$NON-NLS-1$
			}
		}
		return image;
	}
	public static ImageDescriptor getImageDescriptor(String p_key){
		ImageRegistry registry = Activator.getDefault().getImageRegistry(); 
		ImageDescriptor descr =  registry.getDescriptor(p_key);
		if(descr==null) {
			try {
				URL url = new URL(p_key);
				descr = ImageDescriptor.createFromURL(url);
				descr.createImage();
				registry.put(p_key, descr);
			} catch (IOException e) {
				System.err.println("Unable to resolve url"+p_key); //$NON-NLS-1$
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}
		return descr;
	}

}
