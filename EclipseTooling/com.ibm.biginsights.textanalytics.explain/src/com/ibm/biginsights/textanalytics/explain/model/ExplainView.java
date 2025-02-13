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

import com.ibm.avatar.api.tam.ViewMetadata;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.icu.text.DecimalFormat;

public class ExplainView implements ExplainElement
{

  @SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  ViewMetadata viewMD = null;
  String qualifiedName;
  ExplainElement parentElement = null;
  DecimalFormat df = new DecimalFormat("#0.00");

  public ExplainView (ViewMetadata viewMD, String qualifiedName)
  {
    this.viewMD = viewMD;
    this.qualifiedName = qualifiedName;
  }

  @Override
  public String getName ()
  {
    if (viewMD != null)
      return viewMD.getViewName ();
    else
      return "";
  }

  @Override
  public String getComment ()
  {
    if (viewMD != null && viewMD.getComment () != null)
      return viewMD.getComment ();
    else
      return "";
  }

  @Override
  public Image getIcon ()
  {
    Image image = Icons.VIEW_ICON;

    if (isExported ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXPORT_OVL_IMGDESC, IDecoration.TOP_RIGHT);
      image = ovlImage.createImage ();
    }

    if (isExternal ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXTERNAL_OVL_IMGDESC, IDecoration.BOTTOM_LEFT);
      image = ovlImage.createImage ();
    }

    if (isOutputView ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.OUTPUT_OVL_IMGDESC, IDecoration.BOTTOM_RIGHT);
      image = ovlImage.createImage ();
    }

    return image;
  }

  public String getExternalName ()
  {
    return viewMD.getExternalName ();
  }

  public boolean isExternal ()
  {
    return viewMD.isExternal ();
  }

  public boolean isExported ()
  {
    return viewMD.isExported ();
  }

  public boolean isOutputView ()
  {
    return viewMD.isOutputView ();
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

    children.add (ExplainFolder.createSchemaNode (this, viewMD.getViewSchema ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_EXTERNALNAME, (viewMD.getExternalName () == null) ? "" : viewMD.getExternalName ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_OUTPUT_ALIAS, "" + ((viewMD.getOutputAlias () != null) ? viewMD.getOutputAlias () : qualifiedName)));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_COSTRECORD, "" + df.format (viewMD.getCostRecord ().cost ())));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXTERNALVIEW, "" + viewMD.isExternal ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXPORTVIEW, "" + viewMD.isExported ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISOUTPUTVIEW, "" + viewMD.isOutputView ()));

    Collections.sort (children, ExplainModule.elemComp);

    return children.toArray (new ExplainElement[0]);
  }

  @Override
  public boolean hasChildren ()
  {
    return true;
  }

  @Override
  public String getDisplayedName ()
  {
    return viewMD.getViewName ();
  }

  @Override
  public String getTooltip ()
  {
    return getComment ();
  }
}
