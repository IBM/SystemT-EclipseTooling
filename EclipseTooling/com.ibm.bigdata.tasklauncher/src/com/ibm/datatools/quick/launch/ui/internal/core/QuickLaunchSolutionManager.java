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
package com.ibm.datatools.quick.launch.ui.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.forms.editor.IFormPage;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.QuickLaunchConstants;
import com.ibm.datatools.quick.launch.ui.actions.CheckboxMessageDialog;
import com.ibm.datatools.quick.launch.ui.actions.CheckboxMessageDialog.CheckBoxDefinition;
import com.ibm.datatools.quick.launch.ui.actions.GenericCommandAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenBrowserCommandAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenHelpAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenHelpTaskAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenImportWizardCommandAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenNewWizardCommandAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenPerspectiveAction;
import com.ibm.datatools.quick.launch.ui.actions.OpenWebPageAction;
import com.ibm.datatools.quick.launch.ui.actions.ShowSolutionCommandAction;
import com.ibm.datatools.quick.launch.ui.actions.ShowViewAction;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;
import com.ibm.datatools.quick.launch.ui.internal.editor.QuickLaunchEditor;
import com.ibm.datatools.quick.launch.ui.internal.editor.QuickLaunchEditorInput;
import com.ibm.datatools.quick.launch.ui.internal.pref.QuickLaunchPreferences;

