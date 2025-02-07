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
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;

public class ExplainFolder implements ExplainElement
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  public static String FOLDER_NAME_VIEWS = Messages.ExplainModuleView_FOLDER_NAME_VIEWS;
  public static String FOLDER_COMMENT_VIEWS = Messages.ExplainModuleView_FOLDER_COMMENT_VIEWS;
  public static String FOLDER_NAME_DICTIONARIES = Messages.ExplainModuleView_FOLDER_NAME_DICTIONARIES;
  public static String FOLDER_COMMENT_DICTIONARIES = Messages.ExplainModuleView_FOLDER_COMMENT_DICTIONARIES;
  public static String FOLDER_NAME_TABLES = Messages.ExplainModuleView_FOLDER_NAME_TABLES;
  public static String FOLDER_COMMENT_TABLES = Messages.ExplainModuleView_FOLDER_COMMENT_TABLES;
  public static String FOLDER_NAME_FUNCTIONS = Messages.ExplainModuleView_FOLDER_NAME_REQ_FUNCTIONS;
  public static String FOLDER_COMMENT_REQ_FUNCTIONS = Messages.ExplainModuleView_FOLDER_COMMENT_REQ_FUNCTIONS;
  public static String FOLDER_NAME_REQ_MODULES = Messages.ExplainModuleView_FOLDER_NAME_REQ_MODULES;
  public static String FOLDER_COMMENT_REQ_MODULES = Messages.ExplainModuleView_FOLDER_COMMENT_REQ_MODULES;
  public static String FOLDER_MODULE_PROPERTIES = Messages.ExplainModuleView_MODULEPROPS;
  public static String FOLDER_COMMENT_MODULE_PROPERTIES = Messages.ExplainModuleView_COMMENT_MODULEPROPS;
  public static String SCHEMA_NODE_NAME = Messages.ExplainModuleView_SCHEMA;

  private String folderName;
  private String comment;
  private ExplainElement parent;
  private Object element;
  private Image uzIcon;

  private ExplainFolder (String folderName, String comment, ExplainElement parent, Image icon)
  {
    super ();
    this.folderName = folderName;
    this.comment = comment;
    this.parent = parent;
    this.uzIcon = icon;
  }

  private ExplainFolder (String folderName, String comment, ExplainElement parent)
  {
    this (folderName, comment, parent, null);
  }

  @Override
  public String getName ()
  {
    return folderName;
  }

  @Override
  public String getComment ()
  {
    return comment;
  }

  @Override
  public Image getIcon ()
  {
    if (uzIcon == null)
      return Icons.FOLDER_ICON;
    else
      return uzIcon;
  }

  @Override
  public ExplainElement getParent ()
  {
    return parent;
  }

  @Override
  public void setParent (ExplainElement parent)
  {
    this.parent = parent;
  }

  public void setElement (Object element)
  {
    this.element = element;
  }

  public static ExplainFolder getViewsFolder (ExplainModule parentModule)
  {
    return new ExplainFolder (FOLDER_NAME_VIEWS, FOLDER_COMMENT_VIEWS, parentModule);
  }

  public static ExplainFolder getDictionariesFolder (ExplainModule parentModule)
  {
    return new ExplainFolder (FOLDER_NAME_DICTIONARIES, FOLDER_COMMENT_DICTIONARIES, parentModule);
  }

  public static ExplainFolder getTablesFolder (ExplainModule parentModule)
  {
    return new ExplainFolder (FOLDER_NAME_TABLES, FOLDER_COMMENT_TABLES, parentModule);
  }

  public static ExplainFolder getFunctionsFolder (ExplainModule parentModule)
  {
    return new ExplainFolder (FOLDER_NAME_FUNCTIONS, FOLDER_COMMENT_REQ_FUNCTIONS, parentModule);
  }

  public static ExplainFolder getReqModulesFolder (ExplainModule parentModule)
  {
    return new ExplainFolder (FOLDER_NAME_REQ_MODULES, FOLDER_COMMENT_REQ_MODULES, parentModule);
  }

  public static ExplainFolder getModuleInfosFolder (ExplainModule parentModule)
  {
    ExplainFolder modPropsNode = new ExplainFolder (FOLDER_MODULE_PROPERTIES, FOLDER_COMMENT_MODULE_PROPERTIES, parentModule, Icons.INFO_ICON);
    return modPropsNode;
  }

  public static ExplainFolder createSchemaNode (ExplainElement parentElement, TupleSchema schema)
  {
    ExplainFolder schemaNode = new ExplainFolder (SCHEMA_NODE_NAME, SCHEMA_NODE_NAME, parentElement, Icons.INFO_ITEM);
    schemaNode.setElement (schema);
    return schemaNode;
  }

  @Override
  public ExplainElement[] getChildren ()
  {
    ExplainElement[] children = null;

    if (folderName.equals (FOLDER_NAME_VIEWS)) {
      List<ExplainView> allViews = ((ExplainModule)parent).getAqlViews ();
      children = allViews.toArray (new ExplainView[] {});
    }
    else if (folderName.equals (FOLDER_NAME_DICTIONARIES)) {
      List<ExplainDictionary> allDicts = ((ExplainModule)parent).getDictionaries ();
      children = allDicts.toArray (new ExplainDictionary[] {});
    }
    else if (folderName.equals (FOLDER_NAME_TABLES)) {
      List<ExplainTable> allTables = ((ExplainModule)parent).getTables ();
      children = allTables.toArray (new ExplainTable[] {});
    }
    else if (folderName.equals (FOLDER_NAME_FUNCTIONS)) {
      List<ExplainFunction> allFuncs = ((ExplainModule)parent).getFunctions ();
      children = allFuncs.toArray (new ExplainFunction[] {});
    }
    else if (folderName.equals (FOLDER_NAME_REQ_MODULES)) {
      List<ExplainInfo> allReqModules = ((ExplainModule)parent).getReqModules ();
      children = allReqModules.toArray (new ExplainInfo[] {});
    }
    else if (folderName.equals (FOLDER_MODULE_PROPERTIES)) {
      List<ExplainElement> allModuleInfos = ((ExplainModule)parent).getModuleInfos ();
      children = allModuleInfos.toArray (new ExplainElement[] {});
    }
    else if (folderName.equals (SCHEMA_NODE_NAME)) {
      TupleSchema schema = (TupleSchema)element;
      List<ExplainInfo> childList = new ArrayList<ExplainInfo> ();
      for (String fieldName : schema.getFieldNames ()) {
        String dispText = fieldName + " " + schema.getFieldTypeByName (fieldName).toString ();
        childList.add (new ExplainInfo (dispText, dispText, Icons.INFO_SUBITEM));
      }
      children = childList.toArray (new ExplainInfo[] {});
    }

    if (children != null && children.length > 0) {
      for (ExplainElement e : children) {
        e.setParent (this);
      }
    }

    return children;
  }

  @Override
  public boolean hasChildren ()
  {
    if (folderName.equals (FOLDER_NAME_VIEWS)) {
      return !((ExplainModule)parent).getAqlViews ().isEmpty ();
    }
    else if (folderName.equals (FOLDER_NAME_DICTIONARIES)) {
      return !((ExplainModule)parent).getDictionaries ().isEmpty ();
    }
    else if (folderName.equals (FOLDER_NAME_TABLES)) {
      return !((ExplainModule)parent).getTables ().isEmpty ();
    }
    else if (folderName.equals (FOLDER_NAME_REQ_MODULES)) {
      return !((ExplainModule)parent).getReqModules ().isEmpty ();
    }
    else if (folderName.equals (FOLDER_NAME_FUNCTIONS)) {
      return !((ExplainModule)parent).getFunctions ().isEmpty ();
    }
    else if (folderName.equals (FOLDER_MODULE_PROPERTIES)) {
      return true;
    }
    else if (folderName.equals (SCHEMA_NODE_NAME)) {
      return true;
    }

    return false;
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
