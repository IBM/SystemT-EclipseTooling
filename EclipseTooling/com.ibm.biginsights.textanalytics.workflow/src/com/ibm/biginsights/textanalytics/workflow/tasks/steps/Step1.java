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
package com.ibm.biginsights.textanalytics.workflow.tasks.steps;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.CollectionModel;
import com.ibm.biginsights.textanalytics.workflow.tasks.DataFilesTable;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ExtractionTasksExamplesLinksPaths;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;
import com.ibm.icu.text.MessageFormat;

public class Step1 extends Composite
{



  protected FileDirectoryPicker browser;
  protected DataFilesTable tableView;
  protected Combo langCombo;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public Step1 (Composite parent, int style)
  {
    super (parent, style);
    setLayout (new GridLayout (1, false));

    generateStepA ();

    // separator
    Label label = new Label (this, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));

    generateStepB ();

    generateControls ();

    Label clearLast = new Label (this, SWT.NONE);
    GridData gd_clear = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_clear.verticalIndent = 15;
    clearLast.setLayoutData (gd_clear);

    populateStep1Fields();
  }

  /**
   * Populate the Step1 fields with info of project opened in Extraction Plan.
   */
  private void populateStep1Fields ()
  {
    CollectionModel collectionModel = ActionPlanView.getCollection ();

    if (collectionModel != null) {
      langCombo.setText (collectionModel.getLangCode ());
      browser.setFileDirValue (ProjectPreferencesUtil.getPath (collectionModel.getPath ()), true);
    }
  }

  private void generateStepA ()
  {
    Composite stepA = new Composite (this, SWT.NONE);
    stepA.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    stepA.setBounds (0, 0, 64, 64);
    stepA.setLayout (new GridLayout (1, false));

    Text labelA = new Text (stepA, SWT.WRAP);
    labelA.setEditable (false);
    labelA.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    labelA.setBounds (0, 0, 49, 13);
    labelA.setText (Messages.step_1a_title);
    labelA.setFont (Styles.LABEL_FONT);

    browser = new FileDirectoryPicker (stepA, Constants.FILE_OR_DIRECTORY, FileDirectoryPicker.WORKSPACE_ONLY);
    browser.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));

    browser.setTitle (Messages.step_1_browser_title);
    browser.setDescriptionLabelText (Messages.step_1a_inline_text);
    browser.setAllowMultipleSelection(false);
    browser.setAllowedFileExtensions(Constants.SUPPORTED_DOC_FORMATS);  //to display allowed formats
    browser.setEnableShowAllFilesOption(true);                          //to display the Show all files Option.

    browser.addModifyListenerForFileDirTextField (listener);

    Composite langComposite = new Composite (stepA, SWT.NONE);
    langComposite.setLayout (new GridLayout (2, false));
    langComposite.setLayoutData (new GridData (SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));

    Label lblLanguage = new Label (langComposite, SWT.NONE);
    lblLanguage.setAlignment (SWT.CENTER);
    lblLanguage.setText (Messages.lang_title);

    langCombo = new Combo (langComposite, SWT.NONE | SWT.READ_ONLY);
    langCombo.setLayoutData (new GridData (SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    langCombo.setToolTipText (Messages.lang_tootltip);

    LangCode[] codes = LangCode.values ();
    for (int i = 0; i < codes.length; i++) {
      langCombo.add (codes[i].toString ());
    }

    langCombo.addModifyListener (langListener);
  }

  private void generateStepB ()
  {
    Composite stepB = new Composite (this, SWT.NONE);
    stepB.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    stepB.setBounds (0, 0, 64, 64);
    stepB.setLayout (new GridLayout (1, false));

    Text labelB = new Text (stepB, SWT.WRAP);
    labelB.setEditable (false);
    labelB.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    labelB.setBounds (0, 0, 49, 13);
    labelB.setText (Messages.step_1b_title);
    labelB.setFont (Styles.LABEL_FONT);

    ScrolledComposite scroller = new ScrolledComposite (stepB, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
    GridData gd = new GridData (SWT.FILL, SWT.FILL, true, true, 1, 1);
    gd.heightHint = 150;
    scroller.setLayoutData (gd);
    scroller.setExpandHorizontal (true);
    scroller.setExpandVertical (true);

    Composite table_composite = new Composite (scroller, SWT.NONE);
    table_composite.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true, 1, 1));
    table_composite.setLayout (new TableColumnLayout ());
    table_composite.setToolTipText (Messages.step_1b_title);

    tableView = new DataFilesTable ();
    tableView.createPartControl (table_composite);

    scroller.setContent (table_composite);
    scroller.setMinSize (table_composite.computeSize (SWT.DEFAULT, SWT.DEFAULT));
  }

  private void generateControls ()
  {
    Composite buttons_composite = new Composite (this, SWT.NONE);
    buttons_composite.setLayout (new RowLayout (SWT.HORIZONTAL));
    buttons_composite.setLayoutData (new GridData (SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

    Button btnNewButton = new Button (buttons_composite, SWT.NONE);
    btnNewButton.addSelectionListener (new SelectionAdapter () {
      @Override
      public void widgetSelected (SelectionEvent e)
      {
        doOpen ();
      }
    });
    
    
    btnNewButton.setLayoutData (new RowData (65, SWT.DEFAULT));
    btnNewButton.setText (Messages.step_1_open_btn_label);
    
    Composite help_composite = new Composite (this, SWT.NONE);
    help_composite.setLayout (new GridLayout (1, true));
    GridData ldata = new GridData (SWT.LEFT, SWT.CENTER, true, false, 1, 1);
    ldata.horizontalIndent = 15;
    help_composite.setLayoutData (ldata);
    
    Link link1 = new Link(help_composite, SWT.NONE);
    link1.setText(Messages.step_1_example_txt);
    link1.setToolTipText (Messages.open_help_tooltip);
    link1.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_1B_example);
      }
    });
    link1.addListener(SWT.MouseDown, new Listener() {
      @Override
      public void handleEvent(Event event) {
        AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_1B_example);
      }
    });
  }

  protected void doOpen ()
  {
    tableView.getOpenAction ().run ();
  }

  protected void doDelete ()
  {
    tableView.getDeleteAction ().run ();
  }

  ModifyListener listener = new ModifyListener () {
    @Override
    public void modifyText (ModifyEvent e)
    {
      try {
        loadNewFile (browser.getFileDirValue ());
      }
      catch (Exception e1) {
        // Do not show error when extraction is opening. (defect 20119)
        if (!AqlProjectUtils.isActionPlanOpening()) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_collection_not_readable);
        }
      }
    }
  };

  ModifyListener langListener = new ModifyListener () {
    @Override
    public void modifyText (ModifyEvent e)
    {
      String langCode = langCombo.getText ();
      storeCollectionLang (langCode);
    }
  };

  public void reset ()
  {
    tableView.clearFiles ();
    langCombo.deselectAll ();
  }

  public List<String> getSelectedFiles ()
  {
    return tableView.getSelectedFiles ();
  }

  public void clear ()
  {
    browser.clearFileDirValue ();
    langCombo.deselectAll ();   // I tried clearSelection(), it doesn't clear the combo. deselectAll() does.
    tableView.clearFiles ();
  }

  /**
   * Clear the current data collection and load the one specified by the parameter 'path'.<br>
   * If 'path' is empty, current data collection is still cleared and nothing is loaded.
   * @param path The path to a data collection.
   * @throws Exception
   */
  public void loadNewFile (String path) throws Exception
  {
    tableView.clearFiles ();

    if (StringUtils.isEmpty (path)) { // empty path means the document selection field is cleared.
      storeCollectionFile (path);
      return;
    }

    try {
      browser.removeModifyListenerForFileDirTextField (listener);

      String absPath = ProjectPreferencesUtil.getAbsolutePath (path);
      if (StringUtils.isEmpty (absPath)) {
        // Since absPath is empty, show message with the path (w/o the workspace prefix [W])
        if (path.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX))
          path = path.substring (Constants.WORKSPACE_RESOURCE_PREFIX.length ());

        throw new Exception (MessageFormat.format (Messages.extraction_plan_collection_path_not_exists, new Object[] { path }));
      }

      File file = new File (absPath);
      if (!file.exists ())
        throw new Exception (MessageFormat.format (Messages.extraction_plan_collection_path_not_exists, new Object[] { absPath }));

      else if (!file.canRead ())
        throw new Exception (Messages.extraction_plan_collection_not_readable);

      else if (file.isDirectory () || (path.matches (".*(\\.tar\\.gz|\\.tgz|\\.tar|\\.del|\\.zip)"))) {
        Map<String, String> files = AqlProjectUtils.getFilesContent (file);
        for (String afile : files.keySet ()) {
          loadAFile (afile, path);
        }
      }

      else {
        String name = file.getName ();
        loadAFile (name, path);
      }
      
      browser.setFileDirValue (ProjectPreferencesUtil.getPath (path), true);

      storeCollectionFile (path);
    }
    catch (Exception e) {
      browser.clearFileDirValue ();
      tableView.clearFiles ();
      throw e;
    }
    finally {
      browser.addModifyListenerForFileDirTextField (listener);
    }
  }

  /**
   * @param langCode
   */
  public void setLangCode (String langCode)
  {
    String[] langs = langCombo.getItems ();
    if (langCode.isEmpty ()) langCode = "en";

    for (int i = 0; i < langs.length; i++) {
      if (langs[i].equalsIgnoreCase (langCode)) {
        langCombo.select (i);
        return;
      }
    }
  }

  public void showCollectionDialog ()
  {
    browser.showCollectionDialog ();
  }

  /**
   * @param file
   */
  protected void storeCollectionFile (String path)
  {
    if (ActionPlanView.collection != null) {
      ActionPlanView.collection.setPath (path);
      ActionPlanView.serializePlan ();
    }
  }

  /**
   * @param langCode
   */
  protected void storeCollectionLang (String langCode)
  {
    if (ActionPlanView.collection != null) {
      ActionPlanView.collection.setLangCode (langCode);
      ActionPlanView.serializePlan ();
    }
  }

  protected void loadAFile (String label, String parent)
  {
    tableView.addDataFile (label, parent);
  }

  @Override
  protected void checkSubclass ()
  {
    // Disable the check that prevents subclassing of SWT components
  }
}
