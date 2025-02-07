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
package com.ibm.biginsights.textanalytics.patterndiscovery.ruleshistory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.RuleApplied;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * defines job to be run when calling the 'see history' for a given pattern from the pd view
 * 
 * 
 */
public class RulesHistoryJob extends Job
{


 
	RuleApplied rulesRoot;

  public RulesHistoryJob (String name, RuleApplied rulesRoot)
  {
    super (name);
    this.rulesRoot = rulesRoot;
  }

  @Override
  protected IStatus run (IProgressMonitor monitor)
  {
    if (rulesRoot != null) {
      StringBuilder sb = new StringBuilder ();
      sb.append ("<tree>");
      sb.append ("<declarations>");
      sb.append ("<attributeDecl name=\"name\" type=\"String\"/>");
      sb.append ("</declarations>");

      // add the root node to the graph and it will add each of its children
      sb.append (rulesRoot.toXML ());
      sb.append ("</tree>");

      final String xml = sb.toString ();

      Display.getDefault ().asyncExec (new Runnable () {
        @Override
        public void run ()
        {
          IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
          try {
            IViewReference prevView = wbPage.findViewReference (RulesHitoryView.VIEW_ID);

            if (prevView != null) {
              wbPage.hideView (prevView);
            }
            ((RulesHitoryView) wbPage.showView (RulesHitoryView.VIEW_ID)).setDescription (xml);

          }
          catch (Exception e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (ErrorMessages.PATTERN_DISCOVERY_ERROR_GETTING_HISTORY, e);
          }
        }
      });
    }
    else {
      Display.getDefault ().asyncExec (new Runnable () {
        @Override
        public void run ()
        {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowInfo (ErrorMessages.PATTERN_DISCOVERY_NO_HISTORY);
        }
      });
    }
    return Status.OK_STATUS;
  }

}
