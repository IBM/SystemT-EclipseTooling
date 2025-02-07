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
package com.ibm.biginsights.textanalytics.indexer.index;

import java.io.IOException;

import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class IDManager
{



  protected int sequenceID = 100;

  static IDManager instance;

  private IDManager ()
  {

    // TODO -- Need to get the Last generated sequence Id from the file
    // sequenceID

  }

  public static synchronized IDManager getInstance ()
  {
    if (instance == null) {
      instance = new IDManager ();
    }

    return instance;
  }

  public synchronized Integer generateNextSequenceId ()
  {
    return ++sequenceID;
  }

  /**
   * Concatenates the given components by using Constants.QUALIFIED_NAME_SEPARATOR as separator
   * 
   * @param components
   * @return concatenated string
   */
  public String createQualifiedKey (String... components)
  {
    StringBuilder qualifiedKey = new StringBuilder ();

    // apppend all components except the last one
    for (int i = 0; i < components.length - 1; i++) {
      qualifiedKey.append (components[i]).append (Constants.QUALIFIED_NAME_SEPARATOR);
    }

    // append the last component
    qualifiedKey.append (components[components.length - 1]);

    return qualifiedKey.toString ();
  }

  public Integer getReferenceId ()
  {
    Integer refId = generateNextSequenceId ();
    return refId;
  }

  public void write() throws Exception {
		PreferenceStore prefStore = IndexerUtil.getInitailsIndexStore();
		// prefStore.
		prefStore.setValue("sequenceID", sequenceID);
		try {
			prefStore.save();
		} catch (IOException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(
					e.getMessage());

		}
	}

	public void load() {
		PreferenceStore prefStore = IndexerUtil.getInitailsIndexStore();
		int id = prefStore.getInt("sequenceID");
		if (id > 0)
			sequenceID = id;
	}
	
  // public void createElementReference (ElementReference elementReference)
  // {
  // Integer referenceId = elementReference.getElementRefId ();
  // elementReferenceMaster.put (referenceId, elementReference);
  // elementReferenceMasterReverseLookup.put (elementReference, referenceId);
  // }
  //
  // public void createFileTimeStampMaster (String project, String module, String file, Long timeStamp)
  // {
  // Integer fileId = getFileId (project, module, file);
  // lastModifiedTimeStamp.put (fileId, timeStamp);
  // }
  //
  // public void addElementReferenceList (Integer elementId, ElementReference elementReference)
  // {
  // List<ElementReference> elementReferenceList = elementReferenceListMaster.get (elementId);
  // if (elementReferenceList == null) {
  // elementReferenceList = new ArrayList<ElementReference> ();
  // elementReferenceList.add (elementReference);
  // elementReferenceListMaster.put (elementId, elementReferenceList);
  // }
  // else {
  // elementReferenceList.add (elementReference);
  // }
  // }

}
