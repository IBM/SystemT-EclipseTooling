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
package com.ibm.biginsights.textanalytics.nature.utils;

/**
 * Data object to hold details about external dictionary namely name and if it is mandatory. This is used for
 * maintaining external dictionary references by modules.
 * 
 * 
 */
public class ExternalDictionary
{



	private String dictName;
  private boolean mandatory;

  public ExternalDictionary (String dictName, boolean mandatory)
  {
    super ();
    this.dictName = dictName;
    this.mandatory = mandatory;
  }

  public String getDictName ()
  {
    return dictName;
  }

  public boolean isMandatory ()
  {
    return mandatory;
  }

}
