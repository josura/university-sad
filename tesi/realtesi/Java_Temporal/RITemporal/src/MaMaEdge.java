/*
Data structure for representing the edges of a query in the RI matching algorithm
*/

public class MaMaEdge
{
	public int source;
	public int target;
	public int time;
	public MaMaEdge(int source, int target)
	{
		this.source = source;
		this.target = target;	
	}
	
	public MaMaEdge(int source, int target, int time)
	{
		this.source = source;
		this.target = target;
		this.time = time;
	}
}