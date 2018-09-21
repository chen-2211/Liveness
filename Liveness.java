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

	ArrayList<Variable> varList;

	public Parser () {
		this.varList = new ArrayList<Variable>(); 
	}

	/* method for parsing a live statement
	 * @param: line contain live-in or live-out live straight 
	 * from the source code
	 * @add to array list containing all variables on the line
	 */
	public void parseLiveStmt (String line, String liveType, Integer lineNo) {
		String[] tokensSepbySpace = line.split("\\s+"); //(O(n))
		if (liveType.equals("out")) {
			for (int i=1; i<=tokensSepbySpace.length-1; i++) {
				//live-out line always contain variables that are not on the list
				addNewVariabletoList(tokensSepbySpace[i], lineNo); 
			}
		} 
		//live-in line always contain variable that are on the list
		if (liveType.equals("in")) {
			for (int i=1; i<=tokensSepbySpace.length-1; i++) {
				//find variable and add a start line number
				addLineNoToVariableOnList(tokensSepbySpace[i], lineNo, "l"); 
			}
		}

	}

	public void parseAssignStmt (String line, Integer lineNo) {
		String[] tokensSepByEq = line.split("(\\s*):=(\\s*)"); 
		//add a start line number to the LHS token 
		addLineNoToVariableOnList(tokensSepByEq[0], lineNo , "l"); 
		parseExpr(tokensSepByEq[1], lineNo); 
	}

	public void parseExpr (String rhs, Integer lineNo) {
		//consider spaces afer "[" and before "]"
		if (rhs.matches("(mem\\[)([\\s\\S]*)")) {
			rhs = rhs.substring(4, rhs.length()); 
			StringTokenizer st = new StringTokenizer(rhs, "]");
			rhs = st.nextToken(); 
		}  
		//remove opeartors
		String[] operands = rhs.split("((\\s*)(\\*|/|\\+|-)(\\s*))"); 
		//remove digits
		for (int i=0; i<=operands.length-1; i++) {
			if (!operands[i].matches("(\\d+)(\\s*)")) {
				if (addLineNoToVariableOnList(operands[i], lineNo, "r") == -1) {
					addNewVariabletoList(operands[i], lineNo); 
				}
				
			}	
		}

	}

	public void addNewVariabletoList(String varName, Integer lineNo) {
			Variable var = new Variable(varName);
			var.addEndLineNo(lineNo); 
			this.varList.add(var);
	}

	/* method for attempting to add a line number to a variable on the list 
	 * @param: varOnLine is the name of a variable possibly on the list
	 * 		   lineNo is the line number 
	 *		   flag dictates whether varsOnLine is a RHS (input "r") variable 
	 *		   or LHS (input "l") variable
	 * @return: 1 if line number is sucessfully added to an existing variable on the list, -1 otherwise. 
	 */
	public int addLineNoToVariableOnList(String varOnLine, Integer lineNo, String flag) {
		for (int i=0; i<=varList.size()-1; i++) {
			if (varList.get(i).name.equals(varOnLine)) {
				if (flag.equals("r")) {
					//only add an endline number if variable is initating a new live
					if (this.varList.get(i).startAtLines.size() == this.varList.get(i).endAtLines.size()) {
						this.varList.get(i).addEndLineNo(lineNo);
					}
					return 1; 
				} else if (flag.equals("l")) {
					this.varList.get(i).addStartLineNo(lineNo);
					return 1;
				}		
			}
		}
		return -1; 
	}

	/* method printing all object's variables and all its liveness information  
	 * this method is used for debugging and is not part of the application
	 */
	public void printInfo() {
		for (int i=0; i<=this.varList.size()-1; i++) {
			System.out.println(this.varList.get(i).name); 
			for(int j=0; j<=this.varList.get(i).startAtLines.size()-1; j++) {
				System.out.println("StartLine: "+this.varList.get(i).startAtLines.get(j)); 
				System.out.println("EndLine: "+this.varList.get(i).endAtLines.get(j)); 
			}
		}
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
		File inFile = null; 
		if (fInName.length() > 0) {
			inFile = new File(fInName);
		} else {
			System.out.println("Please input a valid file arguement"); 
		}	
		try {
			//Read the entire source code into memory 
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			ArrayList<String> programLines = new ArrayList<String>(); 
			String currentLine;
			while ((currentLine = br.readLine())!=null) {
				programLines.add(currentLine);
			}
			//Parse and store variables
			Parser p = new Parser();
			p.parseLiveStmt(programLines.get(programLines.size()-1), "out", programLines.size()); 
			for (int i=programLines.size()-2; i>=1; i--) {
				p.parseAssignStmt(programLines.get(i), i+1); 
			} 
			p.parseLiveStmt(programLines.get(0), "in", 1);
			p.printInfo();  
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		return null; 
	}

	// PRE: t represents a valid register allocation
	// POST: the register allocation in t is written to file solnName
	public static void writeSolutionToFile(TreeMap<String, Integer> t, String solnName) {
	}

	public static void main(String[] args) {	
		//Assuming input file is arguement 0 and output file is arguement 1
		generateSolution(args[0]); 
	}
	
}
