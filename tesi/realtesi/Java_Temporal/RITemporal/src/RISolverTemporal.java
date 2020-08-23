/*
RI matching algorithm
*/

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.BitSet;
import java.util.Vector;

public class RISolverTemporal
{
	//Matching state machine
	private MatchingMachine mama;
	//Target graph
	private final TemporalGraph targetGraph;
	//Number of matches of query graph into target graph
	private long numMatches;
	//Induced or not?
	private final boolean induced;
	
	/*
	Constructor
	*/
	public RISolverTemporal(TemporalGraph targetGraph, boolean induced)
	{
		this.targetGraph=targetGraph;
		this.induced=induced;
		numMatches=0;
	}
	
	/*
	Compute the number of matches of query graph into target graph
	*/
	void printsolution(int[] sol){
		for(int i =0;i<sol.length;i++) {
			System.out.println("(" + mama.map_state_to_node[i] + ", " + sol[i] + ")");
			
		}
		System.out.println();
	}
	public void solve(TemporalGraph queryGraph, int delta)
	{
		numMatches=0;

		//Compute compatibility domains
		BitSet[] domains=computeDomains(queryGraph,delta);
		//Build the state space representation machine
		this.mama=new MatchingMachine(queryGraph);
		//Compute the set of query symmetry breaking conditions
		Vector<Integer>[] symmCond=queryGraph.getSymmetryConditions(delta);

		TIntHashSet[] targetOutAdjLists=targetGraph.getOutAdjList();
		TIntHashSet[] targetInAdjLists=targetGraph.getInAdjList();
		int nofTargetNodes=targetGraph.getNumNodes();
		int i, j;
		int nof_sn=mama.nof_sn;
		int[] parent_state=mama.parent_state;
		MamaParentType[] parent_type=mama.parent_type;
		//Iterators for the set of target candidate nodes for matching
		//in order to know from which candidate the search should proceed when doing backtracking
		//One iterator for each query node
		int[] candidatesIT=new int[nof_sn];
		//Set of target candidate nodes for matching, one for each query node
		int[][] candidates = new int[nof_sn][];
		//Partial mapping between query and target nodes
		int[] solution = new int[nof_sn];
		for(i=0; i<nof_sn; i++)
			solution[i] = -1;
		//Set of already mapped target nodes
		boolean[] matched = new boolean[nofTargetNodes];

		//Build the set of initial candidate nodes,
		//i.e. the set of target nodes in the domain of the first query node to process
		for(i=0; i<nof_sn; i++)
		{
			if(parent_type[i] == MamaParentType.PARENTTYPE_NULL)
			{
				int n = mama.map_state_to_node[i];
				candidates[i]=new int[domains[n].cardinality()];
				int k = 0;
				for(j = domains[n].nextSetBit(0); j >= 0; j = domains[n].nextSetBit(j+1))
				{
					candidates[i][k]=j;
					k++;
				}
				candidatesIT[i]=-1;
			}
		}

		int psi = -1;
		int si = 0;
		int ci;
		int sip1;
		while(si != -1)
		{
			if(psi >= si)
				//Backtracking: remove mapping for currently processed query node
				matched[solution[si]] = false;
			ci = -1;
			//Process match between currently processed query node and next candidate node
			candidatesIT[si]++;
			while(candidatesIT[si]<candidates[si].length)
			{
				ci = candidates[si][candidatesIT[si]];
				//Add mapping
				solution[si] = ci;
				//Check if target node-query node mapping is feasible
				if(!matched[ci]
						&& domains[mama.map_state_to_node[si]].get(ci)
						&& condCheck(si,solution,symmCond)
						//TODO control on the subgraph and not on the full structure
						&& edgesCheck(si,ci,solution,matched,queryGraph,delta)
						)
					break;
				else
					ci=-1;
				//Mapping is not feasible, go on with next candidate
				candidatesIT[si]++;
			}
			//No candidate target nodes can be mapped to the currently processed query node
			//Do backtracking and go back to the previously processed query node
			if(ci == -1)
			{
				psi = si;
				si--;
			}
			else
			{
				//Mapping is feasible
				if(si == nof_sn -1)
				{
					//TODO final temporal structure control need to be optimized
					Vector<Integer> nodes = new Vector<>(queryGraph.getNumNodes());
					Vector<Integer> mapping = new Vector<>(queryGraph.getNumNodes());
					TIntHashSet[] edges = new TIntHashSet[queryGraph.getNumNodes()];
					TIntHashSet[] outEdgesQuery = queryGraph.getOutAdjList();
					for(int index=0;index<queryGraph.getNumNodes();index++)
			        {
			            edges[index] = new TIntHashSet();
			        }
					for(int index = 0; index < solution.length;index++) {
						nodes.add(solution[index]);
						mapping.add(index);
					}
					for(int index = 0; index < solution.length;index++) {
						nodes.set(mama.map_state_to_node[index], solution[index]);
						mapping.set(index, mama.map_state_to_node[index]);
						
						TIntIterator iteratorEdges = outEdgesQuery[mama.map_state_to_node[index]].iterator();
						while(iteratorEdges.hasNext()) {
							int element = iteratorEdges.next();
							int source = nodes.indexOf(solution[index]);
							int destination = nodes.indexOf(solution[mama.map_node_to_state[element]]);
							if (source >=0 && destination >=0) edges[source].add(destination);
						}
					}
					TemporalGraph subtarget = targetGraph.subgraphSelectedEdges(nodes,edges);
					
					//control if the mapping is right and temporal structure is the same
					if(queryGraph.testMap(subtarget, mapping, delta)) {
						//All query nodes have been mapped. Update the number of occurrences found
						numMatches++;
						//printsolution(solution);
						if(numMatches%10000000==0)
							System.out.println("Found "+numMatches+" occurrences...");
					}
					psi = si;
				}
				else
				{
					//There are still unmapped query nodes. Continue the search
					matched[solution[si]] = true;
					//Go to the next query node to process for matching
					sip1 = si+1;
					if(parent_type[sip1] != MamaParentType.PARENTTYPE_NULL)
					{
						//Build the set of target candidate nodes for matching with the new query node
						if(parent_type[sip1] == MamaParentType.PARENTTYPE_IN)
							candidates[sip1] = targetInAdjLists[solution[parent_state[sip1]]].toArray();
						else
							candidates[sip1] = targetOutAdjLists[solution[parent_state[sip1]]].toArray();
					}
					//Start from the first target candidate node for that query node
					candidatesIT[si+1]=-1;
					psi = si;
					si++;
				}
			}
		}
	}

