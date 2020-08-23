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
    {
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
        System.out.println("Results written in "+outputFile);
	    
        
        
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
