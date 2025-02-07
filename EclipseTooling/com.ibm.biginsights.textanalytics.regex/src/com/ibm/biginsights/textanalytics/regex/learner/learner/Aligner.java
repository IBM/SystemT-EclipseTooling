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
package com.ibm.biginsights.textanalytics.regex.learner.learner;

import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.expression.AlternationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ConcatenationExpression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.Expression;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionLeaf;
import com.ibm.biginsights.textanalytics.regex.learner.expression.ExpressionNode;

/**
 *  This class implements the Needleman-Wunsch sequence alignment algorithm.
 * 
 *         First, the alignment matrix has to be computed. This class also contains the traceback
 *         function of the Needleman-Wunsch-Alignment algorithm. The Aligner aligns the units
 *         according to the simple principle: 1) if next step is horizontal or first row is reached
 *         --> subexpression of expression2 into path (as optional) 2) if next step is vertical or
 *         first column is reached --> subexpression of expression1 into path (as optional) 3) if
 *         next step is diagonal: if subexpression of expression2 is contained in subexpression of
 *         expression1 (e.g. subexpr2 = 'B' and subexpr1 = '(A|B)', the information of the
 *         subexpression of expression2 is added to the subexpression of expression1 and this
 *         subexpression is added to the path. If the subexpression of expression2 is not contained
 *         in the subexpression of expression1, an alternation of the two subexpressions is created
 *         and added to the path.
 * 
 */

public abstract class Aligner {



  public static final boolean DEBUG = false;

  /**
   * This class is used to represent the fields of the alignment matrix used in the algorithm
   */
  private static class Field {

    int score;

    int ancestorRow;

    int ancestorColumn;

    Field(int score, int row, int col) {
      this.score = score;
      this.ancestorRow = row;
      this.ancestorColumn = col;
    }
  }

