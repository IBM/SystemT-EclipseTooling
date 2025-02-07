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

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * defines the View for the History Viewer
 * 
 * 
 */
public class RulesHitoryView extends ViewPart
{



  public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.ruleshistory.view";

  private String xmlDescription;

  private Frame frame;

  @Override
  public void createPartControl (Composite parent)
  {
    Composite composite = new Composite (parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
    frame = SWT_AWT.new_Frame (composite);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.rules_history_view");//$NON-NLS-1$
  }

  @Override
  public void setFocus ()
  {
    // TODO Auto-generated method stub

  }

  /**
   * Set the XML representation of the rules history that should be displayed by the viewer, and create the
   * RulesHistoryApplet vizualization based on it.
   * 
   * @param xmlStr
   */
  public void setDescription (String xmlStr)
  {
    xmlDescription = xmlStr;

    RulesHistoryApplet applet = new RulesHistoryApplet (xmlDescription);
    frame.add (applet);
  }

}
