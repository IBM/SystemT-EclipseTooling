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
package com.ibm.datatools.quick.launch.ui.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;

/** Action to open help side bar
* 
* @since 2010May05
*/

@SuppressWarnings("restriction")
public class OpenWebPageAction extends Action
{


    String m_url = null;

    public OpenWebPageAction( String p_url)
    {
    	// fix up this if it is a internal file
    	if (p_url.startsWith("platform")) {
    		URL myurl;
			try {
				myurl = new URL(p_url);
				URL newURL = FileLocator.toFileURL(myurl);
				m_url = newURL.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    		
    	else
    		m_url = p_url;
    }

    @Override
    public void run()
    {
        if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null )
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            if ( page != null )
            {
                IBrowser browser = org.eclipse.help.internal.browser.BrowserManager.getInstance().createBrowser();
                try
                {
                    if(browser!=null)
                    {
                        browser.displayURL( m_url );
                    }
                }
                catch ( Exception e )
                {
                    Activator.log( e );
                }
            }
        }

    }




}
