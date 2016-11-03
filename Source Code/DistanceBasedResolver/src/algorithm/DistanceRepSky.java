package algorithm;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DistanceRepSky {

	private List<DataPoint> repSkyline;
	private Map<DistanceKey,DataHolder> data;
	private final int X = 0;
	private final int Y = 1;
	
	public DistanceRepSky(List<DataPoint> points, int k){
		
		data = new HashMap<>();
		repSkyline = new LinkedList<>();
		compute(points,k);
	
	}
	
	private void compute(List<DataPoint> points, int k){
				
		int m = points.size()-1;
		k--;
		
		for(int i=0; i < points.size(); i++){
			for(int j=i; j < points.size(); j++){
				radius(i,j, points);
			}
		}
		
		for(int t=1; t <= k-1; t++){
			for(int i=t; i <= m; i++){
				optEr(i,t);
				opt(i,t);
			}
		}
		
		optEr(m,k);
		repSkyline = opt(m,k);
	
	}
	
	private void radius(int i, int j, List<DataPoint> points){
		
		DataHolder holder = getDataHolder(i, j);
				
		double radius = Double.MAX_VALUE;
		int index = i;
		
		for(int u=i; u < j; u++){
			
			double r1,r2;
			
			double x1 = Math.pow(points.get(i).getCoordinate(X)-points.get(u).getCoordinate(X),2);
			double y1 = Math.pow(points.get(i).getCoordinate(Y)-points.get(u).getCoordinate(Y),2);
			r1 = Math.sqrt(x1+y1);
			
			double x2 = Math.pow(points.get(u).getCoordinate(X)-points.get(j).getCoordinate(X),2);
			double y2 = Math.pow(points.get(u).getCoordinate(Y)-points.get(j).getCoordinate(Y),2);
			r2 = Math.sqrt(x2+y2);
			
			double tmp = Math.max(r1, r2);
			int compareVal = Double.compare(radius, tmp);
			
			if(compareVal > 0){
				radius = tmp;
				index = u;
			}
			
		}
		
		holder.setRadius(radius);
		holder.setCenter(points.get(index));
		
	}	
	
	private double optEr(int i, int t){
		
		DataHolder holder = getDataHolder(i, t);
		
		//if optEr was already computed, return the value
		if(holder.getOptEr()!=null)
			return holder.getOptEr();
		
		double optEr = Double.MIN_VALUE;
		
		if(i < 0 || t < 0 || i-1 <= t){
			holder.setOptEr(optEr);
			holder.setOptErMinIndex(t);
			return optEr;
		}else if(t==0){
			holder.setOptEr(getDataHolder(0,i).getRadius());
			holder.setOptErMinIndex(t);
			optEr = getDataHolder(0, i).getRadius();
			return optEr;
		}else{
				
			int index = t; 
			optEr = Double.MAX_VALUE;
			
			for(int j=t; j < i-1; j++){
				
				double tmp = Math.max(optEr(j-1,t-1),getDataHolder(j, i).getRadius());
				int compareVal = Double.compare(optEr, tmp);
				if(compareVal > 0){
					optEr = tmp;
					index = j;
				}
			}	
			
			holder.setOptEr(optEr);
			holder.setOptErMinIndex(index);
			return optEr;
			
		}
	}
	
	private List<DataPoint> opt(int i, int t){
		
		List<DataPoint> repSkyline = new LinkedList<>();
		
		DataHolder holder = getDataHolder(i, t);
		
		//if opt was already computed, return the value
		if(holder.getOpt() != null)
			return holder.getOpt();
		
		if(t < 0 || i < 0){
			holder.setOpt(repSkyline);
			return repSkyline;
			
		}else if(t==0){
			repSkyline.add(getDataHolder(0,i).getCenter());
			holder.setOpt(repSkyline);
			return repSkyline;
		}	
		int v = holder.getOptErMinIndex();
		repSkyline.add(getDataHolder(v, i).getCenter());
		repSkyline.addAll(opt(v-1,t-1));
		holder.setOpt(repSkyline);
		
		return repSkyline;
		
	}
		
	private DataHolder getDataHolder(int i, int j){
		
		DistanceKey key = new DistanceKey(i,j);
		
		DataHolder holder = data.get(key);
		
		if(holder == null)
				holder = new DataHolder();
		
		data.put(key, holder);
		
		
		return holder;
		
	}
	
	public List<DataPoint> getRepSkyline(){
		return repSkyline;
	}
}