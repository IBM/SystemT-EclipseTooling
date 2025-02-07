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
package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class FileDifferencesSideBySideModel
{


  
	public FileDifferencesSideBySideModel (IFile rightFile, IFile leftFile,
    SystemTComputationResult leftModel, SystemTComputationResult rightModel,
    ArrayList<SpanVal> newSpansInLeftFile, ArrayList<SpanVal> oldSpansInRightFile,
    ArrayList<SpanVal> unchangedSpansInLeftFiles, ArrayList<SpanVal> unchangedSpansInRightFiles,
    ArrayList<SpanVal> overlappingSpansInLeftFile, ArrayList<SpanVal> overlappingSpansInRightFile)
  {
    super ();
    this.rightFile = rightFile;
    this.leftFile = leftFile;
    this.leftModel = leftModel;
    this.rightModel = rightModel;
    this.newSpansInLeftFile = newSpansInLeftFile;
    this.oldSpansInRightFile = oldSpansInRightFile;
    this.unchangedSpansInLeftFiles = unchangedSpansInLeftFiles;
    this.unchangedSpansInRightFiles = unchangedSpansInRightFiles;
    this.overlappingSpansInLeftFile = overlappingSpansInLeftFile;
    this.overlappingSpansInRightFile = overlappingSpansInRightFile;
  }

  private IFile rightFile = null;

  public IFile getRightFile ()
  {
    return rightFile;
  }

  public IFile getLeftFile ()
  {
    return leftFile;
  }

  public SystemTComputationResult getLeftModel ()
  {
    return leftModel;
  }

  public SystemTComputationResult getRightModel ()
  {
    return rightModel;
  }

  public ArrayList<SpanVal> getNewSpansInLeftFile ()
  {
    return newSpansInLeftFile;
  }

  public ArrayList<SpanVal> getOldSpansInRightFile ()
  {
    return oldSpansInRightFile;
  }

  public ArrayList<SpanVal> getUnchangedSpansInLeftFiles ()
  {
    return unchangedSpansInLeftFiles;
  }

  public ArrayList<SpanVal> getUnchangedSpansInRightFiles ()
  {
    return unchangedSpansInRightFiles;
  }

  public ArrayList<SpanVal> getOverlappingSpansInLeftFile ()
  {
    return overlappingSpansInLeftFile;
  }

  public ArrayList<SpanVal> getOverlappingSpansInRightFile ()
  {
    return overlappingSpansInRightFile;
  }

  private IFile leftFile = null;
  private SystemTComputationResult leftModel = null;
  private SystemTComputationResult rightModel = null;
  private ArrayList<SpanVal> newSpansInLeftFile = null;
  private ArrayList<SpanVal> oldSpansInRightFile = null;
  private ArrayList<SpanVal> unchangedSpansInLeftFiles = null;
  private ArrayList<SpanVal> unchangedSpansInRightFiles = null;
  private ArrayList<SpanVal> overlappingSpansInLeftFile = null;
  private ArrayList<SpanVal> overlappingSpansInRightFile = null;

}
