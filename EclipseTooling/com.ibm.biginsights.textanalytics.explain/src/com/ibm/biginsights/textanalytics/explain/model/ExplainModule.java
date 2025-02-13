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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.DictionaryMetadata;
import com.ibm.avatar.api.tam.FunctionMetadata;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.api.tam.TableMetadata;
import com.ibm.avatar.api.tam.ViewMetadata;
import com.ibm.biginsights.textanalytics.explain.Icons;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;

public class ExplainModule implements ExplainElement
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

  ModuleMetadata moduleMD = null;
  String planAOG = null;

  List<ExplainView> aqlViews = null;
  List<ExplainDictionary> dictList = null;
  List<ExplainTable> tableList = null;
  List<ExplainFunction> functionList = null;
  List<ExplainInfo> reqModuleList = null;
  List<ExplainElement> moduleInfoList = null;
  ExplainElement[] children = null;

  public static Comparator<ExplainElement> elemComp = new Comparator<ExplainElement>() {
    @Override
    public int compare (ExplainElement v1, ExplainElement v2) {
      return v1.getName ().compareTo (v2.getName ());
    }
  };

  public ExplainModule (String selectedPathname)
  {
    String absoluteFilePath = ProjectPreferencesUtil.getAbsolutePath (selectedPathname);
    if (StringUtils.isNullOrWhiteSpace (absoluteFilePath) == false &&
        absoluteFilePath.endsWith (".tam")) {      //$NON-NLS-1$

      // For workspace resources, file separator will always be "/",
      // for file system resources, file separator is "/" on Linux and "\" on Windows.
      String fileSep = (absoluteFilePath.indexOf ("/") >= 0) ? "/" : System.getProperty("file.separator");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      String modulePath = absoluteFilePath.substring (0, absoluteFilePath.lastIndexOf (fileSep));
      String moduleName = absoluteFilePath.substring (absoluteFilePath.lastIndexOf (fileSep) + 1, absoluteFilePath.lastIndexOf ("."));  //$NON-NLS-1$

      try {
        moduleMD = ModuleMetadataFactory.readMetaData (moduleName, modulePath);
      }
      catch (TextAnalyticsException e) {
        // for error reading module metadata, "moduleMetadata" will be null
        // and explainModule (moduleMetadata) will clear the view.
      }
    }
  }

  private void buildViewList ()
  {
    aqlViews = new ArrayList<ExplainView> ();

    if (moduleMD == null)
      return;

    Set<String> viewNames = new HashSet<String> ();

    viewNames.addAll (Arrays.asList (moduleMD.getExportedViews ()));
    viewNames.addAll (Arrays.asList (moduleMD.getOutputViews ()));
    for (Pair<String,String> pair : moduleMD.getExternalViews ()) {
      viewNames.add (pair.first);
    }
    
    for (String viewQualName : viewNames) {
      ViewMetadata viewMD = moduleMD.getViewMetadata (viewQualName);
      aqlViews.add (new ExplainView(viewMD, viewQualName));
    }

    Collections.sort (aqlViews, elemComp);
  }

  private void buildDictionaryList ()
  {
    dictList = new ArrayList<ExplainDictionary> ();

    if (moduleMD == null)
      return;

    Set<String> dictNames = new HashSet<String> ();
    dictNames.addAll (Arrays.asList (moduleMD.getExportedDictionaries ()));
    dictNames.addAll (Arrays.asList (moduleMD.getExternalDictionaries ()));

    for (String dn : dictNames) {
      DictionaryMetadata dictMD = moduleMD.getDictionaryMetadata (dn);
      dictList.add (new ExplainDictionary (dictMD));
    }

    Collections.sort (dictList, elemComp);
  }

  private void buildTableList ()
  {
    tableList = new ArrayList<ExplainTable> ();

    if (moduleMD == null)
      return;

    Set<String> tableNames = new HashSet<String> ();
    tableNames.addAll (Arrays.asList (moduleMD.getExportedTables ()));
    tableNames.addAll (Arrays.asList (moduleMD.getExternalTables ()));

    for (String tn : tableNames) {
      TableMetadata tableMD = moduleMD.getTableMetadata (tn);
      tableList.add (new ExplainTable (tableMD));
    }

    Collections.sort (tableList, elemComp);
  }

  private void buildFunctionList ()
  {
    functionList = new ArrayList<ExplainFunction> ();

    if (moduleMD == null)
      return;

    for (String fn : moduleMD.getExportedFunctions ()) {
      FunctionMetadata functionMD = moduleMD.getFunctionMetadata (fn);
      functionList.add (new ExplainFunction (functionMD));
    }

    Collections.sort (functionList, elemComp);
  }

  private void buildReqModuleList ()
  {
    reqModuleList = new ArrayList<ExplainInfo> ();

    if (moduleMD == null)
      return;

    Image reqModuleIcon = Icons.MODULE_ICON;
    DecorationOverlayIcon ovlImage = new DecorationOverlayIcon (reqModuleIcon, Icons.EXTERNAL_OVL_IMGDESC, IDecoration.BOTTOM_LEFT);
    reqModuleIcon = ovlImage.createImage ();

    for (String mn : moduleMD.getDependentModules ()) {
      reqModuleList.add (new ExplainInfo (mn, mn, reqModuleIcon));
    }

    Collections.sort (reqModuleList, elemComp);
  }

  private void buildModuleInfoList ()
  {
    moduleInfoList = new ArrayList<ExplainElement> ();

    if (moduleMD == null)
      return;

    moduleInfoList.add (ExplainFolder.createSchemaNode (this, moduleMD.getDocSchema ()));
    moduleInfoList.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_TOKENIZER, moduleMD.getTokenizerType ().toString ()));
    moduleInfoList.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_HOSTNAME, moduleMD.getHostName ()));
    moduleInfoList.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_USERNAME, moduleMD.getUserName ()));
    moduleInfoList.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_PRODVER, moduleMD.getProductVersion ()));
    moduleInfoList.add (ExplainInfo.createInfoObject (Messages.ExplainModuleView_COMPILETIME, moduleMD.getCompilationTime ()));

    Collections.sort (moduleInfoList, elemComp);
  }

  @Override
  public String getName ()
  {
    if (moduleMD != null)
      return moduleMD.getModuleName ();
    else
      return "";
  }

  @Override
  public String getComment ()
  {
    if (moduleMD != null && moduleMD.getComment () != null)
      return moduleMD.getComment ();
    else
      return "";
  }

  @Override
  public Image getIcon ()
  {
    return Icons.MODULE_ICON;
  }

  @Override
  public ExplainElement getParent ()
  {
    return null;
  }

  public List<ExplainView> getAqlViews ()
  {
    if (aqlViews == null)
      buildViewList ();

    return aqlViews;
  }

  public List<ExplainDictionary> getDictionaries ()
  {
    if (dictList == null)
      buildDictionaryList ();

    return dictList;
  }

  public List<ExplainTable> getTables ()
  {
    if (tableList == null)
      buildTableList ();

    return tableList;
  }

  public List<ExplainFunction> getFunctions ()
  {
    if (functionList == null)
      buildFunctionList ();

    return functionList;
  }

  public List<ExplainInfo> getReqModules ()
  {
    if (reqModuleList == null)
      buildReqModuleList ();

    return reqModuleList;
  }

  public List<ExplainElement> getModuleInfos ()
  {
    if (moduleInfoList == null)
      buildModuleInfoList ();

    return moduleInfoList;
  }

  @Override
  public void setParent (ExplainElement parentElement)
  {
    // This 'module' object doesn't have a parent.
  }

  @Override
  public ExplainElement[] getChildren ()
  {
    return new ExplainElement[] {
      ExplainFolder.getModuleInfosFolder (this),
      ExplainFolder.getReqModulesFolder (this),
      ExplainFolder.getViewsFolder (this),
      ExplainFolder.getDictionariesFolder (this),
      ExplainFolder.getTablesFolder (this),
      ExplainFolder.getFunctionsFolder (this)
    };
  }

  @Override
  public boolean hasChildren ()
  {
    return true;
  }

  public ModuleMetadata getMetadata ()
  {
    return moduleMD;
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
