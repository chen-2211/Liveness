package student;
import static org.junit.Assert.*;

import java.util.TreeMap;

import org.junit.Test;


public class Test1 {

	String dataDir = "/home/madras/teaching/18comp333/ass1/data/";
	String dataFileName = "ex1";
	String fInName = dataDir + dataFileName + ".dat";
	String solnInName = dataDir + dataFileName + ".out";

	@Test
	public void testReadWrite() {
		TreeMap<String, Integer> soln;
		Liveness a = new Liveness();
		soln = a.generateSolution(fInName);
		a.writeSolutionToFile(soln, solnInName);
		
//		fail("Not yet implemented");
	}

}
