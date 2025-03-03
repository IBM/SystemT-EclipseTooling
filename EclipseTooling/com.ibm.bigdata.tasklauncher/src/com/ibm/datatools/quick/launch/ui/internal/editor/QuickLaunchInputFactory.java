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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/** Factory used to restore a quick launch editor. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class QuickLaunchInputFactory implements IElementFactory
{



	public final static String SOLUTION_ID = "solutionId"; //$NON-NLS-1$

   
    public IAdaptable createElement( IMemento memento )
    {
        String solutionId = memento.getString( SOLUTION_ID );
        return new QuickLaunchEditorInput(solutionId);
    }

}
