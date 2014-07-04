package jFasta;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Diagonal implements Comparable<Diagonal>
{
	private static Logger logger = LoggersManager.getLogger(Diagonal.class.getName());	
	
	@Override
	public String toString() {
		return "Diagonal [diagonalId=" + diagonalId + ", score=" + score
				+ ", runs=" + runs + "]";
	}

	private int diagonalId;
	private int score, totalDiagonalRunScore;
	private ArrayList<DiagonalRun> runs;
	
	
	public Diagonal(int id)
	{
		diagonalId = id;
		score = 0;
		totalDiagonalRunScore = Integer.MIN_VALUE;
		runs = new ArrayList<DiagonalRun>();
	}
	
	public ArrayList<DiagonalRun> getDiagonalRuns(){
		return runs;
	}
	
	
	public int getScore()
	{
		return score;
	}
	
	public void increaseScore()
	{
		score++;
	}
	
	public void increaseScore(int quantity)
	{
		score += quantity;
	}
	
	public void addDiagonalRun(DiagonalRun run)
	{
		runs.add(run);
	}
	
	public void addDiagonalRun(int i, int j)
	{
		runs.add(new DiagonalRun(i, j));
	}
	
	@Override
	public int compareTo(Diagonal oth) {
		return oth.getScore() - score;
	}

	public int getDiagonalId() {
		return diagonalId;
	}
	
	public int getTotalDiagonalRunScore()
	{
		if (totalDiagonalRunScore == Integer.MIN_VALUE)
		{
			totalDiagonalRunScore = 0;
			for (DiagonalRun dr : runs)
			{
				totalDiagonalRunScore += dr.getScore();
			}
		}
		return totalDiagonalRunScore;
	}
	
	
	public void addHotSpot(HotSpot hs)
	{
		if (runs.size() == 0)
		{
			DiagonalRun run = new DiagonalRun(hs.getI(), hs.getJ());
			run.addHotSpot(hs);	
			runs.add(run);
		}
		else
		{
			boolean fallback = true;
			for (DiagonalRun r : runs)
			{
				if(canExtend(hs, r))
				{
					runs.get(runs.size() - 1).addHotSpot(hs);
					fallback = false;
					break;
				}
			}
			if (fallback)
			{
				DiagonalRun run = new DiagonalRun(hs.getI(), hs.getJ());
				run.addHotSpot(hs);	
				runs.add(run);
			}
		}
		increaseScore();
	}

	private boolean canExtend(HotSpot hs, DiagonalRun run) {
		HotSpot first = run.getFirstHotSpot();
		HotSpot last = run.getLastHotSpot();
		return (hs.in(last.getI()+1, last.getJ()+1)) 
				|| (first.in(hs.getI()+1, hs.getJ()+1));
	}
	

}
