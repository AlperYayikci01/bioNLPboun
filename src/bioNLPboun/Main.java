package bioNLPboun;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {
	public static ArrayList<File> trainFiles = new ArrayList<File>(); // All File objects of training set
	public static ArrayList<Document> trainDocs = new ArrayList<Document>(); // All Document objects of training set
	public static ArrayList<Names> allNames = new ArrayList<Names>(); // All Names objects of names.dmp file.
	
	public static final int NEXT_N_WORDS = 4;
	public static String[] shorteningsInNamesDump = { " sp.", " str.", " aff.", " cf.", " subgen.", " gen.", " nov."};


	public static void main(String[] args) throws Exception{

//		InitializeLogger();
		
		
		
		System.out.print("Reading documents...");
		ConstructDocuments("BioNLP-ST-2016_BB-cat_train");
		System.out.println("Done!");
		
		System.out.print("Reading names.dmp file...");
		ConstructNamesObjects();
		System.out.println("Done!");
		
		System.out.print("Processing documents...");
		ProcessDocuments(trainDocs);
		System.out.println("Done!");

		System.out.print("Testing exact matches with training set...");

		for(Document doc : trainDocs){
			TestMethods.ConstructA2Files(doc);
		}
		

		System.out.println("Done!");
		System.out.println("Evaluate matches with training set...");
		Evaluator.compareA2Files("resources/BB-cat-output-a2-files", "BioNLP-ST-2016_BB-cat_train");
		System.out.println("Done!");
		
		
	}
	
//	private static void InitializeLogger(){
//		try {
//			FileHandler fh = new FileHandler("bioNLPboun.log", false);
//			Logger l = Logger.getLogger("");
//			fh.setFormatter(new SimpleFormatter());
//			l.addHandler(fh);
//			l.setLevel(Level.ALL);
//		} catch (SecurityException | IOException e) {
//			e.printStackTrace();
//		}
//	}

	private static ArrayList<String> ReadFields(String inputFilePath){
		ArrayList<String> fields = new ArrayList<String>();
		try {
			BufferedReader buf = new BufferedReader(new FileReader(inputFilePath));
			String line = null;
			String[] wordsInLine;

			while(true){
				line = buf.readLine();
				if(line == null){
					break;
				}else{
					wordsInLine = line.split("\\|");

					for(String word : wordsInLine){

						if(word.trim().isEmpty())
						{
							fields.add("");
						}
						else fields.add(word.trim());
					}
				}
			}

			buf.close();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fields;

	}

	private static void ConstructDocuments(String folderName) {

		File folder = new File(folderName);
		ArrayList<File> allFiles = new ArrayList<File>(Arrays.asList(folder.listFiles()));
		int docID = 0; 
		for(File file : allFiles){
			if(file.getName().startsWith("BB-cat-") && file.getName().endsWith(".txt")){
				trainFiles.add(file);
				Document doc = new Document();
				doc.doc_id = docID;
				doc.file_name = file.getName();
				try {
					BufferedReader buf = new BufferedReader(new FileReader(file.getAbsolutePath()));
					String line = buf.readLine();
					doc.title = line == null ? "" : line;
					line = buf.readLine();
					doc.paragraph = line == null ? "" : line;
					buf.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				trainDocs.add(doc);
				docID++;
			}
		}

		for(File file : allFiles){

			if(file.getName().startsWith("BB-cat-") && file.getName().endsWith(".a1")){
				for(Document doc : trainDocs){
					String docName = doc.file_name.substring(0,doc.file_name.indexOf("."));
					String a1Name = file.getName().substring(0,file.getName().indexOf("."));
					if(docName.equals(a1Name)){
						try {
							BufferedReader buf = new BufferedReader(new FileReader(file.getAbsolutePath()));
							String line = null;
							String[] wordsInLine;

							while(true){
								line = buf.readLine();
								if(line == null){
									break;
								}else{
									wordsInLine = line.split("\\t");
									if(wordsInLine[1].startsWith("Bacteria") || wordsInLine[1].startsWith("Habitat")){
										Term term = new Term();
										term.isBacteria = (wordsInLine[1].startsWith("Bacteria")) ? true : false;
										term.T_id = Integer.parseInt(wordsInLine[0].substring(1, wordsInLine[0].length()));
										String[] wordsInBacteria;
										wordsInBacteria = wordsInLine[1].split(" ");
										term.start_pos = Integer.parseInt(wordsInBacteria[1]);
										term.end_pos = Integer.parseInt(wordsInBacteria[wordsInBacteria.length-1]);
										term.original_name_txt = wordsInLine[2];
										
										term.original_name_txt = Character.toUpperCase(term.original_name_txt.charAt(0)) + term.original_name_txt.substring(1);
										
										term.name_txt = wordsInLine[2];
										
										term.name_txt = Character.toUpperCase(term.name_txt.charAt(0)) + term.name_txt.substring(1);
										
										if(term.isBacteria == true){
											OPTIMIZE_PunctuationRemoval(term);
																					
											OPTIMIZE_HandleShortenings(term);
											
											OPTIMIZE_ExpandShortenings(term,doc);
											
//											OPTIMIZE_SingleWordExtension(term,doc); // TO DO: No idea why it lowers the precision :D
											
											// TO DO: Need to handle writing errors like "Vibro parahaemolyticus" should be "Vibrio parahaemolyticus"
										}
										
										
//										String[] wordsInDoc = (doc.title + doc.paragraph).split(" ");
										

										
										doc.a1Terms.add(term);
									}
								}
							}
							// OPTIMIZATION 2:  HANDLE ACRONYMS LIKE "MRSA" cases here.
							OPTIMIZE_MatchAcronyms(doc);
							
							
								
							// OPTIMIZATION 3:
							// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
							//						T4	Bacteria 64 75	Escherichia coli O8:K88
							// Escherichia coli O8:K88 should match Escherichia coli
//							removeLongPhrases(doc);

							buf.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
					
				}
			}
		}
		
//		for(String acronym : acronyms.keySet()){
//			System.out.println("$Acronym: \"" + acronym + "\" --> \"" + acronyms.get(acronym) + "\"");
//		}

		
	}
	private static void OPTIMIZE_PunctuationRemoval(Term term){
		// OPTIMIZATION: Special punctuation marks removal
		String name_txt = term.name_txt;
		while(name_txt.contains("\""))
			name_txt = name_txt.replace("\"", "");
		while(name_txt.contains("\'"))
			name_txt = name_txt.replace("'", "");
		while(name_txt.contains("("))
			name_txt = name_txt.replace("(", "");
		while(name_txt.contains(")"))
			name_txt = name_txt.replace(")", "");
		name_txt = name_txt.trim().replaceAll("\\s+", " ");
		term.name_txt = name_txt;
	}
	
	private static void OPTIMIZE_HandleShortenings(Term term){
		// OPTIMIZATION: Special shortenings removal 
		// { "sp.", "str.", "aff.", "cf.", "subgen.", "gen.", "nov."};
		// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
		//						T4	Bacteria 64 75	Escherichia (sp.) coli
		// Escherichia (sp.) coli should match Escherichia coli
		String[] wordsInTerm = term.name_txt.split(" ");
		for(int i = 0; i < wordsInTerm.length; i++){
			if(wordsInTerm[i].contains(":")){
				String wordToRemove = wordsInTerm[i];
				wordToRemove = wordToRemove.substring(wordToRemove.indexOf(":"), wordToRemove.length());
				term.name_txt = term.name_txt.replace(wordToRemove, "");
			}
			if(wordsInTerm[i].equalsIgnoreCase("Type")){
				
			}
			
			
			
//				if(wordsInTerm[i].length() <= 3 || 
//						wordsInTerm[i].contains(":") || 
//						wordsInTerm[i].contains("-")){
//					String str1 = " " + wordsInTerm[i];
//					String str2 = wordsInTerm[i] + " ";
//					if(term.name_txt.contains(str1))
//						term.name_txt = term.name_txt.replace(str1, "");
//					else if(term.name_txt.contains(str2))
//						term.name_txt = term.name_txt.replace(str2, "");
//				}
		}
	}
	
	private static void OPTIMIZE_ExpandShortenings(Term term,Document doc){

		// OPTIMIZATION:  HANDLE "E. coli" cases here
		// E. phlori should match "Escherichia coli" in the text.
		String[] wordsInTerm = term.name_txt.split(" ");
		if(wordsInTerm[0].endsWith(".")){ 
			if(wordsInTerm[1] != null){
				for(Term a1term : doc.a1Terms){
					if(a1term.isBacteria == true){
						if(a1term.name_txt.contains(wordsInTerm[1])){ // If there is "Escherichia coli" before 
							term.name_txt = a1term.name_txt; // Make "E. coli" -> "Escherichia coli"
						}
						else if(a1term.name_txt.charAt(0) == wordsInTerm[0].charAt(0) ){
							if(a1term.name_txt.split(" ").length > 1 && wordsInTerm.length > 1){
								// OPTIMIZATION 2 :
								// Handle  the case :   T3	Bacteria 38 59	Chlamydia trachomatis
								//						T4	Bacteria 64 75	C. psittaci
								// C. psittaci should match Chlamydia psittaci
								term.name_txt = a1term.name_txt.split(" ")[0] + " " + wordsInTerm[1];
							}
						}
						
//							else {
//								// If "coli" is not found in the term
//								// Assume "E. coli" can match something like "E. chloea".
//								String[] wordsInA1Term = a1term.name_txt.split(" ");
//								if(wordsInA1Term.length > 1){
//									if(wordsInA1Term[1].charAt(0) == candidateWords[1].charAt(0)){
//										term.name_txt = a1term.name_txt; 
//									}
//								}
//							}
					}
				}
			}
		}
	}
	
	private static void OPTIMIZE_SingleWordExtension(Term term,Document doc){
		
		// OPTIMIZATION: Single word extension 
		// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
		//						T4	Bacteria 64 75	Escherichia
		// Escherichia should match Escherichia coli
		String[] wordsInTerm = term.name_txt.split(" ");
		if(wordsInTerm.length == 1 && wordsInTerm[0].length() >= 4){ 
			for(Term a1term : doc.a1Terms){
				if(a1term.isBacteria == true){
					if(a1term.name_txt.contains(wordsInTerm[0])){ // If there is "Escherichia coli" before 
						term.name_txt = a1term.name_txt; // Make "Escherichia" -> "Escherichia coli"
					}
				}
			}
		}
	}
	
	private static void OPTIMIZE_MatchAcronyms(Document doc){

		for(Term term : doc.a1Terms){
			if(term.name_txt.length() <= 5 && term.isBacteria == true && !doc.acronyms.containsKey(term.name_txt)){ // Handle acronyms here like "MRSA"
				int acronym_T_id = term.T_id;
//					System.out.println(doc.file_name + " - acronym_T_id : "+ acronym_T_id);
				for(int i = acronym_T_id -1 ; i > 0; i--){
					// Assume first closest bacteria found represents for all the "MRSA" s in the text.
					for(Term a1Term : doc.a1Terms){
						if(a1Term.T_id == i){
							if(a1Term.isBacteria == true && !a1Term.equals(term.name_txt)){
									if(!doc.acronyms.containsKey(term.name_txt)){
//										System.out.println(doc.file_name);
//										System.out.println("$Acronym: \"" + term.name_txt + "\" --> \"" + a1Term.name_txt + "\"");
//										System.out.println("$Acronym: \n" + term + "\n " + a1Term);
										doc.acronyms.put(term.name_txt, a1Term.name_txt);
									}
									
								break;
							}
							break;
						}
					}
					
				}
				for(Term a1Term : doc.a1Terms){
					if(doc.acronyms.containsKey(a1Term.name_txt)){
						a1Term.name_txt = doc.acronyms.get(a1Term.name_txt);
					}
				}
			}
		}

	}
	
	private static void removeLongPhrases(Document doc){
		
		// OPTIMIZATION 3:
		// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
		//						T4	Bacteria 64 75	Escherichia coli O8:K88
		// Escherichia coli O8:K88 should match Escherichia coli
		
		for(Term term : doc.a1Terms){
			String[] wordsInTerm = term.name_txt.split(" ");
			if(wordsInTerm.length > 2 && term.isBacteria == true){
				term.name_txt = wordsInTerm[0] + " " + wordsInTerm[1];
				
				for(Term a1term : doc.a1Terms){
					if(a1term.isBacteria == true){
						String firstTwoWords = wordsInTerm[0] + " " + wordsInTerm[1];
						if(a1term.name_txt.equals(firstTwoWords)){
							term.name_txt = a1term.name_txt;
						}
					}
				}
			}
		}

	}

	private static void ConstructNamesObjects(){

		ArrayList<String> namesDmpFields = new ArrayList<String>();
		namesDmpFields = ReadFields("taxdump/names.dmp");
		int indexWord = 0;
		while(indexWord < namesDmpFields.size()){
			int tax_id = Integer.parseInt(namesDmpFields.get(indexWord));
			String name_txt = namesDmpFields.get(indexWord+1);
			String unique_name = namesDmpFields.get(indexWord+2);
			String name_class = namesDmpFields.get(indexWord+3);

			if(name_txt.charAt(0) == '\"' && name_txt.charAt(name_txt.length()-1) == '\"')
			{
				name_txt = name_txt.replaceAll("\"", "");
			}else if(name_txt.charAt(0) == '\'' && name_txt.charAt(name_txt.length()-1) == '\'')
			{
				name_txt = name_txt.replaceAll("'", "");
			}

            name_txt = name_txt.trim().replaceAll("\\s+", " ");

			Names namesObj = new Names(tax_id,name_txt,unique_name,name_class);
			allNames.add(namesObj);

			indexWord += 4;
		}

	}

	private static void ProcessDocuments(ArrayList<Document> docs){
		
		for(Document doc : docs){
			int term_id = 0;
			ArrayList<Term> candidates = new ArrayList<Term>();
			HashMap<Term, String> originalCandidateVersions = new HashMap<>();
			String text = doc.title + doc.paragraph;
			text = text.trim().replaceAll("\\s+", " ");
			String pattern = "(?U)\\b\\p{Lu}\\p{L}*\\b";
			String[] words = text.split("\\s");
            String word = "";
			int tokenBeginIndex=0;
			int tokenEndIndex=0;
			int tokenLength=0;


			for(int i = 0; i < words.length; i++){

				if(words[i].matches(pattern)){ // Find words starting with capital letter.

					//Normalize word
					word = NormalizeWord(words[i]);
					String token = word;
					String tokenOriginalVersion = "";
					String wordToAdd = "";
					tokenLength = words[i].length();
					tokenBeginIndex = text.indexOf(words[i], tokenEndIndex);
					// Add the next x words to candidates to handle phrases too.
					for(int j = 0; j < NEXT_N_WORDS ; j++){

						if(i+j >= words.length){
							break;
						}
						if(j == 0) {
							tokenEndIndex = tokenBeginIndex + tokenLength;
							tokenOriginalVersion = token;
						}
						else
						{
							tokenEndIndex = tokenBeginIndex + tokenLength + words[i + j].length() + 1; //+1 for space

							tokenLength += words[i + j].length();
							wordToAdd = NormalizeWord(words[i+j]);
							tokenOriginalVersion = token + " " + words[i+j];
							token += " " + wordToAdd;


						}

						Term term = new Term();
						term.term_id = term_id;
						term.name_txt = token;
						term.start_pos = tokenBeginIndex;
						term.end_pos = tokenEndIndex;
						candidates.add(term);
						originalCandidateVersions.put(term, tokenOriginalVersion);
						term_id++;

					}
				}
			}
			
			doc.candidates = candidates;
			doc.originalCandidateVersions = originalCandidateVersions;
		}
	}

	private static String NormalizeWord(String word)
	{
		int lastIndexOfKesme =0;
		word = word.trim();
		word = word.replaceAll(",$", "");
		word = word.replaceAll(";$", "");

		/*if(!Arrays.asList(shorteningsInNamesDump).contains(word)) {
			word = word.replaceAll("\\.$", "");
		}*/

		if(word.contains("'")) {
			lastIndexOfKesme = word.lastIndexOf("'");
			if(lastIndexOfKesme != word.length()-1)
			{
				//eliminate part after kesme
				word = word.substring(0, lastIndexOfKesme - 1);
			}

			//remove other kesmes
			word = word.replaceAll("'", "");
		}

		return word;
	}
}


