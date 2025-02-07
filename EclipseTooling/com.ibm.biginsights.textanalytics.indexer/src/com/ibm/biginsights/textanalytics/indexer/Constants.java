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
package com.ibm.biginsights.textanalytics.indexer;

import java.io.File;

/**
 * List of constants used by the indexer plugin
 * 
 *  Krishnamurthy
 */
public class Constants
{
  @SuppressWarnings("unused")


  public static final String DOC_SCHEMA = "DocumentSchema"; //$NON-NLS-1$

  public static final String SEPERATOR = ";"; //$NON-NLS-1$

  /**
   * The character used to separate components of a qualified name, as in ProjectName.ModuleName.TypeName.ElementName
   */
  public static final char QUALIFIED_NAME_SEPARATOR = '\u2752';

  public static final String UTF_8 = "UTF-8";
  /**
   * Index file names
   */
  public static final String ELEMENT_CACHE_IDX = "elementCache.idx";
  public static final String PROJECT_CACHE_IDX = "projectCache.idx";
  public static final String MODULE_CACHE_IDX = "moduleCache.idx";
  public static final String FILE_CACHE_IDX = "fileCache.idx";
  public static final String INDEXING_STATUS_FILE = "misc.idx";

  /**
   * Path of the Index
   */
  public static final String INDEX_PATH = File.separator + ".metadata" + File.separator + ".plugins" + File.separator
    + "com.ibm.biginsight.textanalytics.indexer";

  public static final String XML_ELEMENT_CACHE_END = "</elementCache>";

  public static final String XML_REFERENCE_HIERARCHY_MAP_CONFIG_END = "</referenceHierarchyMapConfig>";

  public static final String XML_ELEMENT_END = "</element>";

  public static final String XML_REF_ID_END = "</refid>";

  public static final String XML_REF_ID = "<refid>";

  public static final String XML_ELEMENT_ID_ATTR = "<element id=\"";

  public static final String XML_REFERENCE_HIERARCHY_MAP_CONFIG = "<referenceHierarchyMapConfig>";

  public static final String XML_ELEMENT_REFERENCE_MASTER_CONFIG_END = "</elementReferenceMasterConfig>";

  public static final String XML_ELEMENT_REFERENCE_MASTER_END = "</elementReferenceMaster>";

  public static final String XML_ELEMENT_ID_END = "</elementId>";

  public static final String XML_ELEMENT_ID = "<elementId>";

  public static final String XML_ELEMENT_REFERENCE_MASTER_ID = "<elementReferenceMaster id=\"";

  public static final String XML_ELEMENT_REFERENCE_MASTER_CONFIG = "<elementReferenceMasterConfig>";

  public static final String XML_ELEMENT_DEFENITION_MASTER_CONFIG_END = "</elementDefenitionMasterConfig>";

  public static final String XML_ELEMENT_DEF_MASTER_END = "</elementDefMaster>";

  public static final String XML_ID_END = "</id>";

  public static final String XML_ID = "<id>";

  public static final String XML_ELEMENT_REFERENCE_LIST_END = "</elementReferenceList>";

  public static final String XML_ELEMENT_REFERENCE_LIST = "<elementReferenceList>";

  public static final String XML_LOCATION_END = "</location>";

  public static final String XML_LOCATION = "<location>";

  public static final String XML_NAME_END = "</name>";

  public static final String XML_NAME = "<name>";

  public static final String XML_ELEMENT_TYPE_END = "</elementType>";

  public static final String XML_ELEMENT_TYPE = "<elementType>";

  public static final String XML_MODULE_ID_END = "</fileID>";

  public static final String XML_MODULE_ID = "<fileID>";

  public static final String XML_PROJECT_ID_END = "</projectId>";

  public static final String XML_PROJECT_ID = "<projectId>";

  public static final String XML_ELEMENT_DEF_MASTER_ID = "<elementDefMaster id=\"";

  public static final String XML_ELEMENT_DEFENITION_MASTER_CONFIG = "<elementDefenitionMasterConfig>";

  public static final String XML_CLOSE_TAG = "\">";

  public static final String XML_ELEMENT_MASTER_CONFIG_END = "</elementMasterConfig>";

  public static final String XML_ELEMENT_MASTER_END = "</elementMaster>";

  public static final String XML_ELEMENT_MASTER_ID = "<elementMaster id=\"";

  public static final String XML_ELEMENT_MASTER_CONFIG = "<elementMasterConfig>";

  public static final String XML_ELEMENT_CACHE = "<elementCache version=\"";

  public static final String XML_VERSION_ENCODING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  public static final String ELEMENT_REF_ID = "refid";

  public static final String ELEMENT_ID = "elementId";

  public static final String ELEMENT_REFERENCE_LIST = "elementReferenceList";

  public static final String ELEMENT_LOCATION = "location";

  public static final String ELEMENT_NAME = "name";

  public static final String ELEMENT_TYPE = "elementType";

  public static final String ELEMENT_MODULE_ID = "fileID";

  public static final String ELEMENT_PROJECT_ID = "projectId";

  public static final String ID = "id";
  
  public static final String ELEMENT_CACHE_VERSION_ATTRIBUTE = "version";
  
  /**
   * Current version of element cache.
   */
  public static final String ELEMENT_CACHE_LATEST_VERSION = "2.1.2.0";
  
  public static final String ELEMENT_METADATA_ACTIVESTATE = "elementActiveState";
  
  public static final String XML_ELEMENT_METADATA_MAP_CONFIG = "<elementMetadataMapConfig>";
  
  public static final String XML_ELEMENT_METADATA_MAP_CONFIG_END = "</elementMetadataMapConfig>";
  
  public static final String XML_ELEMENT_METADATA_ID = "<elementMetadata id=\"";
  
  public static final String XML_ELEMENT_METADATA_END = "</elementMetadata>";
  
  public static final String XML_ELEMENT_METADATA_ACTIVESTATE = "<elementActiveState>";
  
  public static final String XML_ELEMENT_METADATA_ACTIVESTATE_END = "</elementActiveState>";

}
