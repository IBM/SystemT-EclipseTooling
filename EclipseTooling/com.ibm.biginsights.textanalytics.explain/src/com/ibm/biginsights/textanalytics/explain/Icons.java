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
package com.ibm.biginsights.textanalytics.explain;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class Icons
{

	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +					//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";	//$NON-NLS-1$

 
  public static final Image VIEW_ICON = Activator.getImageDescriptor("icons/AQLview.png").createImage();
  public static final Image DICTIONARY_ICON = Activator.getImageDescriptor("icons/dictionary.gif").createImage();
  public static final Image MODULE_ICON = Activator.getImageDescriptor("icons/Module.png").createImage();
  public static final Image TABLE_ICON = Activator.getImageDescriptor("icons/table2.png").createImage();
  public static final Image FUNCTION_ICON = Activator.getImageDescriptor("icons/function.gif").createImage();
  public static final Image FOLDER_ICON = Activator.getImageDescriptor("icons/folder_open.png").createImage();
  public static final Image INFO_ICON = Activator.getImageDescriptor("icons/info.gif").createImage();
  public static final Image INFO_ITEM = Activator.getImageDescriptor("icons/infoItem.gif").createImage();
  public static final Image INFO_SUBITEM = Activator.getImageDescriptor("icons/infoSubItem.gif").createImage();

  public static final ImageDescriptor EXPORT_OVL_IMGDESC = Activator.getImageDescriptor("icons/overlay_export.png");
  public static final ImageDescriptor EXTERNAL_OVL_IMGDESC = Activator.getImageDescriptor("icons/overlay_external.gif");
  public static final ImageDescriptor OUTPUT_OVL_IMGDESC = Activator.getImageDescriptor("icons/overlay_output.png");
  public static final ImageDescriptor NOTREQUIRED_OVL_IMGDESC = Activator.getImageDescriptor("icons/overlay_empty.gif");
  public static final ImageDescriptor DETERMINISTIC_OVL_IMGDESC = Activator.getImageDescriptor("icons/overlay_deterministic.gif");
}
