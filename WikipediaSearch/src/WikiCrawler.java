import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	private final String source;
	
	// the index where the results go
	private JedisIndex index;
	
	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();
	
	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 * 
	 * @return
	 */
	public int queueSize() {
		return queue.size();	
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param b 
	 * 
	 * @return Indexed URL
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		// FILL THIS IN!
		if (queue.isEmpty()) {
            return null;
        }

        String currentUrl = queue.remove();

        Elements paragraphs;
        if (testing == true) {
        	paragraphs = wf.readWikipedia(currentUrl);
        }
        else {
        	if (index.isIndexed(currentUrl)) {
        		return null;
        	}
        	paragraphs = wf.fetchWikipedia(currentUrl);
        }
		index.indexPage(currentUrl, paragraphs);
        queueInternalLinks(paragraphs);

		return currentUrl;
	}
	
	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
        // FILL THIS IN!
		for (Element paragraph: paragraphs) {
			Iterable<Node> iter = new WikiNodeIterable(paragraph);
			for (Node node: iter) {
				if (isLink(node)){
					Element ele = (Element) node;
					String link = "https://en.wikipedia.org" + ele.attr("href");
					queue.add(link);
				}
			}
		}

	}

	/**
	 * Checks if node is a wikipedia link
	 * 
	 * @param node
	 *
	 * @return b
	 *
	 */
	private boolean isLink(Node node){
		if (node instanceof Element && node.hasAttr("href")) {
			Element ele = (Element) node;
			if (ele.attr("href").startsWith("/wiki/")) { //check if wikipedia 
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchWikipedia(source);
		wc.queueInternalLinks(paragraphs);

		// loop until we index a new page
		int count = 50;
		String res;
		do {
			res = wc.crawl(false);

			if(count-- <= 0)break;
		} while (true);
		
		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
