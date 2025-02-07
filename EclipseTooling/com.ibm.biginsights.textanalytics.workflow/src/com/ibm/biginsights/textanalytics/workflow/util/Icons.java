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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.textanalytics.workflow.Activator;

public class Icons
{


 
	public static final Image COMMENT_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
  "icons/comment.gif");
  public static final Image DICTIONARY_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/dictionary.gif");
  public static final Image REGEX_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/regex.gif");
  public static final Image SPLIT_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/split.gif");
  public static final Image OPEN_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/open.gif");
  public static final Image POS_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/audio.gif");
  public static final Image SEQUENCE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/sequence.gif");
  public static final Image UNION_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/union.gif");
  public static final Image CONSOLIDATE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/consolidate.gif");
  public static final Image FILTER_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/filter.gif");
  public static final Image AUGMENT_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/augment.gif");

  public static final Image AQL_ICON = Activator.getImageDescriptor ("tree_icons/AQLview.png").createImage ();
  
  public static final Image AQL_FILE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
  "icons/file.png");

  public static final Image LABEL_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/tag.gif");

  public static final Image DONE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/done.gif");

  public static final Image LABELS_FOLDER_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/tags_folder.gif");

  public static final Image EXAMPLE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/note.gif");
  public static final Image EXAMPLES_FOLDER_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/notes_folder.gif");

  public static final Image AQL_COMPONENTS_FOLDER_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/components_folder.gif");

  public static final Image BASIC_FEATURES_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/basic_features.gif");

  public static final Image CONCEPTS_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/concepts.gif");

  public static final Image REFINEMENT_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/refinement.gif");

  public static final Image FINALS_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/tree_icons/finals.png");

  public static final Image DELETE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/delete.gif");

  public static final Image RUN_ON_FILE_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/run_on_file.gif");

  public static final Image RUN_ON_LABELED_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/run_on_labeled.gif");

  public static final Image RUN_ON_COLLECTION_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/run_on_collection.gif");

  public static final Image RUN_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/run.gif");

  public static final Image SWITCH_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/ready/extractPlanJump.gif");

  public static final Image EXPORT_AOG_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID,
    "icons/export_AOG.gif");

  public static final Image PROFILER_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/profiler.gif");

  public static final ImageDescriptor bfOverlayImgDesc = Activator.getImageDescriptor ("tree_icons/overlay_basic.png");

  public static final ImageDescriptor cgOverlayImgDesc = Activator.getImageDescriptor ("tree_icons/overlay_candidate.png");

  public static final ImageDescriptor fcOverlayImgDesc = Activator.getImageDescriptor ("tree_icons/overlay_filter.png");

  public static final ImageDescriptor fiOverlayImgDesc = Activator.getImageDescriptor ("tree_icons/overlay_final.png");

  public static final Image SORT_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/sort.png");

  public static final Image ALT_VIEW_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/ready/extractPlanAltView.gif");

  public static final Image EP_VIEW_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/ready/extractPlanView.gif");

  public static final Image EP_FULL_VIEW_ICON = ResourceManager.getPluginImage (Activator.PLUGIN_ID, "icons/ready/extractPlanFullView.gif");

}
