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
package com.ibm.biginsights.textanalytics.resultviewer.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.biginsights.textanalytics.resultmodel.Activator;
import com.ibm.biginsights.textanalytics.resultmodel.Messages;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

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
      jaxbContext = JAXBContext.newInstance (SystemTComputationResult.class);
      this.marshaller = jaxbContext.createMarshaller ();
      // Make the output formatted for human consumption
      this.marshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      this.unmarshaller = jaxbContext.createUnmarshaller ();
    }
    catch (JAXBException e) {
      this.log.logAndShowError (Messages.getString ("SerializerInternalError"), e); //$NON-NLS-1$
    }
  }

  /**
   * Serialize a model to a byte array. The byte array can then be written to disk. Note that the serialization output
   * is UTF-8 encoded. You can create a string from one of the byte arrays with <code>new String(bytes, "UTF-8")</code>.
   * 
   * @param model
   * @return
   */
  public byte[] getBytesForModel (SystemTComputationResult model)
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream ();
    try {

      // The code below encodes the texts of the model to escape
      // the invalid xml characters.
      // There are Unicode characters such as 0x1d that are not
      // XML compatible (usually these are invisible characters). Unfortunately, JAXB (which we use to
      // serialize/deserialize extracted results) writes these on disk without complaining but refuses to read
      // them back.
      Map<Integer, String> textMap = model.getTextMap ();
      Map<Integer, String> newTextMap = new HashMap<Integer, String> ();
      Iterator<Integer> keysIter = textMap.keySet ().iterator ();
      while (keysIter.hasNext ()) {
        Integer key = keysIter.next ();
        String text = textMap.get (key);
        String modifiedText = StringUtils.escapeInvalidXMLChars (model.getDocumentID (), text);
        newTextMap.put (key, modifiedText);
      }
      model.setTextMap (newTextMap);

      this.marshaller.marshal (model, bos);
    }
    catch (JAXBException e) {
      this.log.logAndShowError (Messages.getString ("SerializerInternalError"), e); //$NON-NLS-1$
    }
    finally {
      try {
        if(bos != null){
          bos.close ();
        }
      }
      catch (IOException e) {
        // do nothing
      }
    }
    return bos.toByteArray ();
  }

  /**
   * Write a model to an Eclipse workspace file.
   * 
   * @param file A workspace file. The file's parent folder must exist.
   * @param model The model to be serialized.
   * @return <code>true</code> if the operation was successful; <code>false</code> else. This method does not throw
   *         exceptions. Errors are logged to the eclipse log.
   */
  public boolean writeModelToFile (IFile file, SystemTComputationResult model)
  {
    ByteArrayInputStream bis = null;
    try {
      if (file.exists ()) {
        file.delete (true, new NullProgressMonitor ());
      }
      byte[] bytes = getBytesForModel (model);
      bis = new ByteArrayInputStream (bytes);
      file.create (bis, true, new NullProgressMonitor ());
    }
    catch (CoreException e) {
      this.log.logAndShowError (Messages.getString ("SerializerInternalError"), e); //$NON-NLS-1$
      return false;
    }finally{
      try {
        bis.close ();
      }
      catch (IOException e) {
        this.log.logError (e.getMessage ());
      }
    }
    return true;
  }

  /**
   * Materialize a model from a byte array.
   * 
   * @param bytes The byte array the represents the model.
   * @return The model, or <code>null</code> in case of error.
   */
  public SystemTComputationResult getModelForBytes (byte[] bytes)
  {
    return getModelForInputStream (new ByteArrayInputStream (bytes));
  }

  /**
   * Materialize a model from an input stream.
   * 
   * @param inputStream The input stream to get the model from.
   * @return The model, or <code>null</code> in case of error. This method does not throw exceptions. Errors are logged.
   */
  public SystemTComputationResult getModelForInputStream (InputStream inputStream)
  {

    try {
      return (SystemTComputationResult) this.unmarshaller.unmarshal (inputStream);
    }
    catch (JAXBException e) {
      this.log.logError (Messages.getString ("SerializerInternalError"), e); //$NON-NLS-1$
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
