package bioNLPboun;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {
	public static ArrayList<File> trainFiles = new ArrayList<File>(); // All File objects of training set
	public static ArrayList<Document> trainDocs = new ArrayList<Document>(); // All Document objects of training set
	public static ArrayList<Names> allNames = new ArrayList<Names>(); // All Names objects of names.dmp file.
	public static TreeMap<String, Names> allNamesMap = new TreeMap<>();
	public static ArrayList<String> allNamesList = new ArrayList<>();
	public static final int NEXT_N_WORDS = 4;
	public static String[] shorteningsInNamesDump = { " sp.", " str.", " aff.", " cf.", " subgen.", " gen.", " nov."};


	public static void main(String[] args) throws Exception{

//		InitializeLogger();
		
		
		
		System.out.print("Reading documents...");
		ConstructDocuments("C:\\Users\\berfu\\Desktop\\spring'16\\cmpe492\\BioNLP-ST-2016_BB-cat_train\\BioNLP-ST-2016_BB-cat_train");
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
		Evaluator.compareA2Files("C:\\Users\\berfu\\Desktop\\spring'16\\cmpe492\\resources\\BB-cat-output-a2-files", "C:\\Users\\berfu\\Desktop\\spring'16\\cmpe492\\BioNLP-ST-2016_BB-cat_train\\BioNLP-ST-2016_BB-cat_train");
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
			
			// READ .TXT FILES
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
		
		// READ .A1 FILES
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
																			
											OPTIMIZE_ExpandAbbrevations(term,doc);
											
											OPTIMIZE_SpecialShortenings(term);
											
											
											
//											OPTIMIZE_SingleWordExtension(term,doc); // TO DO: No idea why it lowers the precision :D
											
											// TO DO: Need to handle writing errors like "Vibro parahaemolyticus" should be "Vibrio parahaemolyticus"
											// TO DO: Bacille Calmette Guerin should be Bacille Calmette-Guerin
										
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
		name_txt = name_txt.trim().replace("\\s+", " ");
		term.name_txt = name_txt;
	}
	
	private static void OPTIMIZE_SpecialShortenings(Term term){
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
	
	private static void OPTIMIZE_ExpandAbbrevations(Term term,Document doc){

		// 2 cases here
			// OPTIMIZATION 1 :
				// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
				//						T4	Bacteria 64 75	E. coli
				// E. coli should match "Escherichia coli".
				// Also "E. coli" could have been "E.coli" need to handle that too.
			// OPTIMIZATION 2 : 
				// Handle  the case :   T3	Bacteria 38 59	Chlamydia trachomatis
				//						T4	Bacteria 64 75	C. psittaci
				// C. psittaci should match Chlamydia psittaci
			
		String[] wordsInTerm = term.name_txt.split(" ");
		if(wordsInTerm.length == 1 && wordsInTerm[0].contains(".")){
			// Handle "H.phylori" here
			if(wordsInTerm[0].indexOf(".") > 0 && wordsInTerm[0].indexOf(".") < wordsInTerm[0].length() -1){
				String seperatedWord = wordsInTerm[0].substring(0, wordsInTerm[0].indexOf(".")) +
						". " + wordsInTerm[0].substring(wordsInTerm[0].indexOf(".")+ 1 );
				term.name_txt = seperatedWord;
			}
		}
		
		// HANDLE OPTIMIZATION 1
		wordsInTerm = term.name_txt.split(" ");
		for(int i = 0; i < wordsInTerm.length; i++){
			if(wordsInTerm[i].endsWith(".") && Character.isUpperCase(wordsInTerm[i].charAt(0))){
				if(wordsInTerm[i+1] != null){
					// Here we have a term that has a shortening in its i'th index.
					// For example "E. coli"
					for(Term a1term : doc.a1Terms){
						if(a1term.isBacteria == true){
							
							if(a1term.name_txt.contains(wordsInTerm[i+1])){ 
								String[] wordsInA1Term = a1term.name_txt.split(" ");
								for(int j = 0; j < wordsInA1Term.length; j++){

									if(wordsInA1Term[j].equals(wordsInTerm[i+1])){
										if(wordsInA1Term[j-1] != null){
											if(!wordsInA1Term[j-1].equals(wordsInTerm[i]) && 
												!wordsInA1Term[j-1].endsWith(".") && 
												wordsInA1Term[j-1].substring(0,1).equalsIgnoreCase(wordsInTerm[i].substring(0, 1))){
												
//												System.out.println("$ Term_name_txt before:" + term.name_txt +
//															" --> "+ a1term.name_txt);
												
												term.name_txt = term.name_txt.replace(wordsInTerm[i], wordsInA1Term[j-1]);
												wordsInTerm = term.name_txt.split(" ");
//												System.out.println("$ Term_name_txt after:" + term.name_txt);
												break;
											}
											
										}
										
									}
								}
							}
						}
					}
					
					
				}
			}
		}
		
		
		
		//HANDLE OPTIMIZATION 2
		// Handle  the case :   T3	Bacteria 38 59	Chlamydia trachomatis
		//						T4	Bacteria 64 75	C. psittaci
		// C. psittaci should match Chlamydia psittaci
		wordsInTerm = term.name_txt.split(" ");
		for(int i = 0; i < wordsInTerm.length; i++){
			if(wordsInTerm[i].endsWith(".") && Character.isUpperCase(wordsInTerm[i].charAt(0)) ){
				if(wordsInTerm[i+1] != null){
					// Here we have a term that has a shortening in its i'th index.
					// For example "Non-O1 V. cholerae" i = 1 here
					for(Term a1term : doc.a1Terms){
						// a1Term : "Vibrio cholerae O"
						if(a1term.isBacteria == true){
							if(a1term.name_txt.substring(0,1).equalsIgnoreCase(wordsInTerm[i].substring(0, 1))){
								if(a1term.name_txt.split(" ").length > 1 && i+1 < wordsInTerm.length){
//									System.out.print("$ Apply: \"" + term.name_txt + "\" --> \""+ a1term.name_txt + "\"");
									term.name_txt = term.name_txt.replace(wordsInTerm[i], a1term.name_txt.split(" ")[0]);
									wordsInTerm = term.name_txt.split(" ");
//									System.out.println("\t$ After: \"" + term.name_txt + "\"");
									
									break;
								}
							}
							
						}
					}
					
					
				}
			}
		}
		
//		// If "coli" is not found in the term
//		// Assume "E. coli" can match something like "E. chloea".
//		String[] wordsInA1Term = a1term.name_txt.split(" ");
//		if(wordsInA1Term.length > 1){
//			if(wordsInA1Term[1].charAt(0) == candidateWords[1].charAt(0)){
//				term.name_txt = a1term.name_txt; 
//			}
//		}
		
//		for(Term a1term : doc.a1Terms){
//		if(a1term.isBacteria == true){
//			if(a1term.name_txt.charAt(0) == wordsInTerm[0].charAt(0) ){
//				if(a1term.name_txt.split(" ").length > 1 && wordsInTerm.length > 1){
//					// OPTIMIZATION 2 :
//					// Handle  the case :   T3	Bacteria 38 59	Chlamydia trachomatis
//					//						T4	Bacteria 64 75	C. psittaci
//					// C. psittaci should match Chlamydia psittaci
//					term.name_txt = a1term.name_txt.split(" ")[0] + " " + wordsInTerm[1];
//				}
//			}
//		}
//	}
		
		
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
					if(a1term.name_txt.equalsIgnoreCase(wordsInTerm[0])){ // If there is "Escherichia coli" before 
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
		namesDmpFields = ReadFields("C:\\Users\\berfu\\Desktop\\spring'16\\cmpe492\\taxdump\\names.dmp");
		int indexWord = 0;
		while(indexWord < namesDmpFields.size()){
			int tax_id = Integer.parseInt(namesDmpFields.get(indexWord));
			String name_txt = namesDmpFields.get(indexWord+1);
			String unique_name = namesDmpFields.get(indexWord+2);
			String name_class = namesDmpFields.get(indexWord+3);

			if(name_txt.charAt(0) == '\"' && name_txt.charAt(name_txt.length()-1) == '\"')
			{
				name_txt = name_txt.replace("\"", "");
			}else if(name_txt.charAt(0) == '\'' && name_txt.charAt(name_txt.length()-1) == '\'')
			{
				name_txt = name_txt.replace("'", "");
			}

            name_txt = name_txt.trim().replace("\\s+", " ");

			Names namesObj = new Names(tax_id,name_txt,unique_name,name_class);
			allNames.add(namesObj);
            allNamesMap.put(name_txt, namesObj);
			indexWord += 4;
		}

		allNamesList = new ArrayList<String>(allNamesMap.keySet());
	}

	private static void ProcessDocuments(ArrayList<Document> docs){
		
		for(Document doc : docs){
			int term_id = 0;
			ArrayList<Term> candidates = new ArrayList<Term>();
			HashMap<Term, String> originalCandidateVersions = new HashMap<>();
			String text = doc.title + doc.paragraph;
			text = text.trim().replace("\\s+", " ");
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
		word = word.replace(",$", "");
		word = word.replace(";$", "");

		/*if(!Arrays.asList(shorteningsInNamesDump).contains(word)) {
			word = word.replace("\\.$", "");
		}*/

		if(word.contains("'")) {
			lastIndexOfKesme = word.lastIndexOf("'");
			if(lastIndexOfKesme != word.length()-1)
			{
				//eliminate part after kesme
				word = word.substring(0, lastIndexOfKesme - 1);
			}

			//remove other kesmes
			word = word.replace("'", "");
		}

		return word;
	}


}