/** Quick launch management methods. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class QuickLaunchSolutionManager
{


    public static final String SOLUTIONS_POINT_ID = "com.ibm.bigdata.tasklauncher.solution"; //$NON-NLS-1$

    public static final String SOLUTION_TAG = "solution"; //$NON-NLS-1$

    public static final String ID = "id"; //$NON-NLS-1$

    public static final String LABEL = "label"; //$NON-NLS-1$

    public static final String SHOW_IN_ATTR = "showIn"; //$NON-NLS-1$

    public static final String ON_OPEN_MACRO_ID_ATTR = "onOpenMacroId"; //$NON-NLS-1$

    public static final String PARENT_SOLUTION_ID_ATTR = "parentSolutionId"; //$NON-NLS-1$

    public static final String GROUP_TAG = "group"; //$NON-NLS-1$

    public static final String MACRO_TAG = "macro"; //$NON-NLS-1$

    public static final String COLOR_ATTR = "color"; //$NON-NLS-1$

    public static final String V_SPACE_ATTR = "vspace"; //$NON-NLS-1$
    
    public static final String USECASES_TAG = "usecases"; //$NON-NLS-1$

    public static final String SOLUTION_ID_ATTR = "solutionId"; //$NON-NLS-1$

    public static final String GROUP_ID_ATTR = "groupId"; //$NON-NLS-1$

    public static final String ROWSPAN_ATTR = "rowspan"; //$NON-NLS-1$

    public static final String PERSPECTIVE_ID_ATTR = "perspectiveId"; //$NON-NLS-1$

    public static final String USECASE_TAG = "usecase"; //$NON-NLS-1$

    public static final String DESCRIPTION_ATTR = "description"; //$NON-NLS-1$

    public static final String IMAGE_ATTR = "image"; //$NON-NLS-1$

    public static final String RANK_ATTR = "rank"; //$NON-NLS-1$
    
    public static final String ACTION_TAG = "action"; //$NON-NLS-1$

    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    public static final String SHOW_VIEW_TAG = "showViewAction"; //$NON-NLS-1$

    public static final String VIEW_ID_ATTR = "viewId"; //$NON-NLS-1$

    public static final String STATE_ATTR = "state"; //$NON-NLS-1$

    public static final String SHOW_CHEAT_SHEET_TAG = "showCheatSheetAction"; //$NON-NLS-1$

    public static final String CHEAT_SHEET_ID_ATTR = "cheatSheetId"; //$NON-NLS-1$

    public static final String SHOW_HELP_TAG = "showHelpAction"; //$NON-NLS-1$
    
    public static final String SHOW_HELP_TASK_TAG = "showHelpTaskAction"; //$NON-NLS-1$
    
    public static final String HELP_URL_ATTR = "helpURL"; //$NON-NLS-1$
    
    public static final String HIDE_VIEW_TAG = "hideViewAction"; //$NON-NLS-1$
    
    public static final String SHOW_SOLUTION_TAG = "showSolutionAction"; //$NON-NLS-1$
    
    public static final String COMMAND_ACTION_TAG = "commandAction"; //$NON-NLS-1$
    
    public static final String COMMAND_ID_ATTR = "commandId"; //$NON-NLS-1$

    public static final String SHOW_NEW_WIZARD_TAG = "showNewWizardAction"; //$NON-NLS-1$
    
    public static final String SHOW_IMPORT_WIZARD_TAG = "showImportWizardAction"; //$NON-NLS-1$
    
    public static final String SWITCH_PERSPECTIVE_TAG = "switchPerspectiveAction"; //$NON-NLS-1$
    
    public static final String WIZARD_ID_ATTR = "wizardId"; //$NON-NLS-1$
    
    public static final String STARTUP_TAG = "startup"; //$NON-NLS-1$
    
    public static final String PRODUCT_ID_ATTR = "productId"; //$NON-NLS-1$
    
    public static final String SHOW_WEB_PAGE_TAG = "showWebPageAction"; //$NON-NLS-1$
    
    public static final String URL_ATTR = "url"; //$NON-NLS-1$
    
    public static final String IN_EDITOR_ATTR = "inEditor"; //$NON-NLS-1$
    
    public static final String QUICK_LAUNCH_GROUP = "quickLaunch"; //$NON-NLS-1$

    public static final String FIRST_STEPS_GROUP = "firstSteps"; //$NON-NLS-1$
    
    public static final String SHOW_IN_EDITOR_VALUE = "editor"; //$NON-NLS-1$

    public static final String SHOW_IN_VIEW_VALUE = "view"; //$NON-NLS-1$

    public static final String SHOW_IN_SAME_VALUE = "same"; //$NON-NLS-1$

    public static final String HIDE_VALUE = "hide"; //$NON-NLS-1$

    public static final int DEFAULT_RGB = 255; 

    public static final String DEFAULT_COLOR_KEY = "0xFFFFFF"; //$NON-NLS-1$
    
    public static final int SHOW_IN_EDITOR_CODE = 0; 
    
    public static final int SHOW_IN_VIEW_CODE = 1; 

    public static final int SHOW_IN_SAME_CODE = 2;

    public static final int HIDE_CODE = 3;

    public static Map<String, Solution> m_solutions;

    public static String m_editorSolutionId;

    public static String m_viewSolutionId;
    
    private static QuickLaunchEditorInput m_editorInput;


    private static IEditorInput getEditorInput() 
    {
    	
    	if(m_editorInput==null)
    	{
    		if(getEditorSolutionId()!=null)
    		{
    			m_editorInput = new QuickLaunchEditorInput(getEditorSolutionId());
    		}
    		else
    		{
    			m_editorInput = new QuickLaunchEditorInput(getCurrentPerspectiveId());	
    		}
    	}
    	return m_editorInput;
    }
    public static void setViewSolutionId( String id )
    {
        m_viewSolutionId = id;
    }

    public static void setEditorSolutionId( String id )
    {
        m_editorSolutionId = id;
    }

    public static void setSolutionId( String id )
    {
        setEditorSolutionId( id );
        setViewSolutionId( id );
    }

    public static String getEditorSolutionId()
    {
        if ( m_editorSolutionId == null || m_editorSolutionId.trim().length() == 0 )
        {
            return getBestSolutionId( getCurrentPerspectiveId() );
        }
        else
        {
            return getBestSolutionId( m_editorSolutionId );
        }
    }

    public static String getViewSolutionId()
    {
        if ( m_viewSolutionId == null || m_viewSolutionId.trim().length() == 0 )
        {
            return getBestSolutionId( getCurrentPerspectiveId() );
        }
        else
        {
            return getBestSolutionId( m_viewSolutionId );
        }
    }
    private static String getCurrentPerspectiveId()
    {
        IWorkbenchWindow active = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if(active!=null)
        {
            IWorkbenchPage page = active.getActivePage();
            if(page!=null)
            {
                String perspectiveId = 
                    page.getPerspective().getId();
                return perspectiveId;
            }
        }
        return null;
    }
    private static String getBestSolutionId(String p_requestedId)
    {
        Solution solution = getSolution( p_requestedId );
        if(solution==null )
        {
            solution = getFirstSolution( QuickLaunchSolutionManager.getTopSolutions() );
            if(solution==null)
            {
                solution = getFirstSolution( QuickLaunchSolutionManager.getAllSolutions() );
            }
        }
        return (solution!=null) ? solution.getId() : null;
    }
    private static QuickLaunchSolutionManager.Solution getFirstSolution(QuickLaunchSolutionManager.Solution[] solutions)
    {
        if(solutions!=null && solutions.length>0){
            return solutions[0];
        }
        return null;
    }
    public static Solution getEditorSolution()
    {
        if ( m_editorSolutionId != null )
        {
            Solution solution = getSolution( m_editorSolutionId );
            if ( solution != null && solution.isDefined() )
            {
                return solution;
            }
        }
        return null;
    }

    public static Solution getViewSolution()
    {
        Solution solution = getSolution( getViewSolutionId() );
        if ( solution != null && solution.isDefined() )
        {
            return solution;
        }
        return null;
    }
    public static Solution[] getAllSolutions()
    {
        return getSolutions(true, false);
    }
    
    public static Solution getDefaultSolution()
    {
    	Solution[] solutions = getSolutions(true, false);
    	if (solutions!=null && solutions.length>0) {
    		// Thomas: return the solution with the lowest rank
    		Solution lowestRank = null;
    		for (Solution sol:solutions) {
    			if (lowestRank==null || lowestRank.getRank()>sol.getRank())
    				lowestRank = sol;
    		}
    		return lowestRank;
    	}
    	return null;
    }

    private static Solution[] getSolutions( boolean p_isRequireDefined, boolean p_isTop )
    {
        initSolutions( false );
        ArrayList<Solution> solutions = new ArrayList<Solution>( m_solutions.size() );
        for ( Solution solution : m_solutions.values() )
        {
            if ( !p_isRequireDefined || solution.isDefined() )
            {
                if ( !p_isTop )
                {
                    solutions.add( solution );
                }
                else
                {
                    String parentId = solution.getParentSolutionId();
                    if ( parentId == null || parentId.trim().length() == 0 )
                    {
                        solutions.add( solution );
                    }
                }
            }
        }
        return solutions.toArray( new Solution[ solutions.size() ] );
    }
    public static Solution[] getTopSolutions()
    {
        Solution[] solutions = getSolutions(true, true);
        
        Arrays.sort( solutions, new Comparator<Solution>()
                {

                    
                    public int compare( Solution object1, Solution object2 )
                    {
                        return object1.getRank() - object2.getRank();
                    }

                } );

        
        return solutions;
    }

    public static void showUpdate() throws PartInitException
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( window != null )
        {
            IWorkbenchPage page = window.getActivePage();
            if ( page != null )
            {
                if ( QuickLaunchSolutionManager.getViewSolution() != null )
                {

                    IEditorPart editor = page.findEditor( getEditorInput() );
                    
                    if ( editor != null )
                    {
                        if(page.getActiveEditor()==editor) {
                            page.activate( editor );
                        }
                    }
                }               
            }
        }
                
    }
    
    public static void showSolutionTabChange() throws PartInitException
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( window != null )
        {
            IWorkbenchPage page = window.getActivePage();
            if ( page != null )
            {
                if ( QuickLaunchSolutionManager.getViewSolution() != null )
                {

                    IEditorPart editor = page.findEditor( getEditorInput() );                    
                    
                    if ( editor != null )
                    {
                        if(page.getActiveEditor()==editor) {
                            page.activate( editor );
                            if ( editor instanceof QuickLaunchEditor )
                            {
                            	IFormPage formPage = ((QuickLaunchEditor)editor)
                                        .setActivePage( QuickLaunchSolutionManager
                                                .getEditorSolutionId() );
                                // also force the repaint   
                            	if (formPage.getManagedForm()!=null)
                            		formPage.getManagedForm().reflow(true);
                            }
                        }
                    }                    
                }               
            }
        }
                
    }
    
    public static boolean isShowQuickLaunchEditor()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( window != null )
        {
            IWorkbenchPage page = window.getActivePage();
            if ( page != null )
            {
                if ( QuickLaunchSolutionManager.getViewSolution() != null )
                {

                    return page.findEditor( getEditorInput() ) != null;
                }
            }
        }
        return false;
    	
    }

    public static void showSolutionsEditor( final boolean show ) throws PartInitException
    {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( window != null )
        {
            IWorkbenchPage page = window.getActivePage();
            if ( page != null )
            {
                IEditorPart editor = page.findEditor( getEditorInput() );
                if ( show )
                {
                    if ( editor == null )
                    {
                        page.openEditor( getEditorInput(),
                                QuickLaunchConstants.QUICK_LAUNCH_EDITOR_ID ); //$NON-NLS-1$
//                        assurePerspectiveListener();
                    }
                    else
                    {
                        page.activate( editor );
                        if ( editor instanceof QuickLaunchEditor )
                        {
                            ((QuickLaunchEditor)editor)
                                    .setActivePage( QuickLaunchSolutionManager
                                            .getEditorSolutionId() );
                        }
                    }
                }
                else
                {
                    if ( editor != null )
                    {
                        page.closeEditor( editor, false );
                    }
                }
            }
        }

    }

    private static Color getColor( String key )
    {
        Color result = null;
        if ( key == null || key.length() != 8 || !"0x".equals( key.substring( 0, 2 ) ) ) //$NON-NLS-1$
        {
            key = DEFAULT_COLOR_KEY;
        }
        if ( (result = JFaceResources.getColorRegistry().get( key )) == null )
        {

            int red = parseIntHex( key.substring( 2, 4 ), DEFAULT_RGB );
            int green = parseIntHex( key.substring( 4, 6 ), DEFAULT_RGB );
            int blue = parseIntHex( key.substring( 6 ), DEFAULT_RGB );
            RGB color = new RGB( red, green, blue );

            if ( color != null )
            {
                JFaceResources.getColorRegistry().put( key, color );
                result = JFaceResources.getColorRegistry().get( key );
            }
        }
        return result;
    }

    private static int parseIntHex( String hex, int defaultValue )
    {
        try
        {
            return Integer.parseInt( hex, 16 );
        }
        catch ( NumberFormatException e )
        {
            return defaultValue;
        }
    }

    private static void initSolutions(boolean reload) {

    	assurePerspectiveListener();
    	assurePageListener();

        if(m_solutions!=null && !reload ) return;
        
        
        m_solutions = new HashMap<String, Solution>();
        try
        {
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
                    SOLUTIONS_POINT_ID );
            IExtension[] extensions = extensionPoint.getExtensions();
            
            for ( int i = 0; i < extensions.length; ++i )
            {
                IExtension anExtension = extensions[ i ];
                IConfigurationElement[] elements = anExtension.getConfigurationElements();
                for ( int j = 0; j < elements.length; ++j )
                {
                    IConfigurationElement anElement = elements[ j ];
                    if ( SOLUTION_TAG.equals( anElement.getName() ) )
                    {
                        String solutionId = anElement.getAttribute( ID );
                        String label = anElement.getAttribute( LABEL );
                        String image = anElement.getAttribute( IMAGE_ATTR );
                        int show = processShowIn( anElement.getAttribute( SHOW_IN_ATTR ) );
                        String onOpenMacroId = anElement.getAttribute( ON_OPEN_MACRO_ID_ATTR );
                        String parentSolutionId = anElement.getAttribute( PARENT_SOLUTION_ID_ATTR );
                        int solutionRank = convertToInt( anElement
                                .getAttribute( RANK_ATTR ), 10000 );

                        Solution solution = m_solutions.get( solutionId );

                        if ( solution == null )
                        {
                            solution = new Solution( solutionId, label, image, show, onOpenMacroId, parentSolutionId, solutionRank );
                            m_solutions.put( solutionId, solution );
                        }
                        else if ( !solution.isDefined() )
                        {
                            solution.setDefined( true, label, image, show, solutionRank );
                        }
                        

                        IConfigurationElement[] groups = anElement.getChildren();

                        for ( IConfigurationElement groupElement : groups )
                        {

                            if ( GROUP_TAG.equals( groupElement.getName() ) )
                            {
                                String groupLabel = groupElement.getAttribute( LABEL );
                                String groupId = groupElement.getAttribute( ID );
                                String groupColor = groupElement.getAttribute( COLOR_ATTR );

                                int groupRank = convertToInt( groupElement
                                        .getAttribute( RANK_ATTR ), 10000 );
                                int groupRowspan = convertToInt( groupElement
                                        .getAttribute( ROWSPAN_ATTR ), 1 );
                                int groupVerticalSpacing = convertToInt( groupElement
                                        .getAttribute( V_SPACE_ATTR ), 15 );


                                Group group = new Group( groupId, groupLabel, groupColor,
                                        groupRank, groupRowspan, groupVerticalSpacing );
                                solution.add( group );
                            }
                            else if ( MACRO_TAG.equals( groupElement.getName() ) )
                            {
                                String macroId = groupElement.getAttribute( ID );
                                
                                ArrayList<IAction> actions = processActions(groupElement);
                                
                                solution.addMacro(macroId, actions);
                            }
                        }

                    }
                    else if ( USECASES_TAG.equals( anElement.getName() ) )
                    {
                        IConfigurationElement[] usecases = anElement.getChildren();
                        String solutionId = anElement.getAttribute( SOLUTION_ID_ATTR );
                        String groupId = anElement.getAttribute( GROUP_ID_ATTR );
                        String perspectiveId = anElement.getAttribute(  PERSPECTIVE_ID_ATTR );

                        Solution solution = m_solutions.get( solutionId );

                        if ( solution == null )
                        {
                            solution = new Solution( solutionId );
                            m_solutions.put( solutionId, solution );
                        }
                        
                        for ( IConfigurationElement usecaseElement : usecases )
                        {

                            if ( USECASE_TAG.equals( usecaseElement.getName() ) )
                            {
                                String label = usecaseElement.getAttribute( LABEL );
                                String description = usecaseElement
                                        .getAttribute( DESCRIPTION_ATTR );
                                String imagePath = usecaseElement.getAttribute( IMAGE_ATTR );
                                int usecaseRank = convertToInt( usecaseElement
                                        .getAttribute( RANK_ATTR ), 10000 );

                                ArrayList<IAction> actions = processActions(usecaseElement);

                                addPerspectiveSwitcher(actions, perspectiveId);
                                UseCase usecase = new UseCase( label, description, imagePath,
                                        actions, groupId, usecaseRank );
                                solution.add( usecase );
                            }
                        }
                    }
                    else if (STARTUP_TAG.equals( anElement.getName()))
                    {
                        String productId = anElement.getAttribute( PRODUCT_ID_ATTR );
                        String solutionId = anElement.getAttribute( SOLUTION_ID_ATTR );
                        
                        if(productId!=null && productId.trim().length()>0
                                && solutionId!=null && solutionId.trim().length()>0)
                        {
                            Solution solution = m_solutions.get( solutionId );

                            if ( solution == null )
                            {
                                solution = new Solution( solutionId );
                                m_solutions.put( solutionId, solution );
                            }
                            solution.bindProductId( productId);
                        }
                    }
                }
            }
        }
        catch ( InvalidRegistryObjectException e )
        {
            /*
             * If an InvalidRegistryObjectException is thrown, the plugin
             * defining the extenstion has been updated or unloaded. In that
             * case we'll log the exception and try again.
             */
            Activator.log( e );
            m_solutions = null;
        }
        // Rank solutions
        for(Solution solution : m_solutions.values())
        {
            solution.rankChildren();
        }
    }
    private static ArrayList<IAction> processActions(IConfigurationElement p_secaseElement)
    {
        ArrayList<IAction> actions = new ArrayList<IAction>();
        
        for ( IConfigurationElement usecaseProps : p_secaseElement
                .getChildren() )
        {
            if ( ACTION_TAG.equals( usecaseProps.getName() ) )
            {
                IAction action = null;
                try
                {
                    action = (IAction)usecaseProps
                            .createExecutableExtension( CLASS_ATTR );
                    if ( action !=null )
                    {
                        actions.add( action );
                    }
                }
                catch ( CoreException e )
                {
                    Activator.log( e );
                }
            }
            else if ( SHOW_VIEW_TAG.equals( usecaseProps.getName() ) )
            {
                String viewId = usecaseProps.getAttribute( VIEW_ID_ATTR );
                if(viewId!=null && viewId.trim().length()>0)
                {
                    String state = usecaseProps.getAttribute( STATE_ATTR );
                    if(state==null || state.trim().length()<=0)
                    {
                    	state = "show"; //$NON-NLS-1$
                    }
                    actions.add( new ShowViewAction( viewId, state ) );
                }
            }
            else if ( SHOW_CHEAT_SHEET_TAG.equals( usecaseProps.getName() ) )
            {
                String cheatSheetId = usecaseProps.getAttribute( CHEAT_SHEET_ID_ATTR );
                if(cheatSheetId!=null && cheatSheetId.trim().length()>0)
                {
                    actions.add( new OpenCheatSheetAction( cheatSheetId ) );
                }
            } //
            else if ( SHOW_HELP_TAG.equals( usecaseProps.getName() ) )
            {
                String helpURL = usecaseProps.getAttribute( HELP_URL_ATTR );
                if(helpURL!=null && helpURL.trim().length()>0)
                {
                    actions.add( new OpenHelpAction( helpURL ) );
                }
            }
            else if ( SHOW_HELP_TASK_TAG.equals( usecaseProps.getName() ) )
            {
                String helpURL = usecaseProps.getAttribute( HELP_URL_ATTR );
                if(helpURL!=null && helpURL.trim().length()>0)
                {
                    actions.add( new OpenHelpTaskAction( helpURL ) );
                }
            }
            else if ( HIDE_VIEW_TAG.equals( usecaseProps.getName() ) )
            {
                String viewId = usecaseProps.getAttribute( VIEW_ID_ATTR );
                if(viewId!=null && viewId.trim().length()>0)
                {
                    actions.add( new ShowViewAction( viewId, "hide" ) ); //$NON-NLS-1$
                }
            }
            else if ( SHOW_SOLUTION_TAG.equals( usecaseProps.getName() ) )
            {
                String solId = usecaseProps.getAttribute( SOLUTION_ID_ATTR );
                if(solId!=null && solId.trim().length()>0)
                {
                    actions.add( new ShowSolutionCommandAction( solId ) );
                }
            }
            else if ( SHOW_NEW_WIZARD_TAG.equals( usecaseProps.getName() ) )
            {
                String wizardlId = usecaseProps.getAttribute( WIZARD_ID_ATTR );
                if(wizardlId!=null && wizardlId.trim().length()>0)
                {
                    actions.add( new OpenNewWizardCommandAction( wizardlId ) );
                }
            }
            else if ( SHOW_IMPORT_WIZARD_TAG.equals( usecaseProps.getName() ) )
            {
                String wizardlId = usecaseProps.getAttribute( WIZARD_ID_ATTR );
                if(wizardlId!=null && wizardlId.trim().length()>0)
                {
                    actions.add( new OpenImportWizardCommandAction( wizardlId ) );
                }
            }
            else if ( SWITCH_PERSPECTIVE_TAG.equals( usecaseProps.getName() ) )
            {
                String perspectiveId = usecaseProps.getAttribute( PERSPECTIVE_ID_ATTR );
                if(perspectiveId!=null && perspectiveId.trim().length()>0)
                {
                    actions.add( new OpenPerspectiveAction( perspectiveId ) );
                }
            }            
            else if ( SHOW_WEB_PAGE_TAG.equals( usecaseProps.getName() ) )
            {
                String url = usecaseProps.getAttribute( URL_ATTR );
                String inEditor = usecaseProps.getAttribute( IN_EDITOR_ATTR );
                if ( url != null && url.trim().length() > 0 )
                {
                    if ( inEditor != null && "true".equalsIgnoreCase( inEditor.trim() ) ) //$NON-NLS-1$
                    {
                        actions.add( new OpenBrowserCommandAction( url ) );
                    }
                    else
                    {
                        actions.add( new OpenWebPageAction( url ) );
                    }
                }
            }
            else if ( COMMAND_ACTION_TAG.equals( usecaseProps.getName() ) )
            {
                IAction action = null;
                String id = usecaseProps.getAttribute( COMMAND_ID_ATTR );
                action = new GenericCommandAction( id );
                if ( action !=null )
                {
                    actions.add( action );
                }
            }
        }
        return actions;
    	
    }
    private static ArrayList<IAction> addPerspectiveSwitcher(ArrayList<IAction> actions, String perspectiveId)
    {
        if(perspectiveId!=null && perspectiveId.trim().length()>0)
        {
            IAction switcher = new OpenPerspectiveAction(perspectiveId);
            actions.add(0,switcher);
        }
        return actions;
    }
    private static int processShowIn(String show) 
    {
        if (show == null || show.trim().length() == 0
				|| SHOW_IN_SAME_VALUE.equalsIgnoreCase(show)) {
			return SHOW_IN_SAME_CODE;
		}
        else if (SHOW_IN_VIEW_VALUE.equalsIgnoreCase( show))
        {
            return SHOW_IN_VIEW_CODE;
        }
        else if (SHOW_IN_EDITOR_VALUE.equalsIgnoreCase( show))
        {
            return SHOW_IN_EDITOR_CODE;
        }
        else
        {
            return HIDE_CODE;
        }
    }
    private static Solution getSolution( String id )
    {
        if ( m_solutions == null && id!=null && id.trim().length()>0)
        {
            initSolutions(true);
        }
        return getSolutionInternal( id );
    }
   public static Solution getProductDefaultSolution( String productId )
    {
        if ( m_solutions == null && productId!=null && productId.trim().length()>0)
        {
            initSolutions(true);
        }
        return getSolutionInternal( productId );
    }

    private static int convertToInt( String num, int defaultValue )
    {
        int value = defaultValue;
        try
        {
            value = Integer.parseInt( num );
        }
        catch ( java.lang.NumberFormatException e )
        {
            // NOOP just default
        }
        return value;
    }

    private static Solution getSolutionInternal( String id )
    {
        if ( m_solutions != null )
        {
            Solution solution = m_solutions.get( id );
            if ( solution != null && solution.isDefined() )
            {
                return solution;
            }
        }
        return null;
    }

    public static class UseCase
    {
        String label;

        String description;

        String imagePath;

        IAction action;

        String group;

        int rank;

        public UseCase( String label, String description, String imagePath, ArrayList<IAction> actions,
                String group, int rank )
        {
            this.label = label;
            this.description = description;
            this.imagePath = imagePath;
            this.action = groupActions( actions );
            this.group = group;
            this.rank = rank;
        }

        private IAction groupActions(final ArrayList<IAction> actions)
        {
            IAction action = null;
            if(actions!=null)
            {
                if ( actions.size()==1)
                {
                    action = actions.get( 0 );
                }
                else if (actions.size() > 1)
                {
                    action = new Action() 
                    {

                       @Override
                       public void run()
                       {
                           for(IAction action:actions)
                           {
                               action.run();
                           }
                       }
                        
                    };
                }
            }
            return action;
        }
        public String getLabel()
        {
            return label;
        }

        public String getDescription()
        {
            return description;
        }

        public String getImagePath()
        {
            return imagePath;
        }

        public IAction getAction()
        {
            return action;
        }

        public String getGroup()
        {
            return group;
        }

        public int getRank()
        {
            return rank;
        }

    }

    public static class Group
    {
        String id;

        String label;

        int rank;

        int rowspan;

        String colorId;

        int vspace;

        public Group( String groupId, String groupLabel, String color, int groupRank,
                int groupRowspan, int verticalSpacing )
        {
            this.id = groupId;
            this.rank = groupRank;
            this.colorId = color;
            this.rowspan = groupRowspan;
            this.label = groupLabel;
            this.vspace = verticalSpacing;
        }

        public String getId()
        {
            return id;
        }

        public int getRank()
        {
            return rank;
        }

        public int getRowspan()
        {
            return rowspan;
        }

        public String getLabel()
        {
            return this.label;
        }

        public Color getColor()
        {
            return QuickLaunchSolutionManager.getColor( this.colorId );
        }
        
        public int getVerticalSpacing()
        {
            return this.vspace;
        }


    }

    public static class Solution
    {
        String m_label;

        String m_id;
        
        int m_show; 

        Map<String, ArrayList<UseCase>> m_usecases = new HashMap<String, ArrayList<UseCase>>();

        ArrayList<Group> m_groups = new ArrayList<Group>();

        Map<String,ArrayList<IAction>> m_macros = new HashMap<String,ArrayList<IAction>>();
        
        String m_imagePath;

        boolean m_isDefined;
        
        String m_macroIdOnOpen;

        String m_parentSolutionId;

        int m_rank;
        
        Set<String> m_boundProductIds = new HashSet<String>();

        public Solution( String id, String label, String imagePath, int show, String macroIdOnOpen, String parentSolutionId, int rank )
        {
            m_label = label;
            m_id = id;
            m_imagePath = imagePath;
            m_isDefined = true;
            m_show = show;
            m_macroIdOnOpen = macroIdOnOpen;
            m_parentSolutionId = parentSolutionId;
            m_rank = rank;
        }

        public Solution( String id )
        {
            m_isDefined = false;
            m_id = id;
            m_show = HIDE_CODE;
        }

        public boolean isDefined()
        {
            return m_isDefined;
        }
        public String getId()
        {
            return m_id;
        }
        public void setDefined( boolean isDefined, String label, String imagePath, int show, int solutionRank )
        {
            m_label = label;
            m_imagePath = imagePath;
            m_isDefined = isDefined && label != null;
            m_show = show;
            m_rank = solutionRank;
        }
        public int getShowIn()
        {
            return m_show;
        }
        public void setIsVisible(int show)
        {
            m_show = show;
        }
        public void addAll( ArrayList<UseCase> usecases )
        {
            for ( UseCase usecase : usecases )
            {
                add( usecase );
            }
        }

        public void add( UseCase usecase )
        {
            ArrayList<UseCase> savedUsecases = m_usecases.get( usecase.getGroup() );
            if ( savedUsecases == null )
            {
                savedUsecases = new ArrayList<UseCase>();
                m_usecases.put( usecase.getGroup(), savedUsecases );
            }
            savedUsecases.add( usecase );
        }

        public void add( Group group )
        {
            for ( Group g : m_groups )
            {
                if ( g.getId().equals( group.getId() ) )
                {
                    return;
                }
            }
            m_groups.add( group );
        }

        public void addMacro( String p_macroId, ArrayList<IAction> p_actions)
        {
            
            m_macros.put( p_macroId, p_actions );
        }
        
        public IAction getMacro(String p_macroId)
        {
        	if(m_macroIdOnOpen!=null && m_macroIdOnOpen.trim().length()>0)
        	{
                IAction action = null;
                final ArrayList<IAction> actions =  m_macros.get(p_macroId);
                if(actions!=null)
                {
                    if ( actions.size()==1)
                    {
                        action = actions.get( 0 );
                    }
                    else if (actions.size() > 1)
                    {
                        action = new Action() 
                        {

                           @Override
                           public void run()
                           {
                               for(IAction action:actions)
                               {
                                   action.run();
                               }
                           }
                            
                        };
                    }
                }
                return action;

        	}
        	else
        	{
        		return null;
        	}
        }
        public void bindProductId(String p_productId)
        {
            m_boundProductIds.add(p_productId);
        }
        public boolean isSolutionForProductId(String p_productId)
        {
            return m_boundProductIds.contains( p_productId );
        }
        public String getParentSolutionId() 
        {
            return m_parentSolutionId;
        }
        public int getRank()
        {
            return m_rank;
        }
        public String getLabel()
        {
            return m_label;
        }

        public String getImagePath()
        {
            return m_imagePath;
        }

        protected void rankChildren()
        {
            for ( ArrayList<UseCase> usecases : m_usecases.values() )
            {
                Collections.sort( usecases, new Comparator<UseCase>()
                {

                    
                    public int compare( UseCase object1, UseCase object2 )
                    {

                        return object1.getRank() - object2.getRank();
                    }

                } );
            
            }
//            for ( String group : m_usecases.keySet() )
//            {
//                ArrayList<UseCase> usecases = m_usecases.get(group);
//                Collections.sort( usecases, new Comparator<UseCase>()
//                {
//
//                    @Override
//                    public int compare( UseCase object1, UseCase object2 )
//                    {
//
//                        return object1.getRank() - object2.getRank();
//                    }
//
//                } );
//                m_usecases.put( group, usecases );
//            
//            }
            Collections.sort( m_groups, new Comparator<Group>()
            {

                
                public int compare( Group object1, Group object2 )
                {
                    return object1.getRank() - object2.getRank();
                }

            } );

        }

        public ArrayList<UseCase> getUseCases( String group )
        {
            return m_usecases.get( group );
        }

        public ArrayList<Group> getGroups()
        {
            return m_groups;
        }
        public void onOpenActions() 
        {
        	if(m_macroIdOnOpen!=null && m_macroIdOnOpen.trim().length()>0)
        	{
        		 IAction action = getMacro(m_macroIdOnOpen);
        		 if (action!=null)
        		 {
        			 try {
        				 action.run();
        			 }
        			 catch (NullPointerException e)
        			 {
        				 Activator.log(e);
        			 }
        		 }
        	}
        }
    }
    private static void assurePerspectiveListener()
    {
        IWorkbenchWindow active = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( active != null )
        {
	        if ( Activator.perspectiveListener == null )
	        {
                Activator.perspectiveListener = new PerspectiveAdapter()
                {

                    private void setQuickLaunch( String perspectiveId )
                    {

                        QuickLaunchSolutionManager.setSolutionId( perspectiveId );
                        try
                        {
                            QuickLaunchSolutionManager.Solution solution = QuickLaunchSolutionManager
                                    .getViewSolution();
                            if ( solution != null )
                            {
                                assurePageListener();
                                
                                switch ( solution.getShowIn() )
                                {
                                    case QuickLaunchSolutionManager.HIDE_CODE:
                                        show( false );
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_EDITOR_CODE:
                                        show( true );
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_VIEW_CODE:
                                        show( false );
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_SAME_CODE:
                                        QuickLaunchSolutionManager.showUpdate();
                                        break;

                                }
                            }
                        }
                        catch ( PartInitException e )
                        {
                            // NOOP
                        }
                    }

                    private void openQuickLaunch( String p_id )
                    {
                        QuickLaunchSolutionManager.setSolutionId( p_id );
                        try
                        {
                            QuickLaunchSolutionManager.Solution solution = QuickLaunchSolutionManager
                                    .getViewSolution();
                            if ( solution != null )
                            {
                                switch ( solution.getShowIn() )
                                {
                                    case QuickLaunchSolutionManager.HIDE_CODE:
                                        show( false );
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_EDITOR_CODE:
                                        show( true );
                                        solution.onOpenActions();
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_VIEW_CODE:
                                        show( false );
                                        break;
                                    case QuickLaunchSolutionManager.SHOW_IN_SAME_CODE:
                                        QuickLaunchSolutionManager.showUpdate();
                                        if ( QuickLaunchSolutionManager.isShowQuickLaunchEditor() )
                                        {
                                            Solution editorSolution = QuickLaunchSolutionManager
                                                    .getEditorSolution();
                                            if ( editorSolution != null )
                                            {
                                               	editorSolution.onOpenActions();                                                	
                                            }
                                        }
                                        break;

                                }
                            }
                        }
                        catch ( PartInitException e )
                        {
                            // NOOP
                        }
                    }

                    private void show( boolean editor ) throws PartInitException
                    {
                        QuickLaunchSolutionManager.showSolutionsEditor( editor );
                    }

                    @Override
                    public void perspectiveActivated( IWorkbenchPage page,
                            IPerspectiveDescriptor perspective )
                    {
                        setQuickLaunch( perspective.getId() );
                    }

                    @Override
                    public void perspectiveOpened( IWorkbenchPage page,
                            IPerspectiveDescriptor perspective )
                    {
                        super.perspectiveOpened( page, perspective );
                        openQuickLaunch( perspective.getId() );
                    }

                };
            }
            if(Activator.perspectiveListener!=null)
            {
                active.addPerspectiveListener( Activator.perspectiveListener );            	
            }            
        }
        
    }
    private static void assurePageListener()
    {
        IWorkbenchWindow active = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if ( active != null )
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage();
		    if ( page != null )
		    {
		        if ( Activator.partListener == null )
		        {

                    Activator.partListener = new IPartListener2()
                    {
                        private boolean isPartReferencingMe( IWorkbenchPartReference partRef )
                        {
                            return QuickLaunchConstants.QUICK_LAUNCH_EDITOR_ID.equals( partRef
                                    .getId() );
                        }

                        
                        public void partActivated( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partBroughtToTop( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partClosed( IWorkbenchPartReference partRef )
                        {
                            // Launch Dialog
                            if ( isPartReferencingMe( partRef ) )
                            {
                                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                                IWorkbenchPage page = null;
                                // Do not show the dialog for closing the task launcher when we close workbench.
                                boolean isWorkbenchClosing = PlatformUI.getWorkbench ().isClosing ();
                                if((!isWorkbenchClosing) &&( window != null && (page = window.getActivePage()) != null ))
                                {
                                    page.removePartListener( this );
                                    window.removePerspectiveListener( Activator.perspectiveListener );

                                    Preferences prefs = Activator.getDefault()
                                            .getPluginPreferences();

                                    boolean bPrompt = prefs
                                            .getBoolean( QuickLaunchPreferences.QUERY_ON_CLOSE );
                                    boolean bShowTaskLauncher = prefs.getBoolean(QuickLaunchPreferences.SHOW_TASKLAUNCHER);

                                    if ( !bPrompt )
                                    {                                        
                                        CheckboxMessageDialog switcher = new CheckboxMessageDialog(
                                                window.getShell(), MessageDialog.INFORMATION,                                                
                                                IAManager.QuickLaunchSolutionManager_CloseDialogTitle, 
                                                IAManager.QuickLaunchSolutionManager_CloseDialogMessage, 
                                                IAManager.QuickLaunchSolutionManager_CloseDialogMessage2,
                                                new String[]
                                                    {
                                                        IAManager.QuickLaunchSolutionManager_CloseDialogButton
                                                    }, 0, 
                                                    new CheckBoxDefinition(IAManager.QuickLaunchSolutionManager_CloseDialogPrompt, QuickLaunchPreferences.QUERY_ON_CLOSE, bPrompt),
                                                	new CheckBoxDefinition(IAManager.QuickLaunchSolutionManager_CloseDialogPrompt2, QuickLaunchPreferences.SHOW_TASKLAUNCHER, !bShowTaskLauncher));
                                        
                                        switcher.open();
                                        prefs.setValue( QuickLaunchPreferences.QUERY_ON_CLOSE, switcher.isCheckboxSelected(QuickLaunchPreferences.QUERY_ON_CLOSE) );
                                        prefs.setValue( QuickLaunchPreferences.SHOW_TASKLAUNCHER, !switcher.isCheckboxSelected(QuickLaunchPreferences.SHOW_TASKLAUNCHER) );
                                    }
                                }
                            }
                        }

                        
                        public void partDeactivated( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partHidden( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partInputChanged( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partOpened( IWorkbenchPartReference partRef )
                        {
                        }

                        
                        public void partVisible( IWorkbenchPartReference partRef )
                        {

                            if ( isPartReferencingMe( partRef ) )
                            {
                                updateContent( partRef );
                            }
                        }

                        private void updateContent( IWorkbenchPartReference partRef )
                        {
                            try
                            {
                                QuickLaunchSolutionManager.showUpdate();
                            }
                            catch ( PartInitException e )
                            {
                                Activator.log( e );
                            }
                        }
                    };
                }
	            if(Activator.partListener!=null)
	            {
	            	page.addPartListener( Activator.partListener );
	            }
            }
        }

    }



}
