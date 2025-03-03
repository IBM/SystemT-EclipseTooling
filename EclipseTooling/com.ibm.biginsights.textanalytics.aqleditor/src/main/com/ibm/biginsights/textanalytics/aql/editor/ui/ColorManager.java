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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * ColorManager as generated.  This should be fine for now.
 */
public class ColorManager {



  protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

  public void dispose() {
    Iterator<Color> e = this.fColorTable.values().iterator();
    while (e.hasNext()) {
      e.next().dispose();
    }
  }

  public Color getColor(RGB rgb) {
    Color color = this.fColorTable.get(rgb);
    if (color == null) {
      color = new Color(Display.getCurrent(), rgb);
      this.fColorTable.put(rgb, color);
    }
    return color;
  }
}
