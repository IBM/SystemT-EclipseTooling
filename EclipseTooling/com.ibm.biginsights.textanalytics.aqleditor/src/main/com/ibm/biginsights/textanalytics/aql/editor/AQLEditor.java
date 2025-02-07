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
package com.ibm.biginsights.textanalytics.aql.editor;

import static com.ibm.biginsights.textanalytics.aql.editor.Activator.EDITOR_SCOPE;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.callhierarchy.CallHierarchyUtil;
import com.ibm.biginsights.textanalytics.aql.editor.outline.IAQLOutlineView;
import com.ibm.biginsights.textanalytics.aql.editor.outline.OutlineView;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLConfiguration;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLDocumentProvider;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLEditorUtils;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLViewer;
import com.ibm.biginsights.textanalytics.aql.editor.ui.ColorManager;
import com.ibm.biginsights.textanalytics.aql.library.AQLModel;
import com.ibm.biginsights.textanalytics.aql.library.AQLParseErrorHandler;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.aql.library.ModularAQLModel;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.ViewEditorInput;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IAQLEditor;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * AQLEditor class is responsible for opening, editing and saving AQL files in AQL editor. This class also contains the
 * information about color manager, document providers, AQL library details and other aql configuration details etc.
 */
public class AQLEditor extends TextEditor implements IAQLEditor
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor";

  boolean isModularProject;
  private ColorManager colorManager;
  public static IAQLLibrary aqlLibrary; // Holds the aql library related information
  public AQLModel model;
  public ModularAQLModel modularModel; // AQl Library model for 15 or modular projects
  public IAQLOutlineView outlinePage;

  public AQLConfiguration aqlConfiguration = null;
  public AQLDocumentProvider aqlDocProvider = null;

  private static final Pattern OUTPUT_VIEW_STMT_PATTERN = Pattern.compile ("\\s*output\\s*view\\s*([^\\s]+)\\s*;",
    Pattern.CASE_INSENSITIVE);

  private static final Pattern OUTPUT_VIEW_PATTERN = Pattern.compile ("\\s*output\\s*view\\s*",
    Pattern.CASE_INSENSITIVE);

  private static final Pattern VIEW_NAME_PATTERN = Pattern.compile ("\\s*([^\\s]+)\\s*;", Pattern.CASE_INSENSITIVE);

  public AQLEditor ()
  {
    super ();
    this.colorManager = new ColorManager ();
    outlinePage = createOutlinePage ();

    this.aqlConfiguration = new AQLConfiguration (this.colorManager, this);
    this.aqlDocProvider = new AQLDocumentProvider ();
    // parse status : "AQLLibrary: " + aqlLibrary.hashCode() + aqlLibrary.getLibraryMap() + aqlLibrary.getParsedPath()
    setSourceViewerConfiguration (this.aqlConfiguration);
    setDocumentProvider (this.aqlDocProvider);
    // Register caret listener
    this.setHelpContextId ("com.ibm.biginsights.textanalytics.tooling.help.aql_editor");
  }

  // This method is to check if the project is modular type or non modular.
  private boolean isThisModularProject ()
  {
    IEditorInput input = this.getEditorInput ();
    if (input != null && !(input instanceof IFileEditorInput)) { // we get a FileStoreEditorInput when opening an aql
                                                                 // file outside workspace.
      return false; // aqleditor will treat such files as non-modular aql files.
    }
    IFileEditorInput wkspcInput = (IFileEditorInput) input;
    if (!(wkspcInput == null)) {
      return ProjectUtils.isModularProject (wkspcInput.getFile ().getProject ());
    }
    else {
      try {
        IProject project = ProjectUtils.getSelectedProject ();
        if (project != null) { return ProjectUtils.isModularProject (project); }
      }
      catch (NullPointerException e) {
        return true; // During the initial load when nothing is selected it will return true
      }
      return true; // By default it will load the modular library..
    }
  }

  @Override
  public void init (IEditorSite site, IEditorInput input) throws PartInitException
  {
    if (input instanceof ViewEditorInput) AQLEditorUtils.populateAqlInfoForViewEditorInput ((ViewEditorInput) input);

    super.init (site, input);

    isModularProject = isThisModularProject ();
    if (isModularProject) {
      aqlLibrary = Activator.getModularLibrary ();
      modularModel = (ModularAQLModel) aqlLibrary.getAQLModel ();
    }
    else {
      aqlLibrary = Activator.getLibrary ();
      model = (AQLModel) aqlLibrary.getAQLModel ();
    }
  }

  @Override
  public void dispose ()
  {
    this.colorManager.dispose ();
    this.aqlConfiguration.dispose ();
    this.aqlConfiguration = null;
    this.aqlDocProvider = null;
    super.dispose ();
  }

  public AQLConfiguration getAqlConfiguration ()
  {
    return (AQLConfiguration) getSourceViewerConfiguration ();
  }

  /**
   * Get the current caret offset (number of characters from start of buffer).
   * 
   * @return The caret offset.
   */
  public int getCaretOffset ()
  {
    ISourceViewer sourceViewer = getSourceViewer ();
    StyledText styledText = sourceViewer.getTextWidget ();
    return widgetOffset2ModelOffset (sourceViewer, styledText.getCaretOffset ());
  }

  /**
   * Set the cursor to a certain position and show the cursor.
   * 
   * @param line The line the cursor should go to.
   * @param col The column the cursor should go to.
   */
  public void setCursorAndMoveTo (final int line, final int col)
  {
    StyledText st = getSourceViewer ().getTextWidget ();
    int offset = st.getOffsetAtLine (line - 1);
    offset += col - 1;
    st.setCaretOffset (offset);
    selectAndReveal (offset, 0);
  }

  public void setCursorAndMoveTo (final int offset)
  {
    StyledText st = getSourceViewer ().getTextWidget ();
    st.setCaretOffset (offset);
    selectAndReveal (offset, 0);
  }

  /**
   * @param text
   */
  public int insertText (String text)
  {
    StyledText st = getSourceViewer ().getTextWidget ();
    st.setCaretOffset (st.getCharCount ());
    st.insert ("\n\n");

    int offset = st.getCharCount ();

    st.setCaretOffset (offset);
    st.insert (text);

    st.setCaretOffset (offset);
    selectAndReveal (offset, 0);

    return offset;
  }

  // Add a SystemT specific scope/context for key bindings. AQL editor
  // specific key bindings should
  // only be available inside the AQL editor. See also the context extension
  // in the plugin.xml.
  @Override
  protected void initializeKeyBindingScopes ()
  {
    String[] scopes = new String[] { Messages.AQLEditor_EDITOR_SCOPE, EDITOR_SCOPE };
    setKeyBindingScopes (scopes);
  }

  protected AQLEditor getEditor ()
  {
    IEditorPart editor = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ();

    return (AQLEditor) editor;
  }

  public void editorContextMenuAboutToShow (IMenuManager menu)
  {
    super.editorContextMenuAboutToShow (menu);

    try {
      AQLEditor aqlEditor = getEditor ();

      TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor.getDocumentProvider ();
      IDocument doc = docProvider.getDocument (aqlEditor.getEditorInput ());

      ITextSelection textSelection = (ITextSelection) aqlEditor.getSelectionProvider ().getSelection ();

      int location = textSelection.getOffset ();

      ImageDescriptor regexGenerator = ImageDescriptor.createFromFile (this.getClass (),
        Messages.AQLEditor_REGEX_GEN_ICON);
      ImageDescriptor regexBuilder = ImageDescriptor.createFromFile (this.getClass (),
        Messages.AQLEditor_REGEX_BUILDER_ICON);

      // Comment out. We no longer support running PD from AQL editor (defect 20349)
      // ImageDescriptor patternDiscovery = ImageDescriptor.createFromFile(
      // this.getClass(), Messages.AQLEditor_PATTERN_DISCOVERY_ICON);

      menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new Separator ());

      ITypedRegion region = doc.getPartition (location);

      if (region.getType () == AQLPartitionScanner.AQL_REGEX) {
        final CommandContributionItemParameter builderContributionParameter = new CommandContributionItemParameter (
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow (), Messages.AQLEditor_REGEX_BUILDER_MENU,
          Messages.AQLEditor_REGEX_BUILDER_WIZARD_COMMAND, Collections.emptyMap (), regexBuilder, regexBuilder,
          regexBuilder, Messages.AQLEditor_REGEX_BUILDER, null, null, SWT.PUSH, null, true);

        final CommandContributionItemParameter learnerContributionParameter = new CommandContributionItemParameter (
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow (), Messages.AQLEditor_REGEX_GEN_MENU,
          Messages.AQLEditor_REGEX_GEN_WIZARD_COMMAND, Collections.emptyMap (), regexGenerator, regexGenerator,
          regexGenerator, Messages.AQLEditor_REGEX_GENERATOR, null, null, SWT.PUSH, null, true);

        menu.appendToGroup (Messages.AQLEditor_ADDITIONS, new CommandContributionItem (builderContributionParameter));
        menu.appendToGroup (Messages.AQLEditor_ADDITIONS, new CommandContributionItem (learnerContributionParameter));

      }

      String candidateToken = getViewCandidateToken (doc, region);

      // Adding Rename Element item to context menu
      final CommandContributionItemParameter eleRenameContributionParam = new CommandContributionItemParameter (
        PlatformUI.getWorkbench ().getActiveWorkbenchWindow (),
        null,
        "com.ibm.biginsights.textanalytics.aql.editor.command.elementrename", //$NON-NLS-1$
        Collections.emptyMap (), null, null, null, Messages.AQLEditor_ELEMENT_RENAME_COMMAND, null, null, SWT.PUSH,
        null, true);

      menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new CommandContributionItem (
        eleRenameContributionParam));

      menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new Separator ());
      // Context Menu Option for opening dependency hierarchy
      if (CallHierarchyUtil.isView (candidateToken)) {
        final CommandContributionItemParameter dependencyHierarchyContributionParameter = new CommandContributionItemParameter (
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow (),
          null,
          "com.ibm.biginsights.textanalytics.aql.editor.command.parentHierarchy", //$NON-NLS-1$
          Collections.emptyMap (), null, null, null, Messages.AQLEditor_OPEN_DEPENDENCY_HIERARCHY, null, null,
          SWT.PUSH, null, true);

        menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new CommandContributionItem (
          dependencyHierarchyContributionParameter));
      }

      // Context Menu Option for opening reference hierarchy
      if (CallHierarchyUtil.isView (candidateToken)) {
        final CommandContributionItemParameter referenceHierarchyContributionParameter = new CommandContributionItemParameter (
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow (),
          null,
          "com.ibm.biginsights.textanalytics.aql.editor.command.childHierarchy", //$NON-NLS-1$
          Collections.emptyMap (), null, null, null, Messages.AQLEditor_OPEN_REFERENCE_HIERARCHY, null, null, SWT.PUSH,
          null, true);

        menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new CommandContributionItem (
          referenceHierarchyContributionParameter));
      }

      // Comment out. We no longer support running PD from AQL editor (defect 20349)
      // if (getOutputViewName(location, doc, region) != null) {
      // final CommandContributionItemParameter pdContributionParameter = new CommandContributionItemParameter(
      // PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
      // null,
      // Messages.AQLEditor_PATTERN_DISCOVERY_WIZARD_COMMAND,
      // Collections.emptyMap(), patternDiscovery,
      // patternDiscovery, patternDiscovery,
      // Messages.AQLEditor_PATTERN_DISCOVERY, null, null,
      // SWT.PUSH, null, true);
      //
      // menu.appendToGroup(Messages.AQLEditor_ADDITIONS,
      // new CommandContributionItem(pdContributionParameter));
      // }
      // not adding condition for this, as it will be handler within navigation handler
      if (CallHierarchyUtil.isAQLElement (candidateToken)) {
        final CommandContributionItemParameter declarationContributionParameter = new CommandContributionItemParameter (
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow (), null, Messages.AQLEditor_OPEN_DECLARATION_COMMAND,
          Collections.emptyMap (), null, null, regexGenerator, Messages.AQLEditor_OPEN_DECLARATION, null, null, SWT.PUSH,
          null, true);

        menu.appendToGroup (IWorkbenchActionConstants.MB_ADDITIONS, new CommandContributionItem (
          declarationContributionParameter));
      }

    }
    catch (Exception e) {
      e.printStackTrace ();
    }
  }

  // This method is to return the token in the selected region on the document.
  /**
   * Will find a token in document at the location where cursor has been placed
   * 
   * @param doc
   * @param region
   * @return
   */
  public String getViewCandidateToken (IDocument doc, ITypedRegion region)
  {
    try {
      int startLine = -1;

      int caretOffset = getCaretOffset ();

      startLine = doc.getLineOfOffset (caretOffset);

      StyledText s = getSourceViewer ().getTextWidget ();
      String sentence = doc.get (doc.getLineOffset (startLine), doc.getLineLength (startLine));
      int cursorLocation = s.getCaretOffset () - doc.getLineOffset (startLine);

      String token = ""; //$NON-NLS-1$
      if (region.getType () == AQLPartitionScanner.AQL_STRING) {
        token = CallHierarchyUtil.getCurrentTokenBetweenQuotes (sentence, cursorLocation);
      }
      else {
        token = CallHierarchyUtil.getCurrentToken (sentence, cursorLocation);
      }

      return token;
    }
    catch (BadLocationException e) {
      // TODO Auto-generated catch block
      // Invalid location. Returning empty string.
      return "";
    }

  }

  public static String getOutputViewName (int clickOffset, IDocument doc, ITypedRegion region)
  {
    String aqlSnippet = "";

    int regionOffset = region.getOffset ();
    int regionLength = region.getLength ();

    boolean moveFront = true;
    boolean moveBack = true;

    int start = (clickOffset - regionOffset > 1) ? (clickOffset - 1) : regionOffset;

    int length = clickOffset - start;

    String ret = null;

    do {
      try {
        aqlSnippet = doc.get (start, length);

        Matcher match = OUTPUT_VIEW_STMT_PATTERN.matcher (aqlSnippet);
        if (match.find ()) {
          ret = match.group (1);
          int first = match.start ();
          int last = match.end ();
          if (clickOffset > 0) if (clickOffset <= (start + first + 1) || clickOffset >= (start + last)) {
            ret = null;
            return ret;
          }
        }
      }
      catch (BadLocationException e) {
        // do nothing, we should expect this if we happen to fall in a
        // space that is not a valid text location
      }
      finally {

        if (ret != null) return ret;

        // if already found a string that begins with output view the we
        // only keep looking for the right part of it
        if (moveFront) {
          Matcher matchFront = OUTPUT_VIEW_PATTERN.matcher (aqlSnippet);
          if (matchFront.find ()) {

            int first = matchFront.start ();

            if (clickOffset > (start + first)) {
              moveFront = false;
            }
          }
        }

        // the same step as above but in this case for the right part of
        // the statement
        if (moveBack) {
          Matcher matchBack = VIEW_NAME_PATTERN.matcher (aqlSnippet);
          if (matchBack.find ()) {
            int last = matchBack.end ();

            if (clickOffset < (start + last)) {
              moveBack = false;
            }
          }
        }

        // if we just look in the entire region and couldn't find any
        // output view, then we just return null
        if (start == regionOffset && length == regionLength) return null;

        // otherwise,
        // if we cannot find the output view in the current sample text,
        // lets try by expanding the text that we are searching
        // since usually the user is expected to click in the name of
        // the output view, we should have more characters in front than
        // after the current offset.

        // set new start
        int oldStart = start;
        if (moveFront) {
          start = ((start - regionOffset) > 1) ? (start - 1) : regionOffset;
        }

        // set new length. we make sure that we don't use any character
        // out off the original range

        length = ((length + oldStart - start + 1) < (regionLength - start + regionOffset)) ? (length + oldStart - start + 1)
          : (regionLength - start + regionOffset);

      }
    }
    while ((start >= region.getOffset () && length <= region.getLength ())
      || (start >= region.getOffset () && !moveBack) || (!moveFront && length <= region.getLength ()));

    return null;
  }

  protected void selectionChanged (SelectionChangedEvent event)
  {

    if (outlinePage != null) {
      // update();
    }
  }

  public IAQLOutlineView createOutlinePage ()
  {
    return new OutlineView (this);
  }

  @SuppressWarnings("rawtypes")
  public Object getAdapter (Class key)
  {

    if (IContentOutlinePage.class.equals (key)) { return outlinePage; }
    return super.getAdapter (key);

  }

  public void update ()
  {

    outlinePage.update ();
    // outlinePage.setSelection(getViewer().getTextWidget().getCaretOffset());
  }

  /*
   * parse the file and mark errors when the file is saved, this method is called before the project builds
   */

  @Override
  public void doSave (IProgressMonitor progressMonitor)
  {

    super.doSave (progressMonitor);
    IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
    IEditorPart editorPart = page.getActiveEditor ();
    ITextEditor editor1 = (ITextEditor) editorPart;
    StyledText s = (StyledText) editor1.getAdapter (Control.class);

    IEditorInput genericInput = this.getEditorInput ();
    if (!(genericInput instanceof IFileEditorInput)) {
      return;
    }

    IFileEditorInput input = (IFileEditorInput) genericInput;
    IFile aqlFile = input.getFile ();
    IPath aqlFilePath = aqlFile.getLocation ();
    String aqlLoc = aqlFilePath.toOSString ();

    IProject project = aqlFile.getProject ();
    boolean isInSearchPath = ProjectPreferencesUtil.isAQLInSearchPath (project, aqlLoc);

    // parse only the files that have .aql extension. refer 17886
    if (StringUtils.isEmpty (aqlFilePath.getFileExtension ())
      || !(aqlFilePath.getFileExtension ().equals (Constants.AQL_FILE_EXTENSION_STRING)) || !isInSearchPath) return;

    // defect 27058: in 2.0, parse only valid aql files
    if (ProjectUtils.isModularProject (project)) {
      if (!ProjectUtils.isValidAQLFile20 (aqlFile)) return;
    }

    AQLParser parser = new AQLParser (s.getText (), aqlFilePath.toOSString ());
    boolean isModularProject = ProjectUtils.isModularProject (aqlFile.getProject ());
    parser.setBackwardCompatibilityMode (!isModularProject);
    parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
    StatementList statementList = parser.parse ();
    // handle errors while reconciling
    AQLParseErrorHandler reporter = new AQLParseErrorHandler ();
    LinkedList<ParseException> parseException = statementList.getParseErrors ();
    Iterator<ParseException> itr = parseException.iterator ();
    reporter.deleteMarkers (aqlFile);
    while (itr.hasNext ()) {
      try {
        ParseException pe1 = itr.next ();
        reporter.handleError (pe1, IMarker.SEVERITY_ERROR, aqlFile);
      }
      catch (Exception e) {
        e.printStackTrace ();
      }
    }
  }

  public void setEditorTitleImage (Image titleImage)
  {
    setTitleImage (titleImage);
  }

  /**
   * activates content assist
   */
  public void activateContentAssist ()
  {
    // activating content assist
    AQLViewer viewer = (AQLViewer) getSourceViewer ();
    viewer.doOperation (ISourceViewer.CONTENTASSIST_PROPOSALS);
  }

  protected void createActions ()
  {

    super.createActions ();

    IAction a = new TextOperationAction (Messages.getResourceBundle (),
      "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
    a.setActionDefinitionId (ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    setAction ("ContentAssistProposal", a);

  }
}
