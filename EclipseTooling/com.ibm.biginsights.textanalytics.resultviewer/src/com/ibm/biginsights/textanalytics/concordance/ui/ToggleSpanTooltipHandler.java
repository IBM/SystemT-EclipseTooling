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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

public class ToggleSpanTooltipHandler extends AbstractHandler implements
		IElementUpdater, ISelectionChangedListener {

	@SuppressWarnings("unused")


	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the setting from the Preference plugin
		final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		final boolean enableTooltip = wprefs.getPrefToggleSpanToolTip ();
		wprefs.setPrefToggleSpanToolTip (!enableTooltip);
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window
				.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.resultviewer.commands.toggleSpanTooltip",
							null);//$NON-NLS-1$
		}
		return null;
	}

	public void updateElement(UIElement element, Map parameters) {
		final ImageDescriptor toolTipOn = AbstractUIPlugin
				.imageDescriptorFromPlugin(
						"com.ibm.biginsights.textanalytics.resultviewer",
						"icons/tog_tooltip_on.gif");//$NON-NLS-1$ //$NON-NLS-2$

		final ImageDescriptor toolTipOff = AbstractUIPlugin
				.imageDescriptorFromPlugin(
						"com.ibm.biginsights.textanalytics.resultviewer",
						"icons/tog_tooltip_off.gif");//$NON-NLS-1$ //$NON-NLS-2$

		final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		final boolean enableTooltip = wprefs.getPrefToggleSpanToolTip ();

		if (enableTooltip) {
			element.setIcon(toolTipOn);
			element.setTooltip(Messages.spanTooltipDisabled);
		} else {
			element.setIcon(toolTipOff);
			element.setTooltip(Messages.spanTooltipEnabled);
		}
	}

	protected void requestRefresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window
				.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.resultviewer.commands.enableToolTip",
							null);//$NON-NLS-1$
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//Do Nothing
	}

}
