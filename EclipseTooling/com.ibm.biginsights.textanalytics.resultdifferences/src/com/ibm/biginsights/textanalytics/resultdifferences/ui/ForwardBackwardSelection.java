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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;

public class ForwardBackwardSelection implements ISelectionChangedListener, SelectionListener {



	protected transient AnalysisResultExplorer explorer;

	private Button forward;
	
	private Button backward;
	
	private Button backwardToBegin;
	
	private Button forwardToEnd;
	
	private Combo numCombo;
	
	private Label pageLabel;

	private ISelectionChangedListener provider1;
	
	private ISelectionChangedListener provider2;
	
	public ForwardBackwardSelection(AnalysisResultExplorer aExplorer, 
			ISelectionChangedListener prov1, 
			ISelectionChangedListener prov2){
		this.explorer=aExplorer;
		this.provider1=prov1;
		this.provider2=prov2;
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
	    // if the selection in the tree on the left side changed, then the 
		// values for the pages have to be recalculated and redrawn.
        setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
	    // go through all the buttons and check, which are still working
		if (explorer.endEnabled()){				
			this.forwardToEnd.setEnabled(true);
		} else {
			this.forwardToEnd.setEnabled(false);
		}
		if (explorer.beginEnabled()){
			this.backwardToBegin.setEnabled(true);
		} else {
			this.backwardToBegin.setEnabled(false);
		}
		if (explorer.backwardEnabled()){
			this.backward.setEnabled(true);
		} else {
			this.backward.setEnabled(false);
		}
		if (explorer.forwardEnabled()){
			this.forward.setEnabled(true);
		} else {
			this.forward.setEnabled(false);
		}
	}
	
	
	public void createForwardBackward (FormToolkit toolkit, Composite parent){
		
		
		// create all the necessary buttons and comobo boxes and use this class as 
		// selection listener, also for the tree changes the clas will be set as selection change listener
                
		// first create the new composite to position all the buttons and labels in
		Composite comp= toolkit.createComposite(parent);        
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.END;        
        comp.setLayoutData(gd);       
		GridLayout layout= new GridLayout(8, false);		
		layout.marginHeight=0;		
		layout.horizontalSpacing=1;
		comp.setLayout(layout);

		// the page number counter is in a label
		pageLabel= toolkit.createLabel(comp, NLS.bind(
			          Messages.getString("AnalysisResultsView_ShownRange"), new Object[]{  //$NON-NLS-1$
			            new Integer(0), new Integer(0), 
			            new Integer(0)}));
	    
		GridData data = new GridData();		
	    pageLabel.setLayoutData(data);	    	           

        data = new GridData();
        // this is necessary to have enought space for the page numbers
        data.horizontalIndent=60;
        Label l= toolkit.createLabel(comp, Messages.getString("AnalysisResultsView_PageMessage")+ ": "); //$NON-NLS-1$ //$NON-NLS-2$
        l.setLayoutData(data);
        
        numCombo = new Combo(comp, SWT.SINGLE|SWT.READ_ONLY);        
        data = new GridData();
        numCombo.setLayoutData(data);                
        numCombo.add("20"); //$NON-NLS-1$
        numCombo.add("50"); //$NON-NLS-1$        
        numCombo.add("100"); //$NON-NLS-1$        
	    numCombo.setText(numCombo.getItem(0));
	    explorer.setSelectionSize(20);
        numCombo.addSelectionListener(this);         	   
	    toolkit.adapt(numCombo);


        data = new GridData();
        data.horizontalIndent=10;
	    backwardToBegin= new Button(comp, SWT.PUSH);
	    backwardToBegin.setImage(Activator.getImage("back_to_begin.gif")); //$NON-NLS-1$
        backwardToBegin.addSelectionListener(this); 	    
	    backwardToBegin.setLayoutData(data);
	    
	    backward= new Button(comp, SWT.PUSH);
	    backward.setImage(Activator.getImage("back.gif")); //$NON-NLS-1$
	    backward.addSelectionListener(this);
	    
	    forward= new Button(comp,SWT.PUSH);
	    forward.setImage(Activator.getImage("forward.gif")); //$NON-NLS-1$
	    forward.addSelectionListener(this);

	    forwardToEnd= new Button(comp,SWT.PUSH);
	    forwardToEnd.setImage(Activator.getImage("forward_to_end.gif")); //$NON-NLS-1$
	    forwardToEnd.addSelectionListener(this);
	    // go through all the buttons and check, which are still working
		if (explorer.endEnabled()){				
			this.forwardToEnd.setEnabled(true);
		} else {
			this.forwardToEnd.setEnabled(false);
		}
		if (explorer.beginEnabled()){
			this.backwardToBegin.setEnabled(true);
		} else {
			this.backwardToBegin.setEnabled(false);
		}
		if (explorer.backwardEnabled()){
			this.backward.setEnabled(true);
		} else {
			this.backward.setEnabled(false);
		}
		if (explorer.forwardEnabled()){
			this.forward.setEnabled(true);
		} else {
			this.forward.setEnabled(false);
		}

	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
		// nothing to do
	}

	// react to selections the user makes in the comboboxes
	// which contain analysis results to choose
	public void widgetSelected(SelectionEvent e) {

	   if (e.getSource().equals(forward)) {
	        explorer.goForward();	        
			this.provider1.selectionChanged(new SelectionChangedEvent(
						explorer, explorer.getSelection()));
			this.provider2.selectionChanged(new SelectionChangedEvent(
						explorer, explorer.getSelection()));					
			setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
		} else if (e.getSource().equals(forwardToEnd)) {
	        explorer.goEnd();	        
	        this.provider1.selectionChanged(new SelectionChangedEvent(
						explorer, explorer.getSelection()));
	        this.provider2.selectionChanged(new SelectionChangedEvent(
						explorer, explorer.getSelection()));					
			setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
  	    } else if (e.getSource().equals(backward)) {
            explorer.goBackward();            
            this.provider1.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));
            this.provider2.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));
		    setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
  	    } else if (e.getSource().equals(backwardToBegin)) {
            explorer.goBegin();            
            this.provider1.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));
            this.provider2.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));
		    setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
	    } else if (e.getSource().equals(numCombo)) {
			// get the number of elements per page
			String num = numCombo.getItem(numCombo.getSelectionIndex());
			Integer i=Integer.parseInt(num);
			explorer.setSelectionSize(i.intValue());
			this.provider1.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));
			this.provider2.selectionChanged(new SelectionChangedEvent(
					explorer, explorer.getSelection()));						
			setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
	    }
	   	
	    // go through all the buttons and check, which are still working
		if (explorer.endEnabled()){				
			this.forwardToEnd.setEnabled(true);
		} else {
			this.forwardToEnd.setEnabled(false);
		}
		if (explorer.beginEnabled()){
			this.backwardToBegin.setEnabled(true);
		} else {
			this.backwardToBegin.setEnabled(false);
		}
		if (explorer.backwardEnabled()){
			this.backward.setEnabled(true);
		} else {
			this.backward.setEnabled(false);
		}
		if (explorer.forwardEnabled()){
			this.forward.setEnabled(true);
		} else {
			this.forward.setEnabled(false);
		}

	}
	
	 public void setPageRange(int start, int end, int max){	   
		   pageLabel.setText(NLS.bind(
			          Messages.getString("AnalysisResultsView_ShownRange"), new Object[]{  //$NON-NLS-1$
			            new Integer(start), new Integer(end), 
			            new Integer(max)}));
		   
	       pageLabel.setSize(pageLabel.getSize());
	       // make sure the label will be resized accordingly
	       pageLabel.pack();
	   }
}
