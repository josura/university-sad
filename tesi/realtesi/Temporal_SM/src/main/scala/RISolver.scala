import gnu.trove.iterator.TIntIterator

import gnu.trove.set.hash.TIntHashSet

import java.util.BitSet

import java.util.Vector

import MamaParentType._

class RISolver(tGraph: TemporalGraph,induc:Boolean) {

//Matching state machine
  private var mama: MatchingMachine = _

//Target graph
  private val targetGraph: TemporalGraph = tGraph

//Number of matches of query graph into target graph
  var numMatches: Long = 0

  def getNumMatches():Long = numMatches

//Induced or not?
  private val induced: Boolean = induc

  /*
	Compute the number of matches of query graph into target graph
	*/

  def solve(queryGraph: TemporalGraph,delta:Int): Unit = {
    numMatches = 0
//Build the state space representation machine
     this.mama = new MatchingMachine(queryGraph)
//Compute compatibility domains
    val domains: Array[BitSet] = computeDomains(queryGraph,delta)
//Compute the set of query symmetry breaking conditions
    val symmCond: Array[Vector[Integer]] = queryGraph.getSymmetryConditions
    val targetOutAdjLists: Array[TIntHashSet] = targetGraph.getOutAdjList
    val targetInAdjLists: Array[TIntHashSet] = targetGraph.getInAdjList
    val nofTargetNodes: Int = targetGraph.getNumNodes
    var i: Int = 0
    var j: Int = 0
    val nof_sn: Int = mama.nof_sn
    val parent_state: Array[Int] = mama.parent_state
    val parent_type: Array[MamaParentType] = mama.parent_type
//One iterator for each query node
    val candidatesIT: Array[Int] = Array.ofDim[Int](nof_sn)
//Set of target candidate nodes for matching, one for each query node
    val candidates: Array[Array[Int]] = Array.ofDim[Array[Int]](nof_sn)
//Partial mapping between query and target nodes
    val solution: Array[Int] = (Array.ofDim[Int](nof_sn)) map(value => -1)
//Set of already mapped target nodes
    val matched: Array[Boolean] = Array.ofDim[Boolean](nofTargetNodes)
//i.e. the set of target nodes in the domain of the first query node to process
    for (i <- 0 until nof_sn) {
      if (parent_type(i) == MamaParentType.PARENTTYPE_NULL) {
        val n: Int = mama.map_state_to_node(i)
        candidates(i) = Array.ofDim[Int](domains(n).cardinality())
        var k: Int = 0
        j = domains(n).nextSetBit(0)
        while (j >= 0) {
          candidates(i)(k) = j 
          k += 1
          j = domains(n).nextSetBit(j + 1)
        }
        candidatesIT(i) = -1
      }
    }
    var psi: Int = -1
    var si: Int = 0
    var ci: Int = 0
    var sip1: Int = 0
    while (si != -1) {
      if (psi >= si) //Backtracking: remove mapping for currently processed query node
        matched(solution(si)) = false
      ci = -1
//Process match between currently processed query node and next candidate node
      candidatesIT(si) += 1
      var notmatched= true
      while (candidatesIT(si) < candidates(si).length && notmatched) {
        ci = candidates(si)(candidatesIT(si))
//Add mapping
        solution(si) = ci
//Check if target node-query node mapping is feasible
        if(!matched(ci) && 
            domains(mama.map_state_to_node(si)).get(ci) &&
            condCheck(si, solution, symmCond) &&
            edgesCheck(si, ci, solution, matched,queryGraph,delta)
            //comment below if feasibility rules are not calculated
            //&& queryGraph.testNodesMapping(targetGraph,mama map_state_to_node si,,solution,delta)
            ){
              notmatched=false
              println("match found for query node " + mama.map_state_to_node(si) + " to target node but i dont know" + ci)
            } else ci = -1
//Mapping is not feasible, go on with next candidate
        if (notmatched)candidatesIT(si) += 1
      }
//Do backtracking and go back to the previously processed query node
      if (ci == -1) {
        psi = si 
        si -= 1
      } else {
//Mapping is feasible
        if (si == nof_sn - 1) {
//All query nodes have been mapped. Update the number of occurrences found
          numMatches += 1
          if (numMatches % 10000000 == 0)
            println("Found " + numMatches + " occurrences...")
          psi = si
        } else {
//There are still unmapped query nodes. Continue the search
          matched(solution(si)) = true
//Go to the next query node to process for matching
          sip1 = si + 1
          if (parent_type(sip1) != MamaParentType.PARENTTYPE_NULL) {
              candidates(sip1) ={if (parent_type(sip1) == MamaParentType.PARENTTYPE_IN)
                targetInAdjLists(solution(parent_state(sip1))).toArray()
              else targetOutAdjLists(solution(parent_state(sip1))).toArray()
            }
          }
//Start from the first target candidate node for that query node
          candidatesIT(si + 1) = -1
          psi = si 
          si += 1
        }
      }
    }
//No candidate target nodes can be mapped to the currently processed query node
//No candidate target nodes can be mapped to the currently processed query node
  }
//Iterators for the set of target candidate nodes for matching
//in order to know from which candidate the search should proceed when doing backtracking
//Build the set of initial candidate nodes,
//Iterators for the set of target candidate nodes for matching
//in order to know from which candidate the search should proceed when doing backtracking
//Build the set of initial candidate nodes,

