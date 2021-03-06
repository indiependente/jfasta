package jFasta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

public class Fasta 
{

	private static Logger logger = Logger.getLogger(Fasta.class.getName());
	
	private static Fasta instance = null;
	public static Fasta getInstance()
	{
		if (instance == null)
			instance = new Fasta();
		return instance;
	}

	public static final int BEST_DIAGONALS = 10;

	private int ktup;
	
	/* Considering a band of - w/2 , + w/2 */
	private int width;
	private String reference, query, targetReference, targetQuery;
	
	private Diagonal bestDiagonal;

	private Map<Integer, Diagonal> diagonals;

	private static final String PAM250_CONFIG_FILE = "./data/pam250.txt";

	private Fasta()
	{

	}

	public Fasta setup(int k, int w, String s1, String s2)
	{
		reference = s1;
		query = s2;
		ktup = k;
		width = (w > 16) ? 0 : w;
		if (w <= 0)
			throw new RuntimeException("Width must be between 1 and 16."); ;
		bestDiagonal = null;
		diagonals = new HashMap<Integer, Diagonal>();
		return this;
	}

	public void finalize()
	{
		diagonals.clear();
		bestDiagonal = null;
		diagonals = null;
	}
	
	public void execute()
	{
		Profiler p = new Profiler("Fasta::execute");
		
		findKTuples();
		
		makeSumTable();
		
		applySubstitutionMatrix();
		
		selectBestDiagonal();
		
		joinDiagonals();
		
		performSmithWaterman();
		
		p.end();
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
		diagonals = Utils.sortByValue(diagonals);
		
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
		Profiler p = new Profiler("Fasta::findKTuples");
		HashMap<String, ArrayList<Integer>> matchesInRef = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> matchesInQuery = new HashMap<String, ArrayList<Integer>>();

		findKTuples(reference, matchesInRef);
		findKTuples(query, matchesInQuery);
//		TreeSet<HotSpot> hotspots = new TreeSet<HotSpot>();
		
		Map<Integer, TreeSet<HotSpot>> hotspots = new HashMap<Integer, TreeSet<HotSpot>>();	
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
						int diagId = i - j;
						HotSpot hs = new HotSpot(i, j);
						if (!hotspots.containsKey(diagId))
							hotspots.put(diagId, new TreeSet<HotSpot>());
						hotspots.get(diagId).add(hs);
					}
				}
			}
		}
		hotspots = Utils.sortByValueSize(hotspots);
		int i = 0;
		for (Entry<Integer, TreeSet<HotSpot>> e : hotspots.entrySet())
		{
			if (i > BEST_DIAGONALS)
				break;
			Diagonal dlg = getDiagonal(e.getKey());
			for (HotSpot h : e.getValue())
				dlg.addHotSpot(h);
			i++;
		}
		hotspots.clear();
		matchesInQuery.clear();
		matchesInRef.clear();
		matchesInQuery = null;
		matchesInRef = null;
		hotspots = null;
		p.end();
		System.gc();
	}

	public Diagonal getDiagonal(int id)
	{
		if (!diagonals.containsKey(id))
			diagonals.put(id, new Diagonal(id));
		return diagonals.get(id); 
	}

	private void findKTuples(String str, HashMap<String, ArrayList<Integer>> matches)
	{
		Profiler p = new Profiler("Fasta::findKTuples::internal");

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
		
		p.end();
		
	}


	public int getKTup()
	{
		return ktup;
	}



	private void performSmithWaterman() {

		SmithWaterman sw = new SmithWaterman(targetQuery, targetReference);
		System.out.println("Total Alignment Score: " + sw.computeSmithWaterman());
		sw = null;
		System.gc();
		
	}

	private void joinDiagonals() {
		int[] bounds = bestDiagonal.getDiagonalBounds();
		int halfW = width / 2;
		int queryStart = bounds[0] - halfW + (Math.abs(bounds[0] - halfW));
		int refStart = bounds[1] - halfW + (Math.abs(bounds[1] - halfW));
		int queryEnd = bounds[2] + halfW - (((bounds[2] + halfW) > (query.length() - 1))? Math.abs((bounds[2] + halfW) - query.length()-1) : 0);
		int refEnd = bounds[3] + halfW - (((bounds[3] + halfW) > (reference.length() - 1))? Math.abs((bounds[3] + halfW) - reference.length()-1) : 0);
		targetQuery = query.substring(queryStart, queryEnd + 1);
		targetReference = reference.substring(refStart, refEnd + 1);
	}

	private void applySubstitutionMatrix() {
		Profiler p = new Profiler("Fasta::applySubstitutionMatrix");
		Reader fileReader = null;
		try 
		{
			fileReader = new FileReader(PAM250_CONFIG_FILE);
			ScoringScheme scoringMatrix = new ScoringMatrix(new BufferedReader(fileReader));
			for (int key : diagonals.keySet())
			{
				
				for (DiagonalRun run : diagonals.get(key).getDiagonalRuns())
				{
					String strRef = run.extractStringFromReference(reference);
					String strQuery = run.extractStringFromQuery(query);
					int score = 0;
					for (int i = 0, l = strRef.length(); i < l; i++)
					{
						score += scoringMatrix.scoreSubstitution(strRef.charAt(i), strQuery.charAt(i));
					}
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
		finally
		{
			if (fileReader != null) 
			{
				try
				{
					fileReader.close();
				}
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		p.end();
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
		final int INPUT_NUMBER = 2;
		try {
			
			if (args.length != INPUT_NUMBER)
				{
					logger.severe("Usage: java -jar filename1 filename2");
					System.exit(1);
				}
			
			String queryFile = args[0];
			String referenceFile = args[1];
			
			CharSequence seq1 = new CharSequence(new BufferedReader(new FileReader(queryFile)));
			CharSequence seq2 = new CharSequence(new BufferedReader(new FileReader(referenceFile)));
			
			Fasta fas = Fasta.getInstance().setup(5, 16, seq1.toString(), seq2.toString());
			fas.execute();

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidSequenceException e) {
			e.printStackTrace();
		}
		
	}

}
