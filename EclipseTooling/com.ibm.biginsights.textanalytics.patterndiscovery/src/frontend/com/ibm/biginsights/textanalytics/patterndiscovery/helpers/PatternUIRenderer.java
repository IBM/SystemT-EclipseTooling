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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

/**
 * render a bubble with a size based on the number of elements it groups
 * 
 * @see AbstractShapeRenderer
 * 
 */
public class PatternUIRenderer extends AbstractShapeRenderer
{



  protected static double diameter = 25;

  public void setDiameter (double adiameter)
  {
    diameter = adiameter;
  }

  @Override
  protected Shape getRawShape (VisualItem item)
  {
    int s = (Integer) item.get ("Size");
    double size = Math.log (s) * Math.log (s) * 10 + diameter;
    return new Ellipse2D.Double (item.getX (), item.getY (), size, size);
  }
}
