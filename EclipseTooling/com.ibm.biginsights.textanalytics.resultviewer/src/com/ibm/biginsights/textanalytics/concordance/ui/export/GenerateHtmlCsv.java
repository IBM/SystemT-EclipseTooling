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
package com.ibm.biginsights.textanalytics.concordance.ui.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.export.model.TableData;
import com.ibm.biginsights.textanalytics.concordance.ui.export.model.TableView;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

public class GenerateHtmlCsv
{
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+         //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	/** HTML that marks the beginning of a table of tuples. */
	private static final String TABLE_BEGIN = "<table class=\"aqltups\">"; //$NON-NLS-1$

	/** HTML that marks the beginning of an annotation. */
	private static final String ANNOT_BEGIN_MARKER = "<font color = gray>[</font>"; //$NON-NLS-1$

	private static final String COLOR_BEGIN1 = "<font color = #8833bb><b>"; //$NON-NLS-1$

	private static final String COLOR_BEGIN2 = "<font color = #aa22aa><b>"; //$NON-NLS-1$

	private static final String COLOR_BEGIN3 = "<font color = #bb1111><b>"; //$NON-NLS-1$

	private static final String COLOR_BEGIN4 = "<font color = #ee0000><b>"; //$NON-NLS-1$

	private static final String COLOR_BEGIN5 = "<font color = #ff0000><b>"; //$NON-NLS-1$

	/** HTML that marks the end of an annotation. */
	private static final String ANNOT_END_MARKER = "<font color = gray>]</font>"; //$NON-NLS-1$

	private static final String COLOR_END = "</b></font>"; //$NON-NLS-1$

	private static final String INPUT_DOCUMENT = "Input Document"; //$NON-NLS-1$

	/**
	 * Maximum length of the strings returned by {@link #shorten(String)}; must
	 * be even.
	 */
	private static final int SHORTEN_STR_LEN = 100;

	private String csvDirectory;
	private String htmlDirectory;
	private Writer out;
	private Map<Integer, String> sourceIdTextMap = new HashMap<Integer, String> ();

	public GenerateHtmlCsv (String exportDirPath)
  {
    createExportSubDirectories (exportDirPath);
  }

