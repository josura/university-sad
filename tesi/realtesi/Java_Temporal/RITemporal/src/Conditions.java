
public class Conditions {
    public int notTimeRespecting;
    public int deltaNotRespected;
    public int deltaRespected;

    public Conditions(int notT,int deltaN,int deltaR) {
    	int notTimeRespecting = notT;
        int deltaNotRespected = deltaN;
        int deltaRespected = deltaR;
    }
    
    public boolean LTOE (Conditions that){
      return (notTimeRespecting <= that.notTimeRespecting && deltaNotRespected <= that.deltaNotRespected && deltaRespected <= that.deltaRespected); 
    }
    
    @Override
    public String toString(){
      return " NTR " + notTimeRespecting+ " DNR " + deltaNotRespected + " DR " + deltaRespected;
    } 
}
