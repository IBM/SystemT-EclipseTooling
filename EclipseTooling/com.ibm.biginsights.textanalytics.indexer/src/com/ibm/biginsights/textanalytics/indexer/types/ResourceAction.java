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
package com.ibm.biginsights.textanalytics.indexer.types;

public enum ResourceAction {
	//ADDED: This action represents the AQL File / Module / Project added to workspace thru Project Explorer
	//DELETED: This action represents the AQL File / Module / Project deleted to workspace thru Project Explorer
	//RENAMED: This action represents the AQL File / Module / Project renamed to workspace thru Project Explorer
	//Moved: This action represents the AQL File / Module / Project moved thru Project Explorer
	//UPDATED: This represent the change in the content of an AQL file.
	//OPEN : This action is triggered when Prject is Open.
	//CLOSE : This action is triggered when project is closed.
ADDED, DELETED, RENAMED, UPDATED, MOVED, OPEN, CLOSE;



}
