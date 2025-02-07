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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public class AQLViewer extends ProjectionViewer {


    
    public static final String CONTEXT_VIEWER = "com.ibm.biginsights.textanalytics.aql.editor.ui.viewer"; //$NON-NLS-1$
    public static final String CONTEXT_POSITION = "com.ibm.biginsights.textanalytics.aql.editor.ui.position"; //$NON-NLS-1$    
    
    public AQLViewer( Composite parent,
                       IVerticalRuler ruler,
                       IOverviewRuler overviewRuler,
                       boolean showsAnnotationOverview,
                       int styles)
    {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
    }
    
    public int getCursorOffset()
    {   
        StyledText styledText = getTextWidget();
        return widgetOffset2ModelOffset( styledText.getCaretOffset() );
    }

    protected IFormattingContext createFormattingContext()
    {
        IFormattingContext context = super.createFormattingContext();
        
        context.setProperty( CONTEXT_VIEWER, this );
        context.setProperty( CONTEXT_POSITION, Integer.valueOf( getCursorOffset() ) );
        
        //
        // HACK
        //
        clearRememberedSelection();
        
        return context;
    }

}