  /**
   * This method creates the alignment matrix.
   * 
   * @param Expression
   *          sample1 may be a complex Expression
   * @param Expression
   *          sample2 may only be a "simple" Expression (new sample, sampleAnalysis run, all
   *          subexpression have a character type)
   * @return Field[][] --> alignment matrix.
   */
  private static Field[][] computeAlignmentMatrix(ArrayList<Expression> subexpr1,
      ArrayList<Expression> subexpr2) {
    final int length_sample1 = subexpr1.size();
    final int length_sample2 = subexpr2.size();
    final Field fields[][] = new Field[length_sample1 + 1][length_sample2 + 1];
    fields[0][0] = new Field(0, -1, -1);
    // initialize first row
    for (int i = 1; i < length_sample2 + 1; i++) {
      fields[0][i] = new Field(fields[0][i - 1].score - 1, 0, i - 1);
    }
    // initialize first column
    for (int i = 1; i < length_sample1 + 1; i++) {
      fields[i][0] = new Field(fields[i - 1][0].score - 1, i - 1, 0);
    }
    // compute rest of the fields
    // for each row
    for (int i = 1; i < length_sample1 + 1; i++) {
      // for each column
      for (int j = 1; j < length_sample2 + 1; j++) {
        // match score
        int match_score = fields[i - 1][j - 1].score;
        if (subexpr1.get(i - 1).contains(subexpr2.get(j - 1))) {
          match_score++;
        } else {
          match_score--;
        }
        // gap scores
        final int horizontal_gap_score = fields[i][j - 1].score - 1;
        final int vertical_gap_score = fields[i - 1][j].score - 1;
        /*
         * find greatest value: (Definition)
         */
        // match_score is greatest --> diagonal field is predecessor
        if ((match_score > horizontal_gap_score) && (match_score > vertical_gap_score)) {
          fields[i][j] = new Field(match_score, i - 1, j - 1);
        }
        // horizontal_gap_score is greatest --> horizontal field is predecessor
        else if ((horizontal_gap_score > match_score)
            && (horizontal_gap_score > vertical_gap_score)) {
          fields[i][j] = new Field(horizontal_gap_score, i, j - 1);
        }
        // vertical_gap_score is greatest --> horizontal field is predecessor
        else if ((vertical_gap_score > match_score) && (vertical_gap_score > horizontal_gap_score)) {
          fields[i][j] = new Field(vertical_gap_score, i - 1, j);
        }
        // match_score and horizontal_gap_score have same value, are the greatest
        // if horizontal step produces match in next step --> horizontal field is predecessor
        // else diagonal field is predecessor
        else if ((match_score == horizontal_gap_score) && (match_score > vertical_gap_score)) {
          if (j >= 2) {
            if (subexpr1.get(i - 1).contains(subexpr2.get(j - 2))) {
              fields[i][j] = new Field(horizontal_gap_score, i, j - 1);
            } else {
              fields[i][j] = new Field(match_score, i - 1, j - 1);
            }
          }
          // if first subexpression of subexpr2, use match_score and diagonal step
          else {
            fields[i][j] = new Field(match_score, i - 1, j - 1);
          }
        }
        // match_score and vertical_gap_score have same value, are the greatest
        // if vertical step produces match in next step --> vertical field is predecessor
        // else diagonal field is predecessor
        else if ((match_score == vertical_gap_score) && (match_score > horizontal_gap_score)) {
          if (i >= 2) {
            if (subexpr1.get(i - 2).contains(subexpr2.get(j - 1))) {
              fields[i][j] = new Field(vertical_gap_score, i - 1, j);
            } else {
              fields[i][j] = new Field(match_score, i - 1, j - 1);
            }
          }
          // if first subexpression of subexpr1, use match_score and diagonal step
          else {
            fields[i][j] = new Field(match_score, i - 1, j - 1);
          }
        }
        // horizontal_gap_score and vertical_gap_score have same value, are the greatest
        // if vertical step produces match in next step --> vertical field is predecessor
        // else horizontal field is predecessor
        else if ((horizontal_gap_score == vertical_gap_score)
            && (horizontal_gap_score > match_score)) {
          if (i >= 2) {
            if (subexpr1.get(i - 2).contains(subexpr2.get(j - 1))) {
              fields[i][j] = new Field(vertical_gap_score, i - 1, j);
            } else {
              fields[i][j] = new Field(horizontal_gap_score, i, j - 1);
            }
          }
          // if first subexpression of subexpr2, use horizontal step
          else {
            fields[i][j] = new Field(horizontal_gap_score, i, j - 1);
          }
        }
        // if all three scores have the same value
        else if ((match_score == horizontal_gap_score) && (match_score == vertical_gap_score)) {
          // if there is a match at this position --> diagonal field is predecessor
          if (subexpr1.get(i - 1).contains(subexpr2.get(j - 1))) {
            fields[i][j] = new Field(match_score, i - 1, j - 1);
          } else {
            if (j >= 2) {
              // if a horizontal step produces a match in the next step --> horizontal field is
              // predecessor,
              // else vertical field is predecessor
              if (subexpr1.get(i - 1).contains(subexpr2.get(j - 2))) {
                fields[i][j] = new Field(horizontal_gap_score, i, j - 1);
              } else {
                fields[i][j] = new Field(vertical_gap_score, i - 1, j);
              }
            }
            // if first subexpression subexpr2 --> vertical field is predecessor
            else {
              fields[i][j] = new Field(vertical_gap_score, i - 1, j);
            }
          }
        } else {
          System.out.println("Forgot case " + match_score + " " + horizontal_gap_score + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              + vertical_gap_score);
        }
      }
    }

    /* print for debugging */
    if (DEBUG) {
      String line = Messages.Aligner_ALLIGNMENT_TXT;
      for (int i = 0; i < length_sample2; i++) {
        line += subexpr2.get(i).getType() + "\t"; //$NON-NLS-1$
        if (subexpr2.get(i).getType().length() < 8) {
          line += "\t"; //$NON-NLS-1$
        }
      }
      System.out.println(line);
      line = "\t\t"; //$NON-NLS-1$
      for (int i = 0; i < length_sample2 + 1; i++) {
        line += "[" + fields[0][i].score + ", (" + fields[0][i].ancestorRow + "," //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + fields[0][i].ancestorColumn + ")]\t"; //$NON-NLS-1$
      }
      System.out.println(line);
      for (int i = 1; i < length_sample1 + 1; i++) {
        String opt = ""; //$NON-NLS-1$
        if (subexpr1.get(i - 1).isOptional()) {
          opt = "*"; //$NON-NLS-1$
        }
        line = subexpr1.get(i - 1).getType() + opt + "\t"; //$NON-NLS-1$
        if (line.length() < 8) {
          line += "\t"; //$NON-NLS-1$
        }
        for (int j = 0; j < length_sample2 + 1; j++) {
          line += "[" + fields[i][j].score + ", (" + fields[i][j].ancestorColumn + "," //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              + fields[i][j].ancestorRow + ")]\t"; //$NON-NLS-1$
        }
        System.out.println(line);
      }
    }

    return fields;
  }

