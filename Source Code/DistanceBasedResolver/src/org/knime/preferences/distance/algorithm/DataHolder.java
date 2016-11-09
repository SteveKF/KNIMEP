package org.knime.preferences.distance.algorithm;
import java.util.List;
/**
 * Class for holding data for the DistanceRepSky class.
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DataHolder{
	
	private Double optEr;
	private Double radius;
	private Integer optErMinIndex;
	private DataPoint center;;
	private List<DataPoint> opt;

	/**
	 * Constructor
	 */
	protected DataHolder(){}
	
	/**
	 * 
	 * @return Returns the optEr
	 */
	public Double getOptEr(){
		return optEr;
	}
	
	/**
	 * Sets the optEr
	 * @param optEr - value of optEr
	 */
	public void setOptEr(double optEr){
		this.optEr = optEr;
	}
	
	/**
	 * 
	 * @return Returns the radius
	 */
	public Double getRadius(){
		return radius;
	}
	
	/**
	 * Sets the radius
	 * @param radius - value of the radius
	 */
	public void setRadius(double radius){
		this.radius = radius;
	}
	
	/**
	 *
	 * @return  Returns the center of a circle which is a DataPoint
	 */
	public DataPoint getCenter(){
		return center;
	}
	
	/**
	 * Sets the center
	 * @param center - the DataPoint which is the center of a circle
	 */
	public void setCenter(DataPoint center){
		this.center = center;
	}
	
	/**
	 * 
	 * @return Returns the index where the optEr has its minimum
	 */
	public Integer getOptErMinIndex(){
		return optErMinIndex;
	}
	
	/**
	 * Sets the index where the optEr has its minimum
	 * @param optErMinIndex - index
	 */
	public void setOptErMinIndex(int optErMinIndex){
		this.optErMinIndex = optErMinIndex;
	}
	
	/**
	 * 
	 * @return Returns the representative skyline
	 */
	public List<DataPoint> getOpt(){
		return opt;
	}
	
	/**
	 * Sets the representative skyline
	 * @param opt - list of a representative skyline data points
	 */
	public void setOpt(List<DataPoint> opt){
		this.opt = opt;
	}
	
}
