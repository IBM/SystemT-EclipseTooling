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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;

/**
 * The fastest way to insert data into Derby is by 
 * loading a CSV file. Therefore, this class provides a 
 * convenient way of doing so. 
 * 
 * TODO: test vs. batch insert
 * TODO: specify and implement
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class FileBasedInsert implements Insert {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	static int fileNr = 0;
	int myFileNr;
	String myFileName;
	private DebugDBProcessor db;
	private BufferedWriter bw;
	int myArity=-1;
	
	//TODO:constructor
	public FileBasedInsert(DebugDBProcessor db, String scratchDirectory) throws IOException{
		//TODO: generate Unique filename
		myFileName = scratchDirectory+"/dbInsertionFile-"+fileNr+".csv";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(myFileName),
				GroupByNewProcessor.ENCODING));
		myFileNr = fileNr;
		fileNr++;
		this.db = db;
	}
	//insert
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.util.Insert#insert(java.lang.String)
	 */
	public void insert(String... values) throws IOException{
		if(myArity==-1){
			myArity = values.length;
		}
		if(myArity!=values.length){
			throw new RuntimeException("all inserted values need to be of the same arity");
		}
		String line = "";
		for (String string : values) {
			if(line.length()>0) line += ",";
			line+=string;
		}
		bw.write(line+"\n");
	}
	//doneInserting
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.util.Insert#doneInserting()
	 */
	public void doneInserting() throws IOException{
		bw.close();
	}
	//flush
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.util.Insert#flush()
	 */
	public void flush() throws IOException{
		bw.flush();
	}
	//load
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.util.Insert#load(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void load(String schema, String targetTable, String columnDefinitions) throws SQLException, IOException{
		bw.close();
		db.importCSV(schema, targetTable, columnDefinitions, "NULL", "NULL", new File(myFileName), false);
	}
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.util.Insert#finalize()
	 */
	@Override
	public void finalize(){
		try {
			bw.close();
		} catch (IOException e) {
		}
		(FileUtils.createValidatedFile(myFileName)).delete();
		
	}
}
