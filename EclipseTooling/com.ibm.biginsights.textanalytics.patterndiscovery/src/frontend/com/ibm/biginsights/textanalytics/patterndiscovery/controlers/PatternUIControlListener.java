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
package com.ibm.biginsights.textanalytics.patterndiscovery.controlers;

import java.applet.AppletContext;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import prefuse.Display;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryApplet;

/**
 * control user interface events for the pattern discovery applet
 * 
 * 
 */
public class PatternUIControlListener extends ControlAdapter implements Control
{



  String databaseName = "";
  AppletContext appletContext;
  Display display;
  PatternDiscoveryApplet applet;

  public void setApplet (PatternDiscoveryApplet applet)
  {
    this.applet = applet;
  }

  public void setDatabaseName (String databaseName)
  {
    this.databaseName = databaseName;
  }

  public void setDisplay (Display display)
  {
    this.display = display;
  }

  public void setAppletContext (AppletContext appletContext)
  {
    this.appletContext = appletContext;
  }

  /**
   * handle the event of a user clicking in a bubble
   */
  @Override
  public void itemClicked (VisualItem item, MouseEvent e)
  {
    switch (e.getModifiers ()) {

      // Left button pressed. Show another circular layout.
      case InputEvent.BUTTON1_MASK: {
        if (item instanceof NodeItem) {
          String jsignature = (String) item.get ("OriginalSignature");
          applet.itemClicked (jsignature);
        }
        break;
      }

        // Right button pressed
      case InputEvent.BUTTON3_MASK: {
        if (item instanceof NodeItem) {
          String signature = (String) item.get ("OriginalSignature");

          JPopupMenu jpub = new JPopupMenu ();

          JMenuItem seeHistory = new JMenuItem ("* See History");
          seeHistory.setFont (new Font ("Arial", Font.BOLD, 10));
          seeHistory.addMouseListener (new SeePatternHistoryListener (signature, applet.getJob ()));

          jpub.add (seeHistory);
          jpub.show (e.getComponent (), e.getX (), e.getY ());
        }
        break;
      }
    }
  }

  /**
   * handle the event of a user moving over the bubble
   */
  @Override
  public void itemEntered (VisualItem item, MouseEvent e)
  {
    if (item instanceof NodeItem) {
      String signature = ((String) item.get ("PreviousSignature"));
      int size = (Integer) item.get ("Size");

      JPopupMenu jpub = new JPopupMenu ();
      jpub.add ("Pattern: " + signature);
      jpub.add ("Size: " + size);

      jpub.show (e.getComponent (), e.getX (), e.getY ());
    }
  }

}
