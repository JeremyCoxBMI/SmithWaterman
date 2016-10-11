package org.cchmc.kluesuite.klat;

import java.util.ArrayList;

/**
 *
 * This is a helper class to SmithWaterman  but mostly for SmithWatermanAdvanced.
 * Each instantiation calculates and tracks the statistics for a single alignment possibility.
 *
 * 2016-08-05   v1.6    Making a major upgrade to track multiple alignment statistics
 */
public class PartialAlignment {
    String top, left;
    int score;

    // These are altschul parameters for calculating Evalue, bitscore
    public static double K = (float) 0.041;
    public static double lambda = (float) 0.267;

    /**
     * it is possible that the query will be stored as longer sequence
     * true means query is contained in string "left", otherwise in "top"
     */
    boolean queryASleft;

    /**
     * possibly a duplication of the length parameter.
     * This counts the total number of matches, mismatches, and gaps
     * Therefore, it is in between the query and reference sequence lengths.
     */
    int numAligned;


    /**
     * BlastN format6 reporting term
     * percent of identical matches
     */
    float pident;

    /**
     * BlastN format6 reporting term
     * this is alignment length
     * todo: judges ruling on what length means?
     */
    int length;


    /**
     * BlastN format6 reporting term
     *
     */
    int mismatch;

    /**
     * Total number of gaps in alignment
     */
    int gapopen;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment starts, INCLUSIVE
     */
    int qstart;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment ends, INCLUSIVE
     */
    int qend;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment begins, INCLUSIVE
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    int sstart;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment ends, INCLUSIVE
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    int send;

    /**
     * BlastN format6 reporting term
     */
    double evalue;

    /**
     * BlastN format6 reporting term
     */
    int bitscore;

    ArrayList<AlignmentGap> gaps;


    //intermediate global variables to pass between functions

    /**
     * intermediate global variable
     * while processing alignment output, has a gap been found?
     */
    private boolean inGap;

    /**
     * intermediate global variable
     * for current gap, is it to the left String (true) or top (false)
     */
    private boolean gapISleft;

    /**
     * intermediate global variable
     * first index where gap occurs
     */
    private int gapStart;

    /**
     * intermediate global variable
     * last index where gap observed
     */
    private int gapEnd;

    /**
     * intermediate global variable
     * unclear how this varies from gapStart
     */
    private int gapPos;


    /**
     * for SmithWaterman backwards compatability
     *
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param table
     * @param columns
     * @param rows
     * @param row
     * @param col
     */
    public PartialAlignment(ArrayList<TableEntry[]> table, String columns, String rows, int row, int col) {
        score = table.get(row)[col].score;
        int n = columns.length();
        int m = rows.length();
        top = "";
        left = "";

        if (row < m) {
            for (int k = 0; k < m - row; k++) {
                top += '.';
                left += rows.charAt(m - 1 - k);
            }
        }

        if (col < n) {
            for (int k = 0; k < n - col; k++) {
                left += '.';
                top += columns.charAt(n - 1 - k);
            }
        }

        while (row > 0 || col > 0) {
            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
                //Traceback to DIAGONALLY   UP and LEFT
                col--;
                row--;
                top += columns.charAt(col);
                left += rows.charAt(row);

            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
                //Traceback LEFT
                col--;
                if (row == 0) left += '.';
                else left += "_";
                top += columns.charAt(col); //col was decremented before we right char; so the 1 offset (first entry is blank letter) is included

            } else if (table.get(row)[col].move == TableEntry.DOWN) {
                //Traceback UP
                row--;
                if (col == 0) top += '.';
                else top += "_";
                left += rows.charAt(row);

            }
        }

