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
package com.ibm.biginsights.textanalytics.goldstandard.decorator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Krishnamurthy
 *
 */
public class FileIconDecorator extends LabelProvider implements ILabelDecorator {



	private static Image completeImage;
	private static Image incompleteImage;
	
	@Override
	public Image decorateImage(Image image, Object element) {
		if(element instanceof IFile){
			if(isComplete((IFile)element)){
				return getCompleteImage();
			}else{
				return getIncompleteImage();
			}
		}
		return image;
	}

	private Image getIncompleteImage() {
		if(incompleteImage != null){
			return incompleteImage;
		}else{
			Bundle bundle = GoldStandardPlugin.getDefault().getBundle();
			URL url = FileLocator.find(bundle, new Path("icons/GS_incomplete.png"), null);
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url); 
			incompleteImage =  descriptor.createImage();
			return incompleteImage;
		}
	}

	private Image getCompleteImage() {
		if(completeImage != null){
			return completeImage;
		}else{
			Bundle bundle = GoldStandardPlugin.getDefault().getBundle();
			URL url = FileLocator.find(bundle, new Path("icons/GS_complete.png"), null);
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url); 
			completeImage =  descriptor.createImage();
			return completeImage;
		}
	}

	private boolean isComplete(IFile gsFile) {
		InputStream in = null;
		try {
			in = gsFile.getContents();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String header = br.readLine();
			if(header != null){
				String rootTag = br.readLine();
				if(rootTag != null && rootTag.contains("systemTComputationResult")){
					return rootTag.contains("gsComplete=\"true\"");
				}
			}
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		} catch (IOException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
				}
			}
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String decorateText(String text, Object element) {
		// TODO Auto-generated method stub
		return null;
	}



}
