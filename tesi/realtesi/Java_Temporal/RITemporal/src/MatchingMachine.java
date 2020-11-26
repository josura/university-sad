/*
Data structure used to model the State-Space Representation for the matching process within RI algorithm
Each query node is associated to a state
Query nodes are processed for the search following the order of their states.
So, node with state 0 is processed first, then node with state 1 and so on.
*/

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class MatchingMachine
{
	public enum NodeFlag {NS_CORE, NS_CNEIGH, NS_UNV}
	//Number of query nodes
	public int nof_sn;
	//Number of incident edges to each query node
	public int[] edges_sizes;
	//Number of incident outgoing edges to each query node
	public int[] o_edges_sizes;
	//Number of incident ingoing edges to each query node
	public int[] i_edges_sizes;
	//Set of query edges
	public MaMaEdge[][] edges;
	//Map each node to the corresponding state
	public int[] map_node_to_state;
	//Map each state to the corresponding node
	public int[] map_state_to_node;
	//Parent state of each state S,
	//i.e. state of the predecessor or successor node (in the query graph) of node mapped to state S
	public int[] parent_state;
	//Type of node mapped to parent state of each state S, i.e. predecessor or successor
	public MamaParentType[] parent_type;
	
	/*
	Constructor
	*/
	public MatchingMachine(Graph query)
	{
		nof_sn = query.getNumNodes();
		edges_sizes = new int[nof_sn];
		o_edges_sizes = new int[nof_sn];
		i_edges_sizes = new int[nof_sn];
		edges = new MaMaEdge[nof_sn][];
		map_node_to_state = new int[nof_sn];
		map_state_to_node = new int[nof_sn];
		parent_state = new int[nof_sn];
		parent_type = new MamaParentType[nof_sn];
		build(query);
	}
	
	public MatchingMachine(TemporalGraph query)
	{
		nof_sn = query.getNumNodes();
		edges_sizes = new int[nof_sn];
		o_edges_sizes = new int[nof_sn];
		i_edges_sizes = new int[nof_sn];
		edges = new MaMaEdge[nof_sn][];
		map_node_to_state = new int[nof_sn];
		map_state_to_node = new int[nof_sn];
		parent_state = new int[nof_sn];
		parent_type = new MamaParentType[nof_sn];
		build(query);
	}
	
	/**
	 * build matching machine for temporal graph
	 * @param ssg: query temporal graph
	 */
	public void build(TemporalGraph ssg)
	{
		TIntHashSet[] outAdiacs=ssg.getOutAdjList();
		TIntHashSet[] inAdiacs=ssg.getInAdjList();
		TIntObjectHashMap<Contact>[] outAdiacsTimes = ssg.getOutAdjListTimes();
		int i, j;
		NodeFlag[] node_flags = new NodeFlag[nof_sn];
		int[][] weights = new int[nof_sn][3];
		int[] t_parent_node = new int[nof_sn];
		MamaParentType[] t_parent_type = new MamaParentType[nof_sn];
		for(i=0; i<nof_sn; i++)
		{
			node_flags[i] = NodeFlag.NS_UNV;
			weights[i] = new int[3];
			weights[i][0] = 0;
			weights[i][1] = 0;
			weights[i][2] = outAdiacs[i].size() + inAdiacs[i].size();
			t_parent_node[i] = -1;
			t_parent_type[i] = MamaParentType.PARENTTYPE_NULL;
		}
		int si = 0;
		int n;
		int nIT; int ni;
		int nni;
		int nqueueL = 0, nqueueR = 0;
		int maxi, maxv;
		int tmp;
		while(si < nof_sn)
		{
			if(nqueueL == nqueueR)
			{
				maxi = -1;
				maxv = -1;
				nIT = 0;
				while(nIT < nof_sn)
				{
					if(node_flags[nIT]==NodeFlag.NS_UNV &&  weights[nIT][2] > maxv)
					{
						maxv = weights[nIT][2];
						maxi = nIT;
					}
					nIT++;
				}
				map_state_to_node[si] = maxi;
				map_node_to_state[maxi] = si;
				t_parent_type[maxi] = MamaParentType.PARENTTYPE_NULL;
				t_parent_node[maxi] = -1;
				nqueueR++;
				n = maxi;
				TIntIterator it=outAdiacs[n].iterator();
				while(it.hasNext())
				{
					ni=it.next();
					if(ni != n)
						weights[ni][1]++;
				}
				it=inAdiacs[n].iterator();
				while(it.hasNext())
				{
					ni=it.next();
					if(ni != n)
						weights[ni][1]++;
				}
			}
			if(nqueueL != nqueueR-1)
			{
				maxi = nqueueL;
				for(int mi=maxi+1; mi<nqueueR; mi++)
				{
					if(wcompare(map_state_to_node[mi], map_state_to_node[maxi], weights) < 0)
						maxi = mi;
				}
				tmp = map_state_to_node[nqueueL];
				map_state_to_node[nqueueL] = map_state_to_node[maxi];
				map_state_to_node[maxi] = tmp;
			}
			n = map_state_to_node[si];
			map_node_to_state[n] = si;
			nqueueL++;
			node_flags[n] = NodeFlag.NS_CORE;
			TIntIterator it=outAdiacs[n].iterator();
			while(it.hasNext())
			{
				ni=it.next();
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
						t_parent_type[ni] = MamaParentType.PARENTTYPE_OUT;
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						TIntIterator it2=outAdiacs[ni].iterator();
						while(it2.hasNext())
						{
							nni=it2.next();
							weights[nni][1]++;
						}
					}
				}
			}
			it=inAdiacs[n].iterator();
			while(it.hasNext())
			{
				ni=it.next();
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
						t_parent_type[ni] = MamaParentType.PARENTTYPE_IN;
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						TIntIterator it2=inAdiacs[ni].iterator();
						while(it2.hasNext())
						{
							nni=it2.next();
							weights[nni][1]++;
						}
					}
				}
			}
			si++;
		}
		int e_count,o_e_count,i_e_count;
		for(si = 0; si<nof_sn; si++)
		{
			n = map_state_to_node[si];
			if(t_parent_node[n] != -1)
				parent_state[si] = map_node_to_state[t_parent_node[n]];
			else
				parent_state[si] = -1;
			parent_type[si] = t_parent_type[n];
			e_count = 0;
			o_e_count = 0;
			TIntIterator it=outAdiacs[n].iterator();
			while(it.hasNext())
			{
				int idOut=it.next();
				if(map_node_to_state[idOut]<si)
				{
					e_count++;
					o_e_count++;
				}
			}
			i_e_count = 0;
			it=inAdiacs[n].iterator();
			while(it.hasNext())
			{
				int idIn=it.next();
				if(map_node_to_state[idIn]<si)
				{
					e_count++;
					i_e_count++;
				}
			}
			edges_sizes[si] = e_count;
			o_edges_sizes[si] = o_e_count;
			i_edges_sizes[si] = i_e_count;
			edges[si] = new MaMaEdge[e_count];
			e_count = 0;
			it=outAdiacs[n].iterator();
			
			TIntObjectIterator<Contact> itTimes = outAdiacsTimes[n].iterator();
			while(it.hasNext())
			{
				int idOut=it.next();
				int time = 0;
		        if (itTimes.hasNext()){
		          itTimes.advance();
		          time = itTimes.value().time;
		        }
				if(map_node_to_state[idOut] < si)
				{
					edges[si][e_count]=new MaMaEdge(map_node_to_state[n],map_node_to_state[idOut],time);
					e_count++;
				}
			}
			for(j=0; j<si; j++)
			{
				int sn = map_state_to_node[j];
				it=outAdiacs[sn].iterator();
				itTimes = outAdiacsTimes[sn].iterator();
				while(it.hasNext())
				{
					int idOut=it.next();
					int time = 0;
			        if (itTimes.hasNext()){
			          itTimes.advance();
			          time = itTimes.value().time;
			        }
					if(idOut==n)
					{
						edges[si][e_count]=new MaMaEdge(j,si,time);
						e_count++;
					}
				}
			}
		}
	}
	
	/*
	Build the matching state machine for a query graph
	@param ssg: query graph
	*/
	public void build(Graph ssg)
	{
		TIntHashSet[] outAdiacs=ssg.getOutAdjList();
		TIntHashSet[] inAdiacs=ssg.getInAdjList();
		int i, j;
		NodeFlag[] node_flags = new NodeFlag[nof_sn];
		int[][] weights = new int[nof_sn][3];
		int[] t_parent_node = new int[nof_sn];
		MamaParentType[] t_parent_type = new MamaParentType[nof_sn];
		for(i=0; i<nof_sn; i++)
		{
			node_flags[i] = NodeFlag.NS_UNV;
			weights[i] = new int[3];
			weights[i][0] = 0;
			weights[i][1] = 0;
			weights[i][2] = outAdiacs[i].size() + inAdiacs[i].size();
			t_parent_node[i] = -1;
			t_parent_type[i] = MamaParentType.PARENTTYPE_NULL;
		}
		int si = 0;
		int n;
		int nIT; int ni;
		int nni;
		int nqueueL = 0, nqueueR = 0;
		int maxi, maxv;
		int tmp;
		while(si < nof_sn)
		{
			if(nqueueL == nqueueR)
			{
				maxi = -1;
				maxv = -1;
				nIT = 0;
				while(nIT < nof_sn)
				{
					if(node_flags[nIT]==NodeFlag.NS_UNV &&  weights[nIT][2] > maxv)
					{
						maxv = weights[nIT][2];
						maxi = nIT;
					}
					nIT++;
				}
				map_state_to_node[si] = maxi;
				map_node_to_state[maxi] = si;
				t_parent_type[maxi] = MamaParentType.PARENTTYPE_NULL;
				t_parent_node[maxi] = -1;
				nqueueR++;
				n = maxi;
				TIntIterator it=outAdiacs[n].iterator();
				while(it.hasNext())
				{
					ni=it.next();
					if(ni != n)
						weights[ni][1]++;
				}
				it=inAdiacs[n].iterator();
				while(it.hasNext())
				{
					ni=it.next();
					if(ni != n)
						weights[ni][1]++;
				}
			}
			if(nqueueL != nqueueR-1)
			{
				maxi = nqueueL;
				for(int mi=maxi+1; mi<nqueueR; mi++)
				{
					if(wcompare(map_state_to_node[mi], map_state_to_node[maxi], weights) < 0)
						maxi = mi;
				}
				tmp = map_state_to_node[nqueueL];
				map_state_to_node[nqueueL] = map_state_to_node[maxi];
				map_state_to_node[maxi] = tmp;
			}
			n = map_state_to_node[si];
			map_node_to_state[n] = si;
			nqueueL++;
			node_flags[n] = NodeFlag.NS_CORE;
			TIntIterator it=outAdiacs[n].iterator();
			while(it.hasNext())
			{
				ni=it.next();
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
						t_parent_type[ni] = MamaParentType.PARENTTYPE_OUT;
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						TIntIterator it2=outAdiacs[ni].iterator();
						while(it2.hasNext())
						{
							nni=it2.next();
							weights[nni][1]++;
						}
					}
				}
			}
			it=inAdiacs[n].iterator();
			while(it.hasNext())
			{
				ni=it.next();
				if(ni != n)
				{
					weights[ni][0]++;
					weights[ni][1]--;
					if(node_flags[ni] == NodeFlag.NS_UNV)
					{
						node_flags[ni] = NodeFlag.NS_CNEIGH;
						t_parent_node[ni] = n;
						t_parent_type[ni] = MamaParentType.PARENTTYPE_IN;
						map_state_to_node[nqueueR] = ni;
						map_node_to_state[ni] = nqueueR;
						nqueueR++;
						TIntIterator it2=inAdiacs[ni].iterator();
						while(it2.hasNext())
						{
							nni=it2.next();
							weights[nni][1]++;
						}
					}
				}
			}
			si++;
		}
		int e_count,o_e_count,i_e_count;
		for(si = 0; si<nof_sn; si++)
		{
			n = map_state_to_node[si];
			if(t_parent_node[n] != -1)
				parent_state[si] = map_node_to_state[t_parent_node[n]];
			else
				parent_state[si] = -1;
			parent_type[si] = t_parent_type[n];
			e_count = 0;
			o_e_count = 0;
			TIntIterator it=outAdiacs[n].iterator();
			while(it.hasNext())
			{
				int idOut=it.next();
				if(map_node_to_state[idOut]<si)
				{
					e_count++;
					o_e_count++;
				}
			}
			i_e_count = 0;
			it=inAdiacs[n].iterator();
			while(it.hasNext())
			{
				int idIn=it.next();
				if(map_node_to_state[idIn]<si)
				{
					e_count++;
					i_e_count++;
				}
			}
			edges_sizes[si] = e_count;
			o_edges_sizes[si] = o_e_count;
			i_edges_sizes[si] = i_e_count;
			edges[si] = new MaMaEdge[e_count];
			e_count = 0;
			it=outAdiacs[n].iterator();
			while(it.hasNext())
			{
				int idOut=it.next();
				if(map_node_to_state[idOut] < si)
				{
					edges[si][e_count]=new MaMaEdge(map_node_to_state[n],map_node_to_state[idOut]);
					e_count++;
				}
			}
			for(j=0; j<si; j++)
			{
				int sn = map_state_to_node[j];
				it=outAdiacs[sn].iterator();
				while(it.hasNext())
				{
					int idOut=it.next();
					if(idOut==n)
					{
						edges[si][e_count]=new MaMaEdge(j,si);
						e_count++;
					}
				}
			}
		}
	}
	
	private int wcompare(int i, int j, int[][] weights)
	{
		for(int w=0; w<3; w++)
		{
			if(weights[i][w] != weights[j][w])
				return weights[j][w] - weights[i][w];
		}
		return i-j;
	}

}
