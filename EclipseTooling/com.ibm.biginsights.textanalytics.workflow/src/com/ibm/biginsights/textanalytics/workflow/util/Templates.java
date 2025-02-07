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
package com.ibm.biginsights.textanalytics.workflow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlTypes;

public class Templates
{



  public static final String view_name = Pattern.quote ("<ViewName>");
  public static final String columns_space = Pattern.quote ("<columns_space>");

  public static final String location = "templates";

  public static final String DICTIONARY_TLP = "dictionary.tpl";
  public static final String REGEX_TLP = "regex.tpl";
  public static final String POS_TLP = "pos.tpl";

  public static final String SELECT_TPL = "select.tpl";
  public static final String UNION_TLP = "union.tpl";
  public static final String BLOCK_TLP = "block.tpl";
  public static final String PATTERN_TLP = "pattern.tpl";

  public static final String CONSOLIDATE_TLP = "consolidate.tpl";
  public static final String PREDFILTER_TLP = "predFilter.tpl";
  public static final String SETFILTER_TLP = "setFilter.tpl";

  public static final String OUTPUT_VIEW = "output.tpl";

  public static final String EXPORT_VIEW = "export_view.tpl";
  public static final String EXPORT_DICT = "export_dict.tpl";
  public static final String EXPORT_TABL = "export_table.tpl";
  public static final String EXPORT_FUNC = "export_fucntion.tpl";

  public static final String DEFAULT_AQL_TLP = "aqlFileTemplate.tpl";

  public static InputStream getTemplate (String template) throws IOException
  {
    String templatePathLocation = location + Path.SEPARATOR + template;
    IPath templatePath = new Path (templatePathLocation);
    InputStream is = FileLocator.openStream (Activator.getDefault ().getBundle (), templatePath, true);
    return is;
  }

//  public static InputStream getTemplateFromAqlType (AqlTypes type) throws IOException
//  {
//
//    switch (type) {
//      case DICTIONARY:
//        return getTemplate (DICTIONARY_TLP);
//      case PARTofSPEECH:
//        return getTemplate (POS_TLP);
//      case REGEX:
//        return getTemplate (REGEX_TLP);
//
//      case SELECT:
//        return getTemplate (SELECT_TPL);
//      case UNION:
//        return getTemplate (UNION_TLP);
//      case BLOCK:
//        return getTemplate (BLOCK_TLP);
//      case PATTERN:
//        return getTemplate (PATTERN_TLP);
//
//      case PREDICATEbasedFILTER:
//        return getTemplate (PREDFILTER_TLP);
//      case SETbasedFILTER:
//        return getTemplate (SETFILTER_TLP);
//      case CONSOLIDATE:
//        return getTemplate (CONSOLIDATE_TLP);
//
//      case EXPORTDICTIONARY:
//        return getTemplate (EXPORT_DICT);
//      case EXPORTTABLE:
//        return getTemplate (EXPORT_TABL);
//      case EXPORTVIEW:
//        return getTemplate (EXPORT_VIEW);
//      case EXPORTFUNCTION:
//        return getTemplate (EXPORT_FUNC);
//    }
//
//    return null;
//  }

  public static String getTemplateString (String template) throws Exception
  {
    InputStream is = getTemplate (template);
    return FileUtils.streamToStr (is, Constants.ENCODING);
  }

  private static String getTemplateString (String template, String viewName, boolean doOutput, boolean doExport) throws Exception
  {

    InputStream is = getTemplate (template);
    String basic = FileUtils.streamToStr (is, Constants.ENCODING);

    basic = basic.replaceAll (view_name, viewName);

    String langCode = ActionPlanView.getLangCode ();
    if (langCode != null && !langCode.isEmpty ()) basic = basic.replace ("<language_code(s)>", langCode);

    if (doOutput) {
      String out_str = getTemplateString (OUTPUT_VIEW);
      out_str = out_str.replaceAll (view_name, viewName);
      basic = String.format ("%s\n\n%s", basic, out_str);
    }

    if (doExport && canAddExportStatement(template)) {
      String out_str = getTemplateString (EXPORT_VIEW);
      out_str = out_str.replaceAll (view_name, viewName);
      basic = String.format ("%s\n\n%s", basic, out_str);
    }

    return basic;
  }

  private static boolean canAddExportStatement (String template)
  {
    // These templates already contain export statement, don't need to add another
    return (template != EXPORT_VIEW) && (template != EXPORT_DICT) && (template != EXPORT_TABL) && (template != EXPORT_FUNC);
  }

//  public static String getViewTemplateString (String template, String viewName, boolean doOutput) throws Exception
//  {
//
//    template = template.replaceAll (view_name, viewName);
//
//    if (doOutput) {
//      InputStream outStream = getTemplate (OUTPUT_VIEW);
//      String out_str = FileUtils.streamToStr (outStream, Constants.ENCODING);
//      out_str = out_str.replaceAll (view_name, viewName);
//      template = String.format ("%s\n\n%s", template, out_str);
//    }
//
//    return template;
//  }

  public static String getTemplateFromAqlType (AqlTypes type, String viewName, boolean doOutput, boolean doExport) throws Exception
  {

    switch (type) {
      case DICTIONARY:
        return getTemplateString (DICTIONARY_TLP, viewName, doOutput, doExport);
      case PARTofSPEECH:
        return getTemplateString (POS_TLP, viewName, doOutput, doExport);
      case REGEX:
        return getTemplateString (REGEX_TLP, viewName, doOutput, doExport);

      case SELECT:
        return getTemplateString (SELECT_TPL, viewName, doOutput, doExport);
      case UNION:
        return getTemplateString (UNION_TLP, viewName, doOutput, doExport);
      case BLOCK:
        return getTemplateString (BLOCK_TLP, viewName, doOutput, doExport);
      case PATTERN:
        return getTemplateString (PATTERN_TLP, viewName, doOutput, doExport);

      case PREDICATEbasedFILTER:
        return getTemplateString (PREDFILTER_TLP, viewName, doOutput, doExport);
      case SETbasedFILTER:
        return getTemplateString (SETFILTER_TLP, viewName, doOutput, doExport);
      case CONSOLIDATE:
        return getTemplateString (CONSOLIDATE_TLP, viewName, doOutput, doExport);

      case EXPORTVIEW:
        return getTemplateString (EXPORT_VIEW, viewName, doOutput, doExport);
      case EXPORTDICTIONARY:
        return getTemplateString (EXPORT_DICT, viewName, doOutput, doExport);
      case EXPORTTABLE:
        return getTemplateString (EXPORT_TABL, viewName, doOutput, doExport);
      case EXPORTFUNCTION:
        return getTemplateString (EXPORT_FUNC, viewName, doOutput, doExport);
    }

    return "";
  }

}
