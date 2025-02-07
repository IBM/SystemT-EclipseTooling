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
package com.ibm.biginsights.textanalytics.regex.command;

/**
 * This class facilitates the communication of regexes between the AQL editor and the various regex
 * builder tools. A regex builder should accept such a container as input with an initial regex, and
 * update the container with the result.
 */
public class RegexContainer {


  
  private String regex;

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getRegex() {
    return this.regex;
  }
  
}
