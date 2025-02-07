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
package com.ibm.biginsights.textanalytics.workflow.perspectives;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.tasks.ExtractionTasksView;

/**
 * Defines the Perspective to be used for Text Analytics
 * 
 * 
 */
public class TextAnalyticsPerspective implements IPerspectiveFactory
{



  public static final String ID = "com.ibm.biginsights.textanalytics.workflow.perspectives.TextAnalyticsPerspective";

  public TextAnalyticsPerspective ()
  {
    super ();
  }

  /**
   * creates the initial layout for this perspective and adds the wanted elements to be displayed by default (@note the
   * user is free to modify these values)
   */
  public void createInitialLayout (IPageLayout layout)
  {

    // Creates the overall folder layout.
    // Note that each new Folder uses a percentage of the remaining
    // EditorArea.

    String ID_PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$

    String editorArea = layout.getEditorArea ();

    IFolderLayout left_folder = layout.createFolder ("leftFolder", IPageLayout.LEFT, (float) 0.2, editorArea); //$NON-NLS-1$
    left_folder.addView (ExtractionTasksView.ID);
    left_folder.addView (ID_PROJECT_EXPLORER);
    left_folder.addView (JavaUI.ID_PACKAGES);
    left_folder.addView (BIConstants.LOCATIONS_VIEW_ID);

    IFolderLayout right_folder = layout.createFolder ("topRight", IPageLayout.RIGHT, 0.8f, layout.getEditorArea ());
    right_folder.addView (ActionPlanView.ID);

    IFolderLayout bottom_folder = layout.createFolder ("bottom", IPageLayout.BOTTOM, (float) 0.7, editorArea); //$NON-NLS-1$
    bottom_folder.addView (IPageLayout.ID_PROBLEM_VIEW);
    bottom_folder.addPlaceholder (NewSearchUI.SEARCH_VIEW_ID);
    bottom_folder.addView (IConsoleConstants.ID_CONSOLE_VIEW);
    bottom_folder.addView (ConcordanceView.VIEW_ID);

    layout.addActionSet (IDebugUIConstants.LAUNCH_ACTION_SET);
    layout.addActionSet (JavaUI.ID_ACTION_SET);
    layout.addActionSet (JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
    layout.addActionSet (IPageLayout.ID_NAVIGATE_ACTION_SET);

    // views - workflow ui
    layout.addShowViewShortcut (ActionPlanView.ID); // NON-NLS-1
    layout.addShowViewShortcut (ExtractionTasksView.ID);

    // views - java
    layout.addShowViewShortcut (JavaUI.ID_PACKAGES);
    layout.addShowViewShortcut (JavaUI.ID_SOURCE_VIEW);

    // views - search
    layout.addShowViewShortcut (NewSearchUI.SEARCH_VIEW_ID);

    // views - debugging
    layout.addShowViewShortcut (IConsoleConstants.ID_CONSOLE_VIEW);

    // views - standard workbench
    layout.addShowViewShortcut (IPageLayout.ID_OUTLINE);
    layout.addShowViewShortcut (IPageLayout.ID_PROBLEM_VIEW);
    layout.addShowViewShortcut (IPageLayout.ID_TASK_LIST);
    layout.addShowViewShortcut (IProgressConstants.PROGRESS_VIEW_ID);
    layout.addShowViewShortcut (ID_PROJECT_EXPLORER);
    layout.addShowViewShortcut ("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

    // new actions - Java project creation wizard
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
    layout.addNewWizardShortcut ("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

    // 'Window' > 'Open Perspective' contributions
    layout.addPerspectiveShortcut (BIConstants.BI_PERSPECTIVE_ID);

  }

}
