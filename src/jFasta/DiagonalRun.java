package jFasta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiagonalRun
{
	private int id;
	private int i, j;
	private int score;
	private List<HotSpot> hotSpostList;
	
	public List<HotSpot> getHotSpostList() {
		return hotSpostList;
	}

	public DiagonalRun(int i, int j)
	{
		this.id = i - j;
		this.i = i;
		this.j = j;
		this.score = 0;
		hotSpostList = new ArrayList<HotSpot>();
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "DiagonalRun [id=" + id + ", i=" + i + ", j=" + j
				+ ", hotSpostList=" + hotSpostList + "]";
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}
	
	public void sort()
	{
		Collections.sort(hotSpostList);
	}
	
	public void addHotSpot(HotSpot hs){
		sort();
		hotSpostList.add(hs);
	}
	
	public HotSpot getHotSpotAt(int i){
		return hotSpostList.get(i);
	}
	
	public HotSpot getLastHotSpot(){
		return  hotSpostList.size()>0 ? hotSpostList.get(hotSpostList.size()-1) : null;
	}
	
	public HotSpot getFirstHotSpot(){
		return  hotSpostList.size()>0 ? hotSpostList.get(0) : null;
	}
	
	public String extractStringFromReference(String ref)
	{
		HotSpot last = getLastHotSpot();
		int lastIndex = j + (last.getJ() - j + last.getK());
		return ref.substring(j, lastIndex);
	}
	
	public String extractStringFromQuery(String qry)
	{
		HotSpot last = getLastHotSpot();
		int lastIndex = i + (last.getI() - i + last.getK());
		return qry.substring(i, lastIndex);
	}

	public void increaseScore(int score) 
	{
		this.score += score;
	}

	public int getScore() {
		return score;
	}

}
