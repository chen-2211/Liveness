/**
* The Liveness program implements a register allocation fucntion 
* for a valid "Liveness" source code. Specifically, the program parsed
* a given input soruce file line by line to retrieve its unique 
* variables and their liveness information. Based on these information,
* the programm allocate variables to a set of contiguous registers such that
* a minimal number of registers is used during run time. This following work  
* is a collaborative project. 
*
* @author DEYE CHEN (44624522), LUKE MCLEAN (44617582) 
* @version 2.0
* @since 2018-09-02
*/
package student;

import java.util.*;
import java.io.*;

/**
 *An instance of a parser class contain an arraylist of all variables in the source code.
 *The methods of a Parser should parse an input program line and store variables and its liveness information 
 *in the arraylist
 */
class Parser {

	ArrayList<Variable> varList;

	public Parser () {
		this.varList = new ArrayList<Variable>(); 
	}

	/* method for parsing a live statement
	 * @param: line contain live-in or live-out live straight 
	 * from the source code, liveTpe is string flag denoting the line type and lineNo is the line number 
	 * @add to array list containing all variables on the line
	 */
	public void parseLiveStmt (String line, String liveType, Integer lineNo) {
		String[] tokensSepbySpace = line.split("\\s+"); //(O(n))
		if (liveType.equals("in")) {
			for (int i=1; i<=tokensSepbySpace.length-1; i++) {
				//live-in line always contain new variables 
				addNewVariabletoList(tokensSepbySpace[i], lineNo); 
			}
		} 
		//live-out line always contain variables in the array list
		if (liveType.equals("out")) {
			for (int i=1; i<=tokensSepbySpace.length-1; i++) {
				//find variable and add a start line number
				addLineNoToVariableOnList(tokensSepbySpace[i], lineNo, "r"); 
			}
		}

	}

	/* method for parsing an assignment statement
	 * @param: line contain live-in or live-out live straight
	 * from the source code, and lineNo is line number 
	 * @add to array list containing all variables on the line
	 */
	public void parseAssignStmt (String line, Integer lineNo) {
		//check for last line
		if (line.matches("^live-out.*")) {
			parseLiveStmt(line, "out", lineNo); 
			return; 
		}
		String[] tokensSepByEq = line.split("(\\s*):=(\\s*)"); 
		//LHS variable is always in array list
		if (addLineNoToVariableOnList(tokensSepByEq[0], lineNo, "l") == -1) {
			addNewVariabletoList(tokensSepByEq[0], lineNo); 
		}
		parseExpr(tokensSepByEq[1], lineNo); 
	}

	/* method for parsing a an exoression line
	 * @param: expr is a string representing an arithmetic expression lineNo is the line number 
	 * @return an array list containing all variables on the line
	 */
	public void parseExpr (String rhs, Integer lineNo) {
		//remove mem[]
		if (rhs.matches("^mem\\[.*")) {
			rhs = rhs.substring(4, rhs.length()); 
			StringTokenizer st = new StringTokenizer(rhs, "]");
			rhs = st.nextToken(); 
		}  
		//remove arithmetic opeartors
		String[] operands = rhs.split("((\\s*)(\\*|/|\\+|-)(\\s*))"); 
		
		for (int i=0; i<=operands.length-1; i++) {
			//ignore digits
			if (!operands[i].matches("(\\d+)(\\s*)")) {
				//RHS variable will get their endline number updated/added
				addLineNoToVariableOnList(operands[i], lineNo, "r");
			}	
		}

	}

