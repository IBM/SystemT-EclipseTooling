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
package com.ibm.biginsights.project.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
public class PrereqChecker {
	
	public static boolean isChmodInstalled(){
		
		try {
			
			Process p = Runtime.getRuntime().exec("chmod"); //$NON-NLS-1$
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	
	public static boolean isXULRunnerPathSet(){
		//only a linux platform issue
		if(System.getProperty("os.name").toLowerCase().indexOf("win") == -1){ //$NON-NLS-1$ //$NON-NLS-2$
			
			String xulPathRunner = System.getProperty("org.eclipse.swt.browser.XULRunnerPath"); //$NON-NLS-1$
			if(xulPathRunner != null ){	

				File file = new File(xulPathRunner);
				if(file.isDirectory() && file.exists()){
					//if 64 bit XULRunner must use 64 bit eclipse
					if(xulPathRunner.contains("lib64")){					 //$NON-NLS-1$
						String osgiArch = System.getProperty("osgi.arch"); //$NON-NLS-1$
						if(osgiArch != null && osgiArch.contains("64") ){ //$NON-NLS-1$
							//XULRunnerPath set and 64 bit
							return true;
						}else{
							MessageDialog.openError(Activator.getDefault().getActiveWorkbenchShell(), Messages.Error_Title, Messages.PREREQ_XULRUNNER_INVALID_ARCH);	
							return false;
						}
					}
					//XULRunnerPath set and 32 bit
					return true;
				}else{
					//dir does not exist
					MessageDialog.openError(Activator.getDefault().getActiveWorkbenchShell(), Messages.Error_Title, Messages.PREREQ_XULRUNNER_NOTSET);	
					return false;
				}
			}
			
			//check if the firefox version is over 3.6
			//if no firefox version found then they may not have a problem
			String version = checkFirefoxVersion();
			StringTokenizer tokenizer = new StringTokenizer(version, "."); //$NON-NLS-1$
			if(tokenizer.hasMoreTokens()){
				String part1 = tokenizer.nextToken();
				if(tokenizer.hasMoreTokens()){
					String part2 = tokenizer.nextToken();
					if(Integer.valueOf(part1) > 3 ||
					   (Integer.valueOf(part1) == 3 && Integer.valueOf(part2) > 6)	){
							//XULRunnerPath not set
							MessageDialog.openError(Activator.getDefault().getActiveWorkbenchShell(), Messages.Error_Title, Messages.PREREQ_XULRUNNER_NOTSET);	
							return false;
					}
				}
			}
			
		}
		
		return true;
	}
	
	public static String checkFirefoxVersion(){
		try {
			
			Process p = Runtime.getRuntime().exec("firefox -v"); //$NON-NLS-1$
			p.waitFor();
			if(p == null || p.exitValue()!=0){
				// no firefox available
				return ""; //$NON-NLS-1$
			}else{
				byte[] inBytes = new byte[1024];
				InputStream in = p.getInputStream();
				in.read(inBytes);
				String inString = new String(inBytes);
				in.close();
				if(inString.startsWith("Mozilla Firefox")){ //$NON-NLS-1$
					String version = inString.substring("Mozilla Firefox".length()).trim(); //$NON-NLS-1$
					return version;
				}			
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return ""; //$NON-NLS-1$
	}
	
//	public static boolean isInternalBrowserOK(){
		//ON LINUX JAVA DUMP CAUSED WHEN XULRunnerPath NOT SET - GENERAL PROTECTION FAULT
		//NO EXCEPTION THROWN, ECLIPSE CRASHES
//		return PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable();

//		try{
//			Shell dialog = new Shell(Workbench.getInstance().getActiveWorkbenchWindow().getShell(), SWT.DIALOG_TRIM );
//			Browser browser = new Browser(dialog, SWT.BORDER);
//			browser.setText("Some text");
//			dialog.close();
//		}catch(Exception e){
//			//cannot create/use browser
//			return false; 
//		}
//		return true;
//	}
	
	
	
}
