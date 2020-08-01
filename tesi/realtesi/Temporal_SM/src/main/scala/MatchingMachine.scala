/*
Data structure used to model the State-Space Representation for the matching process within RI algorithm
Each query node is associated to a state
Query nodes are processed for the search following the order of their states.
So, node with state 0 is processed first, then node with state 1 and so on.
 */

import gnu.trove.iterator.TIntIterator

import gnu.trove.set.hash.TIntHashSet

import MamaParentType._


class MatchingMachine (query:TemporalGraph) {
   object NodeFlag extends Enumeration {
    type NodeFlag = Value
    val NS_CORE,NS_CNEIGH,NS_UNV = Value
  }
  import NodeFlag._
//Number of query nodes
  var nof_sn: Int = query.getNumNodes()

//Number of incident edges to each query node
  var edges_sizes: Array[Int] =  new Array[Int](nof_sn);

//Number of incident outgoing edges to each query node
  var o_edges_sizes: Array[Int] = new Array[Int](nof_sn);

//Number of incident ingoing edges to each query node
  var i_edges_sizes: Array[Int] = new Array[Int](nof_sn);

//Set of query edges
  var edges: Array[Array[MaMaEdge]] = new Array[Array[MaMaEdge]](nof_sn)

//Map each node to the corresponding state
  var map_node_to_state: Array[Int] = new Array[Int](nof_sn);

//Map each state to the corresponding node
  var map_state_to_node: Array[Int] = new Array[Int](nof_sn);

//i.e. state of the predecessor or successor node (in the query graph) of node mapped to state S
  var parent_state: Array[Int] = new Array[Int](nof_sn);

//Type of node mapped to parent state of each state S, i.e. predecessor or successor
  var parent_type: Array[MamaParentType] =new  Array[MamaParentType](nof_sn)

  build(query)
  /*
	Build the matching state machine for a query graph
	@param ssg: query graph
	*/

