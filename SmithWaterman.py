### Jeremy Cox
### Mar 13, 2014
### Optimal alignment finder
### Written for Python 3.2 (printTablePretty won't work in Python 2.7)

### v2: fixed bugs/report formatting
### v3: reporting format changed; code streamlined

import time
import copy
import math
import re

#GLOBAL CONSTS

# Values for best move
MOVES = [ 'd', 'R', 'D' ]
        # diagonal, Right, Down
DIAG = 'd'
RIGHT = 'R'
DOWN = 'D'

# if right or down move are equal
TIE = "R"
###by convention, longest string is across top

# scoring of matches
#MATCH = 1
#MATCH according to wikipedia is +2   https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm#Example
MATCH = 2
MISMATCH = -1
GAP = -1

def buildTable ( string1, string2 ):
    #the processing table for string alignment
    n = len(string1)
    m = len(string2)

    ###by convention, longest string is across top
    if m > n:
        n = m
        temp = string2
        string2 = string1
        string1 = temp

    table = [[(' ',0) for x in range(n+1)] for y in range(m+1)]
    for k in range(n+1):
        table[0][k] = (RIGHT, 0)
    for k in range(m+1):
        table[k][0] = (DOWN, 0)
    table[0][0] = ('*', 0)

    for k in range(m+1):
        table[0][k] = (RIGHT, 0)

    return (table, string1, string2)

def calculateTable( table, colV, rowV ):
    n = len(colV)
    m = len(rowV)

    for k in range (1, m+1):
        #calculate row k and column k first
        for j in range (k, n+1):
            if ( colV[j-1] == rowV[k-1] ):  #they match; -1 because table has extra col/row
                diagonal = MATCH + table[k-1][j-1][1] #1 plus previous diagonal score
            else:
                diagonal = MISMATCH + table[k-1][j-1][1]
            down = GAP + table[k-1][j][1]
            right = GAP + table[k][j-1][1]

            if ( diagonal > down and diagonal > right ):
                table[k][j] = (DIAG, diagonal)
            else:
                if right == down:
                    table[k][j] = (TIE, right)
                else:
                    if right > down:
                        table[k][j] = (RIGHT, right)
                    else:
                        table[k][j] = (DOWN, down)

        #calculate column k
        for j in range(k+1, m+1):
            if ( colV[k-1] == rowV[j-1] ):  #they match; -1 because table has extra col/row
                diagonal = MATCH + table[j-1][k-1][1] #1 plus previous diagonal score
            else:
                diagonal = MISMATCH + table[j-1][k-1][1]
            down = GAP + table[j-1][k][1]
            right = GAP + table[j][k-1][1]

            #V3: changed to >=
            if ( diagonal >= down and diagonal >= right ):
                table[j][k] = (DIAG, diagonal)
            else:
                if right == down:
                    table[j][k] = (TIE, right)
                else:
                    if right > down:
                        table[j][k] = (RIGHT, right)
                    else:
                        table[j][k] = (DOWN, down)
    return table

def printTable( aTable, colV, rowV ):
    stringo = " )  ),"
    for k in range(len(colV)):
        stringo = stringo + "    " + colV[k] + "   ),"
    print(stringo)
    print( '_:  )', aTable[0] )
    for k in range(1, len(rowV)+1):
        print( rowV[k-1] + ':  )', aTable[k] )

def partialAlignment( table, colV, rowV, i1, j1):  #row, column format
    #returns triple: score, column_alignment, row_alignment
    n = len(colV)
    m = len(rowV)
    score = table[i1][j1][1]
    top = ''
    left = ''

    if i1 < m:
        for k in range( (m - i1) ):
            top = top + '.'
            left += rowV[ (m-1-k) ]
    if j1 < n:
        for k in range( (n - j1) ):
            left += '.'
            top += colV[ (n-1-k) ]

    m = i1
    n = j1

    while m > 0 or n > 0:
        if( table[m][n][0] == DIAG ):
            #print ('diagonal detected ', table[m][n])
            n -= 1
            m -= 1
            top = top + colV[n]
            left = left + rowV[m]
        else:
            if( table[m][n][0] == RIGHT ):
                n -= 1
                if m == 0:
                    left += '.'
                else:
                    left += '_'
                top = top + colV[n]

            else:
                if( table[m][n][0] == DOWN ):
                    m -= 1
                    if n == 0:
                        top = top + '.'
                    else:
                        top += '_'
                    left = left + rowV[m]
        #print(n, " top  ", top[::-1])
        #print(m, " left ", left[::-1])

    return ( score, top[::-1], left[::-1] )





