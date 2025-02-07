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
package com.ibm.biginsights.textanalytics.workflow.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Message pointers for the Workflow, see the file #BUNDLE_NAME for the definition of each constant
 * 
 * 
 */
public class Messages extends NLS
{



	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.workflow.messages.messages"; //$NON-NLS-1$
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle (BUNDLE_NAME);

  // -------------------------

  public static String add_label_text;
  public static String add_label_tootltip;
  public static String add_direct_label_text;
  public static String add_direct_label_tootltip;
  public static String add_bf_label_text;
  public static String add_bf_label_tooltip;
  public static String add_cg_label_text;
  public static String add_cg_label_tooltip;
  public static String add_fc_label_text;
  public static String add_fc_label_tooltip;
  public static String add_final_label_text;
  public static String add_final_label_tooltip;

  public static String add_label_input;

  public static String add_example_text;
  public static String add_example_tootltip;

  public static String add_comment_text;
  public static String add_comment_tootltip;

  public static String edit_comment_text;
  public static String edit_comment_tootltip;

  public static String add_aql_rule_text;
  public static String add_aql_rule_tootltip;

  public static String add_bf_aql_statement_text;
  public static String add_bf_aql_statement_tooltip;
  public static String add_cg_aql_statement_text;
  public static String add_cg_aql_statement_tooltip;
  public static String add_fc_aql_statement_text;
  public static String add_fc_aql_statement_tooltip;
  public static String add_final_aql_statement_text;
  public static String add_final_aql_statement_tooltip;

  public static String sort_alphabetically;
  public static String sort_alphabetically_tooltip;

  public static String run_default_text;
  public static String run_default_tootltip;

  public static String run_module_default_text;
  public static String run_module_default_tootltip;

  public static String running_message;

  public static String run_file_selected_text;
  public static String run_file_selected_tootltip;

  public static String run_labeled_files_text;
  public static String run_labeled_files_tootltip;

  public static String run_input_collection_text;
  public static String run_input_collection_tootltip;

  public static String run_module_on_file_selected_text;
  public static String run_module_on_labeled_files_text;
  public static String run_module_on_input_collection_text;

  public static String run_profiler_text;
  public static String run_profiler_tootltip;
  public static String run_profiler_title;
  public static String run_profiler_message;

  public static String delete_children_text;
  public static String delete_children_tootltip;
  public static String delete_children_title;
  public static String delete_child_message;
  public static String delete_children_message;

  public static String change_project_text;
  public static String change_project_tootltip;

  public static String copy_text;
  public static String copy_tootltip;

  public static String cut_text;
  public static String cut_tootltip;

  public static String paste_text;
  public static String paste_tootltip;

  public static String rename_text;
  public static String rename_tootltip;
  public static String rename_title;
  public static String rename_message;
  public static String rename_input_title;
  public static String rename_input_message;

  public static String change_aql_file_text;
  public static String change_aql_file_tootltip;

  public static String add_element_text;
  public static String add_element_tootltip;

  public static String open_collection_text;
  public static String open_collection_tootltip;

  public static String open_export_text;
  public static String open_export_tootltip;

  public static String label_done_message;
  public static String label_working_message;
  public static String label_done_suffix;

  // ----messages---

  public static String missing_text_analytics_nature_title;
  public static String missing_text_analytics_nature_message;

  public static String add_view_default_message;
  public static String add_view_basic_message;
  public static String add_view_candidate_message;
  public static String add_view_refinement_message;

  // ----errors-------
  public static String text_analytics_internal_error_message;
  public static String field_empty_error_message;

  // ----menu labels-------
  public static String add_view_to_plan_label;
  public static String add_to_label;

  // ---example wizard-----
  public static String create_label_wizard_window_title;
  public static String create_label_wizard_page_title;
  public static String example_wizard_window_title;
  public static String example_wizard_page_title;
  public static String example_wizard_page_label;
  public static String example_wizard_page_to_exising_label;
  public static String example_wizard_page_to_new_label;
  public static String example_wizard_page_create_new_label;
  // --- create example page ----
  public static String create_example_page_message;
  public static String create_example_page_parent_message;

  // ---choose project dialog------
  public static String choose_project_dialog_title;
  public static String choose_project_dialog_project;

  // ---create aql statement dialog-----
  public static String create_aql_statement_dialog_title;
  public static String create_aql_statement_dialog_name;
  public static String create_aql_statement_dialog_name_dict;
  public static String create_aql_statement_dialog_name_func;
  public static String create_aql_statement_dialog_name_table;

  public static String create_aql_statement_module_name;
  public static String create_aql_statement_aqlfile_name;
  public static String create_aql_statement_aqltype_name;
  public static String create_aql_statement_dialog_type;
  public static String create_aql_statement_dialog_output_message;
  public static String create_aql_statement_dialog_export_message;
  public static String create_aql_statement_dialog_validation_message;
  public static String create_aql_statement_dialog_validation_module_message;
  public static String create_aql_statement_dialog_validation_aqlfile_message;
  public static String create_aql_statement_dialog_aql_file_creation_error;

