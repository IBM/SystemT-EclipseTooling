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
package com.ibm.biginsights.textanalytics.resultdifferences.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;

/**
 * Utility class for creating a drop down button that provides expand actions
 * for TreeViewers
 * 
 * 
 * 
 */

public class ActionUtilities {



	/**
	 * @param treeViewer
	 *            The TreeViewer to expand the tree complete
	 * @return An IAction representing an collapse buttons
	 */
	public static IAction createTreeCollapse(
			final IResultsView view) {

		final IAction collapseAllAction = new Action(Messages.getString("ActionUtilities_CollapseAll"), //$NON-NLS-1$
				IAction.AS_PUSH_BUTTON) {
			public void run() {
				view.getActiveTreeViewer().collapseAll();
			}
		};
		collapseAllAction.setActionDefinitionId("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.CollapseAll");
		collapseAllAction.setImageDescriptor(Activator
				.getImageDescriptor("org.eclipse.search",  //$NON-NLS-1$
						"icons/full/elcl16/collapseall.gif")); //$NON-NLS-1$
		collapseAllAction.setToolTipText(Messages.getString("ActionUtilities_CollapseAll")); //$NON-NLS-1$
		collapseAllAction.setEnabled(true);
		return collapseAllAction;
	}
 
	/**
	 * @param treeViewer
	 *            The TreeViewer to expand the tree complete
	 * @return An IAction representing an expand buttons
	 */
	public static IAction createTreeExpansion(
			final IResultsView view) {

		final IAction expandAllAction = new Action(Messages.getString("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.ExpandAll"), //$NON-NLS-1$
				IAction.AS_PUSH_BUTTON) {
			public void run() {
				view.getActiveTreeViewer().expandAll();
			}
		};
		expandAllAction.setActionDefinitionId("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.ExpandAll");
		expandAllAction.setImageDescriptor(Activator
				.getImageDescriptor("expandall.gif")); //$NON-NLS-1$
		expandAllAction.setToolTipText(Messages.getString("ActionUtilities_ExpandAll")); //$NON-NLS-1$
		expandAllAction.setEnabled(true);
		return expandAllAction;
	}

	/**
	 * @param treeViewer
	 *            The TreeViewer to expand items of
	 * @return An IAction representing a drop down button with expand buttons
	 */
	public static IAction createTreeExpansionDropDown(
			final TreeViewer treeViewer) {

		final IAction expandAllAction = new Action(Messages.getString("ActionUtilities_ExpandAll"), //$NON-NLS-1$
				IAction.AS_DROP_DOWN_MENU) {
			public void run() {
				treeViewer.expandAll();
			}
		};

		expandAllAction.setImageDescriptor(Activator
				.getImageDescriptor("expandall.gif")); //$NON-NLS-1$
		expandAllAction.setToolTipText(Messages.getString("ActionUtilities_ExpandAll")); //$NON-NLS-1$
		expandAllAction.setMenuCreator(new IMenuCreator() {

			private Menu fMenu;

			public void dispose() {
				if (fMenu != null) {
					fMenu.dispose();
				}
			}

			public Menu getMenu(final Control parent) {
				if (fMenu != null) {
					fMenu.dispose();
				}

				fMenu = new Menu(parent);

				final IAction collapseAllAction = new Action(
						Messages.getString("ActionUtilities_ExpandToFirstLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						treeViewer.collapseAll();
					}
				};

				final IAction expandToSecondLevelAction = new Action(
						Messages.getString("ActionUtilities_ExpandToSecondLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						treeViewer.expandToLevel(2);
					}
				};

				final IAction expandToThirdLevelAction = new Action(
						Messages.getString("ActionUtilities_ExpandToThirdLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						treeViewer.expandToLevel(3);
					}
				};

				new ActionContributionItem(collapseAllAction).fill(fMenu, -1);
				new ActionContributionItem(expandToSecondLevelAction).fill(
						fMenu, -1);
				new ActionContributionItem(expandToThirdLevelAction).fill(
						fMenu, -1);
				new ActionContributionItem(expandAllAction).fill(fMenu, -1);
				return fMenu;
			}

			public Menu getMenu(final Menu parent) {
				return null;
			}

		});

		return expandAllAction;
	}
	
	/**
	 * @param treeViewer
	 *            The TreeViewer to expand items of
	 * @return An IAction representing a drop down button with expand buttons
	 */
	public static IAction createTreeExpansionDropDown(
			final IResultsView view) {
		
		final IAction expandAllAction = new Action(Messages.getString("ActionUtilities_ExpandAll"), //$NON-NLS-1$
				IAction.AS_DROP_DOWN_MENU) {
			public void run() {
				view.getActiveTreeViewer().expandAll();
			}
		};

		expandAllAction.setImageDescriptor(Activator
				.getImageDescriptor("expandall.gif")); //$NON-NLS-1$
		expandAllAction.setToolTipText(Messages.getString("ActionUtilities_ExpandAll")); //$NON-NLS-1$
		expandAllAction.setMenuCreator(new IMenuCreator() {

			private Menu fMenu;

			public void dispose() {
				if (fMenu != null) {
					fMenu.dispose();
				}
			}

			public Menu getMenu(final Control parent) {
				if (fMenu != null) {
					fMenu.dispose();
				}

				fMenu = new Menu(parent);

				final IAction collapseAllAction = new Action(
						Messages.getString("ActionUtilities_ExpandToFirstLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						view.getActiveTreeViewer().collapseAll();
					}
				};

				final IAction expandToSecondLevelAction = new Action(
						Messages.getString("ActionUtilities_ExpandToSecondLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						view.getActiveTreeViewer().expandToLevel(2);
					}
				};

				final IAction expandToThirdLevelAction = new Action(
						Messages.getString("ActionUtilities_ExpandToThirdLevel"), IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
					public void run() {
						view.getActiveTreeViewer().expandToLevel(3);
					}
				};

				new ActionContributionItem(collapseAllAction).fill(fMenu, -1);
				new ActionContributionItem(expandToSecondLevelAction).fill(
						fMenu, -1);
				new ActionContributionItem(expandToThirdLevelAction).fill(
						fMenu, -1);
				new ActionContributionItem(expandAllAction).fill(fMenu, -1);
				return fMenu;
			}

			public Menu getMenu(final Menu parent) {
				return null;
			}

		});

		return expandAllAction;
	}
}
