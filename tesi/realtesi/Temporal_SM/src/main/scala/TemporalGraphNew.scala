import gnu.trove.iterator.TIntIterator

import gnu.trove.set.hash.TIntHashSet

import gnu.trove.set.hash.THashSet

import gnu.trove.iterator.hash.TObjectHashIterator

import java.util.Arrays

import java.util.Vector

import scala.util.control._
import scala.annotation.switch

class TemporalGraphNew(val directed:Boolean,numNodes:Int) {

  /*Adjacency lists for graph nodes. The i-th entry of the array is the adjacency list for node with ID i.

	The array is a set of integers, representing the IDs of adjacent nodes of node i*/

  

  /*
	Add an edge to the graph
	@param source: the first node of the edge
    @param dest: the second node of the edge
    */
  class Contact(val node:Int, val time:Int){
      override def toString() : String = {
          " --" + time + "--> "+ node
      }
  }
  var outAdjList: Array[TIntHashSet] = new Array[TIntHashSet](numNodes)

  var inAdjList: Array[TIntHashSet] = new Array[TIntHashSet](numNodes)

  def getOutAdjList():Array[TIntHashSet]  = outAdjList

  def getInAdjList():Array[TIntHashSet]  = inAdjList
  //redundant source and destination, part above should be deleted
  var inAdjListTimes: Array[THashSet[Contact]] = new Array[THashSet[Contact]](numNodes)

  var outAdjListTimes: Array[THashSet[Contact]] = new Array[THashSet[Contact]](numNodes) 
  for(i <- 0.until(numNodes)){
        outAdjList(i) = new TIntHashSet();
        inAdjList(i) = new TIntHashSet();
        inAdjListTimes(i) = new THashSet[Contact]()
        outAdjListTimes(i) = new THashSet[Contact]()
    }
    
  def addEdge(source: Int, dest: Int, time:Int): TemporalGraphNew = {
    outAdjList(source).add(dest)
    inAdjList(dest).add(source)
    outAdjListTimes(source).add(new Contact(dest,time))
    inAdjListTimes(dest).add(new Contact(source,time))
    if (!directed) {
      outAdjList(dest).add(source)
      inAdjList(source).add(dest)
      outAdjListTimes(dest).add(new Contact(source,time))
      inAdjListTimes(source).add(new Contact(dest,time))
    }
    this
  }

  /*
    Get the adjacency string of the graph.
    The adjacency string is string obtained by concatenating the rows of the adjacency matrix.
   */

  def getAdjString(): String = {
    val adjString: StringBuilder = new StringBuilder()
    for (i <- 0 until outAdjList.length) {
      for (j <- 0 until outAdjList.length) {
        if (outAdjList(i).contains(j)) adjString.append("1")
        else adjString.append("0") 
      }
    }
    adjString.toString
  }

  /*
	Get the number of nodes in the network
	*/

  def getNumNodes(): Int = outAdjList.length