        top = new StringBuilder(top).reverse().toString();
        left = new StringBuilder(left).reverse().toString();
    }

    public float getPercentIdentity(){
        return pident;
    }

    /**
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param table
     * @param columns
     * @param rows
     * @param row
     * @param col
     * @param queryISrows   query may or may not be the rows, to output correctly, this must be known
     */
    public PartialAlignment(ArrayList<TableEntry[]> table, String columns, String rows, int row, int col, boolean queryISrows) {

        queryASleft = queryISrows;
//        String rows, columns;
//        if (query.length() > refSeq.length()){
//            rows = refSeq;
//            columns = query;
//            queryASleft = false;
//        } else {
//            rows = refSeq;
//            columns = query;
//            queryASleft = true;
//        }

        //Mark as uninitialized
        qstart = -1;
        qend = -1;
        sstart = -1;
        send = -1;
        inGap = false;
        gapISleft = false;
        gapStart = -1;
        gapEnd = -1;
        gapPos = -1;
        mismatch = 0;

        gaps = new ArrayList<AlignmentGap>();

        score = table.get(row)[col].score;
        int n = columns.length();
        int m = rows.length();
        int numMatches = 0;
        numAligned = 0;
        top = "";
        left = "";

        if (row < m) {
            for (int k = 0; k < m - row; k++) {
                top += '.';
                left += rows.charAt(m - 1 - k);
            }

            if (queryASleft) qstart = row-1;
            else sstart = row-1;
        }

        if (col < n) {
            for (int k = 0; k < n - col; k++) {
                left += '.';
                top += columns.charAt(n - 1 - k);
            }
            if (queryASleft) sstart = col-1;
            else qstart = col-1;
        }

        inGap = false;

        int alignPositionReverse = 0;
        while (row > 0 || col > 0) {
            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
                //Traceback to DIAGONALLY   UP and LEFT

//                char debug1 = rows.charAt(row - 1);
//                char debug2 = columns.charAt(col - 1);
//                int debug3 = table.get(row)[col].score;
//                int debug4 = table.get(row - 1)[col - 1].score;

                if (table.get(row)[col].score > table.get(row - 1)[col - 1].score) numMatches++;
                else mismatch++;

                col--;
                row--;
                top += columns.charAt(col);
                left += rows.charAt(row);
                numAligned++;


            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
                //Traceback LEFT
                col--;
                if (row == 0) {
                    left += '.';
                } else {
                    left += "_";
                    numAligned++;
                    processGap(TableEntry.RIGHT, row, col, alignPositionReverse);
                }
                top += columns.charAt(col); //col was decremented before we right char; so the 1 offset (first entry is blank letter) is included

                //if at end, write last gap to table :: toggle direction triggers a write
                //if (row == 0 && col == 0) processGap(TableEntry.DOWN, row, col);

            } else if (table.get(row)[col].move == TableEntry.DOWN) {
                //Traceback UP
                row--;
                if (col == 0) {
                    top += '.';
                } else {
                    top += "_";
                    numAligned++;
                    processGap(TableEntry.DOWN, row, col, alignPositionReverse);
                }
                left += rows.charAt(row);


            }

            if (qend == -1 && (row == 0 || col == 0)) {
                if (queryASleft) {
                    qend = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    send = col;
                } else {
                    qend = col;
                    send = row;
                }
            }

            if (qstart == -1) {
                if (queryASleft) {
                    if (row < m) {
                        qstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (col < n) {
                        qstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            if (sstart == -1) {
                if (queryASleft) {
                    if (col < n) {
                        sstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (row < m) {
                        sstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            alignPositionReverse++;
        } // end while (trace path through table)

        if (row == 0 && col == 0 && inGap){
            //if at end, and a gap was found, write last gap to table
            if (gapISleft)  gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query
            else            gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query
        }


        //reverse all gap position coordinate
        for (AlignmentGap x : gaps){
            x.pos = numAligned - x.pos - 1;
        }


        //Because I am foolish, I have start and end reversed:
        int temp;
        temp = qstart;
        qstart = qend;
        qend = temp;
        temp = sstart;
        sstart = send;
        send = temp;


        pident = ((float)numMatches) / ((float)numAligned);
        top = new StringBuilder(top).reverse().toString();
        left = new StringBuilder(left).reverse().toString();

        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
        //let S = smith-waterman score
        evalue = K * m * n * Math.exp(-1*score*lambda);
        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
        bitscore = b.intValue();

        gapopen = gaps.size();

        length = numAligned;

    } // end constructor

    /**
     * helper function
     * @param direction whether the gap is RIGHT or DOWN movement
     * @param row   coordinate in table
     * @param col   coordinate in table
     * @param posRev    ??
     */
    private void processGap(int direction, int row, int col, int posRev) {
        if (direction == TableEntry.RIGHT){
            //newGapIsLeft = false;
            if (  inGap && gapISleft == true  ){
                //gaps switching sides
                //write previous gap AND start new gap

                //gap Start/End are reversed because parsing backwards
                gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query

                gapISleft = false;
                gapStart = col;
                gapEnd = col;
                gapPos = posRev;
            } else if (inGap) {
                if (col == gapEnd-1) {
                    //continue gap along top
                    gapEnd = col;
                } else {
                    //write previous gap AND start new gap
                    gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query
                    gapISleft = false;
                    gapStart = col;
                    gapEnd = col;
                    gapPos = posRev;
                }
            } else {
                //new gap
                inGap = true;
                gapISleft = false;
                gapStart = col;
                gapEnd = col;
                gapPos = posRev;
            }
        } else if ( direction == TableEntry.DOWN ) {
            //newGapIsLeft = true;
            if (inGap && gapISleft == false){
                //gaps switching sides
                //write previous gap AND start new gap

                //gap Start/End are reversed because parsing backwards
                gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query

                gapISleft = true;
                gapStart = row;
                gapEnd = row;
                gapPos = posRev;
            } else if (inGap) {
                if (row == gapEnd-1) {
                    //continue gap along left
                    gapEnd = row;
                } else {
                    //write previous gap AND start new gap
                    gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query
                    gapISleft = true;
                    gapStart = row;
                    gapEnd = row;
                    gapPos = posRev;
                }
            } else {
                //new gap
                inGap = true;
                gapISleft = true;
                gapStart = row;
                gapEnd = row;
                gapPos = posRev;
            }
        }
    }

    public String toString(){
        return "SCORE: "+score+" of "+numAligned*SmithWatermanAdvanced.MATCH+" %id "+ pident *100+"\n"+top + "\n" + left;
    }

    /**
     * Full output of alignment statistics
     * @return
     */
    public String toFullReport(){
        String result = "";
        if (queryASleft) {
            result += "RefSeq " + top + "\n";
            result += " Query " + left + "\n";
        } else {
            result += "RefSeq " + left + "\n";
            result += " Query " + top + "\n";
        }
        result += "pident   "+pident*100+"\n";
        result += "length   "+length+"\n";
        result += "mismatch "+mismatch+"\n";
        result += "gapopen  "+gapopen+"\n";
        for(AlignmentGap x : gaps){
            result += x.toString()+"\n";
        }
        result += "qstart   "+qstart+"\n";
        result += "qend     "+qend+"\n";
        result += "sstart   "+sstart+"\n";
        result += "send     "+send+"\n";
        result += "evalue   "+evalue+"\n";
        result += "bitscore "+bitscore+"\n";
        return result;
    }

}