	/*
	Compute the compatibility domains for each query node
	Each domain is represented as a BitSet with numTargetNodes bits.
	Bit 1 in position i means that target node i is in the compatibility domain of that query node
	 */
	private BitSet[] computeDomains(TemporalGraph queryGraph,int delta)
	{
		int numQueryNodes=queryGraph.getNumNodes();
		int numTargetNodes=targetGraph.getNumNodes();
		BitSet[] domains=new BitSet[numQueryNodes];
		int i, j;
		for(i=0;i<domains.length;i++)
			domains[i]=new BitSet(numTargetNodes);

		TIntHashSet[] targetOutAdjLists=targetGraph.getOutAdjList();
		TIntHashSet[] targetInAdjLists=targetGraph.getInAdjList();
		TIntHashSet[] queryOutAdjLists=queryGraph.getOutAdjList();
		TIntHashSet[] queryInAdjLists=queryGraph.getInAdjList();
		for(i=0;i<numTargetNodes;i++)
		{
			//Find compatible query nodes and update domains
			for(j=0;j<domains.length;j++)
			{
				if(queryOutAdjLists[j].size()<=targetOutAdjLists[i].size()
						&& queryInAdjLists[j].size()<=targetInAdjLists[i].size()
						&& queryGraph.testCompatibility(targetGraph,j,i,delta)
						)
				{
					domains[j].set(i);
					//System.out.println(j+"-"+i);
				}
			}
		}

		//Arc consistency check
		int ra, qb, rb;
		boolean notfound;
		for(int qa=0; qa<numQueryNodes; qa++)
		{
			for(ra = domains[qa].nextSetBit(0); ra >= 0; ra = domains[qa].nextSetBit(ra+1))
			{
				//for each edge qa->qb  check if exists ra->rb
				TIntIterator it=queryOutAdjLists[qa].iterator();
				while(it.hasNext())
				{
					qb = it.next();
					notfound = true;
					TIntIterator it2=targetOutAdjLists[ra].iterator();
					while(it2.hasNext())
					{
						rb = it2.next();
						if(domains[qb].get(rb))
						{
							notfound = false;
							break;
						}
					}
					if(notfound)
						domains[qa].set(ra, false);
				}
			}
		}

		return domains;
	}

	/*
	Check if symmetry breaking conditions for the currently matched query node are satisfied
	@param si: id of currently matched query node
	@param solution: set of already matched couples of query-target nodes
	@param matched: array of boolean where the i-th entry is true iff the target node i has been already matched
	*/
	public boolean condCheck(int si, int[] solution, Vector<Integer>[] symmCond)
	{
		boolean condCheck=true;
		Vector<Integer> condNode=symmCond[mama.map_state_to_node[si]];
		int ii;
		for(ii=0;ii<condNode.size();ii++)
		{
			int targetNode=solution[mama.map_node_to_state[condNode.get(ii)]];
			if(solution[si]<targetNode)
			{
				condCheck=false;
				break;
			}
		}
		return condCheck;
	}
	
	/*
	Check if outgoing edges from currently matched nodes in the query and in the target, also matches
	@param si: id of the query node
	@param ci: id of the target node
	@param solution: set of already matched couples of query-target nodes
	@param matched: array of boolean where the i-th entry is true iff the target node i has been already matched
	*/
	public boolean edgesCheck(int si, int ci, int[] solution, boolean[] matched, TemporalGraph query, int delta)
	{
		TIntHashSet[] targetOutAdjLists=targetGraph.getOutAdjList();
		for(int me=0; me<mama.edges_sizes[si]; me++)
		{
			int querySource = mama.edges[si][me].source;
			int queryDest = mama.edges[si][me].target;
			int querytime = mama.edges[si][me].time;
			int source = solution[querySource];
			int target = solution[queryDest];
			int trueSource = mama.map_state_to_node[querySource];
			int trueDest = mama.map_state_to_node[queryDest];
			if(!targetOutAdjLists[source].contains(target))
				return false;
			int time = targetGraph.getOutAdjListTimes()[source].get(target).time;
			if(!query.controlTemporals(query.nodeTemporalStructure(trueDest,querytime,delta),targetGraph.nodeTemporalStructure(target,time,delta))) return false;
		}
		return true;
	}

	public long getNumMatches()
	{
		return numMatches;
	}
}