  /**
	 * Get the list of symmetry breaking conditions of the graph
	 * @return symmBreak: list of symmetry breaking condition
	 */
  def getSymmetryConditions(): Array[Vector[Integer]] = {
     val vv: Vector[Array[Int]] = findAutomorphisms()
    val numNodes: Int = outAdjList.length
    val listCond: Array[Vector[Integer]] =new Array[Vector[Integer]](numNodes)
    for (i <- 0 until listCond.length) {
      listCond(i) = new Vector()
    }
    val vvsize: Int = vv.size
    val broken: Array[Boolean] = Array.ofDim[Boolean](vvsize)
    // create a Breaks object 

    for (i <- 0 until numNodes)  {
    val loop = new Breaks;

// loop inside breakable
    var jout=0
    loop.breakable{
      for (j <- 0 until vvsize)  {
          jout=j
        if (!broken(j) && vv.get(j)(i) != i) loop.break
      }
    }
      if (jout < vvsize) {
        for (k <- 0 until numNodes)  {
          for (j <- 0 until vvsize)  {
            if (!broken(j) && vv.get(j)(i) == k) {
              listCond(k).add(i)
//break
            }
          }
        }
      }
      for (j <- 0 until vvsize) {
        if (vv.get(j)(i) != i) broken(j) = true  
      }
    }
    listCond
  }
/**
  * 
  *
  * @param func: compare function used
  * @param s1: first Tuple6 to compare
  * @param s2: second Tuple6 to compare
  * @return Boolean: conditional
  */
  def controlTemporals(func : (Int,Int) => Boolean,s1:Tuple6[Int,Int,Int,Int,Int,Int],s2:Tuple6[Int,Int,Int,Int,Int,Int]):Boolean = {
    func(s1._1,s2._1) && func(s1._2,s2._2) && func(s1._3,s2._3) && func(s1._4,s2._4) && func(s1._5,s2._5) && func(s1._6,s2._6)
  }
  /**
   * Computes 6 numbers, number of edges that have contact
   * @param destination: contact destination
   * @param time: contact time
   * @param delta: control over paths and consecutive edges
   */
  def nodeTemporalStructure(destination:Int,time:Int,delta:Int): (Int,Int,Int,Int,Int,Int) = {
    var inInf = 0
    var inSup = 0
    var inDeltaRespected = 0
    var inDeltaNotRespected = 0
    
    var outInf = 0
    var outSup = 0
    var outDeltaRespected = 0
    var outDeltaNotRespected = 0
    //computing times |inf| and |sup| for in edges
    val initeratore = inAdjListTimes(destination).iterator
    while (initeratore.hasNext()){
      val element = initeratore.next
      element.time match {
        case x if (x > time) => {inSup += 1}
        case x if (x < time) => {
          inInf += 1
          //TODO override equals and hashcode in Contact for the use of hashmap with object, if contacts are multiples for single edge, override is necessary
          if(outAdjList(element.node).contains(destination)){
            if(time - x <= delta) inDeltaRespected += 1
            else inDeltaNotRespected += 1
          }
        }
        case _ => {}
      } 
    }

    //computing times |inf| and |sup| for out edges
    val outiteratore = outAdjListTimes(destination).iterator
    while (outiteratore.hasNext()){
      val element = outiteratore.next
      element.time match {
        case x if (x > time) => {
          outSup += 1
          if(inAdjList(element.node).contains((destination))){
            if(x - time <= delta) inDeltaRespected += 1
            else inDeltaNotRespected += 1
          }
        }
        case x if (x < time) => outInf += 1
        case _ => {}
      } 
    }
    (inInf,inSup,outInf,outSup,inDeltaNotRespected,inDeltaRespected)
  }

  /**
    * class that implements a condition of the temporal graph
    *
    * @param notT: edges that are not time respecting
    * @param deltaN: edges that do not respect delta condition
    * @param deltaR: edges that respect delta condition
    */
  class Conditions(notT:Int,deltaN:Int,deltaR:Int){
    var notTimeRespecting = notT
    var deltaNotRespected = deltaN
    var deltaRespected = deltaR

