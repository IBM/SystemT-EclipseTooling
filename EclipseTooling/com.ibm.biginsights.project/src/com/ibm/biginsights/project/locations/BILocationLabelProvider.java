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
package com.ibm.biginsights.project.locations;

import java.io.File;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppCategoryFolder;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppTypeFolder;
import com.ibm.biginsights.project.locations.apps.IBigInsightsApp;


public class BILocationLabelProvider extends LabelProvider implements ILabelDecorator{

	
	@Override
	public Image getImage(Object element) {
		if (element instanceof BigInsightsLocationRoot)
			return Activator.getDefault().getImage("/icons/serverRoot.gif"); //$NON-NLS-1$
		else if (element instanceof IBigInsightsLocation)
			return Activator.getDefault().getImage("/icons/server.gif"); //$NON-NLS-1$
		else if (element instanceof IBigInsightsApp)
			return Activator.getDefault().getImage("/icons/defaultApp_16x.gif"); //$NON-NLS-1$
		else if (element instanceof BigInsightsAppFolder ||
				 element instanceof BigInsightsAppCategoryFolder ||
				 element instanceof BigInsightsAppTypeFolder)
			return Activator.getDefault().getImage("/icons/folderApp_16x.gif"); //$NON-NLS-1$
		else if (element instanceof BigInsightsConfFolder)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		else if (element instanceof File) {
			File f = (File)element;
			if (f.isDirectory()) 
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			else
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE); 
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IBigInsightsApp){
			return ((IBigInsightsApp)element).getAppName();
		}
		else if (element instanceof BigInsightsAppCategoryFolder){
			return ((BigInsightsAppCategoryFolder)element).getCategoryName();
		}
		else if (element instanceof BigInsightsAppTypeFolder){
			return ((BigInsightsAppTypeFolder)element).getTypeName();
		}
		else if (element instanceof File) {
			String result = ((File)element).getName();
			return result;
		}
			
		return element.toString();
	}
	
	public static BILocationLabelProvider getDecorator()
	  {
	    IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();

	    if (decoratorManager
	      .getEnabled("com.ibm.biginsights.project.locations.decorator")) //$NON-NLS-1$
	    {
	      return (BILocationLabelProvider) decoratorManager.getLabelDecorator(
	        "com.ibm.biginsights.project.locations.decorator"); //$NON-NLS-1$
	    }
	    return null;
	  }

	
	 public String decorateText(String label, Object obj){
		 String decoration = ""; //$NON-NLS-1$
		 if(obj instanceof IBigInsightsApp){
			 IBigInsightsApp app = (IBigInsightsApp) obj;
			 if(app.getStatus() != null && !app.getStatus().isEmpty()){
				 decoration += " ["+ Messages.Decoration_status + " " + app.getStatus() + "]";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			 }
			 if(app.getCreator() != null && !app.getCreator().isEmpty()){
				 decoration += " [" + Messages.Decoration_creator + " " + app.getCreator() + "]";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			 }
			 return label + decoration;
		 }else if(obj instanceof IBigInsightsLocation){
			 String role = Messages.Decoration_user_role;
			 if(((IBigInsightsLocation)obj).isAdmin()){
				 role = Messages.Decoration_admin_role;
			 }else if(!((IBigInsightsLocation)obj).isUser()){
				 role = Messages.Decoration_unknown;
			 }
			 decoration += " ["+ Messages.Decoration_role + " " + role + "]";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			 return label + decoration;
		 }
		 
		 return label;
	 }

	@Override
	public Image decorateImage(Image image, Object element) {
		if(element instanceof IBigInsightsApp){
			if( ((IBigInsightsApp)element).getStatus().equals(Messages.Decoration_deployed) ){
				return Activator.getDefault().getDecoratedImage(Activator.DECORATED_IMAGE_APPFOLDER_DEPLOYED);				
			}
		}
		
		return image;
		
	}


}
