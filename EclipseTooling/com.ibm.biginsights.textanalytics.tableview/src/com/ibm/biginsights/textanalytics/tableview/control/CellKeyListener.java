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

package com.ibm.biginsights.textanalytics.tableview.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * This class listen to keyboard event for the Explain cells in Result Table viewer.<br>
 * If focus is being at the Explain cell and user presses Enter, the provenance graph for that row will be displayed.
 */
public class CellKeyListener implements KeyListener
{


 
	private CellMouseListener cml = null;

  public void setCellMouseListener (CellMouseListener cml)
  {
    this.cml = cml;
  }

  @Override
  public void keyPressed (KeyEvent e)
  {
  }

  @Override
  public void keyReleased (KeyEvent e)
  {
    // Open provenance view
    if ( e.keyCode == SWT.CR && cml != null)
      cml.showExplainGraph ();
  }

}