    def <= (that : Conditions):Boolean = {
      (notTimeRespecting <= that.notTimeRespecting && deltaNotRespected <= that.deltaNotRespected && deltaRespected <= that.deltaRespected) 
    }
    override def toString(): String = {
      " NTR " + notTimeRespecting+ " DNR " + deltaNotRespected + " DR " + deltaRespected
    } 
  }
  /**
    * test compatibility of target node for a node in this temporal graph
    *
    * @param target: target Temporal Graph
    * @param nodeQ: node to compare in this Graph
    * @param nodeT: node to compare in other Graph
    * @param delta: number that represent delta condition
    * @return Boolean: true if compatibility is possible, false otherwise
    */
  def testCompatibility(target:TemporalGraphNew, nodeQ:Int, nodeT:Int, delta:Int): Boolean = {
        
    val inQuerySize = inAdjList(nodeQ).size()
    val inTargetSize = target.inAdjList(nodeT).size()
    var deltaConditionQuery = new Array[Conditions](inQuerySize)
    var deltaConditionTarget = new Array[Conditions](inTargetSize)

    val inIteratorQuery = inAdjListTimes(nodeQ).iterator()   
    var i = 0
    while(inIteratorQuery.hasNext()){
      deltaConditionQuery(i)= new Conditions(0,0,0)
      val elementQueryin = inIteratorQuery.next()
      val outIteratorQuery = outAdjListTimes(nodeQ).iterator()
      while(outIteratorQuery.hasNext()){
        val elementQueryout = outIteratorQuery.next()
        elementQueryin.time match {
          case x if (x < elementQueryout.time && (elementQueryout.time - x) <= delta ) => {
            deltaConditionQuery(i).deltaRespected += 1
          }
          case x if (x < elementQueryout.time && (elementQueryout.time - x) > delta ) => {
            deltaConditionQuery(i).deltaNotRespected += 1
          }
          case x if x >= elementQueryout.time => {
            deltaConditionQuery(i).notTimeRespecting += 1
          }
        }
      }
      i+=1
    }
    val inIteratorTarget = target.inAdjListTimes(nodeT).iterator()
    i=0
    while(inIteratorTarget.hasNext()){
      deltaConditionTarget(i)= new Conditions(0,0,0)
      val elementTargetin = inIteratorTarget.next()
      val outIteratorTarget = target.outAdjListTimes(nodeT).iterator()
      while(outIteratorTarget.hasNext()){
        val elementTargetout = outIteratorTarget.next()
        elementTargetin.time match {
          case x if (x < elementTargetout.time && (elementTargetout.time - x) <= delta ) => {
            deltaConditionTarget(i).deltaRespected += 1
          }
          case x if (x < elementTargetout.time && (elementTargetout.time - x) > delta ) => {
            deltaConditionTarget(i).deltaNotRespected += 1
          }
          case x if x >= elementTargetout.time => {
            deltaConditionTarget(i).notTimeRespecting += 1
          }
        }
      }
      i+=1
    }
    //var inEdgeMatched = new Array[Short](inAdjList(nodeQ).size())
    var matchedInEdges = 0
    try{
      for(condQuery <- deltaConditionQuery){
          val partialmap = deltaConditionTarget.filter(condi => (condQuery <= condi)).map(value=> true)
            if(partialmap.length > 0){
              if(partialmap.reduce((cond1,cond2) => cond1 && cond2))
                matchedInEdges += 1
          }
      }
    } catch {
      case e:Exception => {}
    }
    //first condition can be omitted if degree condition is computed outside
    //((target.inAdjList(nodeT).size >= inAdjList(nodeQ).size && target.outAdjList(nodeT).size >= outAdjList(nodeQ).size) && 
    (matchedInEdges == inAdjList(nodeQ).size())//)  
  }
  /**
    * test if node in the query could be mapped to a node in target 
    *
    * @param target
    * @param nodeQ
    * @param nodeT
    * @param mappingsPermanent
    * @param delta
    * @return
    */
  def testNodesMapping(target:TemporalGraphNew,nodeQ:Int,nodeT:Int,mappingsPermanent:Array[Int],delta:Int = 5) : Boolean = {
      //decomment if compatibility domains are not used (the control is done in the compatibity domains)
      //if((inAdjListTimes(nodeQ).size() > target.inAdjListTimes(nodeT).size()) || (outAdjListTimes(nodeQ).size() > target.outAdjListTimes(nodeT).size()))return false
      //partial mapping
      var mappings = mappingsPermanent map(identity)
      var countmap = 0
      //check for previous mappings
      for(i<- 0 to mappings.length){
        if((outAdjList(nodeQ).contains(i) && target.outAdjList(nodeT).contains(mappings(i))) ||
            (inAdjList(nodeQ).contains(i) && target.inAdjList(nodeT).contains(mappings(i))) ) 
          countmap+=1
      }
      /*
      var iteratorQueryIn = query2.inAdjListTimes(nodeQ).iterator()
      while(iteratorQueryIn.hasNext()){
                val elementQuery = iteratorQueryIn.next()
              val iteratorTarget = target2.inAdjListTimes(nodeT).iterator()
              while(iteratorTarget.hasNext() && mappings(elementQuery.node)<0){
                val elementTarget = iteratorTarget.next()
      if(query2.controlTemporals(_<=_,query2.nodeTemporalStructure(elementQuery.node,elementQuery.time,100),target2.nodeTemporalStructure(elementTarget.node,elementTarget.time,100))){
        mappings(elementQuery.node)=elementTarget.node
        countmap+=1
      }}}
      var iteratorQueryOut = query2.outAdjListTimes(nodeQ).iterator()
      while(iteratorQueryOut.hasNext()){
                val elementQuery = iteratorQueryOut.next()
              val iteratorTarget = target2.outAdjListTimes(nodeT).iterator()
              while(iteratorTarget.hasNext() && mappings(elementQuery.node)<0){
                val elementTarget = iteratorTarget.next()
      if(query2.controlTemporals(_<=_,query2.nodeTemporalStructure(elementQuery.node,elementQuery.time,100),target2.nodeTemporalStructure(elementTarget.node,elementTarget.time,100))){
        mappings(elementQuery.node)=elementTarget.node
        countmap+=1
      }}}
      */
      //entering edges
      val iteratorQueryIn = inAdjListTimes(nodeQ).iterator()
      while(iteratorQueryIn.hasNext()){
        val elementQuery = iteratorQueryIn.next()
        val iteratorTarget = target.inAdjListTimes(nodeT).iterator()
        //problem with mapping condition if some temporals structure are similar but one node can map to two or more different node, and at the same time, another node could map to only one node
        //a solution to this problem should be the control of all the temporals structures to find the minor one that is not mapped
        while(iteratorTarget.hasNext() && mappings(elementQuery.node)<0){
          val elementTarget = iteratorTarget.next()
          if(controlTemporals(_<=_,
                nodeTemporalStructure(elementQuery.node,elementQuery.time,delta),
                target.nodeTemporalStructure(elementTarget.node,elementTarget.time,delta))){
                  //feasibility rules of level 3 are not considered
            mappings(elementQuery.node) = elementTarget.node
            countmap += 1
          }
        }
      }
      //out edges
      val iteratorQueryOut = outAdjListTimes(nodeQ).iterator()
      while(iteratorQueryOut.hasNext()){
        val elementQuery = iteratorQueryOut.next()
        val iteratorTarget = target.outAdjListTimes(nodeT).iterator()
        while(iteratorTarget.hasNext() && mappings(elementQuery.node)<0){
          val elementTarget = iteratorTarget.next()
          if(controlTemporals(_<=_,
                nodeTemporalStructure(elementQuery.node,elementQuery.time,delta),
                target.nodeTemporalStructure(elementTarget.node,elementTarget.time,delta))){
            mappings(elementQuery.node) = elementTarget.node
            countmap += 1
          }
        }
      }
      if(countmap>=(inAdjList(nodeQ).size + outAdjList(nodeQ).size)) true
      else false
    }

