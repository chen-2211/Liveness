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


class Variable {

	String name;
	ArrayList<Integer> startLines; 
	ArrayList<Integer> endLines;

	public Variable(String variableName) {
		this.startLines = new ArrayList<Integer>();
		this.endLines = new ArrayList<Integer>();
		this.name = variableName; 
	}

	/**
	 * method for adding line number at which the variable end its current live
	 */
	public void addEndLineNo(Integer i) {
		this.endLines.add(i); 
	}

	/**
	 * method for adding line number at which the variable starts its live
	 */
	public void addStartLineNo(Integer i) {
		this.startLines.add(i); 
	}
} 

class VariableList {

	ArrayList<Variable> varList; 
	public VariableList() {
		this.varList = new ArrayList<Variable>(); 
	}

	/* method for adding parsed variable and its liveness information
	 * of an assignment line to the variable list object
	 * @param: varsOnLine contain an array list of parsed variable from an assignment line
	 *         lineNo contain the current line number
	 */
	public void addAssignLineVarToList(ArrayList<String> varsOnLine, Integer lineNo) {
		for (int i=1; i<=varsOnLine.size()-1; i++) {
			//retrive a valid index of a RHS variable only if it's in the list
			//-1 indcates the variable as new variable 
			int varIndexOnList = getIndex(varsOnLine.get(i)); 
			if (varIndexOnList == -1) {
				addNewVariabletoList(varsOnLine.get(i), lineNo); 
			} else if (varList.get(varIndexOnList).startLines.size() == varList.get(varIndexOnList).endLines.size()) {
				varList.get(varIndexOnList).addEndLineNo(lineNo); 
			}
		}
		//Add the line number to the LHS variable already in the list (LHS variables are always in the list)
		AddLineNoToVariableOnList(varsOnLine.get(0), lineNo, "l");
	}

	/* method for adding parsed variable and its liveness information to variable list
	 * @param: varsOnLine contain an array list of parsed variable from an assignment line
	 *         lineNo contain the current line number
	 */
	public void addLiveOutLineVarToList(ArrayList<String> varsOnLine, Integer lineNo) {
		for (int i=0; i<=varsOnLine.size()-1; i++) {
			addNewVariabletoList(varsOnLine.get(i), lineNo); 
		}
	}

	/* method for returning an index of an input variable if it is on the list, otherwise return -1.
	 * @param: varOnLine contain a parsed variable from source code line
	 * @return: return index of varOnLine if it is on the list, otherwise return -1
	 */
	public int getIndex(String varOnLine) {
		for (int i=0; i<=varList.size()-1; i++) {
			if (this.varList.get(i).name.equals(varOnLine)) {
				return i;
			}
		}
		return -1;
	}

	/* method for adding a new RHS variable to list 
	 * @param: name is the name of the variable to be added to list
	 * 		   lineNo is the line number at which the new variable end its live
	 */
	public void addNewVariabletoList(String name, Integer lineNo) {
			Variable var = new Variable(name);
			var.addEndLineNo(lineNo); 
			this.varList.add(var);
	}

	/* method for adding variables on a live-in statement to list 
	 * @param: varsOnLine contain an array list of parsed variable on a live-in line
	 * 		   lineNo is the line number at which the new variable end its live
	 */
	public void addLiveInLineVarToList(ArrayList<String> varsOnLine, Integer lineNo) {
		for (int i=0; i<=varsOnLine.size()-1; i++) {
			AddLineNoToVariableOnList(varsOnLine.get(i),lineNo, "l");
		}		
	}

	/* method for adding a line number to a variable on the list 
	 * @param: varOnLine is the name of a variable already in the list
	 * 		   lineNo is the line number 
	 *		   flag dictates whether varsOnLine is a RHS (input "r") variable 
	 *		   or LHS (input "l") variable
	 */
	public void AddLineNoToVariableOnList(String varOnLine, Integer lineNo, String flag) {
		for (int i=0; i<=varList.size()-1; i++) {
			if (varList.get(i).name.equals(varOnLine)) {
				if (flag.equals("r")) {
					this.varList.get(i).addEndLineNo(lineNo);
					break; 
				} else if (flag.equals("l")) {
					this.varList.get(i).addStartLineNo(lineNo);
					break;
				}		
			}
		}
	}

