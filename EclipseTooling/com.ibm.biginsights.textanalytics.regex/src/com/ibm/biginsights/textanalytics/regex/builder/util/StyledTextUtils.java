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
package com.ibm.biginsights.textanalytics.regex.builder.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ibm.biginsights.textanalytics.regex.Messages;

public class StyledTextUtils {



  /**
   * Adds a context menu to the given StyledText Control with Cut, Copy, Paste (CCP), Delete and
   * SelectAll MenuItems.
   * 
   * @param textControl
   */
  public static void createStyledTextContextMenu(final StyledText textControl) {
    final Clipboard clipboard = new Clipboard(textControl.getDisplay());
    Menu contextMenu = textControl.getMenu();
    if (contextMenu == null)
      // control do not have a context menu yet
      contextMenu = new Menu(textControl);
    else
      // add separator if menu exists
      new MenuItem(contextMenu, SWT.SEPARATOR);
    final MenuItem cutTreeMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
    cutTreeMenuItem.setText(Messages.StyledTextUtils_CUT); 
    final MenuItem copyTreeMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
    copyTreeMenuItem.setText(Messages.StyledTextUtils_COPY); 
    final MenuItem pasteTreeMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
    pasteTreeMenuItem.setText(Messages.StyledTextUtils_PASTE); 
    final MenuItem deleteTreeMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
    deleteTreeMenuItem.setText(Messages.StyledTextUtils_DELETE); 
    new MenuItem(contextMenu, SWT.SEPARATOR);
    final MenuItem selectAllTreeMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
    selectAllTreeMenuItem.setText(Messages.StyledTextUtils_SELECT_ALL); 

    textControl.setMenu(contextMenu);

    contextMenu.addMenuListener(new MenuListener() {

      public void menuHidden(MenuEvent e) {
        // does nothing
      }

      public void menuShown(MenuEvent e) {
        int selectedTextLength = textControl.getSelectionText().length();
        if (selectedTextLength > 0) {
          cutTreeMenuItem.setEnabled(true);
          copyTreeMenuItem.setEnabled(true);
          deleteTreeMenuItem.setEnabled(true);
        } else {
          cutTreeMenuItem.setEnabled(false);
          copyTreeMenuItem.setEnabled(false);
          deleteTreeMenuItem.setEnabled(false);
        }
        TextTransfer transfer = TextTransfer.getInstance();
        String text = (String) clipboard.getContents(transfer);
        if (text != null)
          pasteTreeMenuItem.setEnabled(true);
        else
          pasteTreeMenuItem.setEnabled(false);
      }

    });

    cutTreeMenuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        textControl.cut();
      }
    });

    pasteTreeMenuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        textControl.paste();
      }
    });

    copyTreeMenuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        textControl.copy();
      }
    });

    deleteTreeMenuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Point selectionRange = textControl.getSelectionRange();
        int startSelection = selectionRange.x;
        int endSelection = selectionRange.x + selectionRange.y;
        String newText = "";  //$NON-NLS-1$

        if (startSelection > 0)
          newText = textControl.getText(0, startSelection - 1);

        if (endSelection < textControl.getText().length())
          newText = newText + textControl.getText().substring(endSelection);

        textControl.setText(newText);
        textControl.setSelection(startSelection, startSelection);
      }
    });

    selectAllTreeMenuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        textControl.selectAll();
      }
    });
  }

}
