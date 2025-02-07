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
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import java.awt.Graphics2D;

import prefuse.render.LabelRenderer;
import prefuse.visual.VisualItem;

/**
 * defines the renderer for the labels displayed in the bubbles
 * 
 * @see LabelRenderer
 * 
 */
public class PatternUILabelRenderer extends LabelRenderer
{



  public PatternUILabelRenderer (String field)
  {
    super (field);
  }

  @Override
  public void render (Graphics2D paramGraphics2D, VisualItem item)
  {
    item.set ("Label", getBetterLabel (item));
    super.render (paramGraphics2D, item);
  }

  /**
   * makes sure that we do display a label that makes sense in radio's size of the bubble. if the text is two long we
   * just display the size of the bubble, the user still can see the pattern by mouse overing it
   * 
   * @param item
   * @return
   */
  private String getBetterLabel (VisualItem item)
  {
    int size = (Integer) item.get ("Size");
    String text = (String) item.get ("Label");

    double radio = Math.log (size) * Math.log (size) * 10 + PatternUIRenderer.diameter;

    String txtToDisplay = (radio > (text.length () * 6) && radio > 30) ? text : String.format ("[%d]", size);

    return txtToDisplay;
  }
}
