import java.io.*;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

public class Q2
{

    public static void main(String[] args)
    {
        // pass the input file path as the argument while running this code.
        FileWriter output = null;
        String line = "";
        String previousToken = null;
        int unigramsTotalCount = 0;
        int distinctUnigramTotalCount = 0;
        int bigramsTotalCount = 0;
        int distinctBigramTotalCount = 0;
        HashMap<String, Integer> unigramsMap = new HashMap<String, Integer>();
        HashMap<String, Integer> bigramsMap = new HashMap<String, Integer>();
        HashMap<String,Double> unigramMarginalProbabilitiesMap = new HashMap<String, Double>();
        HashMap<String,Double> bigramMarginalProbabilitiesMap = new HashMap<String, Double>();
        HashMap<String,Double> noSmoothingProbabilitiesMap = new HashMap<String, Double>();
        HashMap<String,Double> addOneSmoothingCountsMap = new HashMap<String, Double>();
        HashMap<String,Double> addOneSmoothingProbabilitiesMap = new HashMap<String, Double>();
        // create an output file
        try
        {
            output = new FileWriter("Q2_output",false);

            // read input file:
            File file = new File(args[0]);
            //user delimitter (space-.-space-space 0r .-space-'') :
            Scanner s = new Scanner(file).useDelimiter(" .  |. ''");
            int n =2;
            while(n!=0)
            //while(s.hasNext())
            {
                n--;
                // add . to the end of the sentence:
                String s1 = s.next() + " .";
                //System.getProperty("line.terminator") to get the line break in any OS:
                s1 = s1.replace(System.getProperty("line.separator")," ");
                //System.out.println(s1);
                // split by white spaces & tab :
                for (String token : s1.split("  | |\\t|\\r"))
                {
                    //System.out.println(previousToken+" | "+token);

                    unigramsTotalCount++;
                    //System.out.println(token +"  " +unigramsTotalCount);
                    if(unigramsMap.containsKey(token))
                    {
                        int count = unigramsMap.get(token);
                        unigramsMap.put(token,count+ 1 );
                    }
                    else
                    {
                        unigramsMap.put(token,1);
                        distinctUnigramTotalCount++;
                    }

                    if(previousToken != null)
                    {
                        bigramsTotalCount++;
                        String pair = token + " | " + previousToken;
                        if(bigramsMap.containsKey(pair))
                        {
                            int pairCount = bigramsMap.get(pair);
                            bigramsMap.put(pair, pairCount +1);
                        }
                        else
                        {
                            bigramsMap.put(pair, 1);
                            distinctBigramTotalCount++;
                        }
                    }
                    previousToken = token;
                }
                previousToken = null;
            }

            //calculate marginal probabilities:

            //for unigrams:
            unigramMarginalProbabilitiesMap = calculateMarginalProbabilities(unigramsMap,unigramsTotalCount);

            //for bigrams:
            bigramMarginalProbabilitiesMap = calculateMarginalProbabilities(bigramsMap,bigramsTotalCount);

            //calculating probabilties for No Smoothing:
            noSmoothingProbabilitiesMap=calculateNoSmoothingProbabilities(bigramsMap,unigramsMap);

            //calculate Add-One Smoothing count:
            addOneSmoothingCountsMap = calculateAddOneSmoothingReconstitutedCounts(bigramsMap,unigramsMap,distinctUnigramTotalCount);

            //calculate Add-one Smoothing Probabilities
            addOneSmoothingProbabilitiesMap = calculateAddOneSmoothingProbabilities(bigramsMap,unigramsMap,distinctUnigramTotalCount);



            //close the scanner object:
            s.close();
            //System.out.println(unigramsTotalCount);
            //System.out.println(distinctUnigramTotalCount);
            //System.out.println(bigramsTotalCount);
            //System.out.println(distinctBigramTotalCount);
            //printHashMap(bigramsMap);
            //printHashMap1(unigramMarginalProbabilitiesMap);
            //System.out.println("===========================================================================");
            //printHashMap1(bigramMarginalProbabilitiesMap);
           // System.out.println("===========================================================================");
            System.out.println("No smoothing probabilities:");
            printHashMap1(noSmoothingProbabilitiesMap);
            System.out.println("===========================================================================");
            System.out.println("Add one smoothing count: ");
            printHashMap1(addOneSmoothingCountsMap);
            System.out.println("===========================================================================");
            System.out.println("Add one smoothing probabilities:");
            printHashMap1(addOneSmoothingProbabilitiesMap);
            System.out.println("===========================================================================");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * function to calculate counts for Add one Smoothing .
     * @param bigramsMap : hashmap of bigrams count
     * @param unigram : hashmap of bigrams count
     * @param distinctUnigramTotalCount
     * */
    private static HashMap<String, Double> calculateAddOneSmoothingReconstitutedCounts(HashMap<String,Integer>bigramsMap , HashMap<String,Integer>unigramsMap, int distinctUnigramTotalCount)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String,Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split(" \\| ");
            String currentWord = tokens[0];
            String givenWord = tokens[1];
            int denominator = (unigramsMap.get(givenWord))+distinctUnigramTotalCount;
            double reconstitutedCount = ((double) entry.getValue() +1) * unigramsMap.get(givenWord)/denominator;
            result.put((currentWord +" | " + givenWord),reconstitutedCount);
        }
        return result;
    }

    /*
     * function to calculate counts for Add one Smoothing .
     * @param bigramsMap : hashmap of bigrams count
     * @param unigram : hashmap of bigrams count
     * @param distinctUnigramTotalCount
     * */
    private static HashMap<String, Double> calculateAddOneSmoothingProbabilities(HashMap<String,Integer>bigramsMap , HashMap<String,Integer>unigramsMap, int distinctUnigramTotalCount)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String,Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split(" \\| ");
            String currentWord = tokens[0];
            String givenWord = tokens[1];
            int denominator = (unigramsMap.get(givenWord))+distinctUnigramTotalCount;
            double probability = ((double) entry.getValue() +1) /denominator;
            result.put((currentWord +" | " + givenWord),probability);
        }
        return result;
    }


    /*
    * function to calculate Normal conditional probabilities or No Smoothing probabilities.
    * @param bigramsMap : hashmap of bigrams count
    * @param unigram : hashmap of bigrams count
    * calculates probability by dividing the bigram count with the count of the given word.
    * */
    private static HashMap<String, Double> calculateNoSmoothingProbabilities(HashMap<String,Integer>bigramsMap , HashMap<String,Integer>unigramsMap)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String,Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split(" \\| ");
            String currentWord = tokens[0];
            String givenWord = tokens[1];
            double probability = (double) entry.getValue()/unigramsMap.get(givenWord);
            result.put((currentWord+ " | "+ givenWord),probability);
        }
        return result;
    }

    private static HashMap<String, Double> calculateMarginalProbabilities(HashMap<String, Integer> map, int totalCount)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String,Integer> entry : map.entrySet())
        {
            result.put(entry.getKey(), (double) entry.getValue()/totalCount);
        }
        return  result;
    }


    // function to print HashMaps:
    private static void printHashMap(HashMap<String, Integer> map)
    {
        Iterator<String> itr = map.keySet().iterator();
        while (itr.hasNext())
        {
            String key = itr.next();
            int value = map.get(key);
            System.out.println(key + " : "+ value);
        }
    }

    private static void printHashMap1(HashMap<String, Double> map)
    {
        Iterator<String> itr = map.keySet().iterator();
        while (itr.hasNext())
        {
            String key = itr.next();
            double value = map.get(key);
            System.out.println(key + " : "+ value);
        }
    }

}
