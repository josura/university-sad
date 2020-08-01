import java.io.BufferedReader

import java.io.FileReader

import java.io.BufferedWriter

import java.io.FileWriter

import java.util.Vector

import gnu.trove.iterator.TIntIterator

import gnu.trove.set.hash.TIntHashSet


class FileManagerTemporal {

  /*
	Read a graph from input file
	@param graphFile: path of the file
	*/

  def readGraph(graphFile: String): TemporalGraph = {
    var g: TemporalGraph = null
    try {
      val br: BufferedReader = new BufferedReader(new FileReader(graphFile))
      var str: String = br.readLine()
      var directed: Boolean = false
      if (str.==("directed")) directed = true
      val numNodes: Int = java.lang.Integer.parseInt(br.readLine())
      g = new TemporalGraph(directed, numNodes)
      str = br.readLine()
      while (str!=null) {
        val split: Array[String] = str.split("[, \t]")
        val source: Int = java.lang.Integer.parseInt(split(0))
        val dest: Int = java.lang.Integer.parseInt(split(1))
        val time: Int = java.lang.Integer.parseInt(split(2))
        g.addEdge(source, dest,time)
        str = br.readLine()
      }
      br.close()
    } catch {
      case e: Exception => println(e.getMessage)

    }
    g
  }

  /*
	Read the set of queries from input file
	@param queriesFile: path of the file
	*/

  def readQueries(queriesFile: String): Vector[TemporalGraph] = {
    val setQueries: Vector[TemporalGraph] = new Vector[TemporalGraph]()
    try {
      val br: BufferedReader = new BufferedReader(new FileReader(queriesFile))
      var str: String = br.readLine()
      while (str != null) {
        val direction: String = br.readLine()
        var directed: Boolean = false
        if (direction.==("directed")) directed = true
        val numNodes: Int = java.lang.Integer.parseInt(br.readLine())
        val q: TemporalGraph = new TemporalGraph(directed, numNodes)
        str = br.readLine()
        while (str  != null && !str.startsWith("#")) {
          val split: Array[String] = str.split("[ \t]")
          val source: Int = java.lang.Integer.parseInt(split(0))
          val dest: Int = java.lang.Integer.parseInt(split(1))
          val time: Int = java.lang.Integer.parseInt(split(2))
          q.addEdge(source, dest,time)
          str = br.readLine()
        }
        setQueries.add(q)
      }
      br.close()
    } catch {
      case e: Exception => println(e.toString)

    }
    setQueries
  }

  /*
	Write query results to output file
	*/

  def writeResults(setQueries: Vector[TemporalGraph],
                   setCounts: Vector[Long],
                   setRunningTimes: Vector[Double],
                   outputFile: String): Unit = {
    try {
      val bw: BufferedWriter = new BufferedWriter(new FileWriter(outputFile))
      bw.write("Query\tNum_occ\tTime (secs)\n")
      for (i <- 0 until setQueries.size) {
        val q: TemporalGraph = setQueries.get(i)
        val adjString: String = q.getAdjString
        bw.write(
          adjString + "\t" + setCounts.get(i) + "\t" + setRunningTimes.get(i) +
            "\n")
      }
      bw.close()
    } catch {
      case e: Exception => println(e.getMessage)

    }
  }

}