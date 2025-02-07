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

import java.io.IOException;
import java.sql.SQLException;

public interface Insert {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	//insert
	public abstract void insert(String... values) throws IOException, SQLException;

	//doneInserting
	public abstract void doneInserting() throws IOException, SQLException;

	//flush
	public abstract void flush() throws IOException, SQLException;

	//load
	public abstract void load(String schema, String targetTable,
			String columnDefinitions) throws SQLException, IOException;

	public abstract void finalize();

}
