/*
MAIN CLASS
Run RI algorithm for finding the number of occurrences of a query graph in the target graph
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

import gnu.trove.iterator.TIntObjectIterator;

public class RIIncrementalTarget
{

	/*
	Main method
	@param args: values of input parameters for RI
	*/
	public static void main(String[] args)
    {
		FileManagerTemporal fmnet  = new FileManagerTemporal();
	    FileManagerTemporal fmquery  = new FileManagerTemporal();
	    //String nomeDataset = "fb-forum.edges";
	    //String nomeDataset = "alreadymapped/SFHH-conf-sensor.edges";
	    String nomeDataset = "ia-frwikinews-user-edits/ia-frwikinews-user-edits.edges";
	    //String nomeDataset = "soc-epinions-trust-dir.edges";
	    TemporalGraph netQueries= fmquery.readGraph("data/alreadymapped/SFHH-conf-sensor.edges");

	    TemporalGraph net= fmnet.readGraph("data/"+nomeDataset);
	    
	    String fileTarget = "/media/josura/E094955094952A54/programmi/universita/university-sad/tesi/realtesi/temporal_subgraph_isomorphism/mydata/target.gdf";
    	fmnet.writeGraph(fileTarget,net);
    
	    
	    //List of counts, one for each read query
        Vector<Long> setCounts=new Vector<>();
        //List of all running times, one for each read query
        Vector<Double> setRunningTimes=new Vector<>();
        
        //TemporalGraph query = netQueries.randomWeaklyConnectedSubgraph(3);

        TemporalGraph query = new TemporalGraph(true, 4);
	    query.addEdge(0,1,1).addEdge(0,2,1).addEdge(1,3,3).addEdge(3,2,4).addEdge(1,2,8);
	    
        Vector<TemporalGraph> setTarget = new Vector<>(97);
        for (int i = 10000; i < 20000; i+=100) {
        	TemporalGraph randomTarget = net.randomWeaklyConnectedSubgraph(i); 
        	if(randomTarget!=null) {
        		setTarget.add(randomTarget);
        		//String writeFile = "/media/josura/E094955094952A54/programmi/universita/university-sad/tesi/realtesi/temporal_subgraph_isomorphism/mydata/target" + i/100 + ".gdf";
            	//fmnet.writeGraph(writeFile,randomTarget);
        	}
        	else i -=100;
        	
        }
        System.out.println("finished creating targets");
        String outputFile="risultatiTargetincrementali2.csv";
        
        for(int i=0;i<setTarget.size();i++)
        {
            System.out.println("Matching query "+(i+1)+"...");
            long inizio=System.currentTimeMillis();
            TemporalGraph t=setTarget.get(i);
            RISolverTemporal rinet = new RISolverTemporal(t, false);
            rinet.solve(query,1000);
            long numOccs=rinet.getNumMatches();
            setCounts.add(numOccs);
            double fine=System.currentTimeMillis();
            double totalTime=(fine-inizio)/1000;
            setRunningTimes.add(totalTime);
            System.out.println("Done! Found "+numOccs+" occurrences");
        }
        
        fmnet.writeResults(setTarget, net, setCounts, setRunningTimes, outputFile);
	    
        
        
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
