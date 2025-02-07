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

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;

/** Action to open a default script editor. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class OpenSQLEditor extends Action
{



    @Override
    public void run()
    {
        super.run();
        try
        {
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                    ".sqlxeditor_project" ); //$NON-NLS-1$
            if ( !project.exists() )
            {
                project.create( new NullProgressMonitor() );
            }
            if ( !project.isOpen() )
            {
                project.open( new NullProgressMonitor() );
            }

            final IFile iFile = createNewScript( project, IAManager.OpenSQLEditor_SCRIPT_FILE_BASE, 1, IAManager.OpenSQLEditor_SCRIPT_EXT );

            //Thomas: commented out for now for August; don't want to add more datatools dependencies
//            final SQLXEditorFileEditorInput input = new SQLXEditorFileEditorInput( iFile );
//            Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
//                    .getActivePage().openEditor( input, QuickLaunchConstants.SQLXEDITOR2_ID ); //$NON-NLS-1$	

        }
        catch ( PartInitException e )
        {
        	Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
        catch ( CoreException e )
        {
        	Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
    }

    private IFile createNewScript( final IProject project, String base, int num, String extension )
            throws CoreException
    {
        for ( int i = num; i < 10000; ++i )
        {
            Path path = new Path( base + i + "." + extension ); //$NON-NLS-1$
            final IFile iFile = project.getFile( path );
            if ( !iFile.exists() )
            {
                iFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
                return iFile;
            }
        }
        return null;
    }



}
