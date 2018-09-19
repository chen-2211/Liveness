/**
* The Liveness program implements a register allocation fucntion 
* for a valid "Liveness" source code. Specifically, the program parsed
* a given input soruce file line by line to retrieve its unique 
* variables and their liveness information. Based on these information,
* the programm optimally allocate them so a minimal number of registers 
* is used during run time. 
*
* @author DEYE CHEN
* @SID 44624522
* @version 1.0
* @since 2018-09-02
*/
package student;

import java.util.*;
import java.io.*;


//An instance of a parser class contain an arraylist of all variables in the source code
//the method should parse a given program line and store its info
class Parser {

	ArrayList<Variable> Vars;
	 
	/* method for parsing a live statement
	 * @param: line contain live-in or live-out live straight 
	 * from the source code
	 * @add to array list containing all variables on the line
	 */
	public void parseLiveStmt (String line, String liveType) {

	}

	public void parseAssignStmt (String line) {

	}
}


class Variable {

	String name;
	//example: say variable a is alive at line 2-5 and 9-11, then
	//a.startAtLine = {5, 2}, a.endAtLine = {11, 9} 
	ArrayList<Integer> startAtLines; 
	ArrayList<Integer> endAtLines;

	public Variable(String variableName) {
		this.startAtLines = new ArrayList<Integer>();
		this.endAtLines = new ArrayList<Integer>();
		this.name = variableName; 
	}

	/**
	 * method for adding line number at which the variable end its current live
	 */
	public void addEndLineNo(Integer i) {
		this.endAtLines.add(i); 
	}

	/**
	 * method for adding line number at which the variable starts its live
	 */
	public void addStartLineNo(Integer i) {
		this.startAtLines.add(i); 
	}
} 

















public class Liveness {	
	




	// PRE: fInName is a valid input file
	// POST: returns a TreeMap mapping variables (String) to registers (Integer) 
	public static TreeMap<String, Integer> generateSolution(String fInName) {
		return null; 
	}

	// PRE: t represents a valid register allocation
	// POST: the register allocation in t is written to file solnName
	public static void writeSolutionToFile(TreeMap<String, Integer> t, String solnName) {
	}

	public static void main(String[] args) {	
		//Assuming input file is arguement 0 and output file is arguement 1
		
	}
	
}
