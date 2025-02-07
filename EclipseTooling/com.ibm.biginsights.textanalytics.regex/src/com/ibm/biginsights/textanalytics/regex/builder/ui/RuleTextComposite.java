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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.builder.util.StyledTextUtils;

/**
 * Composite to use in regular expression widgets: Shows a styled text composite where the user is
 * requested to input a regex rule.
 * <p>
 * Parts of the rule (regular expression constructs) will be highlighted, as well as undo/redo in
 * this styledText box is implemented.
 * 
 *  Abraham
 * 
 */
public class RuleTextComposite {



/** styled text widget */
  private StyledText mTextRule;

/** style range to regular expression construct description mapping */
  private HashMap<StyleRange, String> styleToConstrustDescr;

  /** text highlighting */
  private MenuItem hightLightTextItem;

  private boolean enableHighlightRegExConstructs = true;

  /** undo/redo support */
  private MenuItem undoItem;

  private MenuItem redoItem;

  /** max items in undo/redo list */
  private static final int MAX_UNDO_ITEMS = 30;

  /** time delay to aggregate insert/replace events */
  private static final int MAX_TIME_DELAY = 800;

  /** eventType, if text was inserted/replaced */
  private static int INSERTIONEVENT = 1;

  private static int REPLACINGEVENT = 2;

  /** undo/redo stack list */
  private final List<SavedTextItem> undoItemList = new LinkedList<SavedTextItem>();

  private final List<SavedTextItem> redoItemList = new LinkedList<SavedTextItem>();

  /** true, if widget is created and values initially set */
  private boolean refreshWidgets = false;

  /**
   * Object, which will be saved as Undo/redo event. Hold informationLabel necessary to set previous
   * states.
   * 
   * 
   * 
   */
  private static class SavedTextItem {

    private static final int REPLACE_USING_DELETE = 1;

    private static final int REPLACE_USING_BACKSPACE = 2;

    long time;

    String text;

    int startPosition;

    int eventType;

    int replaceType = 0;

    int nextStartPosition;

    public SavedTextItem(int eventType, long time, String text, int startPosition) {
      super();
      this.time = time;
      this.text = text;
      this.startPosition = startPosition;
      this.eventType = eventType;
      this.replaceType = 0;
      if (eventType == INSERTIONEVENT) {
        this.nextStartPosition = startPosition + text.length();
      } else {
        if (eventType == REPLACINGEVENT) {
          this.nextStartPosition = startPosition;
        } else {
          throw new UnsupportedOperationException(
              Messages.RuleTextComposite_UNSUPPORTED_OP_EXCEPTION);
        }
      }
    }

    public boolean append(int eventType1, long time1, String modifiedText, int startPosition1) {
      // only append if same eventtype, in timeframe and if not more than one character modfied
      if ((this.eventType != eventType1) || (time1 - this.time > MAX_TIME_DELAY)
          || (modifiedText.length() > 1)) {
        return false;
      }

      if ((eventType1 == INSERTIONEVENT) && (startPosition1 == this.nextStartPosition)) {
        this.nextStartPosition++;
        this.text = this.text + modifiedText;
        this.time = time1;
        return true;
      }
      if (eventType1 == REPLACINGEVENT) {
        if ((startPosition1 + 1 == this.startPosition)
            && ((this.replaceType == 0) || (this.replaceType == SavedTextItem.REPLACE_USING_BACKSPACE))) {
          // backspace was used again
          this.replaceType = SavedTextItem.REPLACE_USING_BACKSPACE;
          this.startPosition--;
          this.text = modifiedText + this.text;
          this.time = time1;
          this.nextStartPosition--;
          return true;
        } else if (((this.replaceType == 0) || (this.replaceType == SavedTextItem.REPLACE_USING_DELETE))
            && (startPosition1 == this.startPosition)) {
          // del-key was used
          this.replaceType = SavedTextItem.REPLACE_USING_DELETE;
          this.text = this.text + modifiedText;
          return true;
        }
      } else {
        throw new UnsupportedOperationException(
            Messages.RuleTextComposite_UNSUPPORTED_OP_EXCEPTION);
      }
      return false;
    }
  }

  public StyledText createControl(final Composite client, FormToolkit toolkit) {
    // create a text box for the rule
    this.mTextRule = new StyledText(client, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
    if (toolkit != null) {
      toolkit.adapt(this.mTextRule);
    }
    // set ContextMenu
    addUndoRedoSupport();
    StyledTextUtils.createStyledTextContextMenu(this.mTextRule);
    addHighLightingControl();
    this.mTextRule.setWordWrap(true);
    this.mTextRule.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    this.mTextRule.addExtendedModifyListener(new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent event) {
        highlightRegExConstructs(client.getDisplay(), RuleTextComposite.this.mTextRule);
      }
    });

