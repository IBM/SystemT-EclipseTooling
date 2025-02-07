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
package com.ibm.biginsights.textanalytics.indexer.types;

public enum ElementType
{
  UNKNOWN,

  VIEW, EXTERNAL_VIEW,

  DICTIONARY, EXTERNAL_DICTIONARY,

  TABLE, EXTERNAL_TABLE,

  FUNCTION, EXTERNAL_FUNCTION,

  MODULE, 
  
  /**
   * VIEW_OR_TABLE is a transient type. The persisted index files should never contain this type
   * in any definition entry. This type is used while indexing references in a select statement's
   * from-list, when we are unable to determine whether the element is a view or table.
   */ 
  VIEW_OR_TABLE;         
  
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$


}
