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
package com.ibm.biginsights.textanalytics.indexer;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator, IStartup
{



  public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.indexer"; //$NON-NLS-1$

  private static BundleContext context;

  static BundleContext getContext ()
  {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start (BundleContext bundleContext) throws Exception
  {
    Activator.context = bundleContext;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop (BundleContext bundleContext) throws Exception
  {
    Activator.context = null;
  }

  @Override
  public void earlyStartup ()
  {
    // FileCache.getInstance().test();
    // FileCache.getInstance().load();
    // ElementCache.getInstance().test();
    // ElementCache.getInstance().read();

    try {
      new StartupIndexer ().start ();
      // new TextAnalyticsIndexer().reindex ();
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

    IWorkbench workbench = PlatformUI.getWorkbench ();
    workbench.addWorkbenchListener (new IWorkbenchListener () {
      public boolean preShutdown (IWorkbench workbench, boolean forced)
      {
        new StartupIndexer ().stop ();
        return true;
      }

      public void postShutdown (IWorkbench workbench)
      {

      }
    });

  }

}
