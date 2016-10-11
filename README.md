# SmithWaterman
Python/Java Smith Waterman algorithm for string alignment (generic). 

Java implementation with advanced tools specific to DNA alignments.

## Packages
	SmithWaterman.py	Calculates string alignments; blind to DNA code
						Running this program demonstrates the library functionality
	
	smithwatermanalignmnet (Java)
		SmithWaterman.java			Calculates string alignments; blind to DNA code.  A port of above code.
		SmithWatermanAdvanced.java	Aligns DNA concientiously according to DNA character set definition, which includes wildcards
		DNAcodes.java				Definition of DNA coding and character equivalency\
		TableEntry.java				Helper Class (a simple struct) to SmithWaterman
		PartialAlignment.java		Helper Class to SmithWaterman