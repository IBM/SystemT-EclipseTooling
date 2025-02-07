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

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

/**
 * defines the layout used by the applet that display the bubbles
 * 
 * @see Layout
 * 
 */
public class PatternUIDecoratorLayout extends Layout
{



  public PatternUIDecoratorLayout (String group)
  {
    super (group);
  }

  @Override
  public void run (double frac)
  {
    @SuppressWarnings("unchecked")
    Iterator<DecoratorItem> iter = m_vis.items (m_group);
    while (iter.hasNext ()) {
      DecoratorItem decorator = iter.next ();
      VisualItem decoratedItem = decorator.getDecoratedItem ();
      Rectangle2D bounds = decoratedItem.getBounds ();

      double x = bounds.getCenterX ();
      double y = bounds.getCenterY ();

      setX (decorator, null, x);
      setY (decorator, null, y);
    }
  }

}