  /*
	Computes all possible automorphisms of a graph
	@param adjMat: adjacency matrix of a graph
	*/

  private def findAutomorphisms(): Vector[Array[Int]] = {
    val nof_nodes: Int = outAdjList.length
    val fDir: Array[Int] = Array.ofDim[Int](nof_nodes)
    val fRev: Array[Int] = Array.ofDim[Int](nof_nodes)
    for (i <- 0 until nof_nodes)  {
      fDir(i) = -1
      fRev(i) = -1 
    }
    val sequence: Array[Array[Int]] = Array.ofDim[Int](nof_nodes, nof_nodes)
    for (i <- 0 until nof_nodes){
      for (j <- 0 until nof_nodes) {
        if (outAdjList(i).contains(j) || outAdjList(j).contains(i)) {
          var numNeighs: Int = 0
          for (k <- 0 until nof_nodes) {
            if (outAdjList(j).contains(k)) { numNeighs += 1; numNeighs - 1 }
            if (outAdjList(k).contains(j) && !outAdjList(j).contains(k)) {
              numNeighs += 1; numNeighs - 1
            }
          }
          sequence(i)(j) = numNeighs
        } else {
          sequence(i)(j) = 0
        }
      }
    }
    for (i <- 0 until nof_nodes) { Arrays.sort(sequence(i))  }
    val support: Array[Boolean] = Array.ofDim[Boolean](nof_nodes * nof_nodes)
    for (i <- 0 until (nof_nodes*nof_nodes)) { support(i) = false }
    for (i <- 0 until nof_nodes) {
      for (j <- 0 until nof_nodes) {
        val loop = new Breaks;

// loop inside breakable
        loop.breakable{
        for (k <- 0 until nof_nodes) {
          if (sequence(i)(k) != sequence(j)(k)) {
            if (k >= nof_nodes) support(i * nof_nodes + j) = true
            loop.break
            }
          }
        }   
      }
    }
    val vv: Vector[Array[Int]] = new Vector[Array[Int]]()
    for (g <- 0 until nof_nodes) {
      if (support(g * nof_nodes)) {
        fDir(0) = g
        fRev(g) = 0
        val pos: Int = 1
        isomorphicExtensions(fDir, fRev, vv, support, pos)
        fRev(fDir(0)) = -1
        fDir(0) = -1
      }
    }
    vv
  }

