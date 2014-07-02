package jFasta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Fasta 
{
	private int ktup;
	private String reference, query;
	
	private HashMap<String, ArrayList<Integer>> lookupTableRef, lookupTableQuery;
	private HashMap<String, Integer> commonWords; 
	private TreeMap<Integer, Integer> sumTable;
	

	public static class DiagonalComparator implements Comparator<Integer>
	{
		private TreeMap<Integer, Integer> ref;
		
		public void setDiagonalComparator(TreeMap<Integer, Integer> map)
		{
			ref = map;
		}

		@Override
		public int compare(Integer a, Integer b) 
		{
			return b - a;
		}
		
	}
	
	
	public Fasta(int k, String s1, String s2)
	{
		reference = s1;
		query = s2;
		ktup = k;
		lookupTableRef = new HashMap<String, ArrayList<Integer>>();
		lookupTableQuery = new HashMap<String, ArrayList<Integer>>();
		commonWords = new HashMap<String, Integer>();
		DiagonalComparator dc = new DiagonalComparator();
		sumTable = new TreeMap<Integer, Integer>(dc);
		dc.setDiagonalComparator(sumTable);
	}
	
	public void execute()
	{
		findKTuples();
		makeSubTable();
		sortDiagonals();
		applySubstitutionMatrix();
		joinDiagonals();
		performSmithWaterman();
	}
	
	
	
	private void makeSubTable() 
	{
		// n + m - (2 * (k - 1) + 1)
		// parallelizzare?
		// unire punto 4 ? ricald
		for (String str : commonWords.keySet())
		{
			ArrayList<Integer> listRef = lookupTableRef.get(str);
			ArrayList<Integer> listQuery = lookupTableQuery.get(str);
			for (int j : listRef)
			{
				for (int i : listQuery)
				{
					int diagonalId = i - j;
					if (!sumTable.containsKey(diagonalId))
						sumTable.put(diagonalId, 0);
					sumTable.put(diagonalId, sumTable.get(diagonalId) + 1);
					System.out.println("[" + i + "-" + j + "] " + diagonalId + " :> " + sumTable.get(diagonalId));
				}
			}
			
		}
		for (Entry<Integer, Integer> e : sumTable.entrySet())
			System.out.println("-->> " + e.getKey() + " ..:::> " + e.getValue());

	}

	private void performSmithWaterman() {
		// TODO Auto-generated method stub
		
	}

	private void joinDiagonals() {
		// TODO Auto-generated method stub
		
	}

	private void applySubstitutionMatrix() {
		// TODO Auto-generated method stub
		
	}

	private void sortDiagonals() 
	{
		
		
	}
	
	private void findKTuples(String str, HashMap<String, ArrayList<Integer>> actualTable)
	{
		int n = str.length();
		for (int i = 0; i < (n - ktup + 1); i++)
		{
			String sub = str.substring(i, i + ktup);
			System.out.println(sub);
			if(!actualTable.containsKey(sub))
			{
				actualTable.put(sub, new ArrayList<Integer>());
				actualTable.get(sub).add(i);
				System.out.println(sub + ">>" + i);
			}
			else
			{
				actualTable.get(sub).add(i);
				System.out.println(sub + ">>" + i);
			}
		}
	}

	private void findKTuples() 
	{
		findKTuples(reference, lookupTableRef);
		findKTuples(query, lookupTableQuery);
		for (String str : lookupTableRef.keySet())
		{
			if (lookupTableQuery.containsKey(str))
			{
				int times = lookupTableRef.get(str).size() * lookupTableQuery.get(str).size();
				commonWords.put(str, times);
				System.out.println(str + " : " + times);
			}
		}
	}

	public static void main(String[] args) 
	{
		Fasta fas = new Fasta(2, "CCATCGCCATCG", "GCATCGGC");
		fas.execute();
	}

}