	/* method for adding a new LHS variable to list 
	 * @param: name is the name of the variable to be added to list
	 * 		   
	 */
	public void addNewVariabletoList(String varName, Integer lineNo) {
			Variable var = new Variable(varName);
			var.addStartLineNo(lineNo); 
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
					//either update endline number or add a new one
					if (this.varList.get(i).startAtLines.size() != this.varList.get(i).endAtLines.size()) {
						this.varList.get(i).addEndLineNo(lineNo);
					} else {
						//update the last end line number
						this.varList.get(i).endAtLines.set(this.varList.get(i).endAtLines.size()-1, lineNo); 
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

}

/*
 * Variable class contain liveness information of a variable
 */
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


class Register {

	ArrayList<Variable> varsOnReg; 

	public Register() {
		varsOnReg = new ArrayList<Variable>(); 
	}
}

class RegisterList {

	//register number is its index on regList + 1 
	ArrayList<Register> regList;
	
	public RegisterList() {
		regList = new ArrayList<Register>(); 
	}

	/* method for a new register to list with variable assigned to it 
	 * @param: var is the variable to be assigned to the new register
	 */
	public void addNewReg(Variable var) {
		Register reg = new Register(); 
		reg.varsOnReg.add(var); 
		regList.add(reg); 
	}

	/* method for attempting assigning a variable to a register on register list
	 * @param: var is the variable to be assigned
	 * @return true if var is successfully assigned, false otherwise 
	 */
	public boolean assigned(Variable var) {
		//try to assign it to a register on list
		for (int i=0; i<=regList.size()-1; i++) {
			for (int j=0; j<=regList.get(i).varsOnReg.size()-1; j++) {
				if (overLap(regList.get(i).varsOnReg.get(j), var)) {
					//try next register if there's an overlap
					break; 
				}
				//if it doesn overlap with any variable on a register
				//then assign variable to register 
				if (j==regList.get(i).varsOnReg.size()-1) {
					regList.get(i).varsOnReg.add(var); 		
					return true; 
				}
			}
		}
		return false; 
	}

	/* method for checking whether 2 variables "overlap", that is, they both
	 * exist on a same period of time
	 * @param: var1 is the first variable for comparsion 
	 *         var2 is the second variable for comparion
	 * @return true if the 2 variables "overlap", false otherwise. 
	 */
	public boolean overLap(Variable var1, Variable var2) {
		for (int i=0; i<var1.startAtLines.size(); i++) {
			for (int j=0; j<var2.startAtLines.size(); j++) {
				if ((var1.endAtLines.get(i).intValue()>=var2.endAtLines.get(j).intValue() &&
					var2.endAtLines.get(j).intValue()>=var1.startAtLines.get(i).intValue()) 
				    || (var1.endAtLines.get(i).intValue()>=var2.startAtLines.get(j).intValue() &&
				       var2.startAtLines.get(j).intValue()>=var1.startAtLines.get(i).intValue())) {
					return true; 
				}
			}
		}
		return false; 
	}
}

//changed methods in class liveness to static to run in main 
public class Liveness {	
	/* method for allocating a list of variable to a register
 	* @param: inputVarList is the list of variables in the source code
 	* @return: a tree map containing a list of variables and their assigned registers
 	*/
	public static TreeMap<String, Integer> registerAllocation(ArrayList<Variable> inputVarList) {		 
		RegisterList inputRegList = new RegisterList(); 
		//For each variable, either assign it to an existing register or a new register
		for (int i=0; i<=inputVarList.size()-1; i++) {
			if (!inputRegList.assigned(inputVarList.get(i))) {
				inputRegList.addNewReg(inputVarList.get(i)); 
			}
		}
		//Map the register list to the tree map 
		TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
		for (int i=0; i<=inputRegList.regList.size()-1; i++) {
			for (int j=0; j<=inputRegList.regList.get(i).varsOnReg.size()-1; j++) {
				tm.put(inputRegList.regList.get(i).varsOnReg.get(j).name, i+1); 
			}
		} 
		return tm; 
	}

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
			//Read source code into memory 
			BufferedReader br = new BufferedReader(new FileReader(inFile)); 
			Parser p = new Parser();
			String currentLine = br.readLine();
			p.parseLiveStmt(currentLine, "in", 1); 
			int lineCounter = 2; 
			while ((currentLine = br.readLine())!=null) {
				//Parse and store variables 
				p.parseAssignStmt(currentLine, new Integer(lineCounter)); 
				lineCounter++;
			}
			return registerAllocation(p.varList); 
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		return null; 
	}

	// PRE: t represents a valid register allocation
	// POST: the register allocation in t is written to file solnName
	public static void writeSolutionToFile(TreeMap<String, Integer> t, String solnName) {
		File outFile = null; 
		if (solnName.length() > 0) {
			outFile = new File(solnName);
		} else {
			System.out.println("Invalid output"); 
		}	
		try {
			//write the number of register first
			PrintWriter pw = new PrintWriter(new FileWriter(outFile)); 
			pw.println(t.values().stream().max(Integer::compare).get());
			for (Map.Entry m:t.entrySet()) {
				pw.println(m.getKey()+" "+m.getValue()); 
			}
			pw.close(); 
		} catch(IOException e) {
			e.printStackTrace(); 
		}
	}

	public static void main(String[] args) {	
		//Assuming input file is arguement 0 and output file is arguement 1
		writeSolutionToFile(generateSolution(args[0]), args[1]);
	}
	
}
