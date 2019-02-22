import java.io.*;
import java.util.*;


public class Q2
{

    static FileWriter output = null;

    public static void main(String[] args)
    {
        // pass the input file path as the argument while running this code.
        if(args.length ==0)
        {
            System.out.println("ERROR: Missing input file path in arguments!");
            return;
        }
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
        HashMap<Integer,Integer> nBucket = new HashMap<Integer, Integer>();
        HashMap<String,Double> goodTuringCountsMap = new HashMap<String, Double>();
        HashMap<String, Double> goodTuringProbabilitiesMap = new HashMap<String, Double>();

        // create an output file
        try
        {
            output = new FileWriter("Q2_output.txt",false);

            // read input file:
            File file = new File(args[0]);
            //user delimiter (space-.-space-space 0r .-space-''-space--space) :
            Scanner s = new Scanner(file).useDelimiter(" .  |. ''  ");
            //int n =2;
            //while(n!=0)
            while(s.hasNext())
            {
                //n--;
                // add . to the end of the sentence:
                String s1 = s.next();
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
                        String pair = previousToken + "~~~" + token;
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

            //create N Buckets:
            nBucket = createNBucket(bigramsMap);

            //calculate Good Turing Counts:
            goodTuringCountsMap = calculateGoodTuringCounts(bigramsMap,nBucket,bigramsTotalCount);

            //calculate Good Turing Probabilities:
            goodTuringProbabilitiesMap = calculateGoodTuringProbabilities(bigramsMap,nBucket,bigramsTotalCount);

            writeUnigramCounts(unigramsMap,distinctUnigramTotalCount);
            writeBigramCounts(bigramsMap,distinctBigramTotalCount);
            writeNoSmoothingProbabilities(noSmoothingProbabilitiesMap);
            writeAddOneSmoothingReconstitutedCounts(addOneSmoothingCountsMap);
            writeAddOneSmoothingProbabilities(addOneSmoothingProbabilitiesMap);
            writeGoodTuringCounts(goodTuringCountsMap);
            writeGoodTuringProbabilities(goodTuringProbabilitiesMap);

            writeToFile("********************* END OF FILE *********************");
            //close the scanner object:
            s.close();
            System.out.println("Input File Scanner closed");
            //close the File Writer Object
            output.close();
            System.out.println("Output File Writer closed");

        // PRINTING & TESTING PART:
            //System.out.println(unigramsTotalCount);
            //System.out.println(distinctUnigramTotalCount);
            //System.out.println(bigramsTotalCount);
            //System.out.println(distinctBigramTotalCount);
            //printHashMap(unigramsMap);
            //printHashMap1(unigramMarginalProbabilitiesMap);
            /*
            System.out.println("===========================================================================");
            //printHashMap1(bigramMarginalProbabilitiesMap);
            //System.out.println("===========================================================================");
            printHashMap1(noSmoothingProbabilitiesMap);
            //System.out.println("===========================================================================");
            printHashMap1(addOneSmoothingCountsMap);
            //System.out.println("===========================================================================");
            printHashMap1(addOneSmoothingProbabilitiesMap);
            System.out.println("===========================================================================");
            printHashMap1(goodTuringCountsMap);
            System.out.println("===========================================================================");
            printHashMap1(goodTuringProbabilitiesMap);
            System.out.println("===========================================================================");
            */
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /*  function to calculate good turing probability:
     *  it gets the count of the bigram - c and gets the corresponding Nc value from the bucket.
     *  then gets the Nc+1 from the bucket and calculates the c* - cStarCount.
     *  *  Returns a Hashmap of bigrams and their c* counts.
     *  */
    private static HashMap<String, Double> calculateGoodTuringCounts(HashMap<String,Integer>bigramsMap,HashMap<Integer, Integer> nBucket, int bigramsTotalCount)
    {
        //TODO: find N0 count
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String, Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            int c = entry.getValue();
            double nc = getNcValue(nBucket,c);
            double ncPlus1 = getNcValue(nBucket,c+1);
            double cStarCount = ((c+1) *ncPlus1)/(nc);
            //note the order for storing counts is given word - current word
            result.put((givenWord +"~~~" + currentWord),cStarCount);
        }
        return result;
    }



    /*  function to calculate good turing probability:
     *  it gets the count of the bigram - c and gets the corresponding Nc value from the bucket.
     *  then gets the Nc+1 from the bucket and calculates the probability.
     *  Returns a Hashmap of bigrams and their good turing probabilities
     *  */
    private static HashMap<String, Double> calculateGoodTuringProbabilities(HashMap<String,Integer>bigramsMap,HashMap<Integer, Integer> nBucket, int bigramsTotalCount)
    {
        double c0Probability = getNcValue(nBucket,1)/bigramsTotalCount;
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(Map.Entry<String, Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            int c = entry.getValue();
            double nc = getNcValue(nBucket,c);
            double ncPlus1 = getNcValue(nBucket,c+1);
            double probability = ((c+1) *ncPlus1)/(nc * bigramsTotalCount);
            //note the order for storing probabilities is current word - given word
            result.put((currentWord +" | " + givenWord),probability);
        }
        return result;
    }

    // function to fetch values from the buckets:
    private static double getNcValue(HashMap<Integer, Integer> nBucket, int i)
    {
        if(nBucket.containsKey(i))
        {
            return nBucket.get(i);
        }
        else
        {
            return 0;
        }
    }

    // function to create buckets for Good Turning Discount Smoothing:
    private static HashMap<Integer, Integer> createNBucket(HashMap<String, Integer> bigramsMap)
    {
        HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
        for(Map.Entry<String,Integer> entry : bigramsMap.entrySet())
        {
            int n = entry.getValue();
            if(result.containsKey(n))
            {
                result.put(n, result.get(n) + 1);
            }
            else
            {
                result.put(n,1);
            }
        }
        return result;
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
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            int denominator = (unigramsMap.get(givenWord))+distinctUnigramTotalCount;
            double reconstitutedCount = ((double) entry.getValue() +1) * unigramsMap.get(givenWord)/denominator;
            //note the order for storing counts is given word - current word
            result.put((givenWord +"~~~" + currentWord),reconstitutedCount);
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
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            int denominator = (unigramsMap.get(givenWord))+distinctUnigramTotalCount;
            double probability = ((double) entry.getValue() +1) /denominator;
            //note the order for storing probabilities is current word - given word
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
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            double probability = (double) entry.getValue()/unigramsMap.get(givenWord);
            //note the order for storing probabilities is current word - given word
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


//********** Writing and printing functions *****************

    // write a line to file
    public static void writeToFile(String string)
    {
        try
        {
            output.write(string + "\n");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //function to write word counts(unigram counts) to the file:
    private static void writeUnigramCounts(HashMap<String, Integer> unigramsMap, int distinctUnigramTotalCount)
    {
        System.out.print("Writing Unigram counts to the file ");
        writeToFile("*********************Unigram Counts*********************");
        writeToFile("Count of distinct unigrams/words = "+ distinctUnigramTotalCount);
        for(Map.Entry<String, Integer> entry : unigramsMap.entrySet())
        {
            writeToFile("Unigram Count -> (" + entry.getKey() + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //function to write bigram counts to the file:
    private static void writeBigramCounts(HashMap<String, Integer> bigramsMap, int distinctBigramTotalCount)
    {
        System.out.print("Writing Bigram counts to the file ");
        writeToFile("*********************Bigram Counts*********************");
        writeToFile("Count of distinct bigrams = "+ distinctBigramTotalCount);
        for(Map.Entry<String, Integer> entry : bigramsMap.entrySet())
        {
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            writeToFile("Bigram Count -> (" + currentWord+ " | "+ givenWord + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //function to write No smoothing or normal conditional probabilities to the file:
    private static void writeNoSmoothingProbabilities(HashMap<String, Double> noSmoothingProbabilitiesMap)
    {
        System.out.print("Writing No Smoothing (Normal Conditional) Probabilities to the file ");
        writeToFile("*********************No Smoothing (Normal Conditional) Probabilities*********************");
        for(Map.Entry<String, Double> entry : noSmoothingProbabilitiesMap.entrySet())
        {
            writeToFile("No Smoothing Prob -> (" + entry.getKey() + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //function to write Add One Smoothing Reconstituted Counts to the file:
    private static void writeAddOneSmoothingReconstitutedCounts(HashMap<String, Double>addOneSmoothingCountsMap)
    {
        System.out.print("Writing Add-One Smoothing Reconstituted counts to the file ");
        writeToFile("*********************Add-One Smoothing Reconstituted Counts*********************");
        for(Map.Entry<String, Double> entry : addOneSmoothingCountsMap.entrySet())
        {
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            writeToFile("Add-One Smoothing Reconstituted Count -> (" + currentWord+ " | "+ givenWord + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //function to write Add-One Smoothing Probabilities to the file:
    private static void writeAddOneSmoothingProbabilities(HashMap<String, Double> addOneSmoothingProbabilitiesMap)
    {
        System.out.print("Writing Add-One Smoothing Probabilities to the file ");
        writeToFile("*********************Add-One Smoothing Probabilities*********************");
        for(Map.Entry<String, Double> entry : addOneSmoothingProbabilitiesMap.entrySet())
        {
            writeToFile("Add-One Smoothing Prob -> (" + entry.getKey() + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    ///////////////////////////
    //function to write Good Turing Smoothing Counts to the file:
    private static void writeGoodTuringCounts(HashMap<String, Double>goodTuringCountsMap)
    {
        System.out.print("Writing Good Turing Smoothing Reconstituted counts to the file ");
        writeToFile("*********************Good Turing Smoothing Reconstituted Counts*********************");
        for(Map.Entry<String, Double> entry : goodTuringCountsMap.entrySet())
        {
            String[] tokens = entry.getKey().split("~~~");
            //note the order - the first word will be "given" or "previous" & the 2nd word will be "current"
            String givenWord = tokens[0];
            String currentWord = tokens[1];
            writeToFile("Good Turing Count -> (" + currentWord+ " | "+ givenWord + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //function to write Good Turing Smoothing Probabilities to the file:
    private static void writeGoodTuringProbabilities(HashMap<String, Double> goodTuringProbabilitiesMap)
    {
        System.out.print("Writing Good Turing Smoothing Probabilities to the file ");
        writeToFile("*********************Good Turing Smoothing Probabilities*********************");
        for(Map.Entry<String, Double> entry : goodTuringProbabilitiesMap.entrySet())
        {
            writeToFile("Good Turing Smoothing Prob -> (" + entry.getKey() + ") = "+ entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
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
