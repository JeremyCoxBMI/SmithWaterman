package smithwatermanalignment;

import DNAcodes;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * SmithWaterman extended to include multiple character equivalencies (see DNAcodes.java)
 *
 * 2016-08-15   v2.0    Imported without modification from v1.6.
 */

public class SmithWatermanAdvanced {


    //according to wikipedia https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm#Example
    static int MATCH = 2;
    static int MISMATCH = -1;
    static int GAP = -1;

    boolean queryISrows;

    final String columns, rows;
    ArrayList<TableEntry[]> table;

//    float pident;

    public SmithWatermanAdvanced(String query, String refseq) {

        if (refseq.length() > query.length()) {
            columns = refseq;
            rows = query;
            queryISrows = true;
        } else {
            columns = query;
            rows = refseq;
            queryISrows = false;
        }
        table = buildTable(columns, rows);

        //debug
        //printPrettyTable(System.out);
        //pident = 0;

        calculateTable();
    }


    static public ArrayList<TableEntry[]> buildTable(String cols, String rows) {

        //cols is now the longest

        ArrayList<TableEntry[]> result = new ArrayList<TableEntry[]>();

        //+1 because first row and first column is "blanks" full of zeroes
//        TableEntry[] blanks = new TableEntry[cols.length()+1];
//        for (int k=0; k<cols.length()+1;k++){
//            blanks[k] = new TableEntry();
//        }


        for (int k = 0; k < rows.length() + 1; k++) {
            result.add(new TableEntry[cols.length() + 1]);       //initializes to move = DIAGONAL, score = 0
        }


        for (int k = 0; k < rows.length() + 1; k++) {
            for (int j = 0; j < cols.length() + 1; j++) {
                result.get(k)[j] = new TableEntry();
            }
        }

        //first row except corner set to RIGHT
        for (int j = 1; j < cols.length() + 1; j++) {
            result.get(0)[j].move = TableEntry.RIGHT;
        }

        //first column except corner set to DOWN
        for (int k = 1; k < rows.length() + 1; k++) {
            result.get(k)[0].move = TableEntry.DOWN;
        }

        //Upper left corner can not place a move.  So it is EMPTY
        result.get(0)[0].move = TableEntry.EMPTY;

        return result;
    }


    private void calculateTable() {
        int n = columns.length();
        int m = rows.length();

        int diagonal, down, right;  //score if said move is made


        //TODO: special case of gap character?  should we include it?

        //BUILD TABLE
        for (int k = 1; k < m + 1; k++) {
            // calculate row k and column k first
            for (int j = k; j < n + 1; j++) {
                //calculate row k, column j next
                if (DNAcodes.equals(columns.charAt(j - 1), rows.charAt(k - 1))) {
                    //they match
                    diagonal = MATCH + table.get(k - 1)[j - 1].score;
                } else {
                    diagonal = MISMATCH + table.get(k - 1)[j - 1].score;
                }
                down = GAP + table.get(k - 1)[j].score;
                right = GAP + table.get(k)[j - 1].score;

                if (diagonal > down && diagonal > right) {
                    table.get(k)[j].move = TableEntry.DIAGONAL;
                    table.get(k)[j].score = diagonal;
                } else {
                    if (right == down) {
                        table.get(k)[j].move = TableEntry.TIE;
                        table.get(k)[j].score = right;
                    } else if (right > down) {
                        table.get(k)[j].move = TableEntry.RIGHT;
                        table.get(k)[j].score = right;
                    } else {
                        table.get(k)[j].move = TableEntry.DOWN;
                        table.get(k)[j].score = down;
                    }
                }
            }   // end for j

            // Necessary Code
            // Do not understand the magic in how it works
            // I think this processes the column to the left?
            for (int j = k + 1; j < m + 1; j++) {
                if (DNAcodes.equals(columns.charAt(k - 1), rows.charAt(j - 1))) {
                    //they match
                    diagonal = MATCH + table.get(j - 1)[k - 1].score;
                } else {
                    diagonal = MISMATCH + table.get(j - 1)[k - 1].score;
                }
                down = GAP + table.get(j - 1)[k].score;
                right = GAP + table.get(j)[k - 1].score;

                if (diagonal >= down && diagonal >= right) {
                    table.get(j)[k].move = TableEntry.DIAGONAL;
                    table.get(j)[k].score = diagonal;
                } else {
                    if (right == down) {
                        table.get(j)[k].move = TableEntry.TIE;
                        table.get(j)[k].score = right;
                    } else if (right > down) {
                        table.get(j)[k].move = TableEntry.RIGHT;
                        table.get(j)[k].score = right;
                    } else {
                        table.get(j)[k].move = TableEntry.DOWN;
                        table.get(j)[k].score = down;
                    }
                }
            } // end for j

        } // end for k
    } // end calculateTable

