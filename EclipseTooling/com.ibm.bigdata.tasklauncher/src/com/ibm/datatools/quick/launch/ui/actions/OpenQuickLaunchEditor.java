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
package com.ibm.datatools.quick.launch.ui.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroPart;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.internal.core.QuickLaunchSolutionManager;
import com.ibm.datatools.quick.launch.ui.internal.core.QuickLaunchSolutionManager.Solution;
import com.ibm.datatools.quick.launch.ui.internal.pref.QuickLaunchPreferences;

/** Action the Quick Launch editor. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class OpenQuickLaunchEditor extends AbstractCommandHandlerAction implements IStartup, IWorkbenchWindowActionDelegate
{



	public static final String QUICK_LAUNCH_EDITOR_COMMAND_ID = "com.ibm.bigdata.tasklauncher.commands.quickLaunchEditor"; //$NON-NLS-1$
    
    @Override
    protected void execute( IHandlerService handlerService ) throws ExecutionException,
            NotDefinedException, NotEnabledException, NotHandledException
    {
        handlerService.executeCommand( QUICK_LAUNCH_EDITOR_COMMAND_ID, null );
    }


    
    public void earlyStartup()
    {
    	Preferences preferences = Activator.getDefault().getPluginPreferences();
    	boolean showTaskLauncher = preferences.getBoolean( QuickLaunchPreferences.SHOW_TASKLAUNCHER );
    	if (showTaskLauncher) {
	        PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable()
	        {
	            public void run()
	            {
	                IProduct product = Platform.getProduct();
	                if ( product != null )
	                {
	                    Solution solutionForProductId = getDefaultSolutionForProductId( product.getId() );
	                    //Thomas: show task launcher independent of productId
	                    // BI plugins will always be installed after another product was installed, so it's not very likely we
	                    // interfere with the welcome experience of the other product
	                    if (solutionForProductId==null)
	                    	solutionForProductId = QuickLaunchSolutionManager.getDefaultSolution();
	
	                    if ( solutionForProductId != null )
	                    {
	
	                        QuickLaunchSolutionManager
	                        .setSolutionId( solutionForProductId.getId() );
	                        
	                        //Thomas: always show task launcher unless user explicitly sets the flag not to open it anymore
	//                        if ( isNoEditorOpen() )
	//                        {
	                            OpenQuickLaunchEditor.this.run();
	                            if ( QuickLaunchSolutionManager.isShowQuickLaunchEditor() )
	                            {
	                                solutionForProductId.onOpenActions();
	                            }
	//                        }
	                        closeIntro();
	
	                    }
	                }
	            }
	            private void closeIntro()
	            {
	                IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
	                if ( intro != null )
	                {
	                    PlatformUI.getWorkbench().getIntroManager().closeIntro( intro );
	                }
	                
	            }
	            private boolean isNoEditorOpen()
	            {
	                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                IWorkbenchPage page = null;
	                if ( window != null && (page = window.getActivePage()) != null )
	                {
	                    if(page.getEditorReferences()!=null && page.getEditorReferences().length==0)
	                    {
	                        return true;
	                    }
	                }
	                return false;
	            }
	            private Solution getDefaultSolutionForProductId(String p_productId)
	            {
	                for(Solution solution : QuickLaunchSolutionManager.getAllSolutions())
	                {
	                    if(solution.isSolutionForProductId( p_productId ))
	                    {
	                        return solution;
	                    }
	                }
	                return null;
	            }
	        } );
    	}        

    }

	public void dispose() {
		// NOOP
		
	}

	
	public void init(IWorkbenchWindow window) {
		// NOOP
		
	}

	
	public void run(IAction action) {
		super.run();		
	}

	
	public void selectionChanged(IAction action, ISelection selection) {
		// NOOP
		
	}



}
