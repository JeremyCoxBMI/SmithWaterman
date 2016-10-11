package org.cchmc.kluesuite.klat;

/**
 * Helper class to SmithWaterman and also SmithWatermanAdvanced
 *
 * 2016-08-15   v2.0    Imported without modification from v1.6.
 */
class TableEntry {

    static int EMPTY = 0;
    static int RIGHT = 1;
    static int DOWN = 2;
    static int DIAGONAL = 3;
    static int TIE = RIGHT;     //by this program's convention, longest is across top, thus tie moves right

    int move;
    int score;

    TableEntry() {
        move = DIAGONAL;
        score = 0;
    }

    TableEntry(int move) {
        this.move = move;
        score = 0;
    }
}
