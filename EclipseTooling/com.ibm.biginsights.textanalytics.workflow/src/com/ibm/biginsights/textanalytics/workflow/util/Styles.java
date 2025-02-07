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
package com.ibm.biginsights.textanalytics.workflow.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class Styles {


	
	public static final Font LABEL_FONT = SWTResourceManager.getFont("Tahoma",
			10, SWT.BOLD);
	public static final Font TABS_FONT = SWTResourceManager.getFont("Tahoma",
			11, SWT.BOLD);
	public static final Color TABS_BG = SWTResourceManager
			.getColor(SWT.COLOR_TITLE_BACKGROUND);
	public static final Color TAB_SELECTED_BG = SWTResourceManager
			.getColor(SWT.COLOR_LIST_SELECTION);
	public static final Color DEFAULT_BG = SWTResourceManager
			.getColor(SWT.COLOR_WIDGET_BACKGROUND);
	
	public static final Color ATTENTION_RED = SWTResourceManager.getColor(255, 0, 0);
}