  // ---create label page---
  public static String create_label_page_parent_label;
  public static String create_label_page_label_message;
  public static String create_label_page_info_message_1;
  public static String create_label_page_info_message_2;
  public static String create_label_page_validation_message_general;
  public static String create_label_page_validation_message_ends;
  public static String create_label_page_validation_message_too_long;
  public static String create_label_page_validation_message_invalid_character;
  public static String create_label_validation_message_duplicate_name;
  public static String create_label_page_validation_message_parent_unclear;
  public static String create_label_page_validation_message_parent_not_exist;
  public static String create_label_dialog_title;
  public static String create_label_dialog_message;


  // --- Simplified Extraction Plan action ---
  public static String simplified_extraction_plan_action_text;
  public static String simplified_extraction_plan_action_tooltip;

  public static String full_view_extraction_plan_action_text;
  public static String full_view_extraction_plan_action_tooltip;
  public static String normal_view_extraction_plan_action_tooltip;

  // --- open action ---
  public static String open_action_text;
  public static String open_action_tootltip;

  public static String open_dictionary_text;
  public static String open_regex_generator_text;

  // --- delete action ---
  public static String delete_action_text;
  public static String delete_action_tootltip;

  // --- step 1 ---
  public static String step_1_title;

  public static String step_1a_title;
  public static String step_1a_inline_text;

  public static String lang_title;
  public static String lang_tootltip;

  public static String step_1b_title;
  public static String step_1b_tootltip;

  public static String step_1_browser_title;
  public static String step_1_open_btn_label;
  public static String step_1_example_txt;

  // --- step 2 ---
  public static String step_2_title;

  public static String step_2a_title;
  public static String step_2a_inline_text_1;
  public static String step_2a_tip2;
  public static String step_2a_example1_link, step_2a_example1_url;

  public static String see_example;

  public static String step_2b_title;
  public static String step_2b_inline_text_1;
  public static String step_2b_insideClue_example;
  public static String step_2b_insideClue_example_tooltip;
  public static String step_2b_outsideClue_example;
  public static String step_2b_outsideClue_example_tooltip;

  // --- step 3 ---
  public static String step_3_title;
  public static String step_3_inline_text;

  public static String step_3a_title;
  public static String step_3a_inline_text;
  public static String step_3a_example;
  public static String step_3a_example_tootlip;

  public static String step_3a_tip1;
  public static String step_3a_tip2;
  public static String step_3a_tip2_noLink;
  public static String step_3a_tip3;
  public static String step_3a_tip3_noLink;

  // --- step 4 ---
  public static String step_4_title;

  public static String step_4a_title;
  public static String step_4a_tip1;
  public static String step_4a_tip2;

  public static String step_4b_title;
  public static String step_4b_tip1;
  public static String step_4b_link1;
  public static String step_4b_link1_noLink;
  public static String step_4b_tip2;
  public static String step_4b_tip3;

  // --- step 5 ---
  public static String step_5_title;
  public static String step_5_text1;
  public static String step_5_text2;

  // --- step 6 ---
  public static String step_6_title;
  public static String step_6_text1;
  public static String step_6_text2;

  // --- errors and logs ---
  public static String extraction_plan_collection_not_readable;
  public static String extraction_plan_collection_error;
  public static String extraction_plan_collection_path_not_exists;
  public static String extraction_plan_collection_language_not_supported;
  public static String extraction_plan_not_ready;
  public static String extraction_plan_not_tagged_files;
  public static String extraction_plan_not_files_selected;

  public static String compiler_error_in_aql_files__profiler;

  public static String missing_view_in_aql_files;
  public static String missing_file_title;
  public static String missing_file_aqlnode_message;
  public static String missing_file_aqlnode_message_2;
  public static String select_file_message;

  public static String create_aql_files_text;
  public static String create_aql_files_inline_text;

  public static String aql_file_added_title;
  public static String aql_file_added_warn;

  public static String open_help_tooltip;

  public static String file_not_found;
  public static String extractionplan_not_ready;

  public static String import_json_import_results;
  public static String import_json_results;
  public static String import_json_results_desc;
  public static String import_json_browse_fs;
  public static String import_json_browse_ws;
  public static String import_json_open_immediate;
  public static String import_json_output_json;
  public static String import_json_target_project;
  public static String import_json_select_project;
  public static String import_json_select_project_desc;
  public static String import_json_error_reading_input;
  public static String import_json_error_writing_output;
  public static String import_json_error_opening_result;
  public static String import_json_error_creating_outFolder;

  public static String import_json_info_empty_input;

  public static String refresh_extraction_plan_job;

  static {
    NLS.initializeMessages (BUNDLE_NAME, Messages.class);
  }

  public static String getString (String key)
  {
    try {
      return RESOURCE_BUNDLE.getString (key);
    }
    catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
