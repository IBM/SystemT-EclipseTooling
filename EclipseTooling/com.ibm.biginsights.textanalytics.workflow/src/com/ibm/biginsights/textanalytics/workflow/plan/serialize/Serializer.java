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
package com.ibm.biginsights.textanalytics.workflow.plan.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;

/**
 * Utility class to serialize and deserialize the models. Models are serialized using JAXB. The methods in this class
 * are not static for implementation reasons, but Serializer objects don't have state and all methods are reentrant.
 */
public class Serializer
{



	private Marshaller marshaller;

  private Unmarshaller unmarshaller;

  private final ILog log;

  /**
   * Create a new serializer. The serializer internally holds a JAXB marshaller and unmarshaller for the model
   * serialization, so it may be worth caching it.
   */
  public Serializer ()
  {
    super ();
    this.log = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance (ActionPlanModel.class);
      this.marshaller = jaxbContext.createMarshaller ();
      // Make the output formatted for human consumption
      this.marshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      this.unmarshaller = jaxbContext.createUnmarshaller ();
    }
    catch (JAXBException e) {
      this.log.logAndShowError (Messages.text_analytics_internal_error_message, e);
    }
  }

  /**
   * Serialize a model to a byte array. The byte array can then be written to disk. Note that the serialization output
   * is UTF-8 encoded. You can create a string from one of the byte arrays with <code>new String(bytes, "UTF-8")</code>.
   * 
   * @param model
   * @return
   */
  public byte[] getBytesForModel (ActionPlanModel model)
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream ();
    try {
      this.marshaller.marshal (model, bos);
    }
    catch (JAXBException e) {
      this.log.logAndShowError (Messages.text_analytics_internal_error_message, e);
    }
    return bos.toByteArray ();
  }

  /**
   * Write a model to an Eclipse workspace file.
   * 
   * @param file A workspace file. The file's parent folder must exist.
   * @param treeObject The model to be serialized.
   * @return <code>true</code> if the operation was successful; <code>false</code> else. This method does not throw
   *         exceptions. Errors are logged to the eclipse log.
   */
  public boolean writeModelToFile (IFile file, ActionPlanModel model)
  {
    try {
      byte[] bytes = getBytesForModel (model);
      ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
      if (!file.exists ())
        file.create (bis, true, new NullProgressMonitor ());
      else
        file.setContents (bis, true, true, new NullProgressMonitor ());
    }
    catch (CoreException e) {
      this.log.logAndShowError (Messages.text_analytics_internal_error_message, e);
      return false;
    }
    return true;
  }

  /**
   * Materialize a model from a byte array.
   * 
   * @param bytes The byte array the represents the model.
   * @return The model, or <code>null</code> in case of error.
   */
  public ActionPlanModel getModelForBytes (byte[] bytes)
  {
    return getModelForInputStream (new ByteArrayInputStream (bytes));
  }

  /**
   * Materialize a model from an input stream.
   * 
   * @param inputStream The input stream to get the model from.
   * @return The model, or <code>null</code> in case of error. This method does not throw exceptions. Errors are logged.
   */
  public ActionPlanModel getModelForInputStream (InputStream inputStream)
  {

    try {
      return (ActionPlanModel) this.unmarshaller.unmarshal (inputStream);
    }
    catch (JAXBException e) {
      this.log.logError (Messages.text_analytics_internal_error_message, e);
      return null;
    }
    finally {
      try {
        inputStream.close ();
      }
      catch (IOException e1) {
        //
      }
    }
  }

}
