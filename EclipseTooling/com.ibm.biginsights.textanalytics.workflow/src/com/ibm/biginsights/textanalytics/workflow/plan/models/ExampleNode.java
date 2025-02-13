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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.editors.SelectionInfo;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.tasks.models.DataFile;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * this class represent an Example node
 * 
 * 
 */
public class ExampleNode extends TreeObject
{

	@SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";
 
	protected Action openEditor;
  protected DataFile datafile;

  protected ExampleModel exampleModel;

  /**
   * @param label
   */
  public ExampleNode (String label)
  {
    super (label, Icons.EXAMPLE_ICON);
    initOpenEditorAction ();
  }

  /**
   * @param model
   */
  public ExampleNode (ExampleModel model)
  {
    super (model.getText (), Icons.EXAMPLE_ICON);
    this.exampleModel = model;
    initOpenEditorAction ();
    datafile = new DataFile (exampleModel.getFileLabel (), exampleModel.getFilePath ());
  }

  public ExampleNode (ExampleModel model, ExamplesFolderNode parent)
  {
    this(model);
    parent.addChild (this);
  }

  /**
   * @return
   */
  public ExampleModel toModel ()
  {
    return exampleModel;
  }

  public SelectionInfo getPair ()
  {
    return new SelectionInfo (exampleModel.getOffset (), exampleModel.getText ());
  }

  /**
	 * 
	 */
  private void initOpenEditorAction ()
  {
    openEditor = new Action () {
      public void run ()
      {
        IEditorPart editor = TaggingEditor.getOpenedEditorWithFile (exampleModel.getFilePath (), exampleModel.getFileLabel ());
        if (editor != null && editor instanceof TaggingEditor) {
          PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().activate (editor);
          ((TaggingEditor) editor).highlight (getPair ());
        }
        else {
          try {
            if (datafile.exists ())
              editor = AqlProjectUtils.openFile (datafile.getInputFile (), TaggingEditor.EDITOR_ID);
          }
          catch (Exception e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (e.getMessage ());
          }

          if (editor != null && editor instanceof TaggingEditor)
            ((TaggingEditor) editor).highlight (getPair ());
        }
        // FIXME handle here if the text has been modified and the
        // offset do
        // not match
      }
    };
  }

  /**
	 * 
	 */
  public void doubleClick ()
  {
    openEditor.run ();
  }

  /**
	 * 
	 */
  public void doclick ()
  {
    TaggingEditor editor = TaggingEditor.getOpenedEditorWithFile (exampleModel.getFilePath(), exampleModel.getFileLabel());
    if (editor != null) {
      PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().activate (editor);

      editor.setHighlightRange (exampleModel.getOffset(), exampleModel.getLength(), true);
      editor.highlight (getPair ());
    }
  }

  public String getFileLabel ()
  {
    return exampleModel.getFileLabel ();
  }

  public String getFilePath ()
  {
    return exampleModel.getFilePath ();
  }

  public String getFileId ()
  {
    return exampleModel.getFilePath() + "___" + exampleModel.getFileLabel();
  }

  public void writeOut (DataOutputStream writeOut) throws IOException
  {

    // label
    byte[] buffer = label.getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // file path
    buffer = exampleModel.getFilePath().getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // file label
    buffer = exampleModel.getFileLabel().getBytes ();
    writeOut.writeInt (buffer.length);
    writeOut.write (buffer);

    // offset
    writeOut.writeInt (exampleModel.getOffset());
    // length
    writeOut.writeInt (exampleModel.getLength());
  }

  public static ExampleNode readIn (DataInputStream readIn) throws IOException
  {
    int size = readIn.readInt ();
    byte[] label = new byte[size];
    readIn.read (label);

    size = readIn.readInt ();
    byte[] filePath = new byte[size];
    readIn.read (filePath);

    size = readIn.readInt ();
    byte[] fileLabel = new byte[size];
    readIn.read (fileLabel);

    int offset = readIn.readInt ();
    int length = readIn.readInt ();

    return new ExampleNode (new ExampleModel (new String (label), new String (filePath), new String (fileLabel),
      offset, length));
  }
}
