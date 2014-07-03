package jFasta;

public class HotSpot implements Comparable<HotSpot>
{
	private int i, j, k;
	
	public HotSpot(int i, int j)
	{
		this.i = i;
		this.j = j;
		this.k = Fasta.getInstance().getKTup();
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	public int getK() {
		return k;
	}
	
	public boolean in(int i, int j)
	{
		return (getI() == i && getJ() == j);
	}
	
	public int[] getLastPoint()
	{
		return new int[] { i + k - 1, j + k - 1 };
	}
	
	public String getKTupleFromReference(String ref)
	{
		return ref.substring(j, j + k);
	}
	
	public String getKTupleFromQuery(String qry)
	{
		return qry.substring(i, i + k);
	}

	@Override
	public String toString() {
		return "HotSpot [i=" + i + ", j=" + j + ", k=" + k + "]";
	}

	@Override
	public int compareTo(HotSpot oth)
	{
		 if (i < oth.i) return -1;
         if (i > oth.i) return +1;
         if (j < oth.j) return -1;
         if (j > oth.j) return +1;
         return 0;
	}
	
	
	
}
