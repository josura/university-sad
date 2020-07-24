import java.util.Vector

//remove if not needed
import scala.collection.JavaConversions._

object RI {

  /*
	Main method
	@param args: values of input parameters for RI
	*/

  def main(args: Array[String]): Unit = {
//Path for the input graph file
    var netFile: String = null
//Path for the query graph file
    var queriesFile: String = null
//Induced or non-induced counting?
    var induced: Boolean = false
//Output result file
    var outputFile: String = "results.txt"
//Reading input parameters
    var i =0;
    while(i < args.length) {
        args(i) match {
            case "-t" => netFile = args(i)
            case "-q" => queriesFile = args(i)
            case "-ind" => induced = true; i -= 1;
            case "-o" => outputFile = args(i)
            case _ => {
                System.out.println("Error! Unrecognizable command '" + args(i) + "'");
                printHelp();
                System.exit(1);
            }
        }
        i += 2
    }
//Error in case network file is missing
    if (netFile == null) {
      println("Error! No input network has been specified!\n")
      printHelp()
      System.exit(1)
    }
//Error in case queries file is missing
    if (queriesFile == null) {
      println("Error! No queries file has been specified!\n")
      printHelp()
      System.exit(1)
    }
//Read input network
    println("\nReading graph file...")
    val fm: FileManager = new FileManager()
    val net: TemporalGraph = fm.readGraph(netFile)
//Read queries file
    println("Reading queries file...")
//List of all read queries
    val setQueries: Vector[TemporalGraph] = fm.readQueries(queriesFile)
//List of counts, one for each read query
    val setCounts: Vector[Long] = new Vector[Long]()
//List of all running times, one for each read query
    val setRunningTimes: Vector[Double] = new Vector[Double]()
//Run RI algorithm
    val ri: RISolver = new RISolver(net, induced)
    for (i <- 0 until setQueries.size) {
      println("Matching query " + (i + 1) + "...")
      val inizio: Long = System.currentTimeMillis()
      val q: TemporalGraph = setQueries.get(i)
      ri.solve(q)
      val numOccs: Long = ri.getNumMatches
      setCounts.add(numOccs)
      val fine: Double = System.currentTimeMillis()
      val totalTime: Double = (fine - inizio) / 1000
      setRunningTimes.add(totalTime)
      println("Done! Found " + numOccs + " occurrences")
    }
//Write results to output file
    fm.writeResults(setQueries, setCounts, setRunningTimes, outputFile)
    println("Results written in " + outputFile)
  }
//Input parameters
//Input parameters

  /*
	Print help string for RI usage
	*/

  def printHelp(): Unit = {
    var help: String = "Usage: java -cp ./out RI -t <networkFile> -q <queriesFile> " +
        "[-ind -o <resultsFile>]\n\n"
    help += "REQUIRED PARAMETERS:\n"
    help += "-n\tInput network file\n"
    help += "-t\tInput queries file\n\n"
    help += "OPTIONAL PARAMETERS:\n"
    help += "-ind\tSearch for induced queries (default=non-induced queries)\n"
    help += "-o\tOutput file where results will be saved (default=results.txt)\n"
    println(help)
  }

}