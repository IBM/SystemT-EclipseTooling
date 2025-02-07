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
package com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationTypesModelProvider;
import com.ibm.biginsights.textanalytics.goldstandard.model.GoldStandardModel;
import com.ibm.biginsights.textanalytics.goldstandard.model.OutputViewModel;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Preference page that allows a user to create / edit / delete annotation types
 * 
 *  Krishnamurthy
 *
 */
public class GSAnnotationTypesPage extends GenericPrefPage implements SelectionListener {


	
	private static final int ID_NEW = 1;
	private static final int ID_COPY = 2;
	private static final int ID_DELETE = 3;
	private static final int ID_SAVE = 4;
	
	protected TableViewer tableViewer;
	
	private static final String PROPERTY_FIELDNAME = "PROPERTY_FIELDNAME"; //$NON-NLS-1$
	private static final String PROPERTY_VIEWNAME = "PROPERTY_VIEWNAME"; //$NON-NLS-1$
	private static final String PROPERTY_ENABLED = "PROPERTY_ENABLED"; //$NON-NLS-1$
	private static final String PROPERTY_SHORTCUTKEY = "PROPERTY_SHORTCUTKEY"; //$NON-NLS-1$
	//private static final String PROPERTY_COLOR = "PROPERTY_COLOR"; //$NON-NLS-1$
	
	private static final String[] columnProperties = {PROPERTY_VIEWNAME, PROPERTY_FIELDNAME, PROPERTY_ENABLED, PROPERTY_SHORTCUTKEY};

	public GSAnnotationTypesPage(IProject project, IFolder gsFolder) {
		super(project, gsFolder);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		createButtonBar(composite);
		createAnnotationTypesTable(composite);
		return composite;
	}

	private void createButtonBar(Composite parent) {
		
		Composite buttonBar = new Composite(parent, SWT.NONE);
		buttonBar.setLayout(new RowLayout());
		createButton(buttonBar, Messages.GSAnnotationTypesPage_NEW, "icons/new.gif", ID_NEW); //$NON-NLS-1$
		createButton(buttonBar, Messages.GSAnnotationTypesPage_COPY, "icons/copy.gif", ID_COPY); //$NON-NLS-1$
		createButton(buttonBar, Messages.GSAnnotationTypesPage_DELETE, "icons/delete.gif", ID_DELETE); //$NON-NLS-1$
		createButton(buttonBar, Messages.GSAnnotationTypesPage_SAVE, "icons/save.gif", ID_SAVE); //$NON-NLS-2$
	}
	
	private void createButton(Composite parent, String label, String imagePath, Integer id) {
		Button b = new Button(parent, SWT.FLAT);
		b.setImage(GoldStandardUtil.getImage(imagePath));
		b.setText (label);
		b.setToolTipText(label);
		b.addSelectionListener(this);
		b.setData(id);
		
	}

	private void createAnnotationTypesTable(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent);
		
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		List<AnnotationType> model = AnnotationTypesModelProvider.getInstance(project.getName(), gsFolder.getName()).getAnnotationTypes();
		if(model != null){
			tableViewer.setInput(model);
		}
		
		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);
		
	}

	private void createColumns(Composite parent) {
		String[] headers = new String[] {Messages.GSAnnotationTypesPage_VIEW_NAME, Messages.GSAnnotationTypesPage_FIELD_NAME, Messages.GSAnnotationTypesPage_ENABLED, Messages.GSAnnotationTypesPage_SHORTCUT_KEY};
		
		tableViewer.setColumnProperties(columnProperties);
		
		//column 1: View Name
		TableViewerColumn column = createTableViewerColumn(headers[0], 100);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				AnnotationType annType = (AnnotationType) element;
				return super.getText(annType.getViewName());
			}
			
		});
		
		//column 2: Field name
		column = createTableViewerColumn(headers[1], 100);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				AnnotationType annType = (AnnotationType) element;
				return super.getText(annType.getFieldName());
			}
			
		});
		

		
		//column 2: Enabled
		column = createTableViewerColumn(headers[2], 100);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				AnnotationType annType = (AnnotationType) element;
				return super.getText(annType.isEnabled());
			}
			
		});
		
		//column 3: Shortcut key
		column = createTableViewerColumn(headers[3], 100);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				AnnotationType annType = (AnnotationType) element;
				return super.getText(annType.getShortcutKey());
			}
			
		});
		
//		//column 4: Highlight color
//		column = createTableViewerColumn(headers[4], 100);
//		column.setLabelProvider(new ColumnLabelProvider() {
//
//			@Override
//			public String getText(Object element) {
//				return null;
//			}
//
//			@Override
//			public Color getBackground(Object element) {
//				AnnotationType annType = (AnnotationType) element;
//				return new Color( tableViewer.getTable().getDisplay(), annType.getColor());
//			}
//			
//		});
		
		setCellEditors();
	}

	private void setCellEditors() {
		Table table = tableViewer.getTable();
		CellEditor[] editors = new CellEditor[4];

		//column 1: View name
		TextCellEditor textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).setTextLimit(60);
        editors[0] = textEditor;
        
		//column 2: Annotation type
		textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).setTextLimit(60);
        editors[1] = textEditor;
        
     
        //column 3: Enabled
        ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(table, new String[] {"true", "false"}, SWT.READ_ONLY); //$NON-NLS-1$ //$NON-NLS-2$
        editors[2] = comboEditor;
        
        //column 4: shortcut key
        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).addVerifyListener(
         new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = "0123456789".indexOf(e.text) >= 0;   //$NON-NLS-1$
				
			}
		 });
        editors[3] = textEditor;
        
        
