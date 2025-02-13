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

import com.ibm.avatar.api.tam.TableMetadata;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class ExplainTable implements ExplainElement
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

  TableMetadata tableMD = null;
  ExplainElement parentElement = null;

  public ExplainTable (TableMetadata tableMD)
  {
    this.tableMD = tableMD;
  }

  @Override
  public String getName ()
  {
    if (tableMD != null)
      return tableMD.getTableName ();
    else
      return "";
  }

  @Override
  public String getComment ()
  {
    if (tableMD != null && tableMD.getComment () != null)
      return tableMD.getComment ();
    else
      return "";
  }

  @Override
  public Image getIcon ()
  {
    Image image = Icons.TABLE_ICON;

    if (isExported ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXPORT_OVL_IMGDESC, IDecoration.TOP_RIGHT);
      image = ovlImage.createImage ();
    }

    if (isExternal ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXPORT_OVL_IMGDESC, IDecoration.BOTTOM_LEFT);
      image = ovlImage.createImage ();
    }

    if (!isRequired ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.NOTREQUIRED_OVL_IMGDESC, IDecoration.BOTTOM_RIGHT);
      image = ovlImage.createImage ();
    }

    return image;
  }

  public boolean isRequired ()
  {
    if (isExternal ())
      // Calling isRequired() for an internal table will result in the exception:
      // "UnsupportedOperationException: This method should never be called for internal tables"
      return ProjectUtils.isTableRequired (tableMD);
    else
      return true;
  }

  public boolean isExternal ()
  {
    return tableMD.isExternal ();
  }

  public boolean isExported ()
  {
    return tableMD.isExported ();
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

    children.add (ExplainFolder.createSchemaNode (this, tableMD.getTableSchema ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISREQUIRED, "" + isRequired ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXPORTTABLE, "" + isExported ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXTERNALTABLE, "" + isExternal ()));

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
    return getName ();
  }

  @Override
  public String getTooltip ()
  {
    return getComment ();
  }
}
