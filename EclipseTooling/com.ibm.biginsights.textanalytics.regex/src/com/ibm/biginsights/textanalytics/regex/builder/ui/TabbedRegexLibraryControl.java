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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabDescriptor;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyViewer;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabContents;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import com.ibm.biginsights.textanalytics.regex.Messages;

/**
 * 
 *  Abraham
 * 
 */
@SuppressWarnings("restriction")
public class TabbedRegexLibraryControl {



  // private boolean mRuleTextIsFocusControl = false;

  // widgets

  TabbedRegexPropertySheetPage tabbedPropertySheetPage;

  ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor;

  private TabbedPropertyComposite tabbedPropertyComposite;

  private TabbedPropertySheetWidgetFactory widgetFactory;

  private final Map<String, Button> constructToButton = new HashMap<String, Button>();

  /**
   * The currently active contributor id, which may not match the contributor id from the workbench
   * part that created this instance.
   */

  protected IStructuredContentProvider tabListContentProvider;

  private TabbedPropertyViewer tabbedPropertyViewer;

  private final Shell shell;

  private TabContents currentTab;

  private Map<TabItem, TabContents> descriptorToTab;

  private final Map<TabContents, Composite> tabToComposite;

  private final List<String> selectionQueue;

  private boolean selectionQueueLocked;

  // private final List tabSelectionListeners;

  private StyledText mRuleText;

  public TabbedRegexLibraryControl(Shell shell) {
    this.shell = shell;
    // init variables
    this.tabbedPropertySheetPageContributor = new ITabbedPropertySheetPageContributor() {

      public String getContributorId() {
        return Messages.TabbedRegexLibraryControl_TAB_REGEX_LIB_CNTRL; 
      }

    };
    this.tabbedPropertySheetPage = new TabbedRegexPropertySheetPage(
        this.tabbedPropertySheetPageContributor);

    this.tabToComposite = new HashMap<TabContents, Composite>();
    this.descriptorToTab = new HashMap<TabItem, TabContents>();

    this.selectionQueue = new ArrayList<String>(10);
    // this.tabSelectionListeners = new ArrayList();

    this.tabListContentProvider = new IStructuredContentProvider() {

      public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
          return ((List<?>) inputElement).toArray();
        }
        return null;
      }

