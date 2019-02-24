import java.io.FileWriter;
import java.util.*;
import java.io.*;
public class Q3 {
    static FileWriter output = null;
    static List<String> wordsList;
    static List<String> correctTagList;
    static List<String> currentTagList;
    static HashMap<String, HashMap<String, Integer>> wordsWithTagsMap;
    static HashMap<String, String> wordWithMostFrequentTagMap;
    static String fromTag = "NN";
    static String[] toTagsArray = {"JJ", "VB"};
    static List<Transformation> transformationsList = new ArrayList<>();
    static List<String> sentenceWordsList;
    static List<String> sentenceTagList;
    static HashMap<String, Integer> tagAndCountMap;
    static HashMap<String, Integer> tagPairAndCountMap = new HashMap<>();
    static HashMap<String, Double> wordGivenTagProbabilityMap = new HashMap<>();
    static HashMap<String, Double> tagGivenPreviousTagProbabilityMap = new HashMap<>();

    public static void main(String[] args) {
        wordWithMostFrequentTagMap = new HashMap<>();
        wordsWithTagsMap = new HashMap<>();
        wordsList = new ArrayList<>();
        correctTagList = new ArrayList<>();
        sentenceWordsList = new ArrayList<>();
        sentenceTagList = new ArrayList<>();
        String line = "";
        String inputSentence = "The_DT standard_?? Turbo_NN engine_NN is_VBZ hard_JJ to_TO work_??";
        boolean flag = false;



        // pass the input file path as the argument while running this code.
        if (args.length == 0) {
            System.out.println("ERROR: Missing input file path in arguments!");
            return;
        }
        try {
            File file = new File(args[0]);
            BufferedReader b = new BufferedReader(new FileReader(file));
            //int n = 3;
            //while (n!=0)
            while ((line = b.readLine()) != null) {
                //line = b.readLine();
                //n--;

                for (String pair : line.split(" ")) {
                    String word = pair.split("_")[0];
                    wordsList.add(word);
                    String tag = pair.split("_")[1];
                    correctTagList.add(tag);
                    //TODO: write a function to create hashmap of all words with all their tags & corresponding counts.
                    storeAllTagsAndCounts(word, tag);

                }

            }
            //TODO: function to get the most frequent tag with every word
            getHighestFrequencyTags();

            //TODO: create a list of tags corresponding to the list of words
            currentTagList = new ArrayList<>();
            for (String word : wordsList) {
                currentTagList.add(wordWithMostFrequentTagMap.get(word));
            }

            //open filewriter object:
            output = new FileWriter("Q3_output.txt", false);

            //TODO: Get best transformation rules for the corupus"
            System.out.println("********************* Part A:  BRill's Tagging *********************");
            System.out.println("Best Rules: ");
            writeToFile("************* BRill's Tagging **************");
            writeToFile("\nBest Rules:");
            getTransformations();

            //TODO: process the input sentence:
            System.out.println("\nGiven input sentence:\n" + inputSentence);
            writeToFile("\nGiven input sentence:\n" + inputSentence);
            //TODO: Replacing ?? with most probable tags learnt:
            System.out.println("\nReplacing ?? with most probable tags learnt:");
            writeToFile("\nReplacing ?? with most probable tags learnt:");
            for (String pair : inputSentence.split(" ")) {
                String word = pair.split("_")[0];
                sentenceWordsList.add(word);
                String tag = pair.split("_")[1];
                String tag1 = "";
                //replace ?? tags with most probabable tags learnt from the corpus
                if (tag.equals("??")) {
                    tag = wordWithMostFrequentTagMap.get(word);
                }
                sentenceTagList.add(tag);

                System.out.print(word + "_" + tag + " ");
                writeToFile(word + "_" + tag + " ");
            }

            System.out.println("\n\nApplying learnt rules to the sentence: ");
            writeToFile("\n\nApplying learnt rules to the sentence: ");

            for (int i = 0; i < sentenceTagList.size(); i++) {
                for (Transformation transformation : transformationsList) {
                    //to ensure negative score rules are not being applied:
                    if (transformation.score > 0) {
                        if (sentenceTagList.get(i) == transformation.fromTag) {
                            if (sentenceTagList.get(i - 1) == transformation.previousTag) {
                                sentenceTagList.set(i, transformation.toTag);
                                flag = true;
                            }
                        }
                    }
                }
            }
            if (!flag) {
                System.out.println("*** No rules applied to the sentence as it doesn't satisfy the rules' criterion.");
                writeToFile("*** No rules applied to the sentence as it doesn't satisfy the rules' criterion.");
            }
            System.out.println("\nFinal Answer:");
            writeToFile("\nFinal Answer:");
            for (int i = 0; i < sentenceWordsList.size(); i++) {
                System.out.print(sentenceWordsList.get(i) + "_" + sentenceTagList.get(i) + " ");
                writeToFile(sentenceWordsList.get(i) + "_" + sentenceTagList.get(i) + " ");
            }
            writeToFile("======================================================================================================");
            writeToFile("\n *********************Part B: Naive Bayes Calculation*********************");
            System.out.println("\n\n********************* Part B:  Naive Bayes Calculation *********************\n");
            //TODO: Naive-Bayes calculations:
            formTagsUnigramsBigrams();
            //TODO: calculate Word|Tag probability:
            calculateWordGivenTagProbability();
            writeWordGivenTagProbabilities();
            //TODO: calculate Tag|Previous Tag probability:
            calculateTagGivenPreviousTagProbability();
            writeTagGivenPreviousTagProbabilities();

            writeToFile("********************* END OF FILE *********************");
            //TODO: close the buffer reader object:
            b.close();
            System.out.println("Input File buffer reader closed");
            //TODO: close the File Writer Object
            output.close();
            System.out.println("Output File Writer closed");


            //*********** PRINTING AND TESTING PART *****************
            //printHashMap(wordsWithTagsMap);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //calculate Tag|Previous Tag probability:
    private static void calculateTagGivenPreviousTagProbability()
    {
        for (Map.Entry<String, Integer> entry : tagPairAndCountMap.entrySet()) {
            String givenTag = entry.getKey().split("~~~")[0];
            String tag = entry.getKey().split("~~~")[1];
            tagGivenPreviousTagProbabilityMap.put("(" + tag + "|" + givenTag + ")", (double) entry.getValue() / tagAndCountMap.get(givenTag));
        }
    }

    //function to calculate Word|Tag probability:
    private static void calculateWordGivenTagProbability()
    {
        for(Map.Entry<String,HashMap<String , Integer>> entry : wordsWithTagsMap.entrySet())
        {
            String word = entry.getKey();
            Map<String, Integer> countOfTagsMap = entry.getValue();
            for(Map.Entry<String,Integer> countEntry: countOfTagsMap.entrySet())
            {
                String tag = countEntry.getKey();
                Integer count = countEntry.getValue();
                // total count of tags in the corpus:
                Integer tagTotalCount = tagAndCountMap.get(tag);
                double probability = (double) count/tagTotalCount;
                wordGivenTagProbabilityMap.put("(" + word + "|" + tag + ")",probability);
            }
        }
    }


    //function to calculate Unigrams & Bigrams for Naive Bayes: (Similar to Ques2 Unigrams & Bigrams calculation)
    private static void formTagsUnigramsBigrams()
    {
        tagAndCountMap = new HashMap<>();
        tagPairAndCountMap = new HashMap<>();
        String previousTag = null;
        for(String tag : correctTagList)
        {
            int count = (tagAndCountMap.containsKey(tag)) ? tagAndCountMap.get(tag) : 0;
            tagAndCountMap.put(tag, count +1);

            if(previousTag != null)
            {
                String key = previousTag +"~~~"+ tag;
                int pairsCount = (tagPairAndCountMap.containsKey(key))?tagPairAndCountMap.get(key) : 0;
                tagPairAndCountMap.put(key, pairsCount+1);
            }
            previousTag = tag;
        }
    }

    //write Word|Tag probabilities to the file:
    private static void writeWordGivenTagProbabilities()
    {
        System.out.print("Writing Word|Tag probabilities to the file ");
        writeToFile("********************* Probability(Word|Tag)*********************");
        for(Map.Entry<String,Double> entry : wordGivenTagProbabilityMap.entrySet())
        {
            writeToFile("Prob("+entry.getKey()+" = "+entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }

    //write Tag|Previous Tag probabilities to the file:
    private static void writeTagGivenPreviousTagProbabilities()
    {
        System.out.print("Writing Tag|Previous Tag to the file ");
        writeToFile("********************* Probability(Tag|Previous Tag)*********************");
        for(Map.Entry<String,Double> entry : tagGivenPreviousTagProbabilityMap.entrySet())
        {
            writeToFile("Prob("+entry.getKey()+" = "+entry.getValue());
        }
        System.out.println(" - - - - - - > COMPLETE!");
    }


    //function to get transformation rules:
    private static void getTransformations()
    {

        List<Transformation> transformationList = getBestTransformation();
        for(Transformation transformation : transformationList)
        {
            if(transformation != null)
            {
                writeToFile(transformation.printRule()+"\n");
                System.out.println(transformation.printRule());
            }
            else
            {
                System.out.println("WARNING: No transformation found!");
            }
        }

    }

    //function to get best transformation by looping on Tags
    //From tag is fixed for both cases in our problem so we loop only over To tags
    //i. Transform “NN” to “JJ”
    //ii. Transform “NN” to “VB”
    //Therefore, From tag is fixed to NN and To Tags are in a list - <"JJ", "VB">.
    private static List<Transformation> getBestTransformation()
    {
        Transformation bestTransformation = null;
        TagScore bestTagScore = null;
        for(String toTag : toTagsArray)
        {
            if(fromTag == toTag)
            {
                continue;
            }
            HashMap<String, Integer> goodTransformationCountsMap = new HashMap<>();
            HashMap<String, Integer> badTransformationCountsMap = new HashMap<>();
            for(int i=1; i< wordsList.size();i++)
            {
                String previousTag = currentTagList.get(i-1);
                if(correctTagList.get(i).equals(toTag) && currentTagList.get(i).equals(fromTag))
                {
                    int goodCount = (goodTransformationCountsMap.containsKey(previousTag)) ? goodTransformationCountsMap.get(previousTag) : 0;
                    goodTransformationCountsMap.put(previousTag, goodCount +1);
                }
                else if(correctTagList.get(i).equals(fromTag) && currentTagList.get(i).equals(fromTag))
                {
                    int badCount = (badTransformationCountsMap.containsKey(previousTag)) ? badTransformationCountsMap.get(previousTag) : 0;
                    badTransformationCountsMap.put(previousTag, badCount +1);
                }
            }

            TagScore tagScore = getBestTagScore(goodTransformationCountsMap, badTransformationCountsMap);
            if(tagScore.tag.length() > 0 )
            {
                if(bestTagScore == null  || bestTagScore.score < tagScore.score)
                {
                    bestTagScore = tagScore;
                    bestTransformation = new Transformation(bestTagScore.tag,fromTag,toTag,bestTagScore.score );
                }
            }
            transformationsList.add(bestTransformation);
        }
        return transformationsList;
    }

    //function to return tag with highest no. of transformations.
    private static TagScore getBestTagScore(HashMap<String, Integer> goodTransformationCountsMap, HashMap<String, Integer> badTransformationCountsMap)
    {
        String bestTag = "";
        long bestScore = Long.MIN_VALUE;
        for(Map.Entry<String,Integer> entry : goodTransformationCountsMap.entrySet())
        {
            String tag = entry.getKey();
            int goodCount = entry.getValue();
            int badCount = (badTransformationCountsMap.containsKey(tag)) ? badTransformationCountsMap.get(tag) : 0;
            long score = goodCount - badCount;
            if(score > bestScore)
            {
                bestTag = tag;
                bestScore = score;
            }
        }
        return new TagScore(bestTag,bestScore);
    }


    private static void getHighestFrequencyTags()
    {
        for(Map.Entry<String, HashMap<String, Integer>> entry : wordsWithTagsMap.entrySet())
        {
            int maxFrequency = -1;
            String maxFrequencyTag = "";
            String word = entry.getKey();
            //tagWithCount will have (Key - Tag & Value - count of that tag) :
            for (Map.Entry<String, Integer> tagWithCount : entry.getValue().entrySet())
            {
                if(tagWithCount.getValue() > maxFrequency)
                {
                    maxFrequencyTag = tagWithCount.getKey();
                    maxFrequency = tagWithCount.getValue();
                }
            }
            // add the tag with max Frequency to a hashmap with that word:
            wordWithMostFrequentTagMap.put(word,maxFrequencyTag);
        }
    }


    /*
    * function to create a Hashmap to store all words with their tags and corresponding counts.
    * tagsAndCountsMap : Hashmap
    * Key is word
    * Value is HashMap of all tags of that word (Key: Tag & Value: count of the tag)
    */
    private static void storeAllTagsAndCounts(String word, String tag)
    {
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

    static class Transformation
    {
        long score;
        String previousTag, fromTag, toTag;


        public Transformation(String previousTag, String fromTag, String toTag, long score) {
            this.previousTag = previousTag;
            this.fromTag = fromTag;
            this.toTag = toTag;
            this.score = score;
        }

        public String printRule() {

            //return "If previous tag is " + previousTag + " and current tag is " + fromTag + " then, change the current tag to " + toTag;
            if(score<0)
            {
                return "If previous tag is " + previousTag + " and current tag is " + fromTag + " then, change the current tag to " + toTag + " --> *** WARNING: This rule has a negative score.";
            }
            else
            {
                return "If previous tag is " + previousTag + " and current tag is " + fromTag + " then, change the current tag to " + toTag;
            }
        }
    }

    static class TagScore {
        String tag;
        Long score;

        public TagScore(String tag, Long score)
        {
            this.tag = tag;
            this.score = score;
        }
    }

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
}



