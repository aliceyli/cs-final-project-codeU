import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by aliceli on 8/9/16.
 */
public class WikiHome {
    public static void main(String[] args) throws IOException {
        Jedis jedis = JedisMaker.make();
        JedisIndex index = new JedisIndex(jedis);

        String validationRegex = "\\w+";
        Scanner input = new Scanner(System.in);
        while(true) {
            System.out.print("Input search request: ");
            String line = input.nextLine().toLowerCase();
            if(line.equals("EXIT"))
                break;
            if(!line.matches(validationRegex)) { // input validation
                System.out.println("Invalid input");
                continue;
            }
            WikiSearch search = WikiSearch.search(line,index);
            List<Map.Entry<String,Integer>> list = search.sort();
            for (Map.Entry<String,Integer> entry: list) {
                System.out.println(entry.getKey());
            }
        }
    }
}
