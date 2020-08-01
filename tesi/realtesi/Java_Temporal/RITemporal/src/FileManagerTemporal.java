/*
Class for handling input and output operations
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

public class FileManagerTemporal
{
	
	int maxNodeMap=0;
	
	HashMap<Integer,NameMapping> mappingNames= new HashMap<>();
    
	/*
	Read a graph from input file
	@param graphFile: path of the file
	*/
	public TemporalGraph readGraph(String graphFile)
    {
        TemporalGraph g=null;
        try
        {
            BufferedReader br=new BufferedReader(new FileReader(graphFile));
            String str=br.readLine();
            boolean directed=false;
            if(str.equals("directed"))
                directed=true;
            int numNodes=Integer.parseInt(br.readLine());
            int source = -1;
            int destination = -1;
            g=new TemporalGraph(directed,numNodes);
            while((str=br.readLine())!=null)
            {
                String[] split=str.split("[, \t]");
                String sourcestr=split[0];
                String deststr=split[1];
                if(!mappingNames.containsKey(sourcestr.hashCode())) {
                	mappingNames.put(sourcestr.hashCode(), new NameMapping(maxNodeMap, sourcestr));
                	source = maxNodeMap;
                	maxNodeMap++;
                }else {
                	source = mappingNames.get(sourcestr.hashCode()).map;
                }
                
                if(!mappingNames.containsKey(deststr.hashCode())) {
                	mappingNames.put(deststr.hashCode(), new NameMapping(maxNodeMap, deststr));
                	destination = maxNodeMap;
                	maxNodeMap++;
                }else {
                	destination = mappingNames.get(deststr.hashCode()).map;
                }
                int time=Integer.parseInt(split[2]);
                g.addEdge(source,destination,time);
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
    public Vector<TemporalGraph> readQueries(String queriesFile)
    {
        Vector<TemporalGraph> setQueries=new Vector<>();
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
                TemporalGraph q=new TemporalGraph(directed,numNodes);
                while((str=br.readLine())!=null && !str.startsWith("#"))
                {
                    String[] split=str.split("[ \t]");
                    int source=Integer.parseInt(split[0]);
                    int dest=Integer.parseInt(split[1]);
                    int time=Integer.parseInt(split[2]);
                    q.addEdge(source,dest,time);
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