  /*
	Compute the compatibility domains for each query node
	Each domain is represented as a BitSet with numTargetNodes bits.
	Bit 1 in position i means that target node i is in the compatibility domain of that query node
	 */

  def computeDomains(queryGraph: TemporalGraph,delta:Int = 100): Array[BitSet] = {
    val numQueryNodes: Int = queryGraph.getNumNodes
    val numTargetNodes: Int = targetGraph.getNumNodes
    val domains: Array[BitSet] = Array.ofDim[BitSet](numQueryNodes)
    for (i <- 0 until domains.length) {
      domains(i) = new BitSet(numTargetNodes)
    }
    val targetOutAdjLists: Array[TIntHashSet] = targetGraph.getOutAdjList
    val targetInAdjLists: Array[TIntHashSet] = targetGraph.getInAdjList
    val queryOutAdjLists: Array[TIntHashSet] = queryGraph.getOutAdjList
    val queryInAdjLists: Array[TIntHashSet] = queryGraph.getInAdjList
    for (i <- 0 until numTargetNodes) {
//Find compatible query nodes and update domains
      for (j <- 0 until domains.length) {
        if (//queryGraph.testCompatibility(targetGraph,j,i,delta) && 
            queryOutAdjLists(j).size <= targetOutAdjLists(i).size &&
            queryInAdjLists(j).size <= targetInAdjLists(i).size) {
          domains(j).set(i)
        }
//System.out.println(j+"-"+i);
//System.out.println(j+"-"+i);
      }
    }
//Arc consistency check
    var ra: Int = 0
    var qb: Int = 0
    var rb: Int = 0
    var notfound: Boolean = false
    for (qa <- 0 until numQueryNodes) {
      ra = domains(qa).nextSetBit(0)
      while (ra >= 0) {
//for each edge qa->qb  check if exists ra->rb
        val it: TIntIterator = queryOutAdjLists(qa).iterator()
        while (it.hasNext) {
          qb = it.next()
          notfound = true
          val it2: TIntIterator = targetOutAdjLists(ra).iterator()
          while (it2.hasNext) {
            rb = it2.next()
            if (domains(qb).get(rb)) {
              notfound = false
//break
            }
          }
          if (notfound) domains(qa).set(ra, false)
        }
        ra = domains(qa).nextSetBit(ra + 1)
      }
    }
    domains
  }

  /*
	Check if symmetry breaking conditions for the currently matched query node are satisfied
	@param si: id of currently matched query node
	@param solution: set of already matched couples of query-target nodes
	@param matched: array of boolean where the i-th entry is true iff the target node i has been already matched
	*/
  def condCheck(si: Int,
                solution: Array[Int],
                symmCond: Array[Vector[Integer]]): Boolean = {
    var condCheck: Boolean = true
    val condNode: Vector[Integer] = symmCond(mama.map_state_to_node(si))
    for (ii <- 0 until condNode.size) {
      val targetNode: Int = solution(mama.map_node_to_state(condNode.get(ii)))
      if (solution(si) < targetNode) {
        condCheck = false
//break
      }
    }
    condCheck
  }

  /**
   * Check if outgoing edges from currently matched nodes in the query and in the target, also matches
   * @param si: id of the query node
	 * @param ci: id of the target node
	 * @param solution: set of already matched couples of query-target nodes
	 * @param matched: array of boolean where the i-th entry is true iff the target node i has been already matched 
   */
  def edgesCheck(si: Int,
                 ci: Int,
                 solution: Array[Int],
                 matched: Array[Boolean],
                 query:TemporalGraph,
                 delta:Int): Boolean = {
    val targetOutAdjLists: Array[TIntHashSet] = targetGraph.getOutAdjList
    val targetInAdjLists: Array[TIntHashSet] = targetGraph.getInAdjList
    for (me <- 0 until mama.edges_sizes(si)) {
      val querySource = mama.edges(si)(me).trueSource
      val queryDest = mama.edges(si)(me).truetarget
      val queryTime = mama.edges(si)(me).time
      val source: Int = solution(mama.edges(si)(me).source)
      val target: Int = solution(mama.edges(si)(me).target)
      if (!targetOutAdjLists(source).contains(target)) return false
      val time = targetGraph.outAdjListTimes(source).get(target).time
      if(!query.controlTemporals(_<=_,query.nodeTemporalStructure(queryDest,queryTime,delta),targetGraph.nodeTemporalStructure(target,time,delta))) return false
    }
    true
  }

}
