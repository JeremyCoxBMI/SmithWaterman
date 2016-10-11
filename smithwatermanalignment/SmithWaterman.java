package smithwatermanalignment;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * This code is more or less direct import of python code written for same purpose.
 * NOTE  this code does not enforce DNA text encoding rules established in DNAcodes.
 *
 * 2016-08-15   v2.0    Imported without modification from v1.6.
 */
public class SmithWaterman {


    //according to wikipedia https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm#Example
    static int MATCH = 2;
    static int MISMATCH = -1;
    static int GAP = -1;

    final String columns, rows;
    ArrayList<TableEntry[]> table;

    public SmithWaterman (String string1, String string2){

        if (string2.length() > string1.length()) {
            columns = string2;
            rows = string1;
        } else {
            columns = string1;
            rows = string2;
        }
        table = buildTable(columns, rows);

        //debug
        //printPrettyTable(System.out);

        calculateTable();
    }




    static public ArrayList<TableEntry[]> buildTable(String cols, String rows ) {

        //cols is now the longest

        ArrayList<TableEntry[]> result = new ArrayList<TableEntry[]>();

        //+1 because first row and first column is "blanks" full of zeroes
//        TableEntry[] blanks = new TableEntry[cols.length()+1];
//        for (int k=0; k<cols.length()+1;k++){
//            blanks[k] = new TableEntry();
//        }


        for (int k = 0; k < rows.length() +1 ; k++) {
            result.add(  new TableEntry[cols.length()+1]  );       //initializes to move = DIAGONAL, score = 0
        }

        //Keep corner as diagonal
        for (int k=0; k< rows.length()+1; k++){
            for (int j=0; j< cols.length()+1; j++){
                result.get(k)[j] = new TableEntry();
            }
        }

        //first row except corner set to RIGHT
        for (int j = 1; j < cols.length()+1; j++) {
            result.get(0)[j].move = TableEntry.RIGHT;
        }

        //first column except corner set to DOWN
        for (int k = 1; k < rows.length()+1; k++) {
            result.get(k)[0].move = TableEntry.DOWN;
        }

        return result;
    }


    private void calculateTable() {
        int n = columns.length();
        int m = rows.length();

        int diagonal, down, right;  //score if said move is made

        for (int k = 1; k < m + 1; k++) {
            // calculate row k and column k first
            for (int j = k; j < n + 1; j++) {
                //calculate row k, column j next
                if (   columns.charAt(j - 1) == rows.charAt(k - 1)  ){
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
            for (int j = k+1; j < m+1; j++) {
                if (   columns.charAt(k - 1) == rows.charAt(j - 1)  ){
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

    public PartialAlignment partialAlignment(int row, int col){
        //either row or column (or both) should be maximum value
        if (row != rows.length() && col != columns.length() )
            return null;
        else
            return new PartialAlignment(table, columns, rows, row, col);

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
            if (table.get(k)[n].score == maximum) result.add(new PartialAlignment(table, columns, rows, k, n));
        }

        for (int k = 2; k < n + 1; k++) {
            if (table.get(m)[k].score == maximum) result.add(new PartialAlignment(table, columns, rows, m, k));
        }

        return result;
    }

    public void printPrettyTable(PrintStream stream ){
        String stringo = "        | ";
        for (int k = 0; k < columns.length(); k++) {
            stringo += " " + columns.charAt(k) + "  | ";
        }
        stream.println(stringo);

        for (int k = 0; k < rows.length()+1; k++) {  //rare case where we print every row
            stringo = "";
            if (k == 0) stringo += " :  ";
            else stringo += rows.charAt(k-1) + ":  ";

            for (int j=0; j < columns.length()+1; j++){
                if (table.get(k)[j].move == TableEntry.DIAGONAL){
                    stringo += "*";
                } else if (table.get(k)[j].move == TableEntry.DOWN){
                    stringo += "^";
                } else if (table.get(k)[j].move == TableEntry.RIGHT){
                    stringo += "<";
                }

                if (table.get(k)[j].score < 0) stringo += Integer.toString(table.get(k)[j].score)+" | ";
                else stringo += " " + Integer.toString(table.get(k)[j].score)+" | ";
            }
            stream.println(stringo);
        }
    }

    public void printPrettyTableTabs(PrintStream stream ){
        String stringo = "\t\t";
        for (int k = 0; k < columns.length(); k++) {
            stringo += " " + columns.charAt(k) + "\t";
        }
        stream.println(stringo);

        for (int k = 0; k < rows.length()+1; k++) {  //rare case where we print every row
            stringo = "";
            if (k == 0) stringo += "\t";
            else stringo += rows.charAt(k-1) + "\t";

            for (int j=0; j < columns.length()+1; j++){
                if (table.get(k)[j].move == TableEntry.DIAGONAL){
                    stringo += "*";
                } else if (table.get(k)[j].move == TableEntry.DOWN){
                    stringo += "^";
                } else if (table.get(k)[j].move == TableEntry.RIGHT){
                    stringo += "<";
                }

                if (table.get(k)[j].score < 0) stringo += Integer.toString(table.get(k)[j].score)+"\t";
                else stringo += " " + Integer.toString(table.get(k)[j].score)+"\t";
            }
            stream.println(stringo);
        }
    }

    public void printPrettyBestResults(PrintStream stream){
        ArrayList<PartialAlignment> pas = bestAlignments();
        stream.println("Best alignment score is "+Integer.toString(pas.get(0).score)+"\n");
        for ( PartialAlignment pat : pas) {
            stream.println(pat+"\n");
        }
    }

    public static void main(String[] args) {
        String[] testStrings = {
                "ATCTGTTCTT",
                "CTTTATGTT",

                "CANDOATTITUDE",
                "CABLEATTACK",

                "JAPAN",
                "ANSWER",

                "CHEESE",
                "CHEDDAR",

                "ATCCTAC",
                "TACCATC",

                "ACGTGATATA",
                "ATATACTGG",

                "CHEESEWHIZ",
                "CHEESEBURGERZ"
        };

        SmithWaterman sw;
        ArrayList<PartialAlignment> pas;

//        sw = new SmithWaterman (testStrings[0], testStrings[1]);
//        sw.printPrettyTable(System.out);
//        pas = sw.bestAlignments();
//        for ( PartialAlignment pat : pas) {
//            System.out.println(pat+"\n");
//        }



        for (int k=0; k < testStrings.length-1; k+=2){
            sw = new SmithWaterman (testStrings[k], testStrings[k+1]);
            sw.printPrettyTable(System.out);
            pas = sw.bestAlignments();
            System.out.println("Best alignment score is "+Integer.toString(pas.get(0).score)+"\n");
            for ( PartialAlignment pat : pas) {
                System.out.println(pat+"\n");
            }
        }

    }

}