  /**
   * Alignment of two expressions
   * 
   * @param Expression
   *          sample1 may be a complex Expression (first type=CONCATENATION)
   * @param Expression
   *          sample2 may only be a "simple" Expression (new sample, type=CONCATENATION)
   * @return ExpressionNode containing the alignment of sample and sample2
   */
  public static ExpressionNode align(ExpressionNode sample1, ExpressionNode sample2) {

    final ArrayList<Expression> subexpr1 = sample1.getSubexpressions();
    final ArrayList<Expression> subexpr2 = sample2.getSubexpressions();

    // get alignment matrix with pointers
    final Field fields[][] = computeAlignmentMatrix(subexpr1, subexpr2);

    // Trace back --> find path (= best alignment)
    ExpressionNode path = new ConcatenationExpression();

    int row = subexpr1.size();
    int col = subexpr2.size();
    // until field[0][0] is reached...
    while (!((row == 0) && (col == 0))) {

      // if next step is horizontal or first row is reached
      if (((fields[row][col].ancestorColumn < col) && (fields[row][col].ancestorRow == row))
          || (row == 0)) {
        // subexpression2 into path (as optional)
        final ExpressionLeaf leaf = (ExpressionLeaf) subexpr2.get(col - 1).clone();
        leaf.setOptional(true);
        path.addSubexpression(leaf);
      }
      // next step is vertical or first column is reached
      else if (((fields[row][col].ancestorColumn == col) && (fields[row][col].ancestorRow < row))
          || (col == 0)) {
        // subexpression1 into path (as optional)
        final Expression expression = subexpr1.get(row - 1).clone();
        expression.setOptional(true);
        path.addSubexpression(expression);
      }
      // next step is diagonal
      else if ((fields[row][col].ancestorColumn < col) && (fields[row][col].ancestorRow < row)) {
        Expression subexpression1 = subexpr1.get(row - 1);
        final ExpressionLeaf subexpression2 = (ExpressionLeaf) subexpr2.get(col - 1);
        // if insertion is successful --> subexpression1 contains the type of subexpression2
        if (subexpression1.insert(subexpression2)) {
          if (subexpression1.isOptional() || subexpression2.isOptional()) {
            subexpression1.setOptional(true);
          }
        }
        // else if insertion is not successful, create new alternation
        else {
          final ExpressionNode node = new AlternationExpression();
          node.addSubexpression(subexpression1);
          node.addSubexpression(subexpression2);
          if (subexpression1.isOptional() || subexpression2.isOptional()) {
            node.setOptional(true);
            subexpression1.setOptional(false);
            subexpression2.setOptional(false);
          }
          subexpression1 = node;
        }
        path.addSubexpression(subexpression1);
      }

      final int temp = row;
      row = fields[row][col].ancestorRow;
      col = fields[temp][col].ancestorColumn;
    }

    if (DEBUG) {
      System.out.println(Messages.Aligner_INVERT_PATH_LABEL + path.toStringWithSamples());
    }

    // traceback --> invert expression
    path = (ExpressionNode) path.invert();

    return path;
  }

}
