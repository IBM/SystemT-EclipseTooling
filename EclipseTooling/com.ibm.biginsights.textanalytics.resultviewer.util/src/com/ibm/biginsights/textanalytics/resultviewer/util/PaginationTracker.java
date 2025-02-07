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
package com.ibm.biginsights.textanalytics.resultviewer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * This class is for allowing paginated results in the Annotation Explorer. One instance of the PaginationTracker is
 * maintained for one run. It maintains the currentPage, filesPerPageCount and also returns the files that belong to a
 * page.
 * 
 *  Madiraju
 */
public class PaginationTracker
{
	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  /* Only one instance is maintained per execution, hence a singleton */
  private static PaginationTracker trackerInstance = null;

  /* The total number of pages calculated based on total number of results and the files-per-page-count */
  private int totalNumberOfPages = 0;

  /* Maintains the files-per-page-count. This number is configurable based on the properties */
  private int filesPerPageCount;

  /* Maintains the index of the current page being displayed */
  private int currentPage;

  /* This has a handle to the result folder. */
  private IFolder resultFolder = null;

  /* This is stored for one execution so that it need not be calculated for every page */
  private Object provParams = null;

  public Object getProvParams ()
  {
    return provParams;
  }

  public void setProvParams (Object provParams)
  {
    this.provParams = provParams;
  }

  /* Private constructor to allow for singleton */
  private PaginationTracker ()
  {
  }

  /* This allows for only one instance of the PaginationTracker to be available on a run */
  public static PaginationTracker getInstance ()
  {
    if (trackerInstance == null) {
      trackerInstance = new PaginationTracker ();
    }
    return trackerInstance;
  }

  /* Getter method for result folder */
  public IFolder getResultFolder ()
  {
    return resultFolder;
  }

  /**
   * Sets the result folder being tracked by Pagination tracker and updates the field tracking the total number of pages
   * in that folder.
   * 
   * @param resultFolder The directory to be tracked
   * @throws CoreException
   */
  public void setResultFolder (IFolder resultFolder) throws CoreException
  {
    int numOfFiles = resultFolder.members ().length;
    setResultFolder (resultFolder, numOfFiles);
  }
  
  /**
   * Sets the result folder being tracked by Pagination tracker and updates the field tracking the total number of pages
   * in that folder. Number of files in the result folder is explicitly set in order to avoid expensive api calls to
   * retrieve the files in result folder and calculate file count.
   * 
   * @param resultFolder The directory to be tracked
   * @param numOfFiles The number of files in that directory
   * @throws CoreException
   */
  public void setResultFolder (IFolder resultFolder, int numOfFiles) throws CoreException
  {
    this.resultFolder = resultFolder;
    
    if (filesPerPageCount <= 0) {
      this.totalNumberOfPages = 1;
      return;
    }
    else if (filesPerPageCount > numOfFiles) {
      filesPerPageCount = numOfFiles;
    }
    // Removed a line that retrieved a filtered list of strf files from result directory, for the sake of improving
    // memory consumption.
    // Since it is a system generated folder, it can be assumed that it will contain only strf files and hence we can
    // rely on variable numOfFiles.
    // This change was done as a part of defect 33744.
    this.totalNumberOfPages = (numOfFiles / filesPerPageCount) + (numOfFiles % filesPerPageCount > 0 ? 1 : 0); // ceiling(resfile.size/filesperpagecount)
  }

  /**
   * This method returns the files for a particular page  
   */
  public ArrayList<IFile> getFiles (int pageNo) throws CoreException
  {
    // Reads all the result files
    List<IFile> resFiles = getResultFiles ();
    if (filesPerPageCount <= 0) { return (ArrayList<IFile>) resFiles; }
    IFile[] resFilesArray = new IFile[resFiles.size ()];
    resFiles.toArray (resFilesArray);

    // Creates a return list of files - corresponding to that page.
    ArrayList<IFile> returnList = new ArrayList<IFile> ();
    int index = (pageNo - 1) * filesPerPageCount;

    // Return the filesPerPageCount number of files or number of files remaining, whichever is lesser
    for (int i = index; (returnList.size () < filesPerPageCount) && (i < resFiles.size ()); i++) {
      returnList.add (resFilesArray[i]);
    }

    // nulling them to reduce memory consumption
    resFiles = null;
    resFilesArray = null;

    return returnList;
  }

  /**
   * This returns all the result files of the execution
   * 
   * @return
   * @throws CoreException
   */
  public List<IFile> getResultFiles () throws CoreException
  {
    final List<IFile> resFileList = new ArrayList<IFile> ();
    if (resultFolder != null) {
      final IResource[] resources = resultFolder.members ();
      for (final IResource resource : resources) {
        if ((resource.getType () == IResource.FILE)) {
          if ((resource.getName ().startsWith (Constants.ALL_DOCS) == false)
            && resource.getName ().endsWith (Constants.STRF_FILE_EXTENSION)) {
            resFileList.add ((IFile) resource);
          }
        }
      }
    }
    return resFileList;
  }

  /**
   * Returns all the result filenames.
   * 
   * @return
   * @throws CoreException
   */
  public List<String> getResultFilenames ()
  {
    final List<String> resFilenameList = new ArrayList<String> ();

    if (resultFolder != null && resultFolder.exists ()) {
      File jFolder = resultFolder.getLocation ().toFile ();
      String[] childNames = jFolder.list ();
      for (String child : childNames) {
        if (child.startsWith (Constants.ALL_DOCS) == false &&
            child.endsWith (Constants.STRF_FILE_EXTENSION) == true) {
          resFilenameList.add (child);
        }
      }
    }

    return resFilenameList;
  }

  /**
   * The files per page count is a fixed static number (for an execution) that shows the number of files being displayed
   * per page.
   * 
   * @return
   */
  public int getFilesPerPageCount ()
  {
    return filesPerPageCount;
  }

  /**
   * The files per page count is a fixed static number (for an execution) that shows the number of files being displayed
   * per page. This is set once, when the TA project is executed from the SystemTRunJob method or from the
   * ShowResultInAnnotationExplorer
   * 
   * @param pageCount
   */
  public void setFilesPerPageCount (int fppCount)
  {
    this.filesPerPageCount = fppCount;
  }

  /**
   * Getter method for current page.
   * 
   * @return
   */
  public int getCurrentPage ()
  {
    return currentPage;
  }

  /**
   * Setter method for current page
   * 
   * @param currentPage
   */
  public void setCurrentPage (int currentPage)
  {
    this.currentPage = currentPage;
  }

  /**
   * Getter method for total number of pages.
   * 
   * @return
   */
  public int getTotalNumberOfPages ()
  {
    return totalNumberOfPages;
  }

}