      public void dispose() {
        // do nothing
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
      }

    };

  }

  public void createControl(final Composite parent) {

    this.widgetFactory = new TabbedPropertySheetWidgetFactory();
    this.tabbedPropertyComposite = new TabbedPropertyComposite(parent, this.widgetFactory, false);
    this.widgetFactory.paintBordersFor(this.tabbedPropertyComposite);
    this.tabbedPropertyComposite.setLayout(new FormLayout());
    final FormData formData = new FormData();
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    formData.top = new FormAttachment(0, 0);
    formData.bottom = new FormAttachment(100, 0);
    this.tabbedPropertyComposite.setLayoutData(formData);

    this.tabbedPropertyViewer = new TabbedPropertyViewer(this.tabbedPropertyComposite.getList());

    this.tabbedPropertyViewer.setContentProvider(this.tabListContentProvider);
    this.tabbedPropertyViewer.setLabelProvider(new TabbedPropertySheetPageLabelProvider());
    this.tabbedPropertyViewer.addSelectionChangedListener(new SelectionChangedListener());

    setInput();

    final Control[] childs = this.tabbedPropertyComposite.getList().getChildren();
    for (int i = 0; i < childs.length; i++) {
      childs[i].addMouseListener(new MouseAdapter() {

        @Override
        public void mouseUp(MouseEvent e) {
          // set focus
          TabbedRegexLibraryControl.this.mRuleText.setFocus();
          TabbedRegexLibraryControl.this.mRuleText.isFocusControl();
          TabbedRegexLibraryControl.this.mRuleText.forceFocus();

        }

      });
    }

  }

  private void setInput() {
    final List<TabItem> tabItems = new LinkedList<TabItem>();
    for (int i = 0; i <= 6; i++) {
      final String tabName = RegExConstructs.getConstructName(i);
      boolean useCheckBox = false;
      if (i == RegExConstructs.CONSTRUCTS_MATCHFLAGS) {
        useCheckBox = true;
      }
      final String[] constructs = RegExConstructs.getRegularExpressionContructs(i);
      final TabItem tabItem = new TabItem(tabName, constructs, useCheckBox);
      tabItems.add(tabItem);
    }
    // update Tabs ...
    updateTabs(tabItems.toArray(new TabItem[0]));

    this.tabbedPropertyViewer.setInput(tabItems);
    final int lastTabSelectionIndex = 0;

    final Object selectedTab = this.tabbedPropertyViewer.getElementAt(lastTabSelectionIndex);
    this.selectionQueueLocked = true;
    try {
      if (selectedTab == null) {
        this.tabbedPropertyViewer.setSelection(null);
      } else {
        this.tabbedPropertyViewer.setSelection(new StructuredSelection(selectedTab));
      }
    } finally {
      this.selectionQueueLocked = false;
    }
  }

  public void setRuleText(StyledText textRule) {
    this.mRuleText = textRule;
  }

  public Button getButtonForConstruct(String construct) {
    return this.constructToButton.get(construct);
  }

  private class TabItem extends TabDescriptor {
    private final String[] regexConstructs;

    // indicating, if we use matchflags here, so we have to add CheckBoxes in the composite
    boolean useCheckBoxes;

    ScrolledComposite contentScrollComposite;

    Composite contentComposite;

    public TabItem(String label, String[] constructs, boolean useCheckBoxes) {
      super(null);
      this.useCheckBoxes = useCheckBoxes;
      this.regexConstructs = constructs;
      setLabel(label);
      setSectionDescriptors(new LinkedList<Object>());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return getLabel().hashCode();
    }

    public void resizeScrolledComposite() {
      final Point currentTabSize = new Point(0, 0);
      currentTabSize.y = (this.contentComposite != null) ? this.contentComposite.computeSize(
          SWT.DEFAULT, SWT.DEFAULT).y : 0;
      currentTabSize.x = (this.contentComposite != null) ? this.contentComposite.computeSize(
          SWT.DEFAULT, SWT.DEFAULT).x : 0;
      this.contentScrollComposite.setMinSize(currentTabSize.x, currentTabSize.y);

    }

    public Composite createControls(Composite parent, final TabbedPropertySheetPage page) {
      final Composite container = page.getWidgetFactory().createComposite(parent, SWT.NO_FOCUS);
      final FormLayout layout = new FormLayout();
      container.setLayout(layout);

      // Composite for labels
      final Composite labelComposite = page.getWidgetFactory().createComposite(container,
          SWT.NO_FOCUS | SWT.BORDER);
      labelComposite.setLayout(new GridLayout(2, false));
      // labelComposite.setBackground(shell.getDisplay().getSystemColor(
      // SWT.COLOR_WIDGET_LIGHT_SHADOW));
      labelComposite.setBackground(TabbedRegexLibraryControl.this.shell.getDisplay()
          .getSystemColor(SWT.COLOR_INFO_BACKGROUND));

      FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      labelComposite.setLayoutData(formData);

      final Label label1 = page.getWidgetFactory().createLabel(labelComposite,
          Messages.TabbedRegexLibraryControl_CONSTRUCT); 
      label1.setAlignment(SWT.CENTER);
      FontData[] fontData = label1.getFont().getFontData();
      fontData[0].setStyle(SWT.BOLD);
      label1.setFont(new Font(label1.getFont().getDevice(), fontData));
      label1.setBackground(labelComposite.getBackground());

      GridData data = new GridData(GridData.CENTER);
      data.widthHint = 100;
      label1.setLayoutData(data);

      final Label label2 = page.getWidgetFactory().createLabel(labelComposite,
          Messages.TabbedRegexLibraryControl_MATCHES); 
      label2.setAlignment(SWT.CENTER);
      fontData = label2.getFont().getFontData();
      fontData[0].setStyle(SWT.BOLD);
      label2.setFont(new Font(label2.getFont().getDevice(), fontData));
      label2.setBackground(labelComposite.getBackground());
      data = new GridData(GridData.CENTER);
      label2.setLayoutData(data);

      // ScrolledComposite for contents
      this.contentScrollComposite = page.getWidgetFactory().createScrolledComposite(container,
          SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
      this.contentScrollComposite.setLayout(new FormLayout());
      formData = new FormData();
      formData.top = new FormAttachment(labelComposite, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.bottom = new FormAttachment(100, 0);
      this.contentScrollComposite.setLayoutData(formData);

      this.contentComposite = page.getWidgetFactory().createComposite(this.contentScrollComposite,
          SWT.NO_FOCUS);
      this.contentComposite.setLayout(new GridLayout(2, false));
      formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.bottom = new FormAttachment(100, 0);
      this.contentComposite.setLayoutData(formData);

      this.contentScrollComposite.setContent(this.contentComposite);
      this.contentScrollComposite.setExpandHorizontal(true);
      this.contentScrollComposite.setExpandVertical(true);
      this.contentScrollComposite.setAlwaysShowScrollBars(false);

      this.contentScrollComposite.addControlListener(new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
          resizeScrolledComposite();
        }
      });

      createContent(page);

      return container;

    }

    private void createContent(TabbedPropertySheetPage page) {
      final int widthLabel = TabbedRegexLibraryControl.this.shell.computeSize(SWT.DEFAULT,
          SWT.DEFAULT).x / 2;

      // create buttons with RegexConstructs
      for (int i = 0; i < this.regexConstructs.length; i++) {
        final Composite buttonComp = page.getWidgetFactory().createComposite(this.contentComposite,
            SWT.NO_FOCUS);
        buttonComp.setLayout(new GridLayout(1, true));
        GridData data = new GridData();
        data.widthHint = 100;
        data.verticalAlignment = SWT.TOP;
        buttonComp.setLayoutData(data);

        Button button = null;
        if (this.useCheckBoxes) {
          button = page.getWidgetFactory().createButton(buttonComp, this.regexConstructs[i],
              SWT.CHECK | SWT.NO_FOCUS);
          button.setAlignment(SWT.CENTER);
          TabbedRegexLibraryControl.this.constructToButton.put(this.regexConstructs[i], button);
          data = new GridData(SWT.CENTER, SWT.CENTER, true, true);
          data.widthHint = 50;
          button.setLayoutData(data);
        } else {
          // use button with SWT.PUSH
          button = page.getWidgetFactory().createButton(buttonComp, this.regexConstructs[i],
              SWT.PUSH | SWT.NO_FOCUS);
          button.setAlignment(SWT.CENTER);
          data = new GridData(SWT.CENTER, SWT.CENTER, true, true);
          data.widthHint = 50;
          button.setLayoutData(data);

        }

        // Accessibility support for special regex expressions
        final String accString = this.regexConstructs[i];
        button.getAccessible ().addAccessibleListener (new AccessibleAdapter() {

          @Override
          public void getName (AccessibleEvent e)
          {
            if (accString.equals (RegExConstructs.CHARACTERS_SPACE))
              e.result = Messages.RegExConstructs_SPACE;
            else if (accString.equals (RegExConstructs.GREEDYQUANTIFIERS_ONCEORNOT))
              e.result = Messages.RegExConstructs_QUESTION_MARK;
            else if (accString.equals (RegExConstructs.GREEDYQUANTIFIERS_AT_LEAST_N_TIMES))
              e.result = Messages.RegExConstructs_COMMA;
            else if (accString.equals (RegExConstructs.PREDEFCHARCLASSES_ANYCHARACTER))
              e.result = Messages.RegExConstructs_DOT;
          }
          
        });

        button.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseUp(MouseEvent e) {
            if (TabbedRegexLibraryControl.this.mRuleText != null) {
              // set focus
              TabbedRegexLibraryControl.this.mRuleText.setFocus();
              TabbedRegexLibraryControl.this.mRuleText.isFocusControl();
              TabbedRegexLibraryControl.this.mRuleText.forceFocus();
            }
          }

        });

        final Composite textComp = page.getWidgetFactory().createComposite(this.contentComposite,
            SWT.NO_FOCUS);
        textComp.setBackground(this.contentComposite.getBackground());
        textComp.setLayout(new GridLayout(1, true));
        data = new GridData();
        textComp.setLayoutData(data);

        final String title = RegExConstructs
            .getDescriptionTitleForConstruct(this.regexConstructs[i]);
        if (title != null) {
          final Label titleLabel = new Label(textComp, SWT.WRAP | SWT.READ_ONLY);
          titleLabel.setBackground(textComp.getBackground());
          titleLabel.setText(title);

          final FontData[] fontData = titleLabel.getFont().getFontData();
          fontData[0].setStyle(SWT.BOLD);
          titleLabel.setFont(new Font(titleLabel.getFont().getDevice(), fontData));

          data = new GridData();
          data.widthHint = widthLabel;
          titleLabel.setLayoutData(data);

        }

        final Text buttonLabel = new Text(textComp, SWT.WRAP | SWT.READ_ONLY);
        buttonLabel.setBackground(textComp.getBackground());
        buttonLabel.setText(RegExConstructs.getDescriptionForConstruct(this.regexConstructs[i]));
        buttonLabel.setEditable(false);

        button.setToolTipText(buttonLabel.getText());

        data = new GridData();
        data.widthHint = widthLabel;
        buttonLabel.setLayoutData(data);

        if (this.useCheckBoxes) {
          button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

              // if (!mRuleTextIsFocusControl)
              // return;

              final Button sourceButton = (Button) e.getSource();
              final String construct = sourceButton.getText();

              // get cursor position
              int offset = TabbedRegexLibraryControl.this.mRuleText.getCaretOffset();

              if (sourceButton.getSelection()) {
                // add to beginning of rule
                TabbedRegexLibraryControl.this.mRuleText.setText(construct
                    + TabbedRegexLibraryControl.this.mRuleText.getText());
                sourceButton.setSelection(true);
                offset = offset + construct.length();
              } else {
                // remove from rule
                String text = TabbedRegexLibraryControl.this.mRuleText.getText();
                final String rule = RegExConstructs.getRegexForRegularExpressionContruct(construct);
                text = text.replaceFirst(rule, "");  //$NON-NLS-1$
                TabbedRegexLibraryControl.this.mRuleText.setText(text);
                offset = offset - construct.length();
              }

              // set cursor position
              TabbedRegexLibraryControl.this.mRuleText.setCaretOffset(offset);
              // set focus to rule text
              // mRuleText.setFocus();
              // mRuleText.forceFocus();

            }
          });

          // check, if this button have to be selected
          final String text = TabbedRegexLibraryControl.this.mRuleText.getText();
          if ((text != null) && (text.length() > 0)) {
            final String rule = RegExConstructs
                .getRegexForRegularExpressionContruct(this.regexConstructs[i]);
            // find character constructs
            final Pattern pattern = Pattern.compile(rule);
            final Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
              button.setSelection(true);
            }
          }

        } else {
          // add current construct to end of regex field
          button.addSelectionListener(new SelectionAdapter() {

            /**
             * opens an input dialog and returns the result from the input field
             * 
             * @param title1
             * @param message
             * @param value
             * @return
             */
            public String openDialog(final String title1, String message, String value, IInputValidator val) {
              String choice = null;
              final InputDialog dialog = new InputDialog(TabbedRegexLibraryControl.this.shell,
                  title1, message, value, val);
              final int dialogResult = dialog.open();
              if (dialogResult == Window.OK) {
                choice = dialog.getValue();
                if ((choice != null) && choice.equals("")) { //$NON-NLS-1$
                  choice = null;
                }
              }
              return choice;
            }

            @Override
            public void widgetSelected(SelectionEvent e) {

              // if (!mRuleTextIsFocusControl)
              // return;

            final Button sourceButton = (Button) e.getSource();
            String construct = sourceButton.getText();
            IInputValidator validator= null;
            final Object[] vars = RegExConstructs.getVariablesInConstruct(construct);
            
            if ((construct.equals(RegExConstructs.GREEDYQUANTIFIERS_EXACTLY_N_TIMES) || construct.equals(RegExConstructs.GREEDYQUANTIFIERS_AT_LEAST_N_TIMES) || construct.equals(RegExConstructs.GREEDYQUANTIFIERS_AT_LEAST_N_NOT_MORE_M)))
            {
            	validator = new IInputValidator() {
                    public String isValid(String newText) {
                      if(containsOnlyNumbers(newText) || newText.equalsIgnoreCase("test"))
                    	  return null;
                      else
                    	  return Messages.TabbedRegexLibraryControl_NUMERIC_CHECK_MSG;
                    }

                    public boolean containsOnlyNumbers(String str) {
                        
                        //It can't contain only numbers if it's null or empty...
                        if (str == null || str.length() == 0)
                            return false;
                        
                        for (int i = 0; i < str.length(); i++) {

                            //If we find a non-digit character we return false.
                            if (!Character.isDigit(str.charAt(i)))
                                return false;
                        }
                        
                        return true;
                    }
            	};
            }
            else
            {
            	validator = null;
            }
            
            
            

              if (vars.length > 0) {
                // open dialog
                String varValue = null;
                for (int j = 0; j < vars.length; j++) {
                  final String[] question = (String[]) vars[j];
                  varValue = openDialog(question[0], question[2], null, validator);
                  
                  if (varValue != null) {
                    // error in replaceFirst, so we have to create it ourself
                    // construct = construct.replaceFirst(
                    // question[1], varValue);
                    final int varIdx = construct.indexOf(question[1]);
                    if (varIdx == 0) {
                      construct = varValue + construct.substring(varIdx + question[1].length());
                    } else if (varIdx == construct.length()) {
                      construct = construct.substring(0, varIdx) + varValue;
                    } else {
                      construct = construct.substring(0, varIdx) + varValue
                          + construct.substring(varIdx + question[1].length());
                    }
                  } else {
                    break;
                  }
                }
                if (varValue == null) {
                  construct = ""; //$NON-NLS-1$
                }
              }

              // get cursor position
              final int offset = TabbedRegexLibraryControl.this.mRuleText.getCaretOffset();
              // insert att cursor position
              final String oldText = TabbedRegexLibraryControl.this.mRuleText.getText();
              TabbedRegexLibraryControl.this.mRuleText.setText(oldText.substring(0, offset)
                  + construct + oldText.substring(offset));
              // set cursor position
              TabbedRegexLibraryControl.this.mRuleText.setCaretOffset(offset + construct.length());
              // set focus
              // mRuleText.setFocus();
              // mRuleText.isFocusControl();
              // mRuleText.forceFocus();
            }

          });

        } // END OF ELSE
      }
    }

  } // END OF private class TabItem extends TabDescriptor

  /**
   * Update the current tabs to represent the given input object. When tabs apply for both the old
   * and new input they are reused otherwise they are disposed. If the current visible tab will not
   * be reused (i.e. will be disposed) we have to send it an aboutToBeHidden() message.
   */
  protected void updateTabs(TabItem[] descriptors) {
    final Map<TabItem, TabContents> newTabs = new HashMap<TabItem, TabContents>(
        descriptors.length * 2);
    boolean disposingCurrentTab = (this.currentTab != null);
    for (int i = 0; i < descriptors.length; i++) {
      TabContents tab = this.descriptorToTab.remove(descriptors[i]);

      if ((tab != null) && tab.controlsHaveBeenCreated()) {
        if (tab == this.currentTab) {
          disposingCurrentTab = false;
        }
      } else {
        tab = (descriptors[i]).createTab();
      }

      newTabs.put(descriptors[i], tab);
    }
    if (disposingCurrentTab) {
      /**
       * If the current tab is about to be disposed we have to call aboutToBeHidden
       */
      this.currentTab.aboutToBeHidden();
      this.currentTab = null;
    }
    disposeTabs(this.descriptorToTab.values());
    this.descriptorToTab = newTabs;
  }

  private void disposeTabs(Collection<TabContents> tabs) {
    for (final Iterator<TabContents> iter = tabs.iterator(); iter.hasNext();) {
      final TabContents tab = iter.next();
      final Composite composite = this.tabToComposite.remove(tab);
      tab.dispose();
      if (composite != null) {
        composite.dispose();
      }
    }
  }

  /**
   * Stores the current tab label in the selection queue. Tab labels are used to carry the tab
   * context from one input object to another. The queue specifies the selection priority. So if the
   * first tab in the queue is not available for the input we try the second tab and so on. If none
   * of the tabs are available we default to the first tab available for the input.
   */
  private void storeCurrentTabSelection(String label) {
    if (!this.selectionQueueLocked) {
      this.selectionQueue.remove(label);
      this.selectionQueue.add(0, label);
    }
  }

  /**
   * Label provider for the ListViewer.
   */
  private static class TabbedPropertySheetPageLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
      if (element instanceof TabDescriptor) {
        return ((TabDescriptor) element).getLabel();
      }
      return null;
    }
  }

  // TODO this solution can be improved, because not all methods of TabbedPropertySheetPage will
  // work, but we need this class for the framework ...!
  class TabbedRegexPropertySheetPage extends TabbedPropertySheetPage {

    public TabbedRegexPropertySheetPage(
        ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
      super(tabbedPropertySheetPageContributor);
    }

    @Override
    protected IStructuredContentProvider getTabListContentProvider() {
      return this.tabListContentProvider;
    }

    @Override
    public TabbedPropertySheetWidgetFactory getWidgetFactory() {
      return TabbedRegexLibraryControl.this.widgetFactory;
    }

  }

  /**
   * SelectionChangedListener for the ListViewer.
   */
  class SelectionChangedListener implements ISelectionChangedListener {

    /**
     * Shows the tab associated with the selection.
     */
    public void selectionChanged(SelectionChangedEvent event) {
      final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      TabContents tab = null;
      final TabItem descriptor = (TabItem) selection.getFirstElement();

      if (descriptor == null) {
        // pretend the tab is empty.
        hideTab(TabbedRegexLibraryControl.this.currentTab);
      } else {
        // create tab if necessary
        // can not cache based on the id - tabs may have the same id,
        // but different section depending on the selection
        tab = TabbedRegexLibraryControl.this.descriptorToTab.get(descriptor);

        if (tab != TabbedRegexLibraryControl.this.currentTab) {
          hideTab(TabbedRegexLibraryControl.this.currentTab);
        }

        Composite tabComposite = TabbedRegexLibraryControl.this.tabToComposite.get(tab);
        if (tabComposite == null) {
          tabComposite = createTabComposite();

          descriptor.createControls(tabComposite,
              TabbedRegexLibraryControl.this.tabbedPropertySheetPage);

          tabComposite.setSize(tabComposite.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
          tabComposite.layout();

          TabbedRegexLibraryControl.this.tabToComposite.put(tab, tabComposite);
        }

        // store tab selection
        storeCurrentTabSelection(descriptor.getLabel());

        if (tab != TabbedRegexLibraryControl.this.currentTab) {
          showTab(tab);
        }

        tab.refresh();
      }
      TabbedRegexLibraryControl.this.tabbedPropertyComposite.getTabComposite().layout(true);
      TabbedRegexLibraryControl.this.currentTab = tab;

    }

    /**
     * Shows the given tab.
     */
    private void showTab(TabContents target) {
      if (target != null) {
        final Composite tabComposite = TabbedRegexLibraryControl.this.tabToComposite.get(target);
        if (tabComposite != null) {
          /**
           * the following method call order is important - do not change it or the widgets might be
           * drawn incorrectly
           */
          tabComposite.moveAbove(null);
          target.aboutToBeShown();
          tabComposite.setVisible(true);
        }
      }
    }

    /**
     * Hides the given tab.
     */
    private void hideTab(TabContents target) {
      if (target != null) {
        final Composite tabComposite = TabbedRegexLibraryControl.this.tabToComposite.get(target);
        if (tabComposite != null) {
          target.aboutToBeHidden();
          tabComposite.setVisible(false);
        }
      }
    }

  }

  /**
   * Helper method for creating property tab composites.
   */
  private Composite createTabComposite() {
    final Composite result = this.widgetFactory.createComposite(this.tabbedPropertyComposite
        .getTabComposite(), SWT.NO_FOCUS);
    result.setVisible(false);
    result.setLayout(new FillLayout());
    final FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    result.setLayoutData(data);
    return result;
  }

}
