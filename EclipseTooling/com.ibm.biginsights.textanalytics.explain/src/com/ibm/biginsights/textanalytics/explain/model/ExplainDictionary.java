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

import com.ibm.avatar.algebra.util.dict.DictParams.CaseSensitivityType;
import com.ibm.avatar.api.tam.DictionaryMetadata;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class ExplainDictionary implements ExplainElement
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  DictionaryMetadata dictionaryMD = null;
  ExplainElement parentElement = null;

  public ExplainDictionary (DictionaryMetadata dictionaryMD)
  {
    this.dictionaryMD = dictionaryMD;
  }

  @Override
  public String getName ()
  {
    if (dictionaryMD != null)
      return dictionaryMD.getDictName ();
    else
      return "";
  }

  @Override
  public String getComment ()
  {
    if (dictionaryMD != null && dictionaryMD.getComment () != null)
      return dictionaryMD.getComment ();
    else
      return "";
  }

  public boolean isExternal ()
  {
    return dictionaryMD.isExternal ();
  }

  public boolean isExported ()
  {
    return dictionaryMD.isExported ();
  }

  public boolean isRequired ()
  {
    if (isExternal ())
      // Calling this for an internal dictionary will result in the exception below:
      // UnsupportedOperationException: This method should never be called for internal dictionaries
      return ProjectUtils.isDictRequired (dictionaryMD);
    else
      return true;

  }

  @Override
  public Image getIcon ()
  {
    Image image = Icons.DICTIONARY_ICON;

    if (isExported ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXPORT_OVL_IMGDESC, IDecoration.TOP_RIGHT);
      image = ovlImage.createImage ();
    }

    if (isExternal ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.EXTERNAL_OVL_IMGDESC, IDecoration.BOTTOM_LEFT);
      image = ovlImage.createImage ();
    }

    if (!isRequired ()) {
      DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (image, Icons.NOTREQUIRED_OVL_IMGDESC, IDecoration.BOTTOM_RIGHT);
      image = ovlImage.createImage ();
    }

    return image;
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

    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_LANGUAGES, getLanguages ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_CASESENS, getCaseType ().toString ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISREQUIRED, "" + isRequired ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXPORTDICT, "" + isExported ()));
    children.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_ISEXTERNALDICT, "" + isExternal ()));

    Collections.sort (children, ExplainModule.elemComp);

    return children.toArray (new ExplainElement[0]);
  }

  @Override
  public boolean hasChildren ()
  {
    return true;
  }

  public DictionaryMetadata getMetadata ()
  {
    return dictionaryMD;
  }

  @Override
  public String getDisplayedName ()
  {
    return getName ();
  }

  @Override
  public String getTooltip ()
  {
    return getComment();
  }

  private CaseSensitivityType getCaseType ()
  {
    return dictionaryMD.getCaseType ();
  }

  private String getLanguages ()
  {
    return dictionaryMD.getLanguages ();
  }
}