  /**
	 * Generate HTML and CSV results for all views.
	 * 
	 * @param model
	 */
  public void generateForAllViews (IConcordanceModel model)
              throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    generateForAllViews (model, true);
  }

	/**
	 * Generate HTML and CSV Results for a view.
	 * 
	 * @param viewModel
	 */
  public void generateView (IAQLTableViewModel viewModel)
              throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    generateView (viewModel, true);
  }

  /**
   * Generate HTML and CSV results for all views.
   * 
   * @param model
   * @param closeOutput TRUE: Add closing tags for html output files.
   *                    FALSE: Caller is responsible for adding closing tags to end of html output files.
   */
  public void generateForAllViews (IConcordanceModel model, boolean closeOutput)
              throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    String viewNames[] = model.getOutputViewNames ();
    for (int i = 0; i < viewNames.length; i++) {
      generateView (model.getViewModel (viewNames[i]), closeOutput);
    }
  }

  /**
   * Generate HTML and CSV Results for a view.
   * 
   * @param viewModel
   * @param closeOutput TRUE: Add closing tags for html output files.
   *                    FALSE: Caller is responsible for adding closing tags to end of html output files.
   */
  public void generateView (IAQLTableViewModel viewModel, boolean closeOutput)
              throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    Map<String, TableView> viewMap = buildTableView (viewModel);
    write (viewModel.getName (), viewMap, closeOutput);
  }

	/**
	 * Writes the results to files.
	 * 
	 * @param viewName
	 * @param viewMap
   * @param closeOutput TRUE: Add closing tags for html output files.
   *                    FALSE: Caller is responsible for adding closing tags to end of html output files.
	 */
	private void write(String viewName, Map<String, TableView> viewMap, boolean closeOutput)
			         throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
		dumpToCSV (viewName, viewMap);
		dumpToHTML (viewName, viewMap, closeOutput);
	}

	/**
	 * Creates html and CSV directories
	 * 
	 * @param directoryPath The path where exported results are generated.
	 */
	public void createExportSubDirectories(String directoryPath) {
		File csvDir = FileUtils.createValidatedFile(directoryPath, Constants.CSV_DIR);
		if (csvDir.exists ())
		  FileUtils.deleteDirectory (csvDir);

		csvDir.mkdir();

		File htmlDir = FileUtils.createValidatedFile(directoryPath, Constants.HTML_DIR);
    if (htmlDir.exists ())
      FileUtils.deleteDirectory (htmlDir);

    htmlDir.mkdir();

		this.csvDirectory = csvDir.getPath();
		this.htmlDirectory = htmlDir.getPath();
	}

	/**
	 * Build Model to generate the HTM nad CSV. Returns a Map of TableView. The
	 * Key for the map is the document name and TableView contains document name
	 * , doc contents, header and Tuples for the view.
	 * 
	 * @param viewModel
	 * @param viewName
	 */
	private Map<String, TableView> buildTableView(IAQLTableViewModel viewModel)
	{
	  String viewName = viewModel.getName ();

	  String[] header;
		String[] tempHeader;
		TableView view;
		Map<String, TableView> viewMap;

		String docName;
		TableData[] cell;
		String cellStr;
		FieldValue cellFV;
		tempHeader = viewModel.getHeaders();

		// Last two strings in the header will be 'Input Document' and
		// 'Double-click this column to explain a tuple !'
		// Above string would be present only if provenance params were
		// provided to the ConcordanceModel that created this AQLTableViewModel
		// refer ConcordanceModel.getViewModel(String)
		if (tempHeader[tempHeader.length - 1].equals(Constants.PROVENANCE_BUTTON_LABEL)) {
		  header = new String[tempHeader.length - 2];
		} else {
		  header = new String[tempHeader.length - 1];
		}
		System.arraycopy(tempHeader, 0, header, 0, header.length);

		IRow rows[] = viewModel.getElements();
		viewMap = new HashMap<String, TableView>();

		for (int j = 0; j < rows.length; j++) {
			docName = rows[j].getInputDocName();
			view = viewMap.get(docName);
			if (view == null) {
				view = new TableView();
				view.setDocName(docName);
				view.setViewName(viewName);
				view.setViewSchema(header);
				view.setCellValue(new LinkedList<TableData[]>());
				viewMap.put(docName, view);
			}

			
			cell = new TableData[header.length];
			for (int j2 = 0; j2 < header.length; j2++) {
				cellStr = rows[j].getLabelForCell(j2);
				cellFV = rows[j].getValueForCell (j2);
				cell[j2] = new TableData(0, 0, cellStr);
        if (cellFV instanceof SpanVal) {
          int openBracket = cellStr.lastIndexOf ('[');    //$NON-NLS-1$
          int closeBracket = cellStr.lastIndexOf (']');   //$NON-NLS-1$
          int hyphen = cellStr.lastIndexOf ('-');         //$NON-NLS-1$
          if (openBracket >= 0 && closeBracket >= 0 && hyphen >= 0 &&
              openBracket < hyphen && hyphen < closeBracket) {
            String text = cellStr.substring (0, openBracket).trim ();
            String start = cellStr.substring (openBracket + 1, hyphen).trim ();
            String end = cellStr.substring (hyphen + 1, closeBracket).trim ();

            /**
		  			 * We need to traverse thru all the Cells to get the Source Text for the last Span.
			  		 * This Source Text will be used in HTML Highlighting. The cell may be of type 
				  	 * ListVal, TextVal or SpanVal. We get sourceId for the last SpanVal by this traversal and
					   * get the text for that source id.
             */
            int sourceId = rows[j].getSourceId (j2);
            if (sourceId != -1) {
              view.setSourceId (sourceId);
              if (sourceIdTextMap.get (Integer.valueOf (sourceId)) == null)
                sourceIdTextMap.put (Integer.valueOf (sourceId), rows[j].getSourceText (sourceId));
            }

            try {
              int startPos = Integer.parseInt (start);
              int endPos = Integer.parseInt (end);
              cell[j2] = new TableData (startPos, endPos, text);
            }
            catch (NumberFormatException e) {
              // cell[j2] will be displayed as-is
            }
          }
        }
			}
			
			view.getCellValue().add(cell);
		}
		return viewMap;
	}

	/**
	 * Writes results to CSV.
	 * 
	 * @param fileName
	 * @param viewMap
	 */
	private void dumpToCSV(String viewName, Map<String, TableView> viewMap)
			         throws IOException
  {
		String[] record;
		TableView view = null;
		String docName = null;
		boolean isHeaderSet = false;
		CSVWriter csvWriter = null;
		try {
      File file = FileUtils.createValidatedFile (csvDirectory, getCsvFilenameForView (viewName));

      // Each file is for one view. If it is already containing some results,
      // it must already have the header row.
      if (file.exists () && file.length () > 0)
        isHeaderSet = true;

			csvWriter = new CSVWriter(new FileWriter(file, true));

			for (Iterator<String> iterator = viewMap.keySet().iterator(); iterator
					.hasNext();) {
				docName = iterator.next();

				view = viewMap.get(docName);

				if (!isHeaderSet) {
					String header[] = view.getViewSchema();
					record = new String[header.length + 1];
					record[0] = INPUT_DOCUMENT; //$NON-NLS-1$
					System.arraycopy(header, 0, record, 1, header.length);
					csvWriter.writeNext(record);
					isHeaderSet = true;
				}
				List<TableData[]> fieldValueList = view.getCellValue();
				for (Iterator<TableData[]> iterator2 = fieldValueList.iterator(); iterator2
						.hasNext();) {
					TableData[] spans = iterator2.next();

					record = new String[spans.length + 1];
					record[0] = docName;
					for (int i = 0; i < spans.length; i++) {
						record[i + 1] = spans[i].toString();
					}

					csvWriter.writeNext(record);
				}
			}
		}
		finally {
			csvWriter.close();
		}
	}

	/**
	 * Writes results to HTML.
	 * 
	 * @param fileName
	 * @param viewName
	 * @param viewMap
   * @param closeOutput TRUE: Add closing tags for html output files.
   *                    FALSE: Caller is responsible for adding closing tags to end of html output files.
	 */
	private void dumpToHTML(String viewName, Map<String, TableView> viewMap, boolean closeOutput)
			         throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
		try {
			openHtmlFileForView(viewName);
			String table = genHTMLBodyPart(viewMap);
			if (StringUtils.isEmpty (table) == false) {
			  out.write(table);
			  out.write("\n"); //$NON-NLS-1$
			}
		}
		finally {
			out.close();
		}

    if (closeOutput)
      writeFooter(getHtmlFilenameForView (viewName));
	}

	/**
	 * Opens a stream for writing to a html output file for a view.
	 * 
	 * @param viewName
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
  private void openHtmlFileForView(String viewName)
	             throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    File file = FileUtils.createValidatedFile (htmlDirectory, getHtmlFilenameForView (viewName));
    out = new OutputStreamWriter (new FileOutputStream (file, true), Constants.ENCODING);
    if (file.exists () == false || file.length () == 0)
      writeHtmlHeader(viewName);
  }

  /**
   * Opens a stream for writing to a html output file for a view.
   * 
   * @param viewName
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void openHtmlFile(String fileName)
               throws UnsupportedEncodingException, FileNotFoundException, IOException
  {
    File file = FileUtils.createValidatedFile (htmlDirectory, fileName);
    out = new OutputStreamWriter (new FileOutputStream (file, true), Constants.ENCODING);
  }

	/**
	 * Write closing html tag and close the output file.
	 */
	private void writeFooter(String fileName) throws IOException
	{
	  openHtmlFile (fileName);
		out.write("\n</body>\n</html>\n"); //$NON-NLS-1$
		out.close ();
	}

  /**
   * Flush buffers and close the output file
   */
  public void writeAllFooters() throws IOException
  {
    File htmlFolder = FileUtils.createValidatedFile (htmlDirectory);
    String[] htmlFiles = htmlFolder.list ();
    for (String fileName : htmlFiles) {
      if (fileName.endsWith (Constants.HTML_EXTENSION) == false)
        continue;

      writeFooter (fileName);
    }
  }

  private String getCsvFilenameForView (String viewName)
  {
    return viewName + Constants.CSV_EXTENSION;
  }

  private String getHtmlFilenameForView (String viewName)
  {
    return viewName + Constants.HTML_EXTENSION;
  }

	/**
	 * Creates Header for HTML
	 * 
	 * @param viewName
	 * @throws IOException
	 */
	private void writeHtmlHeader(String viewName) throws IOException {
		out.write("<html>\n"); //$NON-NLS-1$

		out.write("<META http-equiv=\"Content-Type\""
				+ " content=\"text/html; charset=UTF-8\">\n"); //$NON-NLS-1$

		out.write("<head>\n"); //$NON-NLS-1$

		// Only generate CSS for tables if we're actually generating tables.
		out.write("<style type=\"text/css\">\n" + "    table.aqltups {\n"
				+ "        background-color: lightgray;\n"
				+ "        width: 100%;\n" + "    }\n" + "    th {\n"
				+ "        background-color: darkslategray;\n"
				+ "        color: white;\n" + "    }\n" + "</style>\n"); //$NON-NLS-1$

		out.write("<title>" + viewName + "</title>\n" + "</head>\n"
				+ "<body>\n"); //$NON-NLS-1$
	}

	/**
	 * Replaces the Special char for HTML
	 * 
	 * @param orig
	 * @return
	 */
	private String escapeHTMLSpecials(String orig) {
		if (0 == orig.length()) {
			return orig;
		}

		String ret;

		// Need to do the ampersands first
		ret = orig.replace("&", "&amp;"); //$NON-NLS-1$
		ret = ret.replace("<", "&lt;"); //$NON-NLS-1$
		ret = ret.replace(">", "&gt;"); //$NON-NLS-1$

		return ret;
	}

	/**
	 * Generates the Body for HTML
	 * 
	 * @param viewMap
	 * @return
	 */
	private String genHTMLBodyPart(Map<String, TableView> viewMap) {

		StringBuilder sb = new StringBuilder();

		TableView view = null;
		String docName = null;

		for (Iterator<String> iterator = viewMap.keySet().iterator(); iterator
				.hasNext();) {
			docName = iterator.next();

			sb.append(String.format("\n\n<h1>Document '%s'</h1>\n\n", docName)); //$NON-NLS-1$

			view = viewMap.get(docName);
			sb.append(TABLE_BEGIN);

			sb.append("\n<tr>\n"); //$NON-NLS-1$
			String header[] = view.getViewSchema();
			for (int col = 0; col < header.length; col++)
				sb.append(String.format("    <th>%s</th>\n", header[col])); //$NON-NLS-1$
			sb.append("</tr>\n"); //$NON-NLS-1$

			List<TableData[]> fieldValueList = view.getCellValue();
			for (Iterator<TableData[]> iterator2 = fieldValueList.iterator(); iterator2
					.hasNext();) {
				TableData[] spans = iterator2.next();
				sb.append("<tr>\n"); //$NON-NLS-1$
				for (int i = 0; i < spans.length; i++) {
					sb.append(String.format("   <td>%s</td>\n", spans[i])); //$NON-NLS-1$
				}
				sb.append("</tr>\n"); //$NON-NLS-1$
			}

			// Close out the table
			sb.append("</table>\n<br><br>\n"); //$NON-NLS-1$

			String marked = markAnnotationsInDoc(view, false);
			sb.append(marked);
			sb.append("\n<br><br>\n"); //$NON-NLS-1$

		}

		return sb.toString();

	}

	/**
	 * Sorts the TableData Object based on start index.
	 */
	  private class SpanComparator implements Comparator<TableData>
	  {

	    @Override
	    public int compare(TableData o1, TableData o2) {
	    {
	    	return o1.getStart() - o2.getStart();
	      }
	    }

	  }
	/**
	 * Highlight the document with Last Span column
	 * 
	 * @param view
	 * @param isSnip
	 * @return
	 */
	private String markAnnotationsInDoc(TableView view, boolean isSnip) {
		String schema[] = view.getViewSchema();
		int lastSpan = -1;
		for (int i = 0; i < schema.length; i++) {
			if (schema[i].indexOf("(SPAN)") != -1) //$NON-NLS-1$
				lastSpan = i;
		}
		if (lastSpan == -1)
			return ""; //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		List<TableData> spans = new ArrayList<TableData>();
		List<TableData[]> tempList = null;
		tempList = view.getCellValue();
		
		for (TableData[] spansArr : tempList) {
			TableData data = spansArr[lastSpan];
			// TableData may contains Spans that can be empty.
			// We should not process the highlighting of document
			// for empty spans.
			if(data.getText() != null && !data.getText().isEmpty())
				spans.add(data);
		}
		
		// The highlighting of document starts from the beginning
		// of the document. Sorts the Span based on start index so
		// that we highlight the text based on start index in the
		// document.
		Collections.sort(spans, new SpanComparator());

		// Walk through the document, keeping track of the spans that cover each
		// character position.
		ArrayList<TableData> curSpans = new ArrayList<TableData>();

		// Spans are sorted by begin.
		Iterator<TableData> jtr = spans.iterator();
		TableData nextSpan = null;
		if (jtr.hasNext()) {
			nextSpan = jtr.next();
		}

		// Keep track of which portion of the document we've generated output
		// for.
		int outPos = 0;
		String text = sourceIdTextMap.get(Integer.valueOf(view.getSourceId()));
		for (int pos = 0; pos < text.length(); pos++) {
			// Check whether one or more active annotations end at this point in
			// the document.
			// Note that we go backwards through the arraylist so that we can
			// remove things in place.
			for (int i = curSpans.size() - 1; i >= 0; i--) {
				TableData s = curSpans.get(i);

				if (s.getEnd() == pos) {
					curSpans.remove(s);

					// Output any additional text that falls within s.
					String toAppend = text.substring(outPos, s.getEnd());
					toAppend = escapeHTMLSpecials(toAppend);
					sb.append(toAppend);

					// Put in markers that will later be replaced with some
					// HTML.
					sb.append("END_OF_COLOR"); //$NON-NLS-1$
					sb.append("END_OF_ANNOT"); //$NON-NLS-1$

					if (curSpans.size() > 0) {
						// Still inside an annotation...
						sb.append(String.format("BEGIN_OF_COLOR%d",
								Math.min(curSpans.size(), 5))); //$NON-NLS-1$
					}
					outPos = s.getEnd();
				}
			}

			// Check to see whether an annotation becomes active at this point
			// in the document.
			while (null != nextSpan && nextSpan.getStart() == pos) {

				if (0 == curSpans.size()) {
					// We're not currently in an annotation, so we should output
					// the "between annotations" text in shortened form.
					String toAppend = text.substring(outPos,
							nextSpan.getStart());

					if (isSnip) {
						toAppend = (String) shorten(toAppend, SHORTEN_STR_LEN,
								false);
					}

					// Escape problem characters
					toAppend = escapeHTMLSpecials(toAppend);
					sb.append(toAppend);

				} else {

					// We're currently in an annotation, so we need to add
					// what's been annotated so far, then stop and
					// restart the highlighting.
					String toAppend = text.substring(outPos,
							nextSpan.getStart());

					// Escape any ampersands in the text.
					toAppend = escapeHTMLSpecials(toAppend);

					sb.append(toAppend);
					sb.append("END_OF_COLOR"); //$NON-NLS-1$
				}
				outPos = pos;

				// Add a marker that will be replaced with the symbol for
				// "annotation starts here"
				sb.append("BEGIN_OF_ANNOT"); //$NON-NLS-1$
				sb.append(String.format("BEGIN_OF_COLOR%d",
						Math.min(curSpans.size() + 1, 5))); //$NON-NLS-1$

				if (nextSpan.getEnd() <= nextSpan.getStart()) {

					// SPECIAL CASE: Zero-length span; close it out right away.
					sb.append("END_OF_COLOR"); //$NON-NLS-1$
					sb.append("END_OF_ANNOT"); //$NON-NLS-1$

					if (curSpans.size() > 0) {
						// Still inside an annotation...
						sb.append(String.format("BEGIN_OF_COLOR%d",
								Math.min(curSpans.size(), 5))); //$NON-NLS-1$
					}
					// END SPECIAL CASE
				} else {
					// Add the span to our active set.
					curSpans.add(nextSpan);
				}

				if (jtr.hasNext()) {
					nextSpan = jtr.next();
				} else {
					nextSpan = null;
				}

			}

		}
		if (curSpans.size() > 0) {
			// SPECIAL CASE: One or more annotations reach the end of the
			// document.
			String toAppend = text.substring(outPos);
			toAppend = escapeHTMLSpecials(toAppend);

			sb.append(toAppend);
			sb.append("END_OF_COLOR"); //$NON-NLS-1$
			for (int i = 0; i < curSpans.size(); i++) {
				sb.append("END_OF_ANNOT"); //$NON-NLS-1$
			}
			outPos = text.length();
			// END SPECIAL CASE
		}

		// Don't forget the end of the document!
		String toAppend = text.substring(outPos);
		if (isSnip) {
			toAppend = (String) shorten(toAppend, SHORTEN_STR_LEN, false);
		}
		toAppend = escapeHTMLSpecials(toAppend);

		sb.append(toAppend);

		// Escape all the funky HTML stuff in the original document.
		String markedUp = sb.toString();

		// String tagEscaped = escapeHTMLSpecials(markedUp);
		String tagEscaped = markedUp.replace("<", "&lt;").replace(">", "&gt;"); //$NON-NLS-1$

		// Strip out any carriage return characters.
		tagEscaped = tagEscaped.replace("\r", ""); //$NON-NLS-1$

		// Turn newlines into HTML linebreaks.
		String brEscaped = tagEscaped.replace("\n", "<br>\n"); //$NON-NLS-1$

		// Now that we've escaped all the HTML, we can add the FONT tags.
		String colored = brEscaped
				.replace("BEGIN_OF_ANNOT", ANNOT_BEGIN_MARKER)
				.replace("END_OF_ANNOT", ANNOT_END_MARKER)
				.replace("BEGIN_OF_COLOR1", COLOR_BEGIN1)
				.replace("BEGIN_OF_COLOR2", COLOR_BEGIN2)
				.replace("BEGIN_OF_COLOR3", COLOR_BEGIN3)
				.replace("BEGIN_OF_COLOR4", COLOR_BEGIN4)
				.replace("BEGIN_OF_COLOR5", COLOR_BEGIN5)
				.replace("END_OF_COLOR", COLOR_END); //$NON-NLS-1$

		return colored;
	}

	/**
	 * Shorten a string to <= {@link #SHORTEN_STR_LEN} chars.
	 * 
	 * @param oneLine
	 *            true to avoid adding line breaks to the string when shortening
	 *            it.
	 */
	private CharSequence shorten(CharSequence input, int maxLen, boolean oneLine) {

		final String SINGLE_LINE_ELLIPSIS = "..."; //$NON-NLS-1$

		// "snip" is the sound of part of the text being cut out.
		final String MULTI_LINE_ELLIPSIS = "...\n[ snip! ]\n..."; //$NON-NLS-1$

		String ellipsis = oneLine ? SINGLE_LINE_ELLIPSIS : MULTI_LINE_ELLIPSIS;

		if (input.length() <= maxLen) {
			return input;
		}

		CharSequence begin = input.subSequence(0, maxLen / 2);
		CharSequence end;
		if (false == oneLine) {
			// SPECIAL CASE: Don't change semantics for multi-line printouts
			// yet, to avoid screwing up regression test results.
			end = input.subSequence(input.length() - begin.length(),
					input.length());
			// END SPECIAL CASE
		} else {

			int endLen = maxLen - begin.length() - ellipsis.length();
			end = input.subSequence(input.length() - endLen, input.length());
		}

		return begin + ellipsis + end;
	}

}
