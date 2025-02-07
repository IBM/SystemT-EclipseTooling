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

package com.ibm.biginsights.textanalytics.explain.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.biginsights.textanalytics.explain.model.ExplainElement;
import com.ibm.biginsights.textanalytics.explain.model.ExplainModule;

public class ExplainModuleContentProvider implements ITreeContentProvider
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  @Override
  public void dispose ()
  { 
  }

  @Override
  public void inputChanged (Viewer viewer, Object oldInput, Object newInput)
  {
  }

  @Override
  public Object[] getElements (Object inputElement)
  {
    if (inputElement instanceof String &&
        StringUtils.isNullOrWhiteSpace ((String)inputElement) == false)
      return new Object[] { new ExplainModule ((String)inputElement) };
    else
      return getChildren (inputElement);
  }

  @Override
  public Object[] getChildren (Object parentElement)
  {
    if (parentElement instanceof ExplainElement)
      return ((ExplainElement)parentElement).getChildren ();
    else
      return new Object[]{};
  }

  @Override
  public Object getParent (Object element)
  {
    if (element instanceof ExplainElement)
      return ((ExplainElement)element).getParent ();
    else
      return null;
  }

  @Override
  public boolean hasChildren (Object element)
  {
    if (element instanceof ExplainElement)
      return ((ExplainElement)element).hasChildren ();
    else
      return false;
  }


}
