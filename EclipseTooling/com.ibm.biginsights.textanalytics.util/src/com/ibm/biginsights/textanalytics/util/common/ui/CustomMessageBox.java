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
package com.ibm.biginsights.textanalytics.util.common.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class is to provide a non-editable multiline text field in the dialog area, so that info, error, warn messages can be 
 * copied to clip board by the user. The dialog shall have an icon identifying the type of message(info, error, warn), followed by
 * a non-editable, but copyable text field. The bottom row of the message box shall have an OK button. 
 * 
 * 
 *
 */
public class CustomMessageBox extends MessageDialog {



	protected Text tfMessage;
	protected int messageType;

	private CustomMessageBox(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell,dialogTitle, dialogTitleImage,dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex );
		this.messageType = dialogImageType;
	}

	public static CustomMessageBox createInfoMessageBox(
			Shell parentShell, String dialogTitle,String dialogMessage){
		return new CustomMessageBox(parentShell, dialogTitle, null, dialogMessage, MessageDialog.INFORMATION, new String[] {"OK"}, 0);
	}
	
	public static CustomMessageBox createErrorMessageBox(
			Shell parentShell, String dialogTitle,String dialogMessage){
		return new CustomMessageBox(parentShell, dialogTitle, null, dialogMessage, MessageDialog.ERROR, new String[] {"OK"}, 0);
	}
	
	public static CustomMessageBox createWarnMessageBox(
			Shell parentShell, String dialogTitle,String dialogMessage){
		return new CustomMessageBox(parentShell, dialogTitle, null, dialogMessage, MessageDialog.WARNING, new String[] {"OK"}, 0);
	}
            
	public String getMessage() {
		return message;
	}

	@Override
	protected Control createMessageArea(Composite composite) {

		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER)
					.applyTo(imageLabel);
		}
		// create message
		tfMessage = new Text(composite,  SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

		tfMessage.setText(message);
		GridDataFactory
				.fillDefaults()
				.align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.hint(
						convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
						SWT.DEFAULT).applyTo(tfMessage);
		return composite;
	}

	@Override
	protected Button createButton (Composite parent, int id, String label, boolean defaultButton)
	{
	  Button okButton = super.createButton (parent, id, label, defaultButton);
	  tfMessage.setFocus ();
	  return okButton;
	}
	
	public void setMessage(String message) {
		super.message = message;
		tfMessage.setText(message);
	}

	public void setTitle(String title){
		getShell().setText(title);
	}

	public void setMessageType(int messageType){
		this.messageType = messageType;
	}
	
	public Image getImage() {
        switch (messageType) {
	        case ERROR:
	            return getErrorImage();
	        case INFORMATION:
	        	return getInfoImage();
	        case QUESTION:
	            return getQuestionImage();
	        case WARNING:
	            return getWarningImage();
        }
        return null;
	}
}
