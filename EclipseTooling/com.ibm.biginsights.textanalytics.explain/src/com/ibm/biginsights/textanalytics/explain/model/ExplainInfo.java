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

package com.ibm.biginsights.textanalytics.explain.model;

import org.eclipse.swt.graphics.Image;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.biginsights.textanalytics.explain.Icons;

public class ExplainInfo implements ExplainElement
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  private String name = "";
  private String comment = "";
  private Image icon = null;
  private ExplainElement parentElement = null;

  public ExplainInfo (String name, String comment, Image icon)
  {
    this.name = name;
    this.comment = comment;
    this.icon = icon;
  }

  @Override
  public String getName ()
  {
    return name;
  }

  @Override
  public String getComment ()
  {
    return comment;
  }

  @Override
  public Image getIcon ()
  {
    return icon;
  }

  @Override
  public ExplainElement getParent ()
  {
    return parentElement;
  }

  public void setParent (ExplainElement parentElement)
  {
    this.parentElement = parentElement;
  }

  public static ExplainInfo createInfoObject (String name, String value)
  {
    String displayText = name + ": " + value;
    return new ExplainInfo (displayText , displayText, Icons.INFO_ITEM);
  }

  public static ExplainInfo createInfoObject (Pair<String, String> info)
  {
    return createInfoObject (info.first, info.second);
  }

  @Override
  public ExplainElement[] getChildren ()
  {
    return null;
  }

  @Override
  public boolean hasChildren ()
  {
    return false;
  }

  @Override
  public String getDisplayedName ()
  {
    return getName();
  }

  @Override
  public String getTooltip ()
  {
    return getComment ();
  }
}
