import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}

	/**
	 * returns  map in WikiSearch
	 *
	 *
	 */
	public Map<String,Integer> getMap() {
		return map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 *
	 */
	public void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * Iterates through WikiSearch that and either updates existing key with new relevance score or adds a new entry
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
        Map<String,Integer> results = map;
        for (Map.Entry<String,Integer> entry: that.getMap().entrySet()) {
			String url = entry.getKey();
			// url exists in both this WikiSearch map and that WikiSearch map
			if (map.containsKey(url)) {
        		//update relevance score
        		int newRelevance = totalRelevance(getRelevance(url), that.getRelevance(url));
        		results.put(url,newRelevance);
        	}
        	// url doesn't exist in this WikiSearch map yet
        	else {
        		results.put(url, entry.getValue());
        	}
		}
		WikiSearch resultsWikiSearch = new WikiSearch(results);
        
		return resultsWikiSearch;
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
        Map<String,Integer> results = new HashMap<String,Integer>();
        for (Map.Entry<String,Integer> entry: that.getMap().entrySet()) {
			String url = entry.getKey();
			//url exists in both this WikiSearch map and that WikiSearch map
			if (map.containsKey(url)) {
        		//update relevance score
        		int newRelevance = totalRelevance(getRelevance(url), that.getRelevance(url));
        		results.put(url,newRelevance);
        	}
		}
		WikiSearch resultsWikiSearch = new WikiSearch(results);
		return resultsWikiSearch;
	}
	
	/**
	 * Computes the difference of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
        Map<String,Integer> results = map;
        for (Map.Entry<String, Integer> entry: that.getMap().entrySet()) {
			String url = entry.getKey();
			//url exists in both this WikiSearch map and that WikiSearch map
			if (map.containsKey(url)) {
        		//delete this url from WikiSearch map
        		results.remove(url); 
        	}
		}
		WikiSearch resultsWikiSearch = new WikiSearch(results);
		return resultsWikiSearch;
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
        List<Entry<String, Integer>> entryList = new LinkedList<Entry<String, Integer>>(map.entrySet());
        Comparator<Entry<String,Integer>> entryComparator = new Comparator<Entry<String,Integer>>() {

		    public int compare(Entry<String,Integer> entry1, Entry<String,Integer> entry2) {
		    	//descending sort
		       	if (entry1.getValue() > entry2.getValue()) {
		            return -1;
		        }
		        if (entry1.getValue() < entry2.getValue()) {
		            return 1;
		        }
		        
		        return 0;
	   		}
		};
        Collections.sort(entryList, entryComparator);
		return entryList;
	}


	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
