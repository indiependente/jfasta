package jFasta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Fasta 
{

	private static Logger logger = LoggersManager.getLogger(Fasta.class.getName());
	
	private static Fasta instance = null;
	public static Fasta getInstance()
	{
		if (instance == null)
			instance = new Fasta();
		return instance;
	}

	public static final int BEST_DIAGONALS = 10;

	private int ktup;
	private String reference, query;
	
	private Diagonal bestDiagonal;

	private Map<Integer, Diagonal> diagonals;


	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        @Override
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return -1 * (o1.getValue()).compareTo(o2.getValue());
	        }
	    } );
	
	    Map<K, V> result = new LinkedHashMap<>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}


	private Fasta()
	{
	}

	public Fasta setup(int k, String s1, String s2)
	{
		LoggersManager.disableAll();
		reference = s1;
		query = s2;
		ktup = k;
		bestDiagonal = null;
		diagonals = new HashMap<Integer, Diagonal>();
		return this;
	}

	public void execute()
	{
		findKTuples();
		
		makeSumTable();
		
		applySubstitutionMatrix();
		
		System.out.println(selectBestDiagonal());
		
		joinDiagonals();
		
		performSmithWaterman();
	}

	private Diagonal selectBestDiagonal() 
	{
		if (bestDiagonal == null)
		{
			Diagonal best = null;
			for	(Diagonal d : diagonals.values())
			{
				if (best == null)
				{
					best = d;
				}
				else
				{
					int bestScore = best.getTotalDiagonalRunScore();
					int currentScore = d.getTotalDiagonalRunScore();
					if (currentScore > bestScore)
						best = d;
				}
			}
			bestDiagonal = best;
		}
		return bestDiagonal;
	}


	private void makeSumTable() 
	{
		diagonals = sortByValue(diagonals);
		
		if (diagonals.size() > BEST_DIAGONALS)
		{
			int i = 0;
			for (Integer key : diagonals.keySet())
			{
				if (i >= BEST_DIAGONALS)
					diagonals.remove(key);
				i++;
			}
		}
		System.gc();
	}


	private void findKTuples() 
	{
		HashMap<String, ArrayList<Integer>> matchesInRef = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> matchesInQuery = new HashMap<String, ArrayList<Integer>>();

		findKTuples(reference, matchesInRef);
		findKTuples(query, matchesInQuery);
		
		TreeSet<HotSpot> hotspots = new TreeSet<HotSpot>();
		for (String str : matchesInRef.keySet())
		{
			if (matchesInQuery.containsKey(str))
			{
				ArrayList<Integer> listRef = matchesInRef.get(str);
				ArrayList<Integer> listQuery = matchesInQuery.get(str);
				
				for (int j : listRef)
				{
					for (int i : listQuery)
					{
						HotSpot hs = new HotSpot(i, j);
						hotspots.add(hs);
					}
				}
			}
		}
		for (HotSpot h : hotspots)
		{
			Diagonal dlg = getDiagonal(h.getI() - h.getJ());
			logger.info("\nWorking on diagonal " + dlg.getDiagonalId() + "...adding hotspot " + h);
			dlg.addHotSpot(h);
		}
	}

	public Diagonal getDiagonal(int id)
	{
		if (!diagonals.containsKey(id))
			diagonals.put(id, new Diagonal(id));
		return diagonals.get(id); 
	}

	private void findKTuples(String str, HashMap<String, ArrayList<Integer>> matches)
	{
		int n = str.length();
		for (int i = 0; i < (n - ktup + 1); i++)
		{
			String sub = str.substring(i, i + ktup);
			if(!matches.containsKey(sub))
			{
				matches.put(sub, new ArrayList<Integer>());
				matches.get(sub).add(i);
			}
			else
			{
				matches.get(sub).add(i);
			}
		}
	}


	public int getKTup()
	{
		return ktup;
	}



	private void performSmithWaterman() {
		// TODO Auto-generated method stub

	}

	private void joinDiagonals() {
		// TODO Auto-generated method stub

	}

	private void applySubstitutionMatrix() {
		try 
		{
			ScoringScheme scoringMatrix = new ScoringMatrix(new BufferedReader(new FileReader("./data/pam250.txt")));
			for (int key : diagonals.keySet())
			{
				logger.info("analysing dlg: " + key);
				
				for (DiagonalRun run : diagonals.get(key).getDiagonalRuns())
				{
					String strRef = run.extractStringFromReference(reference);
					String strQuery = run.extractStringFromQuery(query);
					int score = 0;
					int i = 0;
					int l = strRef.length();
					while (i < l)
					{
						score += scoringMatrix.scoreSubstitution(strRef.charAt(i), strQuery.charAt(i));
						i++;
					}
					logger.info("score for " + strRef + " = " + score);
					run.increaseScore(score);
//					if (init1 < score)
//						init1 = score;
				}
			}
		}
		catch (IOException | IncompatibleScoringSchemeException | InvalidScoringMatrixException  e) 
		{
			e.printStackTrace();
		} 
	}


	//
	//	private void findKTuples() 
	//	{
	//		findKTuples(reference, lookupTableRef);
	//		findKTuples(query, lookupTableQuery);
	//		for (String str : lookupTableRef.keySet())
	//		{
	//			if (lookupTableQuery.containsKey(str))
	//			{
	//				int times = lookupTableRef.get(str).size() * lookupTableQuery.get(str).size();
	//				commonWords.put(str, times);
	//				System.out.println(str + " : " + times);
	//			}
	//		}
	//	}
	//	private void makeSumTable() 
	//	{
	//		// n + m - (2 * (k - 1) + 1)
	//		// parallelizzare?
	//		// unire punto 4 ? ricald
	//		Map<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
	//		for (String str : commonWords.keySet())
	//		{
	//			ArrayList<Integer> listRef = lookupTableRef.get(str);
	//			ArrayList<Integer> listQuery = lookupTableQuery.get(str);
	//			for (int j : listRef)
	//			{
	//				for (int i : listQuery)
	//				{
	//					int diagonalId = i - j;
	//					if (!temp.containsKey(diagonalId))
	//						temp.put(diagonalId, 0);
	//					temp.put(diagonalId, temp.get(diagonalId) + 1);
	//					System.out.println("[" + i + "-" + j + "] " + diagonalId + " :> " + temp.get(diagonalId));
	//				}
	//			}
	//			
	//		}
	//		sumTable = (LinkedHashMap<Integer, Integer>) sortByValue(temp); 
	//		temp = null;		
	//		if (sumTable.size() > BEST_DIAGONALS)
	//		{
	//			int i = 0;
	//			for (Integer key : sumTable.keySet())
	//			{
	//				if (i >= BEST_DIAGONALS)
	//					sumTable.remove(key);
	//				i++;
	//			}
	//			
	//		}
	//		System.gc();
	//		for (Entry<Integer, Integer> e : sumTable.entrySet())
	//			System.out.println("-->> " + e.getKey() + " ..:::> " + e.getValue());
	//	}

	public static void main(String[] args) 
	{
		Fasta fas = Fasta.getInstance().setup(2, "CCATCGCCATCG", "CCATCGCCATCG");
		fas.execute();
	}

}