//        //coloumn 5: color
//        editors[4]= new ColorCellEditor(table);
		
        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new AnnotationTypeCellModifier());
		
	}

	private TableViewerColumn createTableViewerColumn(String header, int bounds) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(header);
		column.setWidth(bounds);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public void setFocus() {
		tableViewer.getControl().setFocus();
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Button b = (Button)e.getSource();
		Integer id = (Integer)b.getData();
		Table table = tableViewer.getTable();
		switch(id.intValue()){
		case ID_NEW:
			handleNewButtonClicked(table);
			break;
		
		case ID_COPY:
			handleCopyButtonClicked(table);
			break;

		case ID_DELETE:
			try {
				handleDeleteButtonClicked(table);
			} catch (FileNotFoundException fnfe) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(fnfe.getMessage());
			}
			break;

		case ID_SAVE:
			performApply();
			break;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected void handleNewButtonClicked(Table table){
		int newShortCutKey = table.getItemCount();
		String shortcutKey = (newShortCutKey>=0 && newShortCutKey<=9)? String.valueOf(newShortCutKey):"";
		AnnotationType annotationType = new AnnotationType(Messages.GSAnnotationTypesPage_UNTITLED, Constants.GS_FIELD_MATCH, true, shortcutKey);
		((List<AnnotationType>)tableViewer.getInput()).add(annotationType);
		tableViewer.refresh();
		table.select(table.getItemCount()-1);
	}
	
	@SuppressWarnings("unchecked")
	protected void handleCopyButtonClicked(Table table){
		TableItem items[] = table.getSelection();
		
		for (int i = 0; i < items.length; i++) {
			AnnotationType annType = (AnnotationType)items[i].getData();
			try {
				AnnotationType clone = (AnnotationType) annType.clone();
				((List<AnnotationType>)tableViewer.getInput()).add(clone);
			} catch (CloneNotSupportedException e1) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e1.getMessage());
			}
		}
		tableViewer.refresh();
	}
	
	@SuppressWarnings("unchecked")
	protected void handleDeleteButtonClicked(Table table) throws FileNotFoundException{
		boolean confirmDelete = MessageDialog.openConfirm(getShell(), Messages.GSAnnotationTypesPage_CONFIRM_DELETION, 
				Messages.GSAnnotationTypesPage_CONFIRM_DELETION_MESSAGE);
		AnnotationType annotationType = null;
		if(confirmDelete){
			TableItem tableItems[] = table.getSelection();
			for (int i = 0; i < tableItems.length; i++) {
				annotationType = (AnnotationType) tableItems[i].getData();
				((List<AnnotationType>)tableViewer.getInput()).remove(annotationType);
			}
			tableViewer.refresh();
			
			deleteAnnotationsFromGoldStandard(annotationType);
		}
	}

	private void deleteAnnotationsFromGoldStandard(AnnotationType annotationType) throws FileNotFoundException {
		File gsDir = gsFolder.getLocation().toFile();
		File[] gsFiles = gsDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(Constants.GS_FILE_EXTENSION_WITH_DOT); //$NON-NLS-1$
			}
		});
		
		Serializer ser = new Serializer();
		for (File gsFile : gsFiles) {
			SystemTComputationResult result = ser.getModelForInputStream(new FileInputStream(gsFile));
			GoldStandardModel model = new GoldStandardModel(result);
			OutputViewModel viewModel = model.getOutputViewByViewName(annotationType.getViewName());
			if(viewModel != null){
				String[] fieldNames = viewModel.getFieldNames();
				int fieldIndex = GoldStandardUtil.getFieldIndex(fieldNames, annotationType.getFieldName());
				OutputViewRow[] rows = viewModel.getRows();
				int rowIndex = 0;
				for (int i = 0; i < rows.length; i++) {
					OutputViewRow row = rows[i];
					if(rowContainsSpanWithField(row, fieldIndex)){
						if(fieldNames.length == 1){
							removeRow(viewModel, rowIndex);
							rowIndex--;
						}else{
							removeFieldValue(row, fieldIndex);
						}
					}
					rowIndex++;
				}//end: for each outputview row
				removeFieldName(viewModel, fieldIndex);
				removeFieldType(viewModel, fieldIndex);
				ser.writeModelToFile(gsFolder.getFile(gsFile.getName()), model);
			}
			
		}//end: for each gs file
		
	}

	private void removeFieldType(OutputViewModel viewModel, int fieldIndex) {
		viewModel.removeFieldType(fieldIndex);		
	}

	private void removeFieldName(OutputViewModel viewModel, int fieldIndex) {
		viewModel.removeFieldName(fieldIndex);
	}

	private void removeFieldValue(OutputViewRow row, int fieldIndex) {
		FieldValue[] fVals = row.fieldValues;
		FieldValue[] modifiedFVals = new FieldValue[fVals.length - 1];
		int index = 0;
		for (int i = 0; i < fVals.length; i++) {
			if(i != fieldIndex){
				modifiedFVals[index] = fVals[i];
				index++;
			}
		}
		row.fieldValues = modifiedFVals;
	}

	private void removeRow(OutputViewModel viewModel, int index) {
		viewModel.removeRow(index);
	}

	private boolean rowContainsSpanWithField(OutputViewRow row,	int fieldIndex) {
		FieldValue[] fVals = row.fieldValues;
		if(fVals == null || fVals.length==0){
			return false;
		}
		return (fieldIndex < fVals.length);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void performApply() {
		if(tableViewer != null){
			String strAnnotationTypes = ""; //$NON-NLS-1$
			
			@SuppressWarnings("unchecked")
			List<AnnotationType> model = (List<AnnotationType>) tableViewer.getInput();
			boolean valid = validateModel(model);
			if(valid){
				for (Iterator<AnnotationType> iterator = model.iterator(); iterator.hasNext();) {
					
					AnnotationType annotationType = (AnnotationType) iterator.next();
					strAnnotationTypes += annotationType.toString()+";";			 //$NON-NLS-1$
				}
				
				getPreferenceStore().setValue(PARAM_ANNOTATION_TYPES, strAnnotationTypes);
				super.performApply();
			}
		}
	}



	@Override
	public boolean performOk() {
		if(tableViewer != null){
			@SuppressWarnings("unchecked")
			List<AnnotationType> model = (List<AnnotationType>) tableViewer.getInput();
			boolean valid = validateModel(model);
			if(!valid){
				return false;
			}
		}
		//super.performApply();
		return super.performOk();
	}

	private boolean validateModel(List<AnnotationType> model) {
		int n = model.size();
		for (int i = 0; i < n-1; i++) {
			String shortcutKey1 = model.get(i).getShortcutKey();
			String viewFieldName1 = model.get(i).getViewName() + model.get(i).getFieldName();
			for(int j = i+1; j < n; j++){
				String shortcutKey2 = model.get(j).getShortcutKey();
				String viewFieldName2 = model.get(j).getViewName() + model.get(j).getFieldName();
				if((viewFieldName1.equals(viewFieldName2)
						|| (!StringUtils.isEmpty(shortcutKey1) && shortcutKey1.equals(shortcutKey2)))){
					CustomMessageBox errBox = CustomMessageBox.createErrorMessageBox(getShell(), Messages.GSAnnotationTypesPage_ERROR, 
					Messages.GSAnnotationTypesPage_DUPLICATE_ANNOTATION_TYPE_EXISTS);
					errBox.open();
					performDefaults();
					return false;
				}
			}
		}
		return true;
	}
	
//	private boolean duplicateExists(List<AnnotationType> model, AnnotationType input){
//		if(model == null || input == null){
//			return false;
//		}
//		for (AnnotationType annotationType : model) {
//			if(annotationType.getShortcutKey() == input.getShortcutKey()){
//				return true;
//			}
//			
//			String viewNameFieldName1 = annotationType.getViewName()+annotationType.getFieldName();
//			String viewNameFieldName2 = input.getViewName()+input.getFieldName();
//			if(viewNameFieldName1.equals(viewNameFieldName2)){
//				return true;
//			}
//		}
//		
//		return false;
//	}



	private class AnnotationTypeCellModifier implements ICellModifier{

		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}

		@Override
		public Object getValue(Object element, String property) {
			AnnotationType annType = (AnnotationType) element;
			if(PROPERTY_FIELDNAME.equals(property)){
				return annType.getFieldName();
			}if(PROPERTY_VIEWNAME.equals(property)){
				return annType.getViewName();
			}else if(PROPERTY_ENABLED.equals(property)){
				return (annType.isEnabled()? new Integer(0): new Integer(1));
			}else if(PROPERTY_SHORTCUTKEY.equals(property)){
				return String.valueOf(annType.getShortcutKey());
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public void modify(Object element, String property, Object value) {
			TableItem tableItem = (TableItem)element;
			
			AnnotationType annType = (AnnotationType) tableItem.getData();
			
			if(PROPERTY_FIELDNAME.equals(property)){
				annType.setFieldName((String)value);
			}else if(PROPERTY_VIEWNAME.equals(property)){
				annType.setViewName((String)value);
			}else if(PROPERTY_ENABLED.equals(property)){
				boolean enabled = ((Integer)value).intValue() ==0 ? true: false; 
				annType.setEnabled(enabled);
			}else if(PROPERTY_SHORTCUTKEY.equals(property)){
				annType.setShortcutKey(value.toString());
			}
			
			tableViewer.refresh();
			
		}
		
	}

	@Override
	protected void refreshData() {
		// TODO Auto-generated method stub
		
	}
}
