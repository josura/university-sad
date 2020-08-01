
public class Contact {
	public int node,time;
	
	public Contact(int _node,int _time) {
		node = _node;
		time = _time;
	}
	public String toString(){
	  return " --" + time + "--> "+ node;
	}
	public boolean equals(Contact x){
	    return node == x.node && time == x.time;
	  }
	public int hashCode(){
	    //(node<<16) ^ time   // not for now because the consideration is only for single contacts for edge
	    return node;
	  }
	  public boolean Equals(Contact x){
	    return this.equals(x);
	  }
}
