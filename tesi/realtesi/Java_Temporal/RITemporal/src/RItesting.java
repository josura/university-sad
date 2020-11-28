/*
MAIN CLASS
Run RI algorithm for finding the number of occurrences of a query graph in the target graph
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

import gnu.trove.iterator.TIntObjectIterator;

public class RItesting
{

	/*
	Main method
	@param args: values of input parameters for RI
	*/
	public static void main(String[] args)
    {
		
		    
		TemporalGraph query = new TemporalGraph(true, 4);
	    query.addEdge(0,1,1).addEdge(0,2,1).addEdge(1,3,3).addEdge(3,2,4).addEdge(1,2,8);
	    TemporalGraph target = new TemporalGraph(true, 7);
	    target.addEdge(0,2,1).addEdge(2,1,2).addEdge(2,3,2).addEdge(2,4,2).addEdge(3,5,3).addEdge(3,6,3).addEdge(5,6,6).addEdge(6,4,4).addEdge(5,0,4).addEdge(3,4,8);
	       
	    TemporalGraph query2 = new TemporalGraph(true, 4);
	    query2.addEdge(1,0,2).addEdge(0,2,8).addEdge(2,1,3).addEdge(1,3,1).addEdge(3,0,4);
	    TemporalGraph target2 = new TemporalGraph(true, 14);
	    target2.addEdge(0,1,1).addEdge(0,3,2).addEdge(1,4,3).addEdge(1,3,5).addEdge(2,0,4).addEdge(3,2,10).addEdge(4,5,4).addEdge(5,6,6).addEdge(5,3,2).addEdge(6,3,4).
	        addEdge(6,8,12).addEdge(6,10,9).addEdge(7,6,6).addEdge(7,9,5).addEdge(8,7,7).addEdge(9,6,9).
	        addEdge(10,12,2).addEdge(11,10,3).addEdge(11,12,1).addEdge(11,13,8).addEdge(13,10,20);
	    
	    FileManagerTemporal fmMacktarget  = new FileManagerTemporal();
	    FileManagerTemporal fmMackquery = new FileManagerTemporal();
	    TemporalGraph netMack= fmMacktarget.readGraph("data/na_graph.gdf");
	    TemporalGraph queryMack= fmMackquery.readGraph("data/na_query1.gdf");
	    
	    
	    Vector<Integer> nodi = new Vector<>();
	    nodi.add(10);nodi.add(8);nodi.add(5);
	    TemporalGraph testSub = netMack.subgraph(nodi);
	    
	    RISolverTemporal rinetMack = new RISolverTemporal(netMack, false);
	    rinetMack.solve(queryMack, 5000);
	    System.out.println("numero match net " + rinetMack.getNumMatches());
	   
	    FileManagerTemporal fmTesttarget  = new FileManagerTemporal();
	    FileManagerTemporal fmTestquery = new FileManagerTemporal();
	    TemporalGraph netTest= fmTesttarget.readGraph("data/ia-enron-email-dynamic.edges");
	    TemporalGraph queryTest= fmTestquery.readGraph("data/query.gdf");
	    RISolverTemporal rinetTest = new RISolverTemporal(netTest, false);
	    rinetTest.solve(queryTest, 5000);
	    System.out.println("numero match net " + rinetTest.getNumMatches());
	   
        
        System.out.println("finished");
    }
	
	/*
	Print help string for RI usage
	*/
    public static void printHelp()
    {
        String help = "Usage: java -cp ./out RI -t <networkFile> -q <queriesFile> "+
                "[-ind -o <resultsFile>]\n\n";
        help+="REQUIRED PARAMETERS:\n";
        help+="-n\tInput network file\n";
        help+="-t\tInput queries file\n\n";
        help+="OPTIONAL PARAMETERS:\n";
        help+="-ind\tSearch for induced queries (default=non-induced queries)\n";
        help+="-o\tOutput file where results will be saved (default=results.txt)\n";
        System.out.println(help);
    }
}
