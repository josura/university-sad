/*
Graph data structure for RI
*/

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Vector;

public class Graph
{
	
	/*Adjacency lists for graph nodes. The i-th entry of the array is the adjacency list for node with ID i. 
	The array is a set of integers, representing the IDs of adjacent nodes of node i*/
	private final TIntHashSet[] outAdjList;
    private final TIntHashSet[] inAdjList;
	//Is the graph directed or not?
    private final boolean directed;
	
	/*
	Constructor
	@param directed: is the graph directed or not?
	@param numNodes: number of nodes of the graph
	*/
    public Graph(boolean directed, int numNodes)
    {
        this.directed=directed;
        this.outAdjList=new TIntHashSet[numNodes];
        this.inAdjList=new TIntHashSet[numNodes];
        for(int i=0;i<numNodes;i++)
        {
            outAdjList[i] = new TIntHashSet();
            inAdjList[i] = new TIntHashSet();
        }
    }
	
	/*
	Add an edge to the graph
	@param source: the first node of the edge
	@param dest: the second node of the edge
	*/
    public Graph addEdge(int source, int dest)
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

    /*
	Get the list of symmetry breaking conditions of the graph
	@param adjMat: adjacency matrix of a graph
	*/
    public Vector<Integer>[] getSymmetryConditions()
    {
        int i, j, k;
        Vector<int[]> vv=findAutomorphisms();
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
    private Vector<int[]> findAutomorphisms()
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
                isomorphicExtensions(fDir, fRev, vv, support, pos);
                fRev[fDir[0]] = -1;
                fDir[0] = -1;
            }
        }
        return vv;
    }

    /*
	Computes all isomorphic extensions for a graph
	*/
    private void isomorphicExtensions(int[] fDir, int[] fRev, Vector<int[]> vv, boolean[] support, int pos)
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
                        if(outAdjList[m].contains(j)!=outAdjList[n].contains(fDir[j]))
                        {
                            flag=true;
                            break;
                        }
                        else if(outAdjList[j].contains(m)!=outAdjList[fDir[j]].contains(n))
                        {
                            flag=true;
                            break;
                        }
                    }
                }
                if(!flag)
                {
                    fDir[m] = n;
                    fRev[n] = m;
                    pos++;
                    isomorphicExtensions(fDir, fRev, vv, support, pos);
                    pos--;
                    fRev[fDir[m]] = -1;
                    fDir[m] = -1;
                }
            }
        }
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
            str.append(i).append(" --> ");
            TIntIterator it=outAdjList[i].iterator();
            str.append(it.next());
            while(it.hasNext())
                str.append(",").append(it.next());
            str.append("\n");
        }
        return str.toString();
    }
}
