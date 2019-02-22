import java.io.FileWriter;
import java.util.*;
import java.io.*;
public class Q3
{
    static FileWriter output = null;
    static List<String> wordsList;
    static List<String> correctTagList;
    static List<String> currentTag;
    static HashMap<String, HashMap<String, Integer>> wordsWithTagsMap;
    static HashMap<String, String> wordWithMostFrequentTagMap;

    public static void main(String[] args)
    {
        wordWithMostFrequentTagMap = new HashMap<>();
        wordsWithTagsMap = new HashMap<>();
        wordsList = new ArrayList<>();
        correctTagList = new ArrayList<>();
        String line ="";


        // pass the input file path as the argument while running this code.
        if(args.length ==0)
        {
            System.out.println("ERROR: Missing input file path in arguments!");
            return;
        }
        try
        {
            File file = new File(args[0]);
            BufferedReader b = new BufferedReader(new FileReader(file));
            int n = 3;
            //while ((line = b.readLine()) != null)
            while (n!=0)
            {
                line = b.readLine();
                n--;

                for(String pair: line.split(" "))
                {
                    String word = pair.split("_")[0];
                    wordsList.add(word);
                    String tag = pair.split("_")[1];
                    correctTagList.add(tag);
                    //TODO: write a function to create hashmap of all words with all their tags & corresponding counts.
                    storeAllTagsAndCounts(word,tag);

                }

            }
            printHashMap(wordsWithTagsMap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static void storeAllTagsAndCounts(String word, String tag)
    {
        //tagsAndCountsMap : Hashmap
        // Key is word
        //Value is HashMap of all tags of that word (Key: Tag & Value: count of the tag)

        //create a new entry if the word doesn't exist & just initialize the HashMap
        if(!wordsWithTagsMap.containsKey(word))
        {
            HashMap<String, Integer> tagsAndCountsMap = new HashMap<>();
            wordsWithTagsMap.put(word, tagsAndCountsMap);
        }
        //Assign the proper count in the HashMap:
            HashMap<String, Integer> tagsAndCountsMap = wordsWithTagsMap.get(word);
        int count = (tagsAndCountsMap.containsKey(tag))? tagsAndCountsMap.get(tag) : 0;
        tagsAndCountsMap.put(tag, count + 1 );

    }

    public static void printHashMap(HashMap<String, HashMap<String, Integer>> map)
    {

        for(Map.Entry<String, HashMap<String, Integer>> entry : map.entrySet())
        {
            String key = entry.getKey();
            HashMap<String, Integer> hm = map.get(key);
            Iterator<String> itr1 = hm.keySet().iterator();
            while (itr1.hasNext())
            {
                String tag = itr1.next();
                Integer count = hm.get(tag);
                System.out.println(key + " : "+ tag+"/"+count);
            }
            //int value = map.get(key);
            //System.out.println(key );
        }
    }
}
