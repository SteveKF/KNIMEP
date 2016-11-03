package algorithm;
import java.util.List;

public class DataHolder{
	
	private Double optEr;
	private Double radius;
	private Integer optErMinIndex;
	private DataPoint center;;
	private List<DataPoint> opt;

	public DataHolder(){}
	
	
	public Double getOptEr(){
		return optEr;
	}
	
	public void setOptEr(double optEr){
		this.optEr = optEr;
	}
	
	public Double getRadius(){
		return radius;
	}
	
	public void setRadius(double radius){
		this.radius = radius;
	}
	
	public DataPoint getCenter(){
		return center;
	}
	
	public void setCenter(DataPoint center){
		this.center = center;
	}
	
	public Integer getOptErMinIndex(){
		return optErMinIndex;
	}
	
	public void setOptErMinIndex(int optErMinIndex){
		this.optErMinIndex = optErMinIndex;
	}
	
	public List<DataPoint> getOpt(){
		return opt;
	}
	
	public void setOpt(List<DataPoint> opt){
		this.opt = opt;
	}
	
}
