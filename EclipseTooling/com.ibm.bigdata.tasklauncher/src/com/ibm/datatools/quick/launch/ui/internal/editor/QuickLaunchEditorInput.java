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
package com.ibm.datatools.quick.launch.ui.internal.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import com.ibm.datatools.quick.launch.ui.QuickLaunchConstants;
import com.ibm.datatools.quick.launch.ui.i18n.IconManager;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;

/** Input to the Quick launch editor. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class QuickLaunchEditorInput implements IEditorInput
{



    String m_solutionId;

    public QuickLaunchEditorInput( String p_solutionId )
    {
        m_solutionId = p_solutionId;
    }

    public String getSolutionId()
    {
        return m_solutionId;
    }

    
    public boolean exists()
    {
        return true;
    }

    
    public ImageDescriptor getImageDescriptor()
    {
        return IconManager.getImageDescriptor( IconManager.CONNECT_TO );
    }

    
    public String getName()
    {
        return IAManager.QuickLaunchEditorInput_QUICK_LAUNCH_INPUT;
    }

    
    public IPersistableElement getPersistable()
    {
        return new IPersistableElement()
        {

            
            public String getFactoryId()
            {
                return QuickLaunchConstants.QUICK_LAUNCH_FACTORY_ID;
            }

            
            public void saveState( IMemento memento )
            {
                memento.putString( QuickLaunchInputFactory.SOLUTION_ID, m_solutionId );
            }

        };
    }

    
    public String getToolTipText()
    {
        return IAManager.QuickLaunchEditorInput_QUICK_LAUNCH_TOOLTIP;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter )
    {
        return null;
    }

    public boolean equals( Object o )
    {
        return o instanceof QuickLaunchEditorInput;
    }

}
