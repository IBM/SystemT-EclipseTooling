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
package com.ibm.biginsights.textanalytics.concordance.model.impl;

import java.util.regex.Pattern;

import com.ibm.biginsights.textanalytics.concordance.model.IStringFilter;

public class StringFilter implements IStringFilter {



  private final boolean isRegex;

  private final String string;

  private final Pattern pattern;

  public StringFilter() {
    this(false, null);
  }

  public StringFilter(final boolean isRegex, final String filter) {
    super();
    this.isRegex = isRegex;
    this.string = filter;
    if (filter == null) {
      this.pattern = null;
    } else {
      if (isRegex) {
        this.pattern = Pattern.compile(filter);
      } else {
        this.pattern = wildcardStringToPattern(filter);
      }
    }
  }

  private static final Pattern wildcardStringToPattern(String s) {
    if (s == null) {
      return null;
    }
    // Note: backslash must be the first character to be replaced, for obvious reasons.
    final String[] specialChars = new String[] { "\\", "+", "(", ")", "?", "[", "]", "{", "}", "$", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "^", "|" }; //$NON-NLS-1$ //$NON-NLS-2$
    for (String escape : specialChars) {
      s = s.replace(escape, "\\" + escape); //$NON-NLS-1$
    }
    s = s.replace("*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
    return Pattern.compile(s);
  }

  public Pattern getPattern() {
    return this.pattern;
  }

  public String getString() {
    return this.string;
  }

  public boolean isRegex() {
    return this.isRegex;
  }

}
