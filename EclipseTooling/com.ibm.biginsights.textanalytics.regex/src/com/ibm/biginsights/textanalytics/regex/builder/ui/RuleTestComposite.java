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
package com.ibm.biginsights.textanalytics.regex.builder.ui;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.builder.util.StyledTextUtils;


/**
 * Creates a composite, which shows two styled text widgets, one for the input text to test the
 * current regex rule and the other one shows the matches (if there any)
 * 
 *  Abraham
 * 
 */
public class RuleTestComposite {



  private Button mButtonTest;

  private Label matched;
  private StyledText mTextInput;

  private StyledText mTextOutput;
  
  private Table table;
  private Composite parent;

  private final StyledText ruleText;
  
  public RuleTestComposite(StyledText ruleText) {
    this.ruleText = ruleText;
  }

  public Control createTestRuleControl(Composite parent, Control topControl) {

	  this.parent = parent;
    this.mButtonTest = new Button(parent, SWT.CHECK);
    this.mButtonTest.setText(Messages.RuleTestComposite_TEST_RULE_BUTTON); 
    this.mButtonTest.setSelection(true);

	  this.mButtonTest.setVisible(false);
//    FormData formData = new FormData();
//    formData.top = new FormAttachment(topControl, 5);
//    formData.left = new FormAttachment(0, 5);
//    this.mButtonTest.setLayoutData(formData);

//    final Text infoText = new Text(parent, SWT.NONE);
//    infoText.setText(RegExMessages.getString("RuleSection.105")); //$NON-NLS-1$
//    infoText.setEditable(false);
//    infoText.setBackground(this.mButtonTest.getBackground());
//
//    FormData formData = new FormData();
//    formData.top = new FormAttachment(topControl, 20);
	  final int leftShift = 5;
//    formData.left = new FormAttachment(0, 5);
//    infoText.setLayoutData(formData);

    final Label label2 = new Label(parent, SWT.NONE);
    label2.setText(Messages.RuleTestComposite_TEXTAREA_LABEL); 
    FormData formData = new FormData();
    formData.top = new FormAttachment(topControl, 15);
    formData.left = new FormAttachment(0, 5);
    label2.setLayoutData(formData);

    // create a styled text box for the input text
    this.mTextInput = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
    this.mTextInput.setEditable(true);
    // set ContextMenu
    StyledTextUtils.createStyledTextContextMenu(this.mTextInput);

    this.mTextInput.addExtendedModifyListener(new ExtendedModifyListener() {
      /**
       * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.swt.custom.ExtendedModifyEvent)
       */
      public void modifyText(ExtendedModifyEvent event) {
        testRegex();
      }
    });

    formData = new FormData();
    formData.top = new FormAttachment(label2, 5);
    formData.height = 120;
    formData.width = 100;
    formData.bottom = new FormAttachment(100, 0);
    formData.left = new FormAttachment(0, leftShift);
    formData.right = new FormAttachment(50, -3);
    this.mTextInput.setLayoutData(formData);

    matched = new Label(parent, SWT.NONE);
    matched.setText(Messages.RuleTestComposite_MATCH_LABEL); 
    formData = new FormData();
    formData.top = new FormAttachment(topControl, 15);
    formData.left = new FormAttachment(50, 3);
    matched.setLayoutData(formData);

    
    createTable();
    createText();
    
    
    this.mButtonTest.addSelectionListener(new SelectionAdapter() {
      /**
       * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (!RuleTestComposite.this.mButtonTest.getSelection()) {
          label2.setEnabled(false);
          matched.setEnabled(false);
          RuleTestComposite.this.mTextInput.setEnabled(false);
          RuleTestComposite.this.table.setEnabled(false);
          RuleTestComposite.this.table.setData(null); //$NON-NLS-1$
        } else {
          label2.setEnabled(true);
          matched.setEnabled(true);
          RuleTestComposite.this.mTextInput.setEnabled(true);
          RuleTestComposite.this.table.setEnabled(true);
          testRegex();
        }
      }
    });
    Listener paintListener = new Listener() {
        public void handleEvent(Event event) {
          switch (event.type) {
          case SWT.MeasureItem: {
            TableItem item = (TableItem) event.item;
            String text = getText(item, event.index);
            Point size = event.gc.textExtent(text);
            event.width = size.x;
            event.height = Math.max(event.height, size.y);
            break;
          }
          case SWT.PaintItem: {
            TableItem item = (TableItem) event.item;
            String text = getText(item, event.index);
            Point size = event.gc.textExtent(text);
            int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
            event.gc.drawText(text, event.x, event.y + offset2, true);
            break;
          }
          case SWT.EraseItem: {
            event.detail &= ~SWT.FOREGROUND;
            break;
          }
          }
        }

        String getText(TableItem item, int column) {
          String text = item.getText(column);
//          if (column != 0) {
//            int index = table.indexOf(item);
//            if ((index + column) % 3 == 1) {
//              text += "\nnew line";
//            }
//            if ((index + column) % 3 == 2) {
//              text += "\nnew line\nnew line";
//            }
//          }
          return text;
        }
      };
    
      table.addListener(SWT.MeasureItem, paintListener);
      table.addListener(SWT.PaintItem, paintListener);
      table.addListener(SWT.EraseItem, paintListener);
    
    return this.table;

  }
  
  public void createText()
  {	  
	  this.mTextOutput = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
	  this.mTextOutput.setEditable(false);
	  FormData formData = new FormData();
	  formData.top = new FormAttachment(matched, 5);
	  formData.height = 120;
	  formData.width = 100;
	  formData.bottom = new FormAttachment(100, 0);
	  formData.left = new FormAttachment(50, 3);
	  formData.right = new FormAttachment(100, -5);
	  this.mTextOutput.setLayoutData(formData);
	  this.mTextOutput.setVisible(false);	  
  }

  public void createTable()
  {
	  this.table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL);
	    this.table.setHeaderVisible(true);
	    this.table.setLinesVisible(true);

	    TableColumn[] column = new TableColumn[4];
	    
	    column[0] = new TableColumn(table, SWT.CENTER);
	    column[0].setText(Messages.RuleTestComposite_TEXT_TITLE);
	    column[0].pack();
	    column[0].setWidth(140);
	    column[0].setResizable(true);
	    
	    column[1] = new TableColumn(table, SWT.CENTER);
	    column[1].setText(Messages.RuleTestComposite_START_TITLE);
	    column[1].pack();
	    column[1].setWidth(60);
	    
	    column[2] = new TableColumn(table, SWT.CENTER);
	    column[2].setText(Messages.RuleTestComposite_STOP_TITLE);
	    column[2].pack();
	    column[2].setWidth(60);
	    
	    column[3] = new TableColumn(table, SWT.LEFT);
	    column[3].setText(Messages.RuleTestComposite_SUBPATTERN_TITLE);
	    column[3].pack();
	    column[3].setWidth(195);
	    column[3].setResizable(true);
	    
	        
	    FormData formData = new FormData();
	    formData.top = new FormAttachment(matched, 5);
	    formData.height = 120;
	    formData.width = 100;
	    formData.bottom = new FormAttachment(100, 0);
	    formData.left = new FormAttachment(50, 3);
	    formData.right = new FormAttachment(100, -5);
	    this.table.setLayoutData(formData);

  }
  
  public void testRegex() {
    Matcher matcher = null;
    Pattern pattern = null;
    this.mTextOutput.dispose();
    this.table.dispose();
    createTable();
    this.table.setData(null);
    this.table.setVisible(true);
    this.table.getParent().redraw();
    this.table.setVisible(false);
    this.table.setVisible(true);
    this.parent.layout();
    //this.mTextOutput.setVisible(false);
    //this.table.setRedraw(true);
    //this.table.removeAll();

    final Color red = new Color(this.table.getDisplay(), 226, 31, 38);// Display.getDefault().getSystemColor(SWT.COLOR_RED);
    final Color yellow = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);

    final String inputText = this.mTextInput.getText();
    final String regexRule = RuleTextComposite.removeInvisibleCharacters(this.ruleText.getText());

    this.table.setData(null);
    this.mTextInput.setStyleRange(null); // remove the coloring for the previous matches

    final StringBuffer textOutput = new StringBuffer();

    if (!this.mButtonTest.getSelection() || (inputText.length() == 0) || (regexRule.length() == 0)) {
      // nothing to do;
      return;
    }
    

    try {
      pattern = Pattern.compile(regexRule);
      matcher = pattern.matcher(inputText);

      if (regexRule.length() < 1) {
        //nothing to do
      } else if (matcher.find()) {
        final LinkedList<StyleRange> styleRangesTestText = new LinkedList<StyleRange>();

        do {
          // mark a match
          final StyleRange style = new StyleRange();
          style.start = matcher.start();
          style.length = matcher.end() - matcher.start();
          style.background = yellow;
          // mTextInput.setStyleRange(style);
          styleRangesTestText.add(style);
          
          TableItem item = new TableItem(table, SWT.NONE);
          int c = 0;
          
//          ImageDescriptor tickImageDesc = ImageDescriptor.createFromFile(this.getClass(), "/icons/tick.gif");
//          final Image tickImage = tickImageDesc.createImage();
//          item.setImageIndent(1);
//          item.setImage(0, tickImage);
          
          item.setText(c++, matcher.group() + ""); //$NON-NLS-1$
          item.setText(c++, matcher.start() + ""); //$NON-NLS-1$
          item.setText(c++, matcher.end() + ""); //$NON-NLS-1$
          
          try
          {          
          
          // mark group matches         
          for (int i = 1; i <= matcher.groupCount(); i++) {
        	  if(matcher.group(i) != null && !matcher.group(i).equals("")) //$NON-NLS-1$
        	  {
        		  textOutput.append(i + ": [" + matcher.group(i) + "] ");  //$NON-NLS-1$ //$NON-NLS-2$
        	  }
          }
          if(matcher.groupCount()>=1 && !(textOutput.charAt(0) == ' '))
          {
        	  if(!(textOutput.toString().trim().equals(""))) //$NON-NLS-1$
        	  {
        		item.setText(c++, textOutput.toString().trim());
        	  }
        	  
        		//  item.setText(new String[] { "item a", "item b", "item c", "item d" });
          }
          textOutput.delete(0, textOutput.length());
          
          }
          catch(StringIndexOutOfBoundsException e){
        	  //TODO
          }
          catch(NullPointerException e){
            //TODO
          }
          //textOutput.append("\n"); //$NON-NLS-1$
        } while (matcher.find());
        this.mTextInput.setStyleRanges(styleRangesTestText
            .toArray(new StyleRange[styleRangesTestText.size()]));
      } else if (inputText.length() < 1) {
        textOutput.append(Messages.RuleTestComposite_TYPE_TEXT_MSG); 
      } else {
        textOutput.append(Messages.RuleTestComposite_NO_MATCH_FOUND); 
      }
      
      this.table.setVisible(true);

      // System.out.println("Time needed for test: " + (System.currentTimeMillis() - start));
    } catch (final PatternSyntaxException e) {
    	//this.table.setVisible(false);
        this.table.dispose();
        createText();
        this.mTextOutput.setVisible(true);
        this.mTextOutput.setEnabled(true);
        this.parent.layout();
        String message = e.getMessage().substring(0, e.getMessage().lastIndexOf("\n"));
        this.mTextOutput.setText(Messages.RuleTestComposite_REGEX_ERROR_MSG + "\n" + message);  //$NON-NLS-1$
       
        StyleRange style = new StyleRange();
        style.start = 0;
        style.length = this.mTextOutput.getText().length() - message.length();
        style.background = red; 
        style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
        this.mTextOutput.setStyleRange(style);
        
        style = new StyleRange();
        style.start = this.mTextOutput.getText().indexOf(e.getPattern()) + e.getIndex() - 1 ;
        
        if(e.getIndex() == -1)
        {
        	style.start++;
        }
        if(e.getPattern().length() > e.getIndex())
        {
        	style.start++;
        }
        try
        {
        style.length = 1;
        style.background = red; 
        style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
        this.mTextOutput.setStyleRange(style);
        this.mTextOutput.redraw();
        }
        catch(IllegalArgumentException e1){
        	//TODO
        }
        
    }
  }

  public StyledText getMTextInput() {
    return this.mTextInput;
  }

  public Button getMButtonTest() {
    return this.mButtonTest;
  }

  public StyledText getMTextOutput() {
    return this.mTextOutput;
  }

}
