/*
Class for handling input and output operations
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

public class FileManager
{
    
	/*
	Read a graph from input file
	@param graphFile: path of the file
	*/
	public Graph readGraph(String graphFile)
    {
        Graph g=null;
        try
        {
            BufferedReader br=new BufferedReader(new FileReader(graphFile));
            String str=br.readLine();
            boolean directed=false;
            if(str.equals("directed"))
                directed=true;
            int numNodes=Integer.parseInt(br.readLine());
            g=new Graph(directed,numNodes);
            while((str=br.readLine())!=null)
            {
                String[] split=str.split("[ \t]");
                int source=Integer.parseInt(split[0]);
                int dest=Integer.parseInt(split[1]);
                g.addEdge(source,dest);
            }
            br.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return g;
    }

    /*
	Read the set of queries from input file
	@param queriesFile: path of the file
	*/
    public Vector<Graph> readQueries(String queriesFile)
    {
        Vector<Graph> setQueries=new Vector<>();
        try
        {
            BufferedReader br=new BufferedReader(new FileReader(queriesFile));
            String str=br.readLine();
            while(str!=null)
            {
                String direction=br.readLine();
                boolean directed=false;
                if(direction.equals("directed"))
                    directed=true;
                int numNodes=Integer.parseInt(br.readLine());
                Graph q=new Graph(directed,numNodes);
                while((str=br.readLine())!=null && !str.startsWith("#"))
                {
                    String[] split=str.split("[ \t]");
                    int source=Integer.parseInt(split[0]);
                    int dest=Integer.parseInt(split[1]);
                    q.addEdge(source,dest);
                }
                setQueries.add(q);
            }
            br.close();
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        return setQueries;
    }

	/*
	Write query results to output file
	*/
    public void writeResults(Vector<Graph> setQueries, Vector<Long> setCounts, Vector<Double> setRunningTimes, String outputFile)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write("Query\tNum_occ\tTime (secs)\n");
            int i;
            for(i=0;i<setQueries.size();i++)
            {
                Graph q=setQueries.get(i);
                String adjString=q.getAdjString();
                bw.write(adjString+"\t"+setCounts.get(i)+"\t"+setRunningTimes.get(i)+"\n");
            }
            bw.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}