  def build(ssg: TemporalGraph): Unit = {
    val outAdiacs: Array[TIntHashSet] = ssg.getOutAdjList
    val inAdiacs: Array[TIntHashSet] = ssg.getInAdjList
    val outAdiacsTimes = ssg.outAdjListTimes
    val inAdiacsTimes = ssg.inAdjListTimes
    val node_flags: Array[NodeFlag] = Array.ofDim[NodeFlag](nof_sn)
    val weights: Array[Array[Int]] = Array.ofDim[Int](nof_sn, 3)
    val t_parent_node: Array[Int] = Array.ofDim[Int](nof_sn)
    val t_parent_type: Array[MamaParentType] =
      Array.ofDim[MamaParentType](nof_sn)
    for (i <- 0 until nof_sn) {
      node_flags(i) = NodeFlag.NS_UNV
      weights(i) = Array.ofDim[Int](3)
      weights(i)(0) = 0
      weights(i)(1) = 0
      weights(i)(2) = outAdiacs(i).size + inAdiacs(i).size
      t_parent_node(i) = -1
      t_parent_type(i) = MamaParentType.PARENTTYPE_NULL 
    }
    var si: Int = 0
    var n: Int = 0
    var nIT: Int = 0
    var ni: Int = 0
    var nni: Int = 0
    var nqueueL: Int = 0
    var nqueueR: Int = 0
    var maxi: Int = 0
    var maxv: Int = 0
    var tmp: Int = 0
    for (si <- 0 until nof_sn) {
      if (nqueueL == nqueueR) {
        maxi = -1
        maxv = -1
        for (nIT <- 0 until nof_sn) {
          if (node_flags(nIT) == NodeFlag.NS_UNV && weights(nIT)(2) > maxv) {
            maxv = weights(nIT)(2)
            maxi = nIT
          }
        }
        map_state_to_node(si) = maxi
        map_node_to_state(maxi) = si
        t_parent_type(maxi) = MamaParentType.PARENTTYPE_NULL
        t_parent_node(maxi) = -1 
        nqueueR += 1
        n = maxi
        var it: TIntIterator = outAdiacs(n).iterator()
        while (it.hasNext) {
          ni = it.next()
          if (ni != n) { weights(ni)(1) += 1}
        }
        it = inAdiacs(n).iterator()
        while (it.hasNext) {
          ni = it.next()
          if (ni != n) { weights(ni)(1) += 1}
        }
      }
      if (nqueueL != nqueueR - 1) {
        maxi = nqueueL
        for (mi <- maxi + 1 until nqueueR if wcompare(map_state_to_node(mi),
                                                      map_state_to_node(maxi),
                                                      weights) <
               0) maxi = mi
        tmp = map_state_to_node(nqueueL)
        map_state_to_node(nqueueL) = map_state_to_node(maxi)
        map_state_to_node(maxi) = tmp
      }
      n = map_state_to_node(si)
      map_node_to_state(n) = si 
      nqueueL += 1
      node_flags(n) = NodeFlag.NS_CORE
      var it: TIntIterator = outAdiacs(n).iterator()
      while (it.hasNext) {
        ni = it.next()
        if (ni != n) {
          weights(ni)(0) += 1
          weights(ni)(1) -= 1
          if (node_flags(ni) == NodeFlag.NS_UNV) {
            node_flags(ni) = NodeFlag.NS_CNEIGH
            t_parent_node(ni) = n
            t_parent_type(ni) = MamaParentType.PARENTTYPE_OUT
            map_state_to_node(nqueueR) = ni
            map_node_to_state(ni) = nqueueR 
            nqueueR += 1
            val it2: TIntIterator = outAdiacs(ni).iterator()
            while (it2.hasNext) {
              nni = it2.next() 
              weights(nni)(1) += 1
            }
          }
        }
      }
      it = inAdiacs(n).iterator()
      while (it.hasNext) {
        ni = it.next()
        if (ni != n) {
          { weights(ni)(0) += 1; weights(ni)(0) - 1 }
          { weights(ni)(1) -= 1; weights(ni)(1) + 1 }
          if (node_flags(ni) == NodeFlag.NS_UNV) {
            node_flags(ni) = NodeFlag.NS_CNEIGH
            t_parent_node(ni) = n
            t_parent_type(ni) = MamaParentType.PARENTTYPE_IN
            map_state_to_node(nqueueR) = ni
            map_node_to_state(ni) = nqueueR 
            nqueueR += 1
            val it2: TIntIterator = inAdiacs(ni).iterator()
            while (it2.hasNext) {
              nni = it2.next() 
              weights(nni)(1) += 1
            }
          }
        }
      }
    }
    var e_count: Int = 0
    var o_e_count: Int = 0
    var i_e_count: Int = 0
    for (si <- 0 until nof_sn) {
      n = map_state_to_node(si)
      parent_state(si) =
        if (t_parent_node(n) != -1) map_node_to_state(t_parent_node(n)) else -1
      parent_type(si) = t_parent_type(n)
      e_count = 0
      o_e_count = 0
      var it: TIntIterator = outAdiacs(n).iterator()
      while (it.hasNext) {
        val idOut: Int = it.next()
        if (map_node_to_state(idOut) < si) {
          { e_count += 1; e_count - 1 }
          { o_e_count += 1; o_e_count - 1 }
        }
      }
      i_e_count = 0
      it = inAdiacs(n).iterator()
      while (it.hasNext) {
        val idIn: Int = it.next()
        if (map_node_to_state(idIn) < si) {
          { e_count += 1; e_count - 1 }
          { i_e_count += 1; i_e_count - 1 }
        }
      }
      edges_sizes(si) = e_count
      o_edges_sizes(si) = o_e_count
      i_edges_sizes(si) = i_e_count
      edges(si) = Array.ofDim[MaMaEdge](e_count)
      e_count = 0
      it = outAdiacs(n).iterator()

      var itTimes = outAdiacsTimes(n).iterator()
      while (it.hasNext) {
        val idOut: Int = it.next()
        var time = 0
        if (itTimes.hasNext()){
          itTimes.advance()
          time = itTimes.value().time
        }
        if (map_node_to_state(idOut) < si) {
          edges(si)(e_count) =new MaMaEdge(map_node_to_state(n), map_node_to_state(idOut),time) 
          e_count += 1
        }
      }
      for (j <- 0 until si) {
        val sn: Int = map_state_to_node(j)
        it = outAdiacs(sn).iterator()
        itTimes = outAdiacsTimes(sn).iterator()
        while (it.hasNext) {
          val idOut: Int = it.next()
          var time = 0
          if (itTimes.hasNext()){
            itTimes.advance()
            time = itTimes.value().time
          }
          if (idOut == n) {
            edges(si)(e_count) = new MaMaEdge(j, si,time) 
              e_count += 1
            
          }
        }
      }
    }
  }

  private def wcompare(i: Int, j: Int, weights: Array[Array[Int]]): Int =(0.until(3)).
    find(w => weights(i)(w) != weights(j)(w)).
    map(w => weights(j)(w) - weights(i)(w)).
    getOrElse(i - j)

}

