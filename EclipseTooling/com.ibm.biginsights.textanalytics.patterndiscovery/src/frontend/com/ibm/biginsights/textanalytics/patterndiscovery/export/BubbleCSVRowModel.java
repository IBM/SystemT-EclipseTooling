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
package com.ibm.biginsights.textanalytics.patterndiscovery.export;

/**
 * representation of a bubble for pattern discovery
 * 
 *
 */
public class BubbleCSVRowModel
{


 
	String bubbleSize;
  String bubblePattern;
  String originalPattern;
  String context;
  String dataFile;

  private static final String[] columns = { "Group Size", "Common Pattern", "Semantic Pattern", "Context", "Data File" };

  /**
   * @param bubbleSize
   * @param bubblePattern
   * @param originalPattern
   * @param context
   * @param dataFile
   */
  public BubbleCSVRowModel (String bubbleSize, String bubblePattern, String originalPattern, String context,
    String dataFile)
  {
    super ();
    this.bubbleSize = bubbleSize;
    this.bubblePattern = bubblePattern;
    this.originalPattern = originalPattern;
    this.context = context;
    this.dataFile = dataFile;
  }

  /**
   * 
   * @return
   */
  public static String[] getColumnLabels ()
  {
    return columns;
  }

  @Override
  public String toString ()
  {
    StringBuffer sb = new StringBuffer ();

    sb.append (String.format ("Bubble Size: %s, ", bubbleSize));
    sb.append (String.format ("Bubble Pattern: %s, ", bubblePattern));
    sb.append (String.format ("Original Pattern: %s, ", originalPattern));
    sb.append (String.format ("Context: %s, ", context));
    sb.append (String.format ("Data File: %s", dataFile));

    return sb.toString ();
  }

  /**
   * 
   * @return
   */
  public String[] toStringArray ()
  {
    String[] strArr = { bubbleSize, bubblePattern, originalPattern, context, dataFile };

    return strArr;
  }

  /**
   * @return the bubbleSize
   */
  public String getBubbleSize ()
  {
    return bubbleSize;
  }

  /**
   * @param bubbleSize the bubbleSize to set
   */
  public void setBubbleSize (String bubbleSize)
  {
    this.bubbleSize = bubbleSize;
  }

  /**
   * @return the bubblePattern
   */
  public String getBubblePattern ()
  {
    return bubblePattern;
  }

  /**
   * @param bubblePattern the bubblePattern to set
   */
  public void setBubblePattern (String bubblePattern)
  {
    this.bubblePattern = bubblePattern;
  }

  /**
   * @return the originalPattern
   */
  public String getOriginalPattern ()
  {
    return originalPattern;
  }

  /**
   * @param originalPattern the originalPattern to set
   */
  public void setOriginalPattern (String originalPattern)
  {
    this.originalPattern = originalPattern;
  }

  /**
   * @return the context
   */
  public String getContext ()
  {
    return context;
  }

  /**
   * @param context the context to set
   */
  public void setContext (String context)
  {
    this.context = context;
  }

  /**
   * @return the dataFile
   */
  public String getDataFile ()
  {
    return dataFile;
  }

  /**
   * @param dataFile the dataFile to set
   */
  public void setDataFile (String dataFile)
  {
    this.dataFile = dataFile;
  }

}
