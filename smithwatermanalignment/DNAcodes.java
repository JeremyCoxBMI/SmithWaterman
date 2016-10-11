package smithwatermanalignment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class contains the equivalency information for DNA letter codes.
 */
public class DNAcodes {

    //see    https://en.wikipedia.org/wiki/FASTA_format


    /**
     * Contains many characters (HashSet) that are equivalent to the first
     */
    public static HashMap<Character, HashSet<Character>> equivalency;

//    public static HashMap<Character, HashSet<Character>> substituteSNP;

    /**
     * Maps DNA or RNA letter to its DNA complement
     */
    public static HashMap<Character, Character> complement;


    public static HashSet<Character> all;

    static {
        equivalency = new HashMap<Character, HashSet<Character>>();
//        substituteSNP = new HashMap<Character, HashSet<Character>>();
        complement = new HashMap<Character, Character>();

        all = new HashSet<Character>( Arrays.asList('a','A','t','T','u','U','c','C','g','G','n','N',
                'R','r','Y','y','K','k','M','m','S','s','W','w','B','b',
                'D','d','H','h','V','v','N','n','?'
        ) );
        //Gap character '-' matches nothing
        //Singles
        equivalency.put(  'A', new HashSet<Character>( Arrays.asList('A','a') )  );
        equivalency.put(  'a', new HashSet<Character>( Arrays.asList('A','a') )  );
        equivalency.put(  'G', new HashSet<Character>( Arrays.asList('G','g') )  );
        equivalency.put(  'g', new HashSet<Character>( Arrays.asList('G','g') )  );
        equivalency.put(  'T', new HashSet<Character>( Arrays.asList('T','t','u','U') )  );
        equivalency.put(  't', new HashSet<Character>( Arrays.asList('t','T','u','U') )  );
        equivalency.put(  'U', new HashSet<Character>( Arrays.asList('U','u','t','T') )  );
        equivalency.put(  'u', new HashSet<Character>( Arrays.asList('u','U','t','T') )  );
        equivalency.put(  'C', new HashSet<Character>( Arrays.asList('C','c') )  );
        equivalency.put(  'c', new HashSet<Character>( Arrays.asList('C','c') )  );


        //Doubles
        writeCombinations(  'R', 'r', new HashSet<Character>( Arrays.asList('A','a','G','g') )  );

        writeCombinations(  'Y', 'y', new HashSet<Character>( Arrays.asList('C','T','U','c', 't', 'u') )  );

        writeCombinations(  'K', 'k', new HashSet<Character>( Arrays.asList('G','T','U','g', 't', 'u') )  );

        writeCombinations(  'M', 'm', new HashSet<Character>( Arrays.asList('C','A','c','a') )  );

        writeCombinations(  'S', 's', new HashSet<Character>( Arrays.asList('C','G','c','g') )  );

        writeCombinations(  'W', 'w', new HashSet<Character>( Arrays.asList('A','T','U','a','t','u') )  );

        //Triples
        writeCombinations(  'B', 'b', new HashSet<Character>( Arrays.asList('C','T','G','U','c','t','g','u') )  );

        writeCombinations(  'D', 'd', new HashSet<Character>( Arrays.asList('A','T','G','U','a','t','g','u') )  );

        writeCombinations(  'H', 'h', new HashSet<Character>( Arrays.asList('A','T','C','U','a','t','c','u') )  );

        writeCombinations(  'V', 'v', new HashSet<Character>( Arrays.asList('A','G','C','a','g','c') )  );

        //Wildcards
        equivalency.put(  'N', new HashSet<Character>( all )   );
        equivalency.put(  'n', new HashSet<Character>( all )   );
        equivalency.put(  '?', new HashSet<Character>( all )   );
        for (char c : equivalency.get('N')){
            equivalency.get(c).add('N');
            equivalency.get(c).add('n');
            equivalency.get(c).add('?');
        }


        // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
        // complements
        complement.put('a','T');
        complement.put('A','T');
        complement.put('t','A');
        complement.put('T','A');
        complement.put('u','A');
        complement.put('U','A');
        complement.put('c','G');
        complement.put('C','G');
        complement.put('g','C');
        complement.put('G','C');


        complement.put('r','Y');
        complement.put('R','Y');
        complement.put('y','R');
        complement.put('Y','r');
        complement.put('k','M');
        complement.put('K','M');
        complement.put('m','K');
        complement.put('M','K');
        complement.put('s','S');
        complement.put('S','S');
        complement.put('w','W');
        complement.put('W','W');
        complement.put('b','V');
        complement.put('B','V');
        complement.put('v','B');
        complement.put('V','B');
        complement.put('d','H');
        complement.put('D','H');
        complement.put('h','D');
        complement.put('H','D');
        complement.put('n','N');
        complement.put('N','N');
        complement.put('-','-');
        complement.put('?','N');

    }

    private static void writeCombinations(char letter1, char letter2, HashSet<Character> equals){
        equivalency.put(  letter1, equals  );
        equivalency.put(  letter2, equals  );
        for (char c : equals){
            equivalency.get(c).add(letter1);
            equivalency.get(c).add(letter2);
        }
        equivalency.get(letter1).add(letter1);
        equivalency.get(letter1).add(letter2);
    }


    public static boolean equals(char a, char b){
        if ( equivalency.containsKey(a) && equivalency.containsKey(b))
            return equivalency.get(a).contains(b);
        else
            return false;
    }

    /**
     * lookup complement DNA code
     * @param x
     * @return
     */
    public static char getComplement(char x){
        return complement.get(x);
    }

    /**
     * Returns INVERSE and REVERSE DNA sequence (or, commonly called Reverse Strand)
     * @param sequence
     */
    public static String reverseStrandSequence (String sequence) {
        String result = "";
        int len = sequence.length();
        for( int k = len-1; k >= 0; k--){
            if (complement.containsKey(sequence.charAt(k))) {
                result += getComplement(sequence.charAt(k));
            } else {
                System.err.println("Attempting to reverse strand illegal character : "+sequence.charAt(k)+" :  ABORTING");
                break;
            }
        }
        return result;
    }



    public static void main(String[] args){
        System.out.println("\nThese all should be true");
        System.out.println(DNAcodes.equals('N','n'));
        System.out.println(DNAcodes.equals('n','N'));
        System.out.println(DNAcodes.equals('M','A'));
        System.out.println(DNAcodes.equals('a','M'));
        System.out.println(DNAcodes.equals('W','W'));
        System.out.println(DNAcodes.equals('W','w'));
        System.out.println(DNAcodes.equals('w','W'));

        System.out.println("\nThese all should be false");
        System.out.println(DNAcodes.equals('&','n'));
        System.out.println(DNAcodes.equals('D','C'));
        System.out.println(DNAcodes.equals('c','D'));

        System.out.println("\ntesting reverseStrandSequence");
        String seq = "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAAT" +
                "AACATAT";
        System.out.println("IN  : "+seq);
        System.out.println("OUT : "+reverseStrandSequence(seq));


        System.out.println("\ntesting reverseStrandSequence");
        seq = "ATCCATTCCGTCATACACGCTAACCGGGAACAAAATCAATCTATCATGCACCAGATGTCCCGGACAAGAT";
        System.out.println("IN  : "+seq);
        System.out.println("OUT : "+reverseStrandSequence(seq));

    }

}

