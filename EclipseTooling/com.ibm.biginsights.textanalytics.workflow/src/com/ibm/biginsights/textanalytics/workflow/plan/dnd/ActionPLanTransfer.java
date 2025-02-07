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
package com.ibm.biginsights.textanalytics.workflow.plan.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;

public class ActionPLanTransfer extends ByteArrayTransfer
{



  private static ActionPLanTransfer _instance = new ActionPLanTransfer ();
  private static final String MYTYPENAME = "ActionPlanTransfer";

  public static final int AQL = 12345;
  public static final int EXAMPLE = 12346;
  public static final int LABEL = 12347;
  public static final int BASICS = 12348;
  public static final int CANDIDATES = 12349;
  public static final int REFINEMENT = 12351;
  public static final int FINALS = 12352;
  public static final int LABEL_BASICS = 12353;
  public static final int LABEL_CANDIDATES = 12354;
  public static final int LABEL_REFINEMENTS = 12355;
  public static final int LABEL_FINALS = 12356;

  private static final int MYTYPEID = registerType (MYTYPENAME);

  @Override
  protected int[] getTypeIds ()
  {
    return new int[] { MYTYPEID };
  }

  @Override
  protected String[] getTypeNames ()
  {
    return new String[] { MYTYPENAME };
  }

  public static ActionPLanTransfer getInstance ()
  {
    return _instance;
  }

  public void javaToNative (Object object, TransferData transferData)
  {
    if ( ! isJavaObjectTransferable(object)) return;

    if (isSupportedType (transferData)) {
      try {
        // write data to a byte array and then ask super to convert
        // to
        // pMedium
        ByteArrayOutputStream out = new ByteArrayOutputStream ();
        DataOutputStream writeOut = new DataOutputStream (out);

        TreeObject[] selectedObjectArray = (TreeObject[])object;
        for (int i = 0; i < selectedObjectArray.length; i++) {
          if (selectedObjectArray[i] instanceof AqlNode) {
            AqlNode element = (AqlNode) selectedObjectArray[i];
            writeOut.writeInt (AQL);
            element.writeOut (writeOut);
          }
          else if (selectedObjectArray[i] instanceof ExampleNode) {
            ExampleNode element = (ExampleNode) selectedObjectArray[i];
            writeOut.writeInt (EXAMPLE);
            element.writeOut (writeOut);
          }
          else if (selectedObjectArray[i] instanceof LabelNode) {
            LabelNode element = (LabelNode) selectedObjectArray[i];
            writeOut.writeInt (LABEL);
            element.writeOut (writeOut);
          }
        }

        byte[] buffer = out.toByteArray ();
        writeOut.close ();
        super.javaToNative (buffer, transferData);
      }
      catch (IOException e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      }
    }
  }

  public Object nativeToJava (TransferData transferData)
  {
    if (isSupportedType (transferData)) {

      byte[] buffer = (byte[]) super.nativeToJava (transferData);
      if (buffer == null) return null;

      TreeObject[] elements = new TreeObject[0];

      try {
        ByteArrayInputStream in = new ByteArrayInputStream (buffer);
        DataInputStream readIn = new DataInputStream (in);
        while (readIn.available () > 0) {
          TreeObject[] tempElements = new TreeObject[elements.length + 1];
          System.arraycopy (elements, 0, tempElements, 0, elements.length);
          //
          int type = readIn.readInt ();
          switch (type) {
            case AQL:
              tempElements[elements.length] = AqlNode.readIn (readIn);
            break;

            case EXAMPLE:
              tempElements[elements.length] = ExampleNode.readIn (readIn);
            break;

            case LABEL:
              tempElements[elements.length] = LabelNode.readIn (readIn);
            break;
          }
          elements = tempElements;
        }
        readIn.close ();
      }
      catch (IOException ex) {
        return null;
      }
      return elements;
    }

    return null;
  }

  /**
   * We only allow to DnD objects of the same type and the type has to be ExampleNode or LabelNode or AqlNode.
   */
  private boolean isJavaObjectTransferable(Object object)
  {
    if ( object != null &&
         object instanceof TreeObject[] &&
         ((TreeObject[])object).length > 0 )
    {
      TreeObject[] objectArray = (TreeObject[])object;

      Class<?> clazz = objectArray[0].getClass ();

      if (clazz == ExampleNode.class || clazz == LabelNode.class || clazz == AqlNode.class) {

        for (TreeObject to : objectArray) {
          if (to.getClass () != clazz) return false;
        }

        return true;
      }
    }

    return false;
  }
}