    public PartialAlignment partialAlignment(int row, int col) {
        //either row or column (or both) should be maximum value
        if (row != rows.length() && col != columns.length())
            return null;
        else
            return new PartialAlignment(table, columns, rows, row, col, queryISrows);

    }

    public ArrayList<PartialAlignment> bestAlignments() {
        int n = columns.length();
        int m = rows.length();
        int maximum = -1 * n;
        ArrayList<PartialAlignment> result = new ArrayList<PartialAlignment>();

        for (int k = 2; k < m; k++) {
            maximum = Math.max(table.get(k)[n].score, maximum);
        }

        //last column plus corner
        for (int k = 2; k < n + 1; k++) {
            maximum = Math.max(table.get(m)[k].score, maximum);
        }

        for (int k = 2; k < m; k++) {
            if (table.get(k)[n].score == maximum)
                result.add(new PartialAlignment(table, columns, rows, k, n, queryISrows));
        }

        for (int k = 2; k < n + 1; k++) {
            if (table.get(m)[k].score == maximum)
                result.add(new PartialAlignment(table, columns, rows, m, k, queryISrows));
        }

        return result;
    }

    public void printPrettyTable(PrintStream stream) {
        String stringo = "        | ";
        for (int k = 0; k < columns.length(); k++) {
            stringo += " " + columns.charAt(k) + "  | ";
        }
        stream.println(stringo);

        for (int k = 0; k < rows.length() + 1; k++) {  //rare case where we print every row
            stringo = "";
            if (k == 0) stringo += " :  ";
            else stringo += rows.charAt(k - 1) + ":  ";

            for (int j = 0; j < columns.length() + 1; j++) {
                if (table.get(k)[j].move == TableEntry.DIAGONAL) {
                    stringo += "*";
                } else if (table.get(k)[j].move == TableEntry.DOWN) {
                    stringo += "^";
                } else if (table.get(k)[j].move == TableEntry.RIGHT) {
                    stringo += "<";
                }

                if (table.get(k)[j].score < 0) stringo += Integer.toString(table.get(k)[j].score) + " | ";
                else stringo += " " + Integer.toString(table.get(k)[j].score) + " | ";
            }
            stream.println(stringo);
        }
    }

    public void printPrettyTableTabs(PrintStream stream) {
        String stringo = "\t\t";
        for (int k = 0; k < columns.length(); k++) {
            stringo += " " + columns.charAt(k) + "\t";
        }
        stream.println(stringo);

        for (int k = 0; k < rows.length() + 1; k++) {  //rare case where we print every row
            stringo = "";
            if (k == 0) stringo += "\t";
            else stringo += rows.charAt(k - 1) + "\t";

            for (int j = 0; j < columns.length() + 1; j++) {
                if (table.get(k)[j].move == TableEntry.DIAGONAL) {
                    stringo += "*";
                } else if (table.get(k)[j].move == TableEntry.DOWN) {
                    stringo += "^";
                } else if (table.get(k)[j].move == TableEntry.RIGHT) {
                    stringo += "<";
                }

                if (table.get(k)[j].score < 0) stringo += Integer.toString(table.get(k)[j].score) + "\t";
                else stringo += " " + Integer.toString(table.get(k)[j].score) + "\t";
            }
            stream.println(stringo);
        }
    }

    public void printPrettyBestResults(PrintStream stream) {
        ArrayList<PartialAlignment> pas = bestAlignments();
        int x = Math.min(columns.length(), rows.length());
//        stream.println("Best alignment score is "+Integer.toString(pas.get(0).score)+" of "+x*MATCH+"\n");
        for (PartialAlignment pat : pas) {
            stream.println("\n" + pat + "\n");
        }
    }
}