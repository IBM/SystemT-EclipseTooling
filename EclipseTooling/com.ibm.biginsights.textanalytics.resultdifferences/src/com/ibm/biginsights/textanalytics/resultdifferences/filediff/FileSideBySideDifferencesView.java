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

package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class FileSideBySideDifferencesView extends ViewPart
{



  public static final String ID = "com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileSideBySideDifferencesView"; //$NON-NLS-1$
  private static final HashMap<String, FileDifferencesSideBySideModel> modelMap = new HashMap<String, FileDifferencesSideBySideModel> ();

  protected Display display;
  private Color green = new Color (display, 178, 255, 102);
  private Color orange = new Color (display, 255, 127, 0);
  private Color blue = new Color (display, 135, 206, 250);
  private Color red = new Color (display, 205, 92, 92);

  public void dispose ()
  {
    super.dispose ();
  }

  public static final void setModelForId (String fileName, FileDifferencesSideBySideModel model)
  {
    modelMap.put (fileName, model);
  }

  public void createPartControl (Composite parent)
  {
    try {
      display = parent.getDisplay ();

      final String fileName = getViewSite ().getSecondaryId ();
      if (fileName == null) {
        Label label = new Label (parent, SWT.BORDER);
        label.setText (Messages.getString ("FileSideBySideDifferencesView_SideBySide")); //$NON-NLS-1$
        return;
      }

      FileDifferencesSideBySideModel sbsMdl = modelMap.get (fileName);
      createFileSideBySideView (parent, sbsMdl.getRightFile (), sbsMdl.getLeftFile (),
        sbsMdl.getLeftModel (), sbsMdl.getRightModel (), sbsMdl.getOldSpansInRightFile (),
        sbsMdl.getUnchangedSpansInLeftFiles (), sbsMdl.getUnchangedSpansInRightFiles (),
        sbsMdl.getOverlappingSpansInLeftFile (), sbsMdl.getNewSpansInLeftFile (),
        sbsMdl.getOverlappingSpansInRightFile ());

    }
    catch (Exception e) {
      e.printStackTrace ();

    }
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.file_side_by_side_differences_view");//$NON-NLS-1$
  }

  private void createFileSideBySideView (Composite parent, IFile rightFile, IFile leftFile,
    SystemTComputationResult leftModel, SystemTComputationResult rightModel,
    ArrayList<SpanVal> oldSpansInRightFile, ArrayList<SpanVal> unchangedSpansInLeftFiles,
    ArrayList<SpanVal> unchangedSpansInRightFiles, ArrayList<SpanVal> overlappingSpansInLeftFile,
    ArrayList<SpanVal> newSpansInLeftFile, ArrayList<SpanVal> overlappingSpansInRightFile)
  {

    // Left Hand Panel
    // Get the styled text for the file with all the annotations
    ArrayList<SpanVal> spansToEvaluateMaxOccuranceOfSourceID = new ArrayList<SpanVal>();
    spansToEvaluateMaxOccuranceOfSourceID.addAll (newSpansInLeftFile);
    spansToEvaluateMaxOccuranceOfSourceID.addAll (unchangedSpansInLeftFiles);
    spansToEvaluateMaxOccuranceOfSourceID.addAll (overlappingSpansInLeftFile);
    String inputText1="";
    if (spansToEvaluateMaxOccuranceOfSourceID.size() != 0)
    {
        int maxOccSpanLeftId = getMaxOccuringSpanSourceId (spansToEvaluateMaxOccuranceOfSourceID);
    	inputText1 = leftModel.getTextValueMap ().get (maxOccSpanLeftId);
    }

    spansToEvaluateMaxOccuranceOfSourceID  = new ArrayList<SpanVal>();
    spansToEvaluateMaxOccuranceOfSourceID.addAll(oldSpansInRightFile);
    spansToEvaluateMaxOccuranceOfSourceID.addAll (unchangedSpansInRightFiles);
    spansToEvaluateMaxOccuranceOfSourceID.addAll (overlappingSpansInRightFile);
    String inputText2="";
    if (spansToEvaluateMaxOccuranceOfSourceID.size() != 0)
    {
        int maxOccSpanRightId = getMaxOccuringSpanSourceId (spansToEvaluateMaxOccuranceOfSourceID);
    	inputText2 = rightModel.getTextMap ().get (maxOccSpanRightId);
    }
    // The following is done for defect 17846. If there is only one new or one spurious and no unchanged or overlapping at all
    // then the input text is empty because there is no max occurring span. To tackle this special case, if one input text is 
    // empty, and the other is not, then we'll assign it to the former. This resolves the issue.
    if ( (inputText1.equals("")) && (inputText2.equals("") != true))
    {
    	inputText1=inputText2;
    }
    
    if ( (inputText2.equals("")) && (inputText1.equals("") != true))
    {
    	inputText2=inputText1;
    }

    String leftTitle = "";
    if (leftFile != null)
    {
    	leftTitle = leftFile.getFullPath ().toOSString ();
    }
    else
    {
    	leftTitle = "File does not exist";
    	inputText1="";
    }
    String rightTitle="";
    if (rightFile != null)
    {
    	rightTitle = rightFile.getFullPath ().toOSString ();
    }
    else
    {
    	rightTitle = "File does not exist";
    	inputText2= "";
    }

    final StyledText text2 = createStyledText (true, parent,leftTitle ,
      inputText1, newSpansInLeftFile, unchangedSpansInLeftFiles, overlappingSpansInLeftFile);

    // Right Hand Panel
    // Get the styled text for the file with all the annotations
    final StyledText text1 = createStyledText (false, parent,rightTitle ,
      inputText2, oldSpansInRightFile, unchangedSpansInRightFiles, overlappingSpansInRightFile);

    {
		   // The code below controls the horizontal and vertical scroll bars to	scroll in tandem.
    		    final ScrollBar vBar1 = text1.getVerticalBar();
    		    final ScrollBar vBar2 = text2.getVerticalBar();
    		    final ScrollBar hBar1 = text1.getHorizontalBar();
    		    final ScrollBar hBar2 = text2.getHorizontalBar();

      SelectionListener listener1 = new SelectionAdapter () {
        public void widgetSelected (SelectionEvent e)
        {
        	int y = text1.getTopIndex();
	    	text2.setTopIndex(y);
	    	int x=text1.getHorizontalIndex();
	    	text2.setHorizontalIndex(x);
        }
      };
      SelectionListener listener2 = new SelectionAdapter () {
        public void widgetSelected (SelectionEvent e)
        {
      	  int y = text2.getTopIndex();
    	  text1.setTopIndex(y);
    	  int x = text2.getHorizontalIndex();
    	  text1.setHorizontalIndex(x);
        }
      };
	    vBar1.addSelectionListener(listener1);
	    hBar1.addSelectionListener(listener1);
	    vBar2.addSelectionListener(listener2);
	    hBar2.addSelectionListener(listener2);

      KeyListener kl = new KeyListener () {
        @Override
        public void keyPressed (KeyEvent e)
        {
      	  int y = text2.getTopIndex();
      	  text1.setTopIndex(y);
      	  int x = text2.getHorizontalIndex();
      	  text1.setHorizontalIndex(x);
        }

        @Override
        public void keyReleased (KeyEvent e)
        {
          // TODO Auto-generated method stub
        }
      };
      text2.addKeyListener ((org.eclipse.swt.events.KeyListener) kl);

      KeyListener k2 = new KeyListener () {
        @Override
        public void keyPressed (KeyEvent e)
        {
        	int y = text1.getTopIndex();
    	    text2.setTopIndex(y);
	    	int x=text1.getHorizontalIndex();
	    	text2.setHorizontalIndex(x);
        }

        @Override
        public void keyReleased (KeyEvent e)
        {
          // TODO Auto-generated method stub
        }
      };
      text1.addKeyListener ((org.eclipse.swt.events.KeyListener) k2);

    }
  }

  private StyledText createStyledText (boolean left, Composite parent, String title, String text,
    ArrayList<SpanVal> onlyHereList, ArrayList<SpanVal> unchangedList,
    ArrayList<SpanVal> overlappingList)
  {

    StyledText output = new StyledText (parent,  SWT.BORDER |  SWT.H_SCROLL |	SWT.V_SCROLL);
    output.setText (text);
    output.setEditable (false);
    output.setLayout (new FillLayout ());


    // This is to colour in green the spans that are found in both files - basically unchanged ones
    for (int j = 0; j < unchangedList.size (); j++) {
      SpanVal spanVal = (SpanVal) unchangedList.get (j);
      if (spanVal.end > spanVal.start) {
        StyleRange styleRange = new StyleRange ();
        styleRange.start = spanVal.start;
        styleRange.length = spanVal.end - spanVal.start;
        styleRange.fontStyle = SWT.BOLD;
        styleRange.background = green;
        output.setStyleRange (styleRange);
      }
    }

    // This is to colour in blue the spans that are overlapping
    for (int j = 0; j < overlappingList.size (); j++) {
      SpanVal spanVal = (SpanVal) overlappingList.get (j);
      if (spanVal.end > spanVal.start) {
        StyleRange styleRange = new StyleRange ();
        styleRange.start = spanVal.start;
        styleRange.length = spanVal.end - spanVal.start;
        styleRange.fontStyle = SWT.BOLD;
        styleRange.background = blue;
        output.setStyleRange (styleRange);
      }
    }

    // This is to colour in orange the spans that are found only in this file - basically new ones in right file and
    // deleted ones in left file
    for (int j = 0; j < onlyHereList.size (); j++) {
      SpanVal spanVal = (SpanVal) onlyHereList.get (j);
      if (spanVal.end > spanVal.start) {
        StyleRange styleRange = new StyleRange ();
        styleRange.start = spanVal.start;
        styleRange.length = spanVal.end - spanVal.start;
        styleRange.fontStyle = SWT.BOLD;
        if (left) {
          styleRange.background = orange;
        }
        else {
          styleRange.background = red;
        }
        output.setStyleRange (styleRange);
      }
    }

 //   output.setSize(output.computeSize(2500, SWT.DEFAULT));
    output.setToolTipText (title);
    return output;

  }

  public void setFocus ()
  {}

  /**
   * This is a private method to calculate the highest occurring sourceID of spans in a given set. This is called for
   * the left and right hand side panel display. And is relevant when the spans are over detagged text.
   * 
   * @param spans
   * @return
   */
  private int getMaxOccuringSpanSourceId (ArrayList<SpanVal> spans)
  {
    HashMap<Integer, Integer> sourceIDVsOccuranceCount = new HashMap<Integer, Integer> ();
    // The key is the sourceID and the value is the number of times it has occurred.
    Iterator<SpanVal> iter = spans.iterator ();
    SpanVal span = null;
    int highestOccuringSpanID = 0;
    int highestOccuringSpanIDCount = 0;
    while (iter.hasNext ()) {
      span = iter.next ();
      Integer key = Integer.valueOf (span.sourceID);
      if (sourceIDVsOccuranceCount.containsKey (key)) {
        // If the span source id is present in the map,
        // increment it by one and store it back in the map
        Integer value = sourceIDVsOccuranceCount.get (key);
        int intval = value.intValue ();
        intval = intval + 1;
        sourceIDVsOccuranceCount.put (key, intval);
        if (intval > highestOccuringSpanIDCount) {
          // Also update the highest occurring key and count, if it is greater than what is last stored.
          highestOccuringSpanID = key;
          highestOccuringSpanIDCount = intval;
        }
      }
      else {
        // Store the occurrance of the sourceID in a hashmap
        sourceIDVsOccuranceCount.put (key, 1);
        highestOccuringSpanID = key;
        highestOccuringSpanIDCount = 1;
      }
    }
    return highestOccuringSpanID;
  }
}
