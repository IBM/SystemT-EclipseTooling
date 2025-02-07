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

package com.ibm.biginsights.textanalytics.concordance.ui;

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

public class SerializeObject {



	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	@SuppressWarnings("rawtypes")
	private Class classname;
	
	public SerializeObject(@SuppressWarnings("rawtypes") Class classname) {
		this.classname = classname;
		this.init();
	}
	
	public void init() {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(classname);
			if(this.marshaller == null) {
				this.marshaller = jaxbContext.createMarshaller();
				// Make the output formatted for human consumption
				this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
						Boolean.TRUE);
				this.unmarshaller = jaxbContext.createUnmarshaller();
			}
		} catch (JAXBException e) {
			System.out.println(e);
		}
		
	}
	
	public boolean writeToFile(IFile file, Object obj) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			if (file.exists()) {
				file.delete(true, new NullProgressMonitor());
			}

			this.marshaller.marshal(obj, bos);

			ByteArrayInputStream bis = new ByteArrayInputStream(
					bos.toByteArray());
			
			file.create(bis, true, new NullProgressMonitor());

		} catch (JAXBException e) {
			System.out.println(e);
			return false;
		} catch (CoreException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	  public Object getObjectForStream(InputStream inputStream) {
		  
		    try {
		      return this.unmarshaller.unmarshal(inputStream);
		    } catch (JAXBException e) {
		      return null;
		    }
		    finally
		    {
		    	try
		    	{
		    		inputStream.close();
		    	}
		    	catch(IOException e1)
		    	{
		    		//
		    	}
		    }
		  }
}
