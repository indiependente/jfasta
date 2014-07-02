package jFasta;

import java.util.ArrayList;
import java.util.HashMap;

public class Fasta 
{
	private int ktup;
	private String reference, query;
	
	private HashMap<String, ArrayList<Integer>> lookupTableRef, lookupTableQuery;
	
	public Fasta(int k, String s1, String s2)
	{
		reference = s1;
		query = s2;
		ktup = k;
		lookupTableRef = new HashMap<String, ArrayList<Integer>>();
		lookupTableQuery = new HashMap<String, ArrayList<Integer>>();
	}
	
	public void execute()
	{
		findKTuples();
		sortKTuples();
		applySubstitutionMatrix();
		joinDiagonals();
		performSmithWaterman();
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

	private void sortKTuples() {
		// TODO Auto-generated method stub
		
	}

	private void findKTuples() 
	{
		HashMap<String, ArrayList<Integer>> actualTable;
		
		for (String str : new String[] { reference, query })
		{
			actualTable = (str==reference) ? lookupTableRef : lookupTableQuery;
			int n = str.length();
			for (int i = 0; i < (n - ktup + 1); i++)
			{
				String sub = str.substring(i, i + ktup);
				if(!actualTable.containsKey(sub)){
					actualTable.put(sub, new ArrayList<Integer>(i));
				}
				else{
					actualTable.get(sub).add(i);
				}
			}
		}
	}

	public static void main(String[] args) 
	{
		Fasta fas = new Fasta(10, "antonio", "casino");
		fas.execute();
	}

}