	/* method printing all object's variables and all its liveness information  
	 * this method is used for debugging and is not part of the application
	 */
	public void printInfo() {
		for (int i=0; i<=this.varList.size()-1; i++) {
			System.out.println(this.varList.get(i).name); 
			for(int j=0; j<=this.varList.get(i).startLines.size()-1; j++) {
				System.out.println("StartLine: "+this.varList.get(i).startLines.get(j)); 
				System.out.println("EndLine: "+this.varList.get(i).endLines.get(j)); 
			}
		}
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
	//note: this method can be optimized by dynamic programming 
	public boolean overLap(Variable var1, Variable var2) {
		for (int i=0; i<var1.startLines.size(); i++) {
			for (int j=0; j<var2.startLines.size(); j++) {
				if ((var1.endLines.get(i).intValue()>=var2.endLines.get(j).intValue() &&
					var2.endLines.get(j).intValue()>=var1.startLines.get(i).intValue()) 
				    || (var1.endLines.get(i).intValue()>=var2.startLines.get(j).intValue() &&
				       var2.startLines.get(j).intValue()>=var1.startLines.get(i).intValue())) {
					return true; 
				}
			}
		}
		return false; 
	}

	/* method for printing all registers (and its variables) on list, this 
	 * is used for debugging and is not part of the application
	 */
	public void printInfo() {
		for (int i=0; i<=regList.size()-1; i++) {
			System.out.println("regNo: "+ i); 
			for (int j=0; j<=regList.get(i).varsOnReg.size()-1; j++) {
				System.out.println(regList.get(i).varsOnReg.get(j).name); 
			}
		}
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
			System.out.println("Invalid Arguement"); 
		}	
		//read all input lines into memory, then parse and store variable name
		//and its liveness information (from the last to the first line) 
		//into a variable list called parsedVarList. The program then allocates
		//register based on this information 
		try {		
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			ArrayList<String> programLines = new ArrayList<String>(); 
			String currentLine;
			while ((currentLine = br.readLine())!=null) {
				programLines.add(currentLine);
			}
			VariableList parsedVarList = new VariableList();
			parsedVarList.addLiveOutLineVarToList(parseLiveStmt(programLines.get(programLines.size()-1)), programLines.size());
			for (int i=programLines.size()-2; i>=1; i--) {
				parsedVarList.addAssignLineVarToList(parseAssignStmt(programLines.get(i)), i+1); 
			} 
			parsedVarList.addLiveInLineVarToList(parseLiveStmt(programLines.get(0)), 1);
			//parsedVarList.printInfo();
			br.close(); 
			return registerAllocation(parsedVarList);  
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		//code should not reach here
		return null;
	}

	/* method for allocating a list of variable to a register
	 * @param: inputVarList is the list of variables in the source code
	 * @return: a tree map containing a list of variables and their assigned registers
	 */
	public static TreeMap<String, Integer> registerAllocation(VariableList inputVarList) {		 
		RegisterList inputRegList = new RegisterList(); 
		//For each variable, either assign it to an existing register or a new register
		for (int i=0; i<=inputVarList.varList.size()-1; i++) {
			if (!inputRegList.assigned(inputVarList.varList.get(i))) {
				inputRegList.addNewReg(inputVarList.varList.get(i)); 
			}
		}
		//inputRegList.printInfo(); 
		//Map the register list to the tree map 
		TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
		for (int i=0; i<=inputRegList.regList.size()-1; i++) {
			for (int j=0; j<=inputRegList.regList.get(i).varsOnReg.size()-1; j++) {
				tm.put(inputRegList.regList.get(i).varsOnReg.get(j).name, i+1); 
			}
		} 
		return tm; 
	}

	/* method for parsing a live statement
	 * @param: line contain live-in or live-out live straight 
	 * from the source code
	 * @return an array list containing all variables on the line
	 */
	public static ArrayList<String> parseLiveStmt (String line) {
		ArrayList<String> Vars = new ArrayList<String>();
		String[] tokensSepbySpace = line.split("\\s+"); 
		for (int i=1; i<=tokensSepbySpace.length-1; i++) {
			Vars.add(tokensSepbySpace[i]);
		}
		return Vars; 
	}

	/* method for parsing an assignment statement
	 * @param: line contain an assignment statement straight from source code
	 * @return an array list containing all variables on the line
	 */
	public static ArrayList<String> parseAssignStmt (String line) {
		ArrayList<String> Vars = new ArrayList<String>();
		String[] tokensSepbyEq = line.split("(\\s*)(:=)(\\s*)"); 
		Vars.add(tokensSepbyEq[0]);
		Vars.addAll(parseRHS(tokensSepbyEq[1])); 
		return Vars; 
	}

	/* method for parsing a RHS component of an assignment statement
	 * @param: rhs string of all character after ":=" on an assignment line
	 * @return an array list containing all variables on the line
	 */
	public static ArrayList<String> parseRHS (String rhs) {
		ArrayList<String> Vars = new ArrayList<String>();
		if (rhs.matches("(mem\\[)([\\s\\S]*)")) {
			rhs = rhs.substring(4, rhs.length()); 
			StringTokenizer st = new StringTokenizer(rhs, "]");
			rhs = st.nextToken(); 
		}  
		Vars.addAll(parseExpr(rhs));
		return Vars; 
	}

	/* method for parsing a an exoression line
	 * @param: expr is a string representing an arithmetic expression
	 * @return an array list containing all variables on the line
	 */
	public static ArrayList<String> parseExpr (String expr) {
		ArrayList<String> Vars = new ArrayList<String>();
		String[] operands = expr.split("((\\s*)(\\*|/|\\+|-)(\\s*))"); 
		for (int i=0; i<=operands.length-1; i++) {
			if (!operands[i].matches("(\\d+)(\\s*)")) {
				Vars.add(operands[i]); 
			}	
		}
		return Vars; 
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
