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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import com.ibm.avatar.api.tam.FunctionMetadata;
import com.ibm.avatar.api.tam.Param;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;

public class ExplainFunction implements ExplainElement
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  FunctionMetadata functionMD = null;
  ExplainElement parentElement = null;

  public ExplainFunction (FunctionMetadata functionMD)
  {
    this.functionMD = functionMD;
  }

  @Override
  public String getName ()
  {
    if (functionMD != null)
      return functionMD.getFunctionName ();
    else
      return "";
  }

  @Override
  public String getComment ()
  {
    if (functionMD != null && functionMD.getComment () != null)
      return functionMD.getComment ();
    else
      return "";
  }

  public String getExternalName ()
  {
    return (functionMD != null && functionMD.getExternalName () != null) ? functionMD.getExternalName () : "";
  }

  public String getReturnType ()
  {
    return (functionMD != null && functionMD.getReturnType () != null) ? functionMD.getReturnType () : "";
  }

  @Override
  public Image getIcon ()
  {
    Image image = Icons.FUNCTION_ICON;

    if (isExported ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXPORT_OVL_IMGDESC, IDecoration.TOP_RIGHT);
      image = ovlImage.createImage ();
    }

    if (isDeterministic ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.DETERMINISTIC_OVL_IMGDESC, IDecoration.BOTTOM_RIGHT);
      image = ovlImage.createImage ();
    }

    return image;
  }

  public boolean isDeterministic ()
  {
    return functionMD.isDeterministic ();
  }

  public boolean isExported ()
  {
    return functionMD.isExported ();
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

  @Override
  public ExplainElement[] getChildren ()
  {
    List<ExplainElement> children = new ArrayList<ExplainElement> ();

    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_EXTERNALNAME, functionMD.getExternalName ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_LANGUAGE, functionMD.getLanguage ().toString ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_PARAMS, getParamString (functionMD.getParameters ())));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_RETURNTYPE, functionMD.getReturnType ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_RETURNLIKECOL, (functionMD.getReturnLikeParam () != null) ? functionMD.getReturnLikeParam () : ""));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISDETERMINISTIC, "" + functionMD.isDeterministic ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXPORTFUNC, "" + functionMD.isExported ()));

    Collections.sort (children, ExplainModule.elemComp);

    return children.toArray (new ExplainElement[0]);
  }

  @Override
  public boolean hasChildren ()
  {
    return true;
  }

  private String getParamString (Param[] params)
  {
    String paramStr = "";
    if (params != null) {
      for (Param p : params) {
        paramStr += ", " + p.getName () + " " + p.getType ();   //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (paramStr.length () > 0)
        paramStr = paramStr.substring (2);    // Remove the beginning ", "
    }

    return paramStr;
  }

  @Override
  public String getDisplayedName ()
  {
    return getName ();
  }

  @Override
  public String getTooltip ()
  {
    return getComment ();
  }
}
