import static org.junit.jupiter.api.Assertions.*;

import java.util.Vector;

import org.junit.Before;
import org.junit.jupiter.api.Test;

class RISolverTemporalTest {

	TemporalGraph provaQuery= new TemporalGraph(true, 4);
	TemporalGraph provaTarget= new TemporalGraph(true, 14);
	RISolverTemporal ri1 = null;
	
	@Before
	public void initialize() {
		provaQuery.addEdge(1,0,2).addEdge(0,2,8).addEdge(2,1,3).addEdge(1,3,1).addEdge(3,0,4);
		provaTarget.addEdge(0,1,1).addEdge(0,3,2).addEdge(1,4,3).addEdge(1,3,5).addEdge(2,0,4).addEdge(3,2,10).addEdge(4,5,4).addEdge(5,6,6).addEdge(5,3,2).addEdge(6,3,4).
	        addEdge(6,8,12).addEdge(6,10,9).addEdge(7,6,6).addEdge(7,9,5).addEdge(8,7,7).addEdge(9,6,9).
	        addEdge(10,12,2).addEdge(11,10,3).addEdge(11,12,1).addEdge(11,13,8).addEdge(13,10,20);
		
		Contact test = provaQuery.getInAdjListTimes()[0].get(20);
		ri1 = new RISolverTemporal(provaTarget, false);
		ri1.solve(provaQuery, 5);
	}
	
	@Test
	void testSolve() {
		initialize();
		assertEquals(ri1.getNumMatches(), 2);
	}
	

}