    final MouseTrackListener mouseTracker = new MouseTrackAdapter() {

      @Override
      public void mouseExit(MouseEvent e) {
        RuleTextComposite.this.mTextRule.setToolTipText(null);
      }

      @Override
      public void mouseHover(MouseEvent e) {
        final Point point = new Point(e.x, e.y);

        try {
          final int offset = RuleTextComposite.this.mTextRule.getOffsetAtLocation(point);
          final StyleRange[] ranges = RuleTextComposite.this.mTextRule.getStyleRanges();
          for (int i = 0; i < ranges.length; i++) {
            if ((offset >= ranges[i].start) && (offset <= ranges[i].start + ranges[i].length)) {
              if (RuleTextComposite.this.styleToConstrustDescr.containsKey(ranges[i])) {
                RuleTextComposite.this.mTextRule
                    .setToolTipText(RuleTextComposite.this.styleToConstrustDescr.get(ranges[i]));
                return;
              }
            }
          }
        } catch (final Exception e1) {
          // nothing to do
        }
        RuleTextComposite.this.mTextRule.setToolTipText(null);
      }

    };

    this.mTextRule.addMouseTrackListener(mouseTracker);

    return this.mTextRule;
  }

  /**
   * Add Context Menu entry to enable/disable highlighting of regular expression constructs
   * 
   */
  private void addHighLightingControl() {
    if (this.mTextRule.getMenu() == null) {
      this.mTextRule.setMenu(new Menu(this.mTextRule));
    } else {
      new MenuItem(this.mTextRule.getMenu(), SWT.SEPARATOR);
    }
    this.hightLightTextItem = new MenuItem(this.mTextRule.getMenu(), SWT.CHECK);
    this.hightLightTextItem.setText(Messages.RuleTextComposite_HIGHLIGHT_REGEX_MSG); 
    this.hightLightTextItem.setSelection(this.enableHighlightRegExConstructs);

    this.hightLightTextItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        RuleTextComposite.this.enableHighlightRegExConstructs = RuleTextComposite.this.hightLightTextItem
            .getSelection();
        highlightRegExConstructs(RuleTextComposite.this.mTextRule.getDisplay(),
            RuleTextComposite.this.mTextRule);
      }
    });
  }

  /**
   * Add Undo/Redo support to text control, including contextmenu and keyListener
   * 
   */
  private void addUndoRedoSupport() {
    this.mTextRule.addExtendedModifyListener(new ExtendedModifyListener() {

      public void modifyText(ExtendedModifyEvent event) {
        if (RuleTextComposite.this.refreshWidgets) {
          return;
        }

        // char(s) deleted
        if (event.replacedText.length() > 0) {
          addToUndoItemList(REPLACINGEVENT, event.time + 0xFFFFFFFFL, event.replacedText,
              event.start);
        }

        // char(s) inserted
        final String newText = RuleTextComposite.this.mTextRule.getText().substring(event.start,
            event.start + event.length);
        if (newText.length() > 0) {
          addToUndoItemList(INSERTIONEVENT, event.time + 0xFFFFFFFFL, newText, event.start);
        }

        // enable UndoItem
        RuleTextComposite.this.undoItem.setEnabled(true);
        // disable UndoItem - if the user just modified some characters no more redo actions can be
        // done, only Undo
        RuleTextComposite.this.redoItemList.clear();
        RuleTextComposite.this.redoItem.setEnabled(false);
      }

      private void addToUndoItemList(int eventType, long time, String modifiedText, int textPos) {
        if (RuleTextComposite.this.undoItemList.size() == MAX_UNDO_ITEMS) {
          // remove last item
          RuleTextComposite.this.undoItemList
              .remove(RuleTextComposite.this.undoItemList.size() - 1);
        }

        SavedTextItem lastItem = null;
        if (RuleTextComposite.this.undoItemList.size() > 0) {
          lastItem = RuleTextComposite.this.undoItemList.get(0);
        }

        boolean appendSucceeded = false;
        if (lastItem != null) {
          // try to append to existing item
          appendSucceeded = lastItem.append(eventType, time, modifiedText, textPos);
        }
        if (!appendSucceeded) {
          // add as new Item
          RuleTextComposite.this.undoItemList.add(0, new SavedTextItem(eventType, time,
              modifiedText, textPos));
        }
      }

    });

    // add context menu for undo/redo
    if (this.mTextRule.getMenu() == null) {
      this.mTextRule.setMenu(new Menu(this.mTextRule));
    } else {
      new MenuItem(this.mTextRule.getMenu(), SWT.SEPARATOR);
    }

    this.undoItem = new MenuItem(this.mTextRule.getMenu(), SWT.CASCADE);
    this.undoItem.setText(Messages.RuleTextComposite_UNDO_TEXT); 
    this.undoItem.setAccelerator(SWT.CTRL + 'z');
    this.undoItem.setEnabled(false);

    this.undoItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        makeUndo();
      }
    });

    this.redoItem = new MenuItem(this.mTextRule.getMenu(), SWT.CASCADE);
    this.redoItem.setText(Messages.RuleTextComposite_REDO_TEXT); 
    this.redoItem.setAccelerator(SWT.CTRL + 'y');
    this.redoItem.setEnabled(false);

    this.redoItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        makeRedo();
      }
    });

    // add key listener
    this.mTextRule.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'z')) {
          makeUndo();
        }
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'y')) {
          makeRedo();
        }
      }
    });

  }

  private void makeUndo() {
    this.refreshWidgets = true;
    if (this.undoItemList.size() > 0) {
      // remove last item
      final SavedTextItem lastModify = this.undoItemList.remove(0);
      if (lastModify.eventType == INSERTIONEVENT) {
        this.mTextRule.replaceTextRange(lastModify.startPosition, lastModify.text.length(), "");  //$NON-NLS-1$
        this.mTextRule.setCaretOffset(lastModify.startPosition);
      } else {
        // REPLACINGEVENT
        this.mTextRule.replaceTextRange(lastModify.startPosition, 0, lastModify.text);
        this.mTextRule.setCaretOffset(lastModify.startPosition + lastModify.text.length());
      }
      // add item to redo list
      // no restrictions to redo list, because list will be cleared of new items inserted to
      // undolist
      this.redoItemList.add(0, lastModify);
      this.redoItem.setEnabled(true);
      if (this.undoItemList.size() == 0) {
        this.undoItem.setEnabled(false);
      }
    }
    this.refreshWidgets = false;
  }

  private void makeRedo() {
    this.refreshWidgets = true;
    if (this.redoItemList.size() > 0) {
      // remove last item
      final SavedTextItem lastModify = this.redoItemList.remove(0);
      if (lastModify.eventType == INSERTIONEVENT) {
        this.mTextRule.replaceTextRange(lastModify.startPosition, 0, lastModify.text);
        this.mTextRule.setCaretOffset(lastModify.startPosition + lastModify.text.length());
      } else {
        // REPLACINGEVENT
        this.mTextRule.replaceTextRange(lastModify.startPosition, lastModify.text.length(), "");  //$NON-NLS-1$
        this.mTextRule.setCaretOffset(lastModify.startPosition);
      }
      // add item to undo list
      if (this.undoItemList.size() == MAX_UNDO_ITEMS) {
        // remove last item
        this.undoItemList.remove(this.undoItemList.size() - 1);
      }
      this.undoItemList.add(0, lastModify);
      this.undoItem.setEnabled(true);
      if (this.redoItemList.size() == 0) {
        this.redoItem.setEnabled(false);
      }
    }
    this.refreshWidgets = false;
  }

  /**
   * Object, which will hold informationLabel which regular expression construct with which color at
   * which position/length should be highlighted
   * 
   * 
   * 
   */
  private static class StyledContruct {
    int position;

    int length;

    Color conctructColor;

    String regex;

    public StyledContruct(int position, int length, Color conctructColor, String regex) {
      super();
      this.position = position;
      this.length = length;
      this.conctructColor = conctructColor;
      this.regex = regex;
    }

  }

  /**
   * Hightlight known regular expression constructs
   * 
   * The larger rule will be highlighted, for example the pattern <code>(?x)</code> will be
   * highlighted as ones, even though there is the conctruct <code>?</code> in it. If you insert the
   * pattern <code>?</code> at a separate place, it will be highlighted as known pattern
   */
  protected void highlightRegExConstructs(Display display, StyledText ruleText) {

    this.styleToConstrustDescr = new HashMap<StyleRange, String>();
    // reset all selections
    ruleText.setStyleRange(null);

    if (!this.enableHighlightRegExConstructs) { // hightLightTextItem.getSelection()) {
      // no more actions needed - all highlighting were removed
      return;
    }

    Matcher matcher = null;
    Pattern pattern = null;

    final String inputText = replaceInvisibleCharacters(ruleText.getText(), 'S',
        false);

    final StyledContruct[] styledConstructs = new StyledContruct[inputText.length()];

    for (int constructID = 0; constructID <= 6; constructID++) {
      final String[] constructRules = RegExConstructs
          .getRegexForRegularExpressionContructs(constructID);
      final Color constructColor = RegExConstructs.getColorIDForRegularExpressionContructs(
          constructID, display);

      for (int i = 0; i < constructRules.length; i++) {
        try {
          final String currentRegex = constructRules[i];
          if (currentRegex.length() > 0) {
            // find character constructs
            pattern = Pattern.compile(currentRegex);
            matcher = pattern.matcher(inputText);

            if (matcher.find()) {
              do {

                final int matchStart = matcher.start();
                final int matchLength = matcher.end() - matcher.start();
                if (styledConstructs[matchStart] != null) {
                  final StyledContruct oldConstruct = styledConstructs[matchStart];
                  if (oldConstruct.length < matchLength) {
                    styledConstructs[matchStart] = new StyledContruct(matchStart, matchLength,
                        constructColor, currentRegex);
                  }
                } else {
                  styledConstructs[matchStart] = new StyledContruct(matchStart, matchLength,
                      constructColor, currentRegex);
                }

              } while (matcher.find());
            }
          }

        } catch (final PatternSyntaxException e) {
          // should not occur, because we set these pattern
          e.printStackTrace();
        }

      } // END OF FOR

    } // END OF FOR

    // create styledText
    final LinkedList<StyleRange> styledRanges = new LinkedList<StyleRange>();
    for (int i = 0; i < styledConstructs.length; i++) {
      if (styledConstructs[i] != null) {
        final StyleRange style = new StyleRange();
        style.start = styledConstructs[i].position;
        style.length = styledConstructs[i].length;
        style.background = styledConstructs[i].conctructColor;
        styledRanges.add(style);
        // we will set the styleranges at the end --> needs less time
        // ruleText.setStyleRange(style);
        final String ruleDescr = RegExConstructs
            .getDescriptionForConstructRule(styledConstructs[i].regex);
        this.styleToConstrustDescr.put(style, ruleDescr);
        i = i + styledConstructs[i].length - 1;
      }
    }
    ruleText.setStyleRanges(styledRanges.toArray((new StyleRange[styledRanges.size()])));

  }

  public boolean isRefreshWidgets() {
    return this.refreshWidgets;
  }

  public void setRefreshWidgets(boolean refreshWidgets) {
    this.refreshWidgets = refreshWidgets;
  }

  public boolean isHighLightingEnabled() {
    return this.enableHighlightRegExConstructs;
  }

  public void setHightLighting(boolean highLightRegExContructs) {
    this.enableHighlightRegExConstructs = highLightRegExContructs;
    this.hightLightTextItem.setSelection(highLightRegExContructs);
    highlightRegExConstructs(this.mTextRule.getDisplay(), this.mTextRule);
  }

  /**
   * Remove characters, which were not explicitly created by the user and therefore
   * not visible, for example while pressing the return key 
   * @param text
   * @return
   */
  public static String removeInvisibleCharacters(String text) {
          return replaceInvisibleCharacters(text, '\0', true);
  }

  /**
   * Replace characters which a replacement character, which were not explicitly created by the user and therefore
   * not visible, for example while pressing the return key.<p>
   * If remove was set true, the character will be removed instead. 
   * @param text
   * @param replacement
   * @param remove
   * @return
   */
  public static String replaceInvisibleCharacters(String text, char replacement, boolean remove) {
          StringBuffer newText = new StringBuffer();
          // Issue 98045: since space characters are now visible, we no longer remove them 
          boolean replaceSpacesAtTheEnd = false;
          for (int i = text.length() - 1; i >= 0; i--) {
                  char ch = text.charAt(i);
                  if ((ch == ' ' && replaceSpacesAtTheEnd) ||
                                  (ch != ' ' && Character.isWhitespace(ch))) {
                          // replace characters
                          if (!remove)
                                  newText.append(replacement);
                  }
                  else {
                          replaceSpacesAtTheEnd = false;
                          // add character to text
                          newText.append(ch);
                  }
          }
          return newText.reverse().toString();
          
//        StringBuffer newText = new StringBuffer();
//        for (int i = 0; i < text.length(); i++) {
//                char ch = text.charAt(i);
//                if (Character.isSpaceChar(ch) || Character.isWhitespace(ch)) {
//                        newText.append(replacement);
//                } else 
//                        // add character to text
//                        newText.append(ch);
//        }
//        return newText.toString();
  }

}
