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
package com.ibm.biginsights.textanalytics.aql.editor.hover;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class AQLHoverInformationControl extends AbstractInformationControl 
implements IInformationControlExtension2, IInformationControlExtension3   {


  
  private Browser fBrowser;

  /**
   * Creates a AQLHoverInformationControl with the given shell as parent.
   * This class will be used to create the InformationPresenter which will present the 
   * doc comment information when F2 is pressed on an AQLElement.
   * @param parent the parent shell
   */
  public AQLHoverInformationControl(Shell parent) {
      super(parent, true);
      create();
  }
  
  public void setBackgroundColor (Color background)
  {
    super.setBackgroundColor (background);
  }

  @Override
  public void setForegroundColor (Color foreground)
  {
    super.setBackgroundColor (foreground);
  }
  protected void createContent(Composite parent) {

      try {
          fBrowser = new Browser(getShell(), SWT.NONE);
       
      } 
      catch (SWTError e) {
          MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
          messageBox.setMessage("Browser cannot be initialized."); //$NON-NLS-1$
          messageBox.setText("Error");                             //$NON-NLS-1$
          messageBox.open();
      }
  }
  
  public void setInformation(String content) {
      org.eclipse.swt.graphics.Rectangle r = getShell().getClientArea();
      fBrowser.setBounds(r);
      fBrowser.setText(content);
      fBrowser.setFocus ();
  }

  public Point computeSizeHint() {
     return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
  }
  
  public boolean hasContents() {
      return fBrowser.getText().length() > 0;
  }

  public IInformationControlCreator getInformationPresenterControlCreator() {
      return new IInformationControlCreator() {
          public IInformationControl createInformationControl(Shell parent) {
              return new AQLHoverInformationControl(parent);
          }
      };
  }

  public void setInput(Object input) {
      final String inputString = (String) input;
      setInformation(inputString);
  }

}
