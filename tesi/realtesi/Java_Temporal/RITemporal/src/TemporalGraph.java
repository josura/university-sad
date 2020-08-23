import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TemporalGraph {
	/*Adjacency lists for graph nodes. The i-th entry of the array is the adjacency list for node with ID i. 
	The array is a set of integers, representing the IDs of adjacent nodes of node i*/
	private final TIntHashSet[] outAdjList;
    private final TIntHashSet[] inAdjList;
	//Is the graph directed or not?
    private final TIntObjectHashMap<Contact>[] outAdjListTimes;
    private final TIntObjectHashMap<Contact>[] inAdjListTimes;
    private final boolean directed;
    private int numedges;
	/*
	Constructor
	@param directed: is the graph directed or not?
	@param numNodes: number of nodes of the graph
	*/
    public TemporalGraph(boolean directed, int numNodes)
    {
        this.outAdjListTimes = new TIntObjectHashMap[numNodes];
        this.inAdjListTimes = new TIntObjectHashMap[numNodes];
		this.directed=directed;
        this.outAdjList=new TIntHashSet[numNodes];
        this.inAdjList=new TIntHashSet[numNodes];
        numedges=0;
        for(int i=0;i<numNodes;i++)
        {
            outAdjList[i] = new TIntHashSet();
            inAdjList[i] = new TIntHashSet();
            outAdjListTimes[i] = new TIntObjectHashMap<Contact>();
            inAdjListTimes[i] =  new TIntObjectHashMap<Contact>();
            
        }
    }
	
	/*
	Add an edge to the graph
	@param source: the first node of the edge
	@param dest: the second node of the edge
	*/
    public TemporalGraph addEdge(int source, int dest)
    {
        outAdjList[source].add(dest);
        inAdjList[dest].add(source);
        if(!directed)
        {
            outAdjList[dest].add(source);
            inAdjList[source].add(dest);
        }
        return this;
    }
    
    public TemporalGraph addEdge(int source, int dest,int time)
    {
    	numedges++;
        outAdjList[source].add(dest);
        inAdjList[dest].add(source);
        Contact outnew = new Contact(dest, time);
        Contact innew = new Contact(source, time);
        outAdjListTimes[source].put(outnew.hashCode(), outnew);
        inAdjListTimes[dest].put(innew.hashCode(), innew);
        if(!directed)
        {
        	outAdjListTimes[dest].put(innew.hashCode(), innew);
            inAdjListTimes[source].put(outnew.hashCode(), outnew);
            outAdjList[dest].add(source);
            inAdjList[source].add(dest);
        }
        return this;
    }

    /*
    Get the adjacency string of the graph.
    The adjacency string is string obtained by concatenating the rows of the adjacency matrix.
     */
    public String getAdjString()
    {
        StringBuilder adjString= new StringBuilder();
        int i, j;
        for(i=0;i<outAdjList.length;i++)
        {
            for(j=0;j<outAdjList.length;j++)
            {
                if(outAdjList[i].contains(j))
                    adjString.append("1");
                else
                    adjString.append("0");
            }
        }
        return adjString.toString();
    }

    /*
	Get the list of symmetry breaking conditions of the graph
	@param adjMat: adjacency matrix of a graph
	*/
    public Vector<Integer>[] getSymmetryConditions(int delta)
    {
        int i, j, k;
        Vector<int[]> vv=findAutomorphisms(delta);
        int numNodes=outAdjList.length;
        Vector<Integer>[] listCond=new Vector[numNodes];
        for(i=0;i<listCond.length;i++)
            listCond[i]=new Vector<>();
        int vvsize=vv.size();
        boolean[] broken=new boolean[vvsize];
        for(i=0;i<numNodes;i++)
        {
            for(j=0;j<vvsize;j++)
            {
                if(!broken[j] && vv.get(j)[i]!=i)
                    break;
            }
            if(j<vvsize)
            {
                for(k=i+1;k<numNodes;k++)
                {
                    for (j=0;j<vvsize;j++)
                    {
                        if(!broken[j] && vv.get(j)[i]==k)
                        {
                            listCond[k].add(i);
                            break;
                        }
                    }
                }
            }
            for(j=0;j<vvsize;j++)
            {
                if(vv.get(j)[i]!=i)
                    broken[j]=true;
            }
        }
        return listCond;
    }

    /*
	Computes all possible automorphisms of a graph
	@param adjMat: adjacency matrix of a graph
	*/
    private Vector<int[]> findAutomorphisms(int delta)
    {
        int i, j, k;
        int g;
        int nof_nodes = outAdjList.length;
        int[] fDir = new int[nof_nodes];
        int[] fRev = new int[nof_nodes];
        for (i = 0; i < nof_nodes; i++) {
            fDir[i] = -1;
            fRev[i] = -1;
        }
        int[][] sequence = new int[nof_nodes][nof_nodes];
        for (i = 0; i < nof_nodes; i++)
        {
            for (j = 0; j < nof_nodes; j++)
            {
                if (outAdjList[i].contains(j) || outAdjList[j].contains(i))
                {
                    int numNeighs = 0;
                    for (k = 0; k < nof_nodes; k++)
                    {
                        if (outAdjList[j].contains(k))
                            numNeighs++;
                        if (outAdjList[k].contains(j) && !outAdjList[j].contains(k))
                            numNeighs++;
                    }
                    sequence[i][j] = numNeighs;
                } else {
                    sequence[i][j] = 0;
                }
            }
        }
        for (i = 0; i < nof_nodes; i++)
            Arrays.sort(sequence[i]);

        boolean[] support = new boolean[nof_nodes * nof_nodes];
        for (i = 0; i < nof_nodes * nof_nodes; i++)
            support[i] = false;
        for (i = 0; i < nof_nodes; i++)
        {
            for (j = 0; j < nof_nodes; j++)
            {
                for (k = 0; k < nof_nodes; k++)
                {
                    if (sequence[i][k] != sequence[j][k])
                        break;
                }
                if (k >= nof_nodes)
                    support[i * nof_nodes + j] = true;
            }
        }

        Vector<int[]> vv=new Vector<>();
        for (g = 0; g < nof_nodes; g++)
        {
            if (support[g * nof_nodes])
            {
                fDir[0] = g;
                fRev[g] = 0;
                int pos = 1;
                isomorphicExtensions(fDir, fRev, vv, support, pos,delta);
                fRev[fDir[0]] = -1;
                fDir[0] = -1;
            }
        }
        return vv;
    }

    /*
	Computes all isomorphic extensions for a graph
	*/
    private void isomorphicExtensions(int[] fDir, int[] fRev, Vector<int[]> vv, boolean[] support, int pos, int delta)
    {
        int i, j;
        int nof_nodes = fDir.length;
        int[] cand = new int[nof_nodes];
        int ncand;
        int num;
        for (i = 0; i < nof_nodes; i++)
            cand[i] = -1;
        if (pos == nof_nodes)
        {
            int[] vTemp=new int[nof_nodes];
            for(i=0;i<fDir.length;i++)
                vTemp[i]=fDir[i];
            vv.add(vTemp);
        }
        else
        {
            int n, m;
            boolean flag;
            int[] count = new int[nof_nodes];
            ncand = 0;
            for (i = 0; i < nof_nodes; i++)
                count[i] = 0;
            for (i = 0; i < nof_nodes; i++)
            {
                if(fDir[i]!=-1)
                {        // find their not mapped neighbours
                    Vector<Integer> vNei=new Vector<>();
                    for(j=0;j<nof_nodes;j++)
                    {
                        if(outAdjList[i].contains(j))
                            vNei.add(j);
                        if(outAdjList[j].contains(i) && !outAdjList[i].contains(j))
                            vNei.add(j);
                    }
                    num=vNei.size();
                    for(j=0;j<num;j++)
                    {
                        int neigh=vNei.get(j);
                        if(fDir[neigh]==-1)
                        {
                            if(count[neigh]==0)
                                cand[ncand++]=neigh;
                            count[neigh]++;
                        }
                    }
                }
            }
            m = 0;
            for (i = 1; i < ncand; i++)
            {
                if (count[i] > count[m])
                    m = i;
            }
            m = cand[m];
            ncand = 0;
            boolean[] already = new boolean[nof_nodes];
            for (i = 0; i < nof_nodes; i++)
                already[i] = false;
            for (i = 0; i < nof_nodes; i++)
            {
                if(fDir[i]!=-1)
                {
                    Vector<Integer> vNei=new Vector<>();
                    for(j=0;j<nof_nodes;j++)
                    {
                        if(outAdjList[fDir[i]].contains(j))
                            vNei.add(j);
                        if(outAdjList[j].contains(fDir[i]) && !outAdjList[fDir[i]].contains(j))
                            vNei.add(j);
                    }
                    num=vNei.size();
                    for(j=0;j<num;j++)
                    {
                        int neigh=vNei.get(j);
                        if(!already[neigh] && fRev[neigh]==-1 && support[m*outAdjList.length+neigh])
                        {
                            cand[ncand++]=neigh;
                            already[neigh]=true;
                        }
                    }
                }
            }
            for (i=0;i<ncand;i++)
            {
                n=cand[i];
                flag=false;
                for(j=0;j<nof_nodes;j++)
                {
                    if(fDir[j]!=-1)
                    {
                    	//if temporal structure is not the same than ...
                    	Contact cont1 = outAdjListTimes[m].get(j);
                    	Contact cont2 = outAdjListTimes[n].get(fDir[j]);
                        if(outAdjList[m].contains(j)!=outAdjList[n].contains(fDir[j]) ||
                        		(cont1!=null &&
                    			cont2!=null &&		
                        		!controlTemporals(nodeTemporalStructure(cont1.node,cont1.time,delta),nodeTemporalStructure(cont2.node,cont2.time,delta)))
                        		)
                        {
                            flag=true;
                            break;
                        }
                        else {
                        	cont1 = outAdjListTimes[j].get(m);
                        	cont2 = outAdjListTimes[fDir[j]].get(n);
                        	if(outAdjList[j].contains(m)!=outAdjList[fDir[j]].contains(n) ||
                        			(cont1!=null &&
                        			cont2!=null &&
                        			!controlTemporals(nodeTemporalStructure(cont1.node,cont1.time,delta),nodeTemporalStructure(cont2.node,cont2.time,delta)))
                        			)
                        	{
	                            flag=true;
	                            break;
	                        }
                        }
                    }
                }
                if(!flag)
                {
                    fDir[m] = n;
                    fRev[n] = m;
                    pos++;
                    isomorphicExtensions(fDir, fRev, vv, support, pos,delta);
                    pos--;
                    fRev[fDir[m]] = -1;
                    fDir[m] = -1;
                }
            }
        }
    }

    /**
     * 
     *
     * @param func: compare function used
     * @param s1: first Tuple6 to compare
     * @param s2: second Tuple6 to compare
     * @return Boolean: conditional
     */
     public boolean controlTemporals(Vector<Integer> s1,Vector<Integer> s2){
    	 boolean retval = s1.get(0)<=s2.get(0) && s1.get(1)<=s2.get(1) && s1.get(2)<=s2.get(2) && s1.get(3)<=s2.get(3) && s1.get(4)<=s2.get(4) && s1.get(5)<=s2.get(5);
    	 return retval;
     }
     
     /**
      * Computes 6 numbers, number of edges that have contact
      * @param destination: contact destination
      * @param time: contact time
      * @param delta: control over paths and consecutive edges
      */
     public Vector<Integer> nodeTemporalStructure(int destination,int time,int delta){
       int inInf = 0;
       int inSup = 0;
       int inDeltaRespected = 0;
       int inDeltaNotRespected = 0;
       
       int outInf = 0;
       int outSup = 0;
       int outDeltaRespected = 0;
       int outDeltaNotRespected = 0;
       //computing times |inf| and |sup| for in edges
       TIntObjectIterator<Contact> initeratore = inAdjListTimes[destination].iterator();
       while (initeratore.hasNext()){
         initeratore.advance();
         Contact element = initeratore.value();
         if(element.time > time) {
        	 inSup ++;
         } else if (element.time <= time) {
        	 inInf++;
        	 if(time - element.time <= delta) {
        		 inDeltaRespected++;
        	 } else {
        		 inDeltaNotRespected++;
        	 }
         }
         
       }
       
       //TODO manage contacts that have the same times

       //computing times |inf| and |sup| for out edges
       TIntObjectIterator<Contact> outiteratore = outAdjListTimes[destination].iterator();
       while (outiteratore.hasNext()){
         outiteratore.advance();
         Contact element = outiteratore.value();
         if(element.time > time) {
        	 outSup ++;
        	 if(element.time - time <= delta) {
        		 inDeltaRespected++;
        	 } else {
        		 inDeltaNotRespected++;
        	 }
        	 
         } else if (element.time <= time) {
        	 outInf++;
        	 
         } 
       }
       Vector<Integer> returnVal = new Vector<Integer>(6);
       returnVal.add(inInf);
       returnVal.add(inSup);
       returnVal.add(outInf);
       returnVal.add(outSup);
       returnVal.add(inDeltaNotRespected);
       returnVal.add(inDeltaRespected);
       return returnVal;
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
    public boolean testCompatibility(TemporalGraph target,int nodeQ,int nodeT,int delta){
          
      int inQuerySize = inAdjList[nodeQ].size();
      int inTargetSize = target.inAdjList[nodeT].size();
      Conditions[] deltaConditionQuery = new Conditions[inQuerySize];
      Conditions[] deltaConditionTarget = new Conditions[inTargetSize];

      TIntObjectIterator<Contact> inIteratorQuery = inAdjListTimes[nodeQ].iterator();   
      int i = 0;
      while(inIteratorQuery.hasNext()){
        deltaConditionQuery[i]= new Conditions(0,0,0);
        inIteratorQuery.advance();
        Contact elementQueryin = inIteratorQuery.value();
        TIntObjectIterator<Contact> outIteratorQuery = outAdjListTimes[nodeQ].iterator();
        int x =elementQueryin.time;
        while(outIteratorQuery.hasNext()){
          outIteratorQuery.advance();
          Contact elementQueryout = outIteratorQuery.value();
          if (x < elementQueryout.time && (elementQueryout.time - x) <= delta ) {
        	  deltaConditionQuery[i].deltaRespected++;
          } else if (x < elementQueryout.time && (elementQueryout.time - x) > delta ) {
        	  deltaConditionQuery[i].deltaNotRespected++;
          } else if (x >= elementQueryout.time) {
        	  deltaConditionQuery[i].notTimeRespecting++;
          }
        }
        i++;
      }
      TIntObjectIterator<Contact> inIteratorTarget = target.inAdjListTimes[nodeT].iterator();
      i=0;
      while(inIteratorTarget.hasNext()){
        deltaConditionTarget[i]= new Conditions(0,0,0);
        inIteratorTarget.advance();
        Contact elementTargetin = inIteratorTarget.value();
        TIntObjectIterator<Contact> outIteratorTarget = target.outAdjListTimes[nodeT].iterator();
        int x = elementTargetin.time;
        while(outIteratorTarget.hasNext()){
          outIteratorTarget.advance();
          Contact elementTargetout = outIteratorTarget.value();
          if (x < elementTargetout.time && (elementTargetout.time - x) <= delta ) {
        	  deltaConditionTarget[i].deltaRespected++;
          } else if (x < elementTargetout.time && (elementTargetout.time - x) > delta ) {
        	  deltaConditionTarget[i].deltaNotRespected++;
          } else if (x >= elementTargetout.time) {
        	  deltaConditionTarget[i].notTimeRespecting++;
          }
          
        }
        i++;
      }
      //var inEdgeMatched = new Array[Short](inAdjList(nodeQ).size())
      int matchedInEdges = 0;
      try{
    	int j=0;
        for(j=0;j<deltaConditionQuery.length;j++){
        	Conditions condQuery = deltaConditionQuery[j];
        	List<Conditions> condizioni =  Arrays.asList(deltaConditionTarget);
            List<Conditions> partialmap = condizioni.parallelStream().
            		filter(condi -> (condQuery.LTOE(condi))).
            		collect(Collectors.toList());
              if(partialmap.size() > 0){
                  matchedInEdges ++;
            }
        }
      } catch (Exception e) {

      }
      //first condition can be omitted if degree condition is computed outside
      //((target.inAdjList(nodeT).size >= inAdjList(nodeQ).size && target.outAdjList(nodeT).size >= outAdjList(nodeQ).size) && 
      return (matchedInEdges == inAdjList[nodeQ].size());//)  
    }
    
	/**
	 * final test to see if target subgraph is indeed mappable to query graph
	 * 
	 */
    public boolean testMap(TemporalGraph subtarget,Vector<Integer> mapping,int delta) {
    	for(int i = 0; i < getNumNodes();i++) {
    		int nodeQ = mapping.get(i);
    		int nodeT = i;
    		//test compatibility, aka fingerprint, and nod equivalence of temporal structure because
    		//edges in subgraph can be more than necessary
    		if(!testCompatibility(subtarget, nodeQ, nodeT, delta)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * return subgraph of nodes specified
     * @param Vector of nodes
     * @return subgraph of the nodes specified
     */
    public TemporalGraph subgraph(Vector<Integer> nodes) {
    	int finalNodes = nodes.size();
    	TemporalGraph retGraph = new TemporalGraph(true,finalNodes);
    	for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			Integer integer = (Integer) iterator.next();
	    	TIntObjectIterator<Contact> outIterator = outAdjListTimes[integer].iterator();
	    	int srcIndex = nodes.indexOf(integer);
	    	while(outIterator.hasNext()){
	            outIterator.advance();
	            Contact element = outIterator.value();
	            if(nodes.contains(element.node))
	            	retGraph.addEdge(srcIndex, nodes.indexOf(element.node),element.time);
	    	}
	    	
			
		}
    	return retGraph;
    }
    
    public TemporalGraph subgraphSelectedEdges(Vector<Integer> nodes, TIntHashSet[] edges) {
    	int finalNodes = nodes.size();
    	TemporalGraph retGraph = new TemporalGraph(true,finalNodes);
    	for (Iterator<Integer> iterator = nodes.iterator(); iterator.hasNext();) {
			Integer integer = (Integer) iterator.next();
	    	TIntObjectIterator<Contact> outIterator = outAdjListTimes[integer].iterator();
	    	int srcIndex = nodes.indexOf(integer);
	    	while(outIterator.hasNext()){
	            outIterator.advance();
	            Contact element = outIterator.value();
	            if(nodes.contains(element.node) && edges[nodes.indexOf(integer)].contains(nodes.indexOf(element.node)))
	            	retGraph.addEdge(srcIndex, nodes.indexOf(element.node),element.time);
	    	}
	    	
			
		}
    	return retGraph;
    }
    
    public int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
    
    /**
     * return random subgraph
     * @param numNod: number of nodes
     * @return random subgraph
     */
    public TemporalGraph randomSubgraph(int numNod) {
    	Vector<Integer> nodes = new Vector<Integer>(numNod);
    	int currentIndex=0;
    	int shift = (getNumNodes()-1+numNod)/numNod;
    	while(nodes.size()<numNod && currentIndex < getNumNodes()) {
    		int randnumber;
    		if(currentIndex+shift < getNumNodes())
    			randnumber = getRandomNumber(currentIndex, currentIndex+shift);
    		else randnumber = getRandomNumber(currentIndex, getNumNodes()-1);
    		nodes.add(randnumber);
    		currentIndex += shift ;
    	}
    	return subgraph(nodes);
    }
    
    
    int queuePop(TIntHashSet queue) {
    	int returnVal=-1;
    	TIntIterator iterator = queue.iterator();
    	if(iterator.hasNext()) {
    		returnVal=iterator.next();
    		queue.remove(returnVal);
    	}
    	return returnVal;
    }
    /**
     * return random subgraph where nodes are weakly connected
     * @param numNod: number of nodes
     * @return random subgraph
     */
    public TemporalGraph randomWeaklyConnectedSubgraph(int numNod) {
    	Vector<Integer> nodes = new Vector<Integer>(numNod);
    	int startingnode = getRandomNumber(0, getNumNodes()-1);
    	TIntHashSet queue= new TIntHashSet();
    	queue.add(startingnode);
    	nodes.add(startingnode);
    	boolean finished=false;
    	while(nodes.size()<numNod && !finished) {
    		int CurrentNode = queuePop(queue);
    		if(CurrentNode==-1) return null;
    		for (TIntIterator iterator = inAdjList[CurrentNode].iterator(); iterator.hasNext() && !finished;) {
    			Integer integer = (Integer) iterator.next();
    			if(!nodes.contains(integer)) {
    				queue.add(integer);
    				nodes.add(integer);
    			}
    			if(nodes.size()==numNod)finished=true;
    		}
    		for (TIntIterator iterator = outAdjList[CurrentNode].iterator(); iterator.hasNext() && !finished;) {
    			Integer integer = (Integer) iterator.next();
    			if(!nodes.contains(integer)) {
    				queue.add(integer);
    				nodes.add(integer);
    			}
    			if(nodes.size()==numNod)finished=true;
    		}
    	}
    	return subgraph(nodes);
    }
    
	/*
	Print info about the graph
	*/
    public String toString()
    {
        StringBuilder str= new StringBuilder();
        int i;
        for(i=0;i<outAdjList.length;i++)
        {
        	str.append("source: ").append(i).append(" to {");
            TIntObjectIterator<Contact> it=outAdjListTimes[i].iterator();
            if(it.hasNext()) {
	            	it.advance();
	            	str.append(it.value());
            	}
            while(it.hasNext()) {
            	it.advance();
                str.append(",").append(it.value());
            }
            str.append("\n");
        }
        return str.toString();
    }
    

	/*
	Get the number of nodes in the network
	*/
    public int getNumNodes()
    {
        return outAdjList.length;
    }

	/*
	Get the out-adjacency lists of nodes in the network
	*/
    public TIntHashSet[] getOutAdjList()
    {
        return outAdjList;
    }

    /*
	Get the in-adjacency lists of nodes in the network
	*/
    public TIntHashSet[] getInAdjList()
    {
        return inAdjList;
    }


	public TIntObjectHashMap<Contact>[] getOutAdjListTimes() {
		return outAdjListTimes;
	}

	public TIntObjectHashMap<Contact>[] getInAdjListTimes() {
		return inAdjListTimes;
	}

	public int getNumedges() {
		return numedges;
	}


}
