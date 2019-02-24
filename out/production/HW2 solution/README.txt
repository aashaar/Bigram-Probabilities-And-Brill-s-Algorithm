>> Submitted by: Aashaar Panchalan 
>> Net ID: adp170630

>> Java version : 9.0.1
>> IDE used: IntelliJ IDEA Community Edition 2017.3

>> For Ques.2 : Bigram Probabilities:

	* Build and run the file "Q2.java". Pass the input file path as arguments. eg: Q2.java <input filepath>
	* The output will be stored in a file named "Q2_output.txt"
	* The output file will have following sections(2 examples of each are mentioned here):
		> Unigram counts:
		> Bigram counts: 
		> No smoothing probabilities
		> Add One Smoothing Reconstituted Counts
		> Add One Smoothing Probabilities
		> Good Turing Counts
		> Good Turing Probabilities

	* Notes: For a bigram (results | operating) in the output file : 'results' is current word & 'operating' is previous or given word.
	* Tip : To find any section just do a Ctrl+F and type the section name to skip to that section.

	* Hand computation part is in the pdf file - HW2_AashaarPanchalan.pdf. The values can be found the in the above output file.


>> For Ques.3 : Part A & B:

	* Build and run the file "Q3.java". Pass the input file path as arguments. eg: Q3.java <input filepath>
	* If you face any encoding error during compilation through command line, just try using IDE and pass the file path as argument there, it will work.
	* The output will be stored in a file named "Q3_output.txt"
	For Part A:
	* The program will first find and print the "Best Rules".
	* Then, it'll replace ?? with the most the probable tag & print them.
	* Then, it'll apply the learnt rules. If no rules are applied, it'll display a message - "*** No rules applied to the sentence as it doesn't satisfy the rules' criterion."
	* In this question, the score for NN to JJ was negative for all tags, so this rule won't be applied.
	* Also, for NN to VB the previous tag comes out as MD which doesn't fit in our given sentence, so this rule will also not be applied.
	* The program will then finally print the final answer.
	For Part B:
	* It'll print the probabilities of Word|Tag & Tag|Previous
	* Hand computation part is in the pdf file - HW2_AashaarPanchalan.pdf. The values can be found the in the above output file.