def printTablePretty( aTable, colV, rowV ):
    stringo = '        | '
    for k in range(0, len(colV)):
        stringo += ' ' + colV[k] + '  | '
    print(stringo)

    for k in range(0, len(rowV)+1):
        stringo = ''
        if k == 0:
            stringo += ' :  '
        else:
            stringo += rowV[k-1] + ':  '

        for j in range(0, len(colV)+1):
            if (table[k][j][0] == DIAG):
                stringo += '*'
            if (table[k][j][0] == DOWN):
                stringo += '^'
            if (table[k][j][0] == RIGHT):
                stringo += '<'

            if (table[k][j][1] < 0):
                stringo += str(table[k][j][1]) + ' | '
            else:
                stringo += ' ' + str(table[k][j][1]) + ' | '

        print( stringo )

def printBestAlignments( table, colV, rowV ):
    n = len(colV)
    m = len(rowV)

    top = ''
    left = ''

    maximum = -1*n
    for k in range(2,m): #ignore last cell, counted below
        if table[k][n][1] > maximum:
            maximum = table[k][n][1]

    for k in range(2,n+1): #include last cell
        if table[m][k][1] > maximum:
            maximum = table[m][k][1]

    print("Best alignment score is ", maximum)
    for k in range( m ):
        if table[k][n][1] == maximum:
            (score, colS, rowS) = partialAlignment( table, colV, rowV, k, n)
            print('')
            print( colS )
            print( rowS )
    for k in range( n+1 ):
        if table[m][k][1] == maximum:
            (score, colS, rowS) = partialAlignment( table, colV, rowV, m, k)
            print('')
            print( colS )
            print( rowS )
    print('')

def printPrefixAlignment( table, colV, rowV):
    n = len(colV)
    m = len(rowV)

    top = ''
    left = ''

    maximum = -1*n
    for k in range(2,m): #ignore last cell, the optimal solution
        if table[k][n][1] > maximum:
            maximum = table[k][n][1]

    for k in range( m ):
        if table[k][n][1] == maximum:
            (score, colS, rowS) = partialAlignment( table, colV, rowV, k, n)
            print("optimal suffix-prefix alignment of score ", score, " at ", k, ", ", n)
            print( colS )
            print( rowS )

def printSuffixAlignment( table, colV, rowV):
    n = len(colV)
    m = len(rowV)

    top = ''
    left = ''

    maximum = -1*n
    for k in range(2,m): #ignore last cell, the optimal solution
        if table[m][k][1] > maximum:
            maximum = table[m][k][1]

    for k in range( n ):
        if table[m][k][1] == maximum:
            (score, colS, rowS) = partialAlignment( table, colV, rowV, m, k)
            print("optimal prefix-suffix alignment of score ", score, " at ", m, ", ", k)
            print( colS )
            print( rowS )



if __name__ == "__main__":

    pattern = re.compile(r'\s+')
    #pattern for all whitespace

    time.clock()

    f = open( "teststrings.txt", 'r' )

    lines = []
    for aLine in f:
        if ( len(aLine) > 2):
            lines.append(aLine)

    for k in range(0, len(lines), 2):
        (table, columnNames, rowNames) = buildTable( re.sub(pattern, '', lines[k]) , re.sub(pattern, '', lines[k+1])  )
        table = calculateTable( table, columnNames, rowNames )
        printTablePretty( table, columnNames, rowNames )
        printBestAlignments( table, columnNames, rowNames )