  /*
	Computes all isomorphic extensions for a graph
	*/

  private def isomorphicExtensions(fDir: Array[Int],
                                   fRev: Array[Int],
                                   vv: Vector[Array[Int]],
                                   support: Array[Boolean],
                                   pos: Int): Unit = {
    val nof_nodes: Int = fDir.length
    val cand: Array[Int] = Array.ofDim[Int](nof_nodes)
    var ncand: Int = 0
    var num: Int = 0
    for (i <- 0 until nof_nodes) { cand(i) = -1  }
    if (pos == nof_nodes) {
      val vTemp: Array[Int] = Array.ofDim[Int](nof_nodes)
      for (i <- 0 until fDir.length) { vTemp(i) = fDir(i)  }
      vv.add(vTemp)
    } else {
      var n: Int = 0
      var m: Int = 0
      var flag: Boolean = false
      val count: Array[Int] = Array.ofDim[Int](nof_nodes)
      ncand = 0
      for (i <- 0 until nof_nodes){ count(i) = 0  }
      for (i <- 0 until nof_nodes){
        if (fDir(i) != -1) {
// find their not mapped neighbours
          val vNei: Vector[Integer] = new Vector[Integer]()
          for (j <- 0 until nof_nodes) {
            if (outAdjList(i).contains(j)) vNei.add(j)
            if (outAdjList(j).contains(i) && !outAdjList(i).contains(j))
              vNei.add(j) 
          }
          num = vNei.size
          for (j <- 0 until num) {
            val neigh: Int = vNei.get(j)
            if (fDir(neigh) == -1) {
              if (count(neigh) == 0) cand({ ncand += 1; ncand - 1 }) = neigh
                count(neigh)+=count(neigh)+1
            }
            
          }
        }
        
      }
      m = 0
      for (i <- 1 until ncand) {
        if (count(i) > count(m)) m = i 
      }
      m = cand(m)
      ncand = 0
      val already: Array[Boolean] = Array.ofDim[Boolean](nof_nodes)
      for (i <- 0 until nof_nodes) { already(i) = false  }
      for (i <- 0 until nof_nodes) {
        if (fDir(i) != -1) {
          val vNei: Vector[Integer] = new Vector[Integer]()
          for (j <- 0 until nof_nodes){
            if (outAdjList(fDir(i)).contains(j)) vNei.add(j)
            if (outAdjList(j).contains(fDir(i)) && !outAdjList(fDir(i))
                  .contains(j)) vNei.add(j) 
          }
          num = vNei.size
          for (j <- 0 until num) {
            val neigh: Int = vNei.get(j)
            if (!already(neigh) && fRev(neigh) == -1 && support(
                  m * outAdjList.length + neigh)) {
              cand({ ncand += 1; ncand - 1 }) = neigh
              already(neigh) = true
            }
            
          }
        }
        
      }
      for (i <- 0 until ncand){
        n = cand(i)
        flag = false
        val loop = new Breaks;

// loop inside breakable
        loop.breakable{
          for (j <- 0 until nof_nodes) {
            if (fDir(j) != -1) {
              if (outAdjList(m).contains(j) != outAdjList(n).contains(fDir(j))) {
                flag = true
                loop.break
              } else if (outAdjList(j).contains(m) != outAdjList(fDir(j))
                          .contains(n)) {
                flag = true
                loop.break
              }
            }
            
          }
        }
        if (!flag) {
          fDir(m) = n
          fRev(n) = m 
          var tmp:Int=pos
          tmp += 1
          isomorphicExtensions(fDir, fRev, vv, support, pos) 
          tmp -= 1
          fRev(fDir(m)) = -1
          fDir(m) = -1
        }
        
      }
    }
  }
  /*
	Print info about the graph
	*/

  override def toString(): String = {
    val str: StringBuilder = new StringBuilder()
    for (i <- 0 until outAdjList.length) {
      str.append("source: ").append(i).append(" to {")
      //val it: TIntIterator = outAdjList(i).iterator()
      val it = outAdjListTimes(i).iterator()
      //str.append(it.next())
      while (it.hasNext) str.append(",").append(it.next())
      str.append("}\n") 
    }
    str.toString
  }

}