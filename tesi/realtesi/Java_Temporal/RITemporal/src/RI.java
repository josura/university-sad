/*
MAIN CLASS
Run RI algorithm for finding the number of occurrences of a query graph in the target graph
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

import gnu.trove.iterator.TIntObjectIterator;

public class RI
{

	/*
	Main method
	@param args: values of input parameters for RI
	*/
	public static void main(String[] args)
    {/*
        //Input parameters
		//Path for the input graph file
        String netFile=null;
        //Path for the query graph file
        String queriesFile=null;
		//Induced or non-induced counting?
        boolean induced=false;
		//Output result file
        String outputFile="results.txt";

        //Reading input parameters
        int i;
        for (i=0;i<args.length;i++)
        {
            switch (args[i])
            {
                case "-t" -> netFile = args[++i];
                case "-q" -> queriesFile = args[++i];
                case "-ind" -> induced = true;
                case "-o" -> outputFile = args[++i];
                default -> {
                    System.out.println("Error! Unrecognizable command '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
                }
            }
        }

        //Error in case network file is missing
        if(netFile==null)
        {
            System.out.println("Error! No input network has been specified!\n");
            printHelp();
            System.exit(1);
        }
        //Error in case queries file is missing
        if(queriesFile==null)
        {
            System.out.println("Error! No queries file has been specified!\n");
            printHelp();
            System.exit(1);
        }

        //Read input network
        System.out.println("\nReading graph file...");
        FileManager fm=new FileManager();
        Graph net=fm.readGraph(netFile);

        //Read queries file
        System.out.println("Reading queries file...");
        //List of all read queries
        Vector<Graph> setQueries=fm.readQueries(queriesFile);
        //List of counts, one for each read query
        Vector<Long> setCounts=new Vector<>();
        //List of all running times, one for each read query
        Vector<Double> setRunningTimes=new Vector<>();

        //Run RI algorithm
        RISolver ri=new RISolver(net,induced);
        for(i=0;i<setQueries.size();i++)
        {
            System.out.println("Matching query "+(i+1)+"...");
            long inizio=System.currentTimeMillis();
            Graph q=setQueries.get(i);
            ri.solve(q);
            long numOccs=ri.getNumMatches();
            setCounts.add(numOccs);
            double fine=System.currentTimeMillis();
            double totalTime=(fine-inizio)/1000;
            setRunningTimes.add(totalTime);
            System.out.println("Done! Found "+numOccs+" occurrences");
        }

        //Write results to output file
        fm.writeResults(setQueries,setCounts,setRunningTimes,outputFile);
        System.out.println("Results written in "+outputFile);*/
		
		    
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
	   
	    /*FileManagerTemporal fm  = new FileManagerTemporal();
	    String nomeDataset = "copresence-InVS15.edges";
	    TemporalGraph net= fm.readGraph("data/"+nomeDataset);
	    
	    RISolverTemporal rinet = new RISolverTemporal(net, false);
	    rinet.solve(query2, 1000);
	    System.out.println("numero match net" + rinet.getNumMatches());
	    
	    //List of counts, one for each read query
        Vector<Long> setCounts=new Vector<>();
        //List of all running times, one for each read query
        Vector<Double> setRunningTimes=new Vector<>();

        Vector<TemporalGraph> setQueries= new Vector<>();
        setQueries.add(query);
        setQueries.add(query2);
        String outputFile="risultati"+nomeDataset+".csv";
        
        for(int i=0;i<setQueries.size();i++)
        {
            System.out.println("Matching query "+(i+1)+"...");
            long inizio=System.currentTimeMillis();
            TemporalGraph q=setQueries.get(i);
            rinet.solve(q,1000);
            long numOccs=rinet.getNumMatches();
            setCounts.add(numOccs);
            double fine=System.currentTimeMillis();
            double totalTime=(fine-inizio)/1000;
            setRunningTimes.add(totalTime);
            System.out.println("Done! Found "+numOccs+" occurrences");
        }
        
        fm.writeResults(setQueries, net, setCounts, setRunningTimes, outputFile);
	    
	    /*ri.solve(queryNorm);
	    System.out.println("numero match" + ri.getNumMatches());
	    */
	    /*
	    FileManagerTemporal fm  = new FileManagerTemporal();
	    String nomeDataset = "copresence-InVS15.edges";
	    TemporalGraph net= fm.readGraph("data/"+nomeDataset);
	    
	    RISolverTemporal rinet = new RISolverTemporal(net, false);
	    
	    //List of counts, one for each read query
        Vector<Long> setCounts=new Vector<>();
        //List of all running times, one for each read query
        Vector<Double> setRunningTimes=new Vector<>();
        String queriesFile = "queries/queries.graph";

        Vector<TemporalGraph> setQueries= fm.readQueries(queriesFile);
        String outputFile="risultaticumulativi"+nomeDataset+".csv";
        
        for(int i=0;i<setQueries.size();i++)
        {
            System.out.println("Matching query "+(i+1)+"...");
            long inizio=System.currentTimeMillis();
            TemporalGraph q=setQueries.get(i);
            rinet.solve(q,1000);
            long numOccs=rinet.getNumMatches();
            setCounts.add(numOccs);
            double fine=System.currentTimeMillis();
            double totalTime=(fine-inizio)/1000;
            setRunningTimes.add(totalTime);
            System.out.println("Done! Found "+numOccs+" occurrences");
        }
        
        fm.writeResults(setQueries, net, setCounts, setRunningTimes, outputFile);
	    */
	    
	    System.out.println( target2.randomSubgraph(4));
	    
	    FileManagerTemporal fm  = new FileManagerTemporal();
	    String nomeDataset = "ca-cit-HepPh.edges";
	    TemporalGraph netQueries= fm.readGraph("data/fb-forum.edges");

	    TemporalGraph net= fm.readGraph("data/"+nomeDataset);
	    
	    RISolverTemporal rinet = new RISolverTemporal(net, false);
	    
	    //List of counts, one for each read query
        Vector<Long> setCounts=new Vector<>();
        //List of all running times, one for each read query
        Vector<Double> setRunningTimes=new Vector<>();

        Vector<TemporalGraph> setQueries = new Vector<>(67);
        for (int i = 3; i < 70; i++) {

            setQueries.add(netQueries.randomWeaklyConnectedSubgraph(i));
		}
        String outputFile="risultatiQueryincrementali"+nomeDataset+".csv";
        
        for(int i=0;i<setQueries.size();i++)
        {
            System.out.println("Matching query "+(i+1)+"...");
            long inizio=System.currentTimeMillis();
            TemporalGraph q=setQueries.get(i);
            rinet.solve(q,1000);
            long numOccs=rinet.getNumMatches();
            setCounts.add(numOccs);
            double fine=System.currentTimeMillis();
            double totalTime=(fine-inizio)/1000;
            setRunningTimes.add(totalTime);
            System.out.println("Done! Found "+numOccs+" occurrences");
        }
        
        fm.writeResults(setQueries, net, setCounts, setRunningTimes, outputFile);
	    
        
        
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
