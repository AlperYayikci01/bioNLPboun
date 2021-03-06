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
	public static String[] shorteningsToRemove = { "spp", "spp." ,"strain", "str.", "aff.", "cf.", "subgen.", "gen.", "nov."};
	

	public static void main(String[] args) throws Exception{

//		InitializeLogger();
		
		
		
		System.out.print("Reading documents...");
		ConstructDocuments("BioNLP-ST-2016_BB-cat_test");
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
		Evaluator.compareA2Files("resources/BB-cat-output-a2-files", "BioNLP-ST-2016_BB-cat_test");
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
										Term term = new Term();
										term.isBacteria = (wordsInLine[1].startsWith("Bacteria")) ? true : false;
										term.isHabitat = (wordsInLine[1].startsWith("Habitat")) ? true : false;
										term.T_id = Integer.parseInt(wordsInLine[0].substring(1, wordsInLine[0].length()));
										String[] wordsInBacteria;
										wordsInBacteria = wordsInLine[1].split(" ");
										term.start_pos = Integer.parseInt(wordsInBacteria[1]);
										term.end_pos = Integer.parseInt(wordsInBacteria[wordsInBacteria.length-1]);
										term.original_name_txt = wordsInLine[2];
										
										term.original_name_txt = term.original_name_txt.toLowerCase();
										
										term.name_txt = wordsInLine[2];
										
										term.name_txt = term.name_txt.toLowerCase();
										
										// EXCEPTION: LAB is very common for Lactobacillales and can be in a document without referring therefore here.
										if(term.name_txt.equals("lab") || term.name_txt.equals("lactic acid bacteria")){
											term.name_txt = "Lactobacillales";
											term.term_id = 186826;
										}

										if(term.isBacteria == true){
											OPTIMIZE_PunctuationRemoval(term);
														
											
											
											
											
											
											// TO DO: Need to handle writing errors like "Vibro parahaemolyticus" should be "Vibrio parahaemolyticus"
											// TO DO: Bacille Calmette Guerin should be Bacille Calmette-Guerin
										
										}
										
										
//										String[] wordsInDoc = (doc.title + doc.paragraph).split(" ");
										
										
										doc.a1Terms.add(term);
									}
							}
							// OPTIMIZATION 2:  HANDLE ACRONYMS LIKE "MRSA" cases here.
							OPTIMIZE_ExpandAbbrevations(doc);
							
							OPTIMIZE_SingleWordExtension(doc); 
							
							OPTIMIZE_SpecialShortenings(doc);
							
							OPTIMIZE_MatchAcronyms(doc);
		
							handleLongPhrases(doc);
							
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
	
	private static void OPTIMIZE_SpecialShortenings(Document doc){
		
		for(Term term : doc.a1Terms){
			if(term.isBacteria == true){
				// OPTIMIZATION: Special shortenings removal 
				// { "sp.", "str.", "aff.", "cf.", "subgen.", "gen.", "nov."};
				// "spp.", "spp" , "strain"
				// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
				//						T4	Bacteria 64 75	Escherichia (sp.) coli
				// Escherichia (sp.) coli should match Escherichia coli

				String[] wordsInTerm = term.name_txt.split(" ");
				for(int i = 0; i < shorteningsToRemove.length; i++){
					// If term ends with one of these shortenings they need to be removed.
					String shortening = shorteningsToRemove[i];
					if(term.name_txt.endsWith(shortening)){
//						System.out.print("£SPECIAL \"" + term.name_txt + "\" --> ");
						if(term.name_txt.contains(" " + shortening)){
							term.name_txt = term.name_txt.replace(" " + shortening, ""); // remove sp.
							wordsInTerm = term.name_txt.split(" ");
						}
//						System.out.println("\"" + term.name_txt + "\"");
					}
					
					
				}
				if(term.name_txt.contains("reference") && !term.name_txt.startsWith("reference")){
					// remove what comes after reference
					term.name_txt = term.name_txt.substring(0,term.name_txt.indexOf("reference")-1);
					wordsInTerm = term.name_txt.split(" ");
				}
				if(term.name_txt.contains("atcc") && !term.name_txt.startsWith("atcc")){
					// remove what comes after reference
					term.name_txt = term.name_txt.substring(0,term.name_txt.indexOf("atcc")-1);
					wordsInTerm = term.name_txt.split(" ");
				}
				if(term.name_txt.endsWith("sp") && !term.name_txt.startsWith("sp")){
					// remove what comes after reference
					term.name_txt = term.name_txt.substring(0,term.name_txt.indexOf("sp")-1);
					wordsInTerm = term.name_txt.split(" ");
				}
				if(term.name_txt.endsWith("sp.") && !term.name_txt.startsWith("sp.")){
					// remove what comes after reference
					term.name_txt = term.name_txt.substring(0,term.name_txt.indexOf("sp.")-1);
					wordsInTerm = term.name_txt.split(" ");
				}
				wordsInTerm = term.name_txt.split(" ");
				for(int i = 0; i < wordsInTerm.length; i++){
					if(wordsInTerm[i].contains(":")){
						String wordToRemove = wordsInTerm[i];
						wordToRemove = wordToRemove.substring(wordToRemove.indexOf(":"), wordToRemove.length());
						term.name_txt = term.name_txt.replace(wordToRemove, "");
						wordsInTerm = term.name_txt.split(" ");
					}
					if(wordsInTerm[i].contains("kim") && wordsInTerm[i].length() > 3){
						String wordToRemove = wordsInTerm[i];
						wordToRemove = wordToRemove.substring(wordToRemove.indexOf("kim")+3, wordToRemove.length());
						term.name_txt = term.name_txt.replace(wordToRemove, "");
						wordsInTerm = term.name_txt.split(" ");
					}
					if(wordsInTerm[i].equals("ssp")){
						term.name_txt = term.name_txt.replace("ssp", "subsp."); // remove non-o1.
						wordsInTerm = term.name_txt.split(" ");
					}
					if(wordsInTerm[i].equals("ssp.")){
						term.name_txt = term.name_txt.replace("ssp.", "subsp."); // remove non-o1.
						wordsInTerm = term.name_txt.split(" ");
					}
					if(wordsInTerm[i].equals("ara+")){
						if(term.name_txt.contains(" ara+")){
							term.name_txt = term.name_txt.replace(" ara+", ""); // remove non-o1.
							wordsInTerm = term.name_txt.split(" ");
						}
						else if(term.name_txt.contains("ara+ ")){
							term.name_txt = term.name_txt.replace("ara+ ", ""); // remove non-o1.
							wordsInTerm = term.name_txt.split(" ");
						}
		//				System.out.println("£BEFORE" + term.name_txt);
						term.name_txt += " (Ara+ biotype)";
		//				System.out.println("£AFTER" + term.name_txt);
					}
					if(wordsInTerm[i].equals("ara-")){
		//				System.out.println("£BEFORE" + term.name_txt);
						term.name_txt = "burkholderia pseudomallei";
		//				System.out.println("£AFTER" + term.name_txt);
					}
					if(wordsInTerm[i].endsWith("bacteria")){
//						System.out.println("£BEFORE" + term.name_txt);
						term.name_txt = term.name_txt.replace("bacteria", "bacterium");
//						System.out.println("£AFTER" + term.name_txt);
					}
	
					// "non-o1 vibrio cholerae" should match "vibrio cholerae non-o1"
					if(wordsInTerm[i].contains("-")  && wordsInTerm[i].length() < 8 && i != wordsInTerm.length-1){
						String subtype = wordsInTerm[i];
						if(term.name_txt.contains(" " + subtype)){
							term.name_txt = term.name_txt.replace(" " + subtype, ""); // remove non-o1.
							wordsInTerm = term.name_txt.split(" ");
						}
						else if(term.name_txt.contains(subtype + " ")){
							term.name_txt = term.name_txt.replace(subtype + " ", ""); // remove non-o1.
							wordsInTerm = term.name_txt.split(" ");
						}
		//				System.out.println("£BEFORE" + term.name_txt);
						term.name_txt += " " + subtype;
		//				System.out.println("£AFTER" + term.name_txt);
					}
					wordsInTerm = term.name_txt.split(" ");
					// "eschericia coli type a" should match "eschericia coli a"
					if(wordsInTerm[i].equalsIgnoreCase("type")){
		//				System.out.print("£FALAN \"" + term.name_txt + "\" --> ");
						term.name_txt = term.name_txt.replace("type ", "");
		//				System.out.println("\"" + term.name_txt + "\"");
					}
					wordsInTerm = term.name_txt.split(" ");
					// Remove special shortenings
					for(int j = 0; j < shorteningsToRemove.length; j++){
						String shortening = shorteningsToRemove[j];
						
		//				if(wordsInTerm[i].contains(shortening)){
		//					System.out.print("£FALAN \"" + term.name_txt + "\" --> ");
		//					if(i + 1 <= wordsInTerm.length -1){
		//						// If shortening is not at the end of the term, remove what comes next after the shortening.
		//						// Not sure doing this is optimal !!
		//						// especailly if we need the info to separate its sub types.
		//						System.out.print("£FALAN \"" + term.name_txt + "\" --> ");
		//						term.name_txt = term.name_txt.substring(0, term.name_txt.indexOf(shortening)-1);
		//						wordsInTerm = term.name_txt.split(" ");
		//						System.out.println("\"" + term.name_txt + "\"");
		//						continue;
		//					}
		//					
		//				}
						
						if(term.name_txt.contains(shortening)){
		//					System.out.print("£SPECIAL \"" + term.name_txt + "\" --> ");
							if(term.name_txt.contains(" " + shortening)){
								term.name_txt = term.name_txt.replace(" " + shortening, ""); // remove sp.
								wordsInTerm = term.name_txt.split(" ");
							}
							else if(term.name_txt.contains(shortening + " ")){
								term.name_txt = term.name_txt.replace(shortening + " ", ""); // remove sp.
								wordsInTerm = term.name_txt.split(" ");
							}
		//					System.out.println("\"" + term.name_txt + "\"");
						}
						
						
					}
					
					
				}
			}
		}
	}
	
	private static void OPTIMIZE_ExpandAbbrevations(Document doc){
		for(Term term : doc.a1Terms){
			if(term.isBacteria == true){
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
					if(wordsInTerm[i].endsWith(".") && !wordsInTerm[i].equals("sp.") && !wordsInTerm[i].equals("subsp.")){
						if(i+1 < wordsInTerm.length){
							// Here we have a term that has a shortening in its i'th index and a second word after it.
							// For example "E. coli"
							int term_T_id = term.T_id;
							for(int T_index = term_T_id -1 ; T_index > 0; T_index--){
								for(Term a1term : doc.a1Terms){
									if(a1term.T_id == T_index){
										if(a1term.isBacteria == true && i+1 < wordsInTerm.length){
											if(a1term.name_txt.contains(wordsInTerm[i+1])){
		
												String[] wordsInA1Term = a1term.name_txt.split(" ");
												int j = -1;
												for(int index = 0; index < wordsInA1Term.length; index++){
													if(wordsInA1Term[index].equals(wordsInTerm[i+1])){
														j = index;
													}
												}
												// here a1Term has "coli" in its j'th index.	
														
												if(j-1 >= 0){
													if(!wordsInA1Term[j-1].equals(wordsInTerm[i]) && 
														!wordsInA1Term[j-1].endsWith(".") && 
														wordsInA1Term[j-1].substring(0,1).equalsIgnoreCase(wordsInTerm[i].substring(0, 1))
														&& !wordsInA1Term[i].equals("sp.") && !wordsInA1Term[i].equals("subsp.")){
														
														if(term.name_txt.contains(wordsInA1Term[j-1])){
															// OPTIMIZATION : Handle lactobacillus lb. gasseri case here.
															if(term.name_txt.contains(" " + wordsInTerm[i]))
																term.name_txt = term.name_txt.replace(" " + wordsInTerm[i], ""); // remove lb.
															else if(term.name_txt.contains(wordsInTerm[i] + " "))
																term.name_txt = term.name_txt.replace(wordsInTerm[i] + " ", ""); // remove lb.
		//													System.out.println("%1" + term.name_txt);
															break;
														}
														
		//												System.out.println("$before \"" + term.name_txt +
		//															"\" : \""+ a1term.name_txt + "\"");
		
														term.name_txt = term.name_txt.replace(wordsInTerm[i], wordsInA1Term[j-1]);
														wordsInTerm = term.name_txt.split(" ");
														
		//												System.out.println("$after \"" + term.name_txt + "\"");
														
														//OPTIMIZATION : Handle "E.coli" match with "escherichia coli o8" case here.
														if(wordsInTerm.length == 2 && a1term.name_txt.split(" ").length > 2 && a1term.name_txt.startsWith(term.name_txt)){
		//													System.out.println("£before \"" + term.name_txt +
		//															"\" : \""+ a1term.name_txt + "\"");
															term.name_txt = a1term.name_txt;
															wordsInTerm = term.name_txt.split(" ");
		//													System.out.println("£after \"" + term.name_txt + "\"");
														}
														
		
														break;
													}
													
												}
												
											// Handle "Yersinia" , "Y. pestis" case		
											} else if(a1term.name_txt.split(" ").length == 1 && a1term.name_txt.charAt(0) == wordsInTerm[i].charAt(0)){
//												System.out.println("£before \"" + term.name_txt +
//														"\" : \""+ a1term.name_txt + "\"");
												term.name_txt = term.name_txt.replace(wordsInTerm[i], a1term.name_txt);
												wordsInTerm = term.name_txt.split(" ");
//												System.out.println("£after \"" + term.name_txt + "\"");
												break;
											}
										}
										break;
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
					if(wordsInTerm[i].endsWith(".") && !wordsInTerm[i].equals("sp.") && !wordsInTerm[i].equals("subsp.")){
						if(i+1 < wordsInTerm.length){
							// Here we have a term that has a shortening in its i'th index.
							// For example "Non-O1 V. cholerae" i = 1 here
							for(Term a1term : doc.a1Terms){
								// a1Term : "Vibrio cholerae O"
								if(a1term.isBacteria == true){
									if(a1term.name_txt.substring(0,1).equalsIgnoreCase(wordsInTerm[i].substring(0, 1))){
										if(a1term.name_txt.split(" ").length > 1 && i+1 < wordsInTerm.length){
											
											if(term.name_txt.contains(a1term.name_txt.split(" ")[0])){
												// OPTIMIZATION : Handle lactobacillus lb. gasseri case here.
												if(term.name_txt.contains(" " + wordsInTerm[i]))
													term.name_txt = term.name_txt.replace(" " + wordsInTerm[i], ""); // remove lb.
												else if(term.name_txt.contains(wordsInTerm[i] + " "))
													term.name_txt = term.name_txt.replace(wordsInTerm[i] + " ", ""); // remove lb.
		//										System.out.println("%2" + term.name_txt);
												break;
											}
											
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
		}
	}
	
	private static void OPTIMIZE_SingleWordExtension(Document doc){
		
		// OPTIMIZATION: Single word extension 
		// Handle  the case :   T3	Bacteria 38 59	Escherichia coli
		//						T4	Bacteria 64 75	Escherichia
		// Escherichia should match Escherichia coli
		for(Term term : doc.a1Terms){
			String[] wordsInTerm = term.name_txt.split(" ");
			if(wordsInTerm.length == 1 && wordsInTerm[0].length() >= 4){ 
				for(Term a1term : doc.a1Terms){
					// TO DO: Need to handle T9	Bacteria 151 182	Lactobacillus (Lb.) gasseri K 7
					// 						 T17	Bacteria 334 346	Lactobacilli
					// Need edit distance here when contains fails.
					
					
					
					if(a1term.isBacteria == true){
						if(a1term.name_txt.contains(wordsInTerm[0]) && !wordsInTerm[0].equals(a1term.name_txt)){ // If there is "Escherichia coli" before 
//							System.out.println("$ SINGLE: \"" + term.name_txt + "\" --> \"" + a1term.name_txt + "\"");
							term.name_txt = a1term.name_txt; // Make "Escherichia" -> "Escherichia coli"
							break;
						}
					}
				}
			}
		}
	}
	
// PROBLEMS
// bacille calmette guerin should match bacille calmette-guerin
// Edit distance within document
	
	
	
	
	
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
	
	private static void handleLongPhrases(Document doc){
		
		// OPTIMIZATION:
		// Handle  the case :   T3	Bacteria 64 75	Escherichia coli K 7
		//						
		// Escherichia coli K 7 should match Escherichia coli K7
		for(Term term : doc.a1Terms){
			if(term.isBacteria == true){
				String[] wordsInTerm = term.name_txt.split(" ");
				for(int i = 0; i < wordsInTerm.length ; i++){
					if( i + 1 == wordsInTerm.length )
						break;
					 // i = 2
					if(wordsInTerm[i].length() == 1 && wordsInTerm[i+1].length() == 1 ){
						String mergedName = "";
						for(int j = 0 ; j < wordsInTerm.length ; j++){
							if(j == i){
								mergedName += wordsInTerm[i] + wordsInTerm[i+1];
								j = i+1;
								if(j + 1 < wordsInTerm.length){
									mergedName += " ";
								}else{
									break;
								}
							}else{
								mergedName += wordsInTerm[j] + " ";
							}

						}
//						System.out.println("$ " + doc.file_name);
//						System.out.println("$ \"" + term.name_txt + "\" --> \"" + mergedName + "\"");
						term.name_txt = mergedName;
					}
				}
			}
		}

	}

	private static void ConstructNamesObjects(){

		ArrayList<String> namesDmpFields = new ArrayList<String>();
		namesDmpFields = ReadFields("taxdump/names.dmp");
		int indexWord = namesDmpFields.size()-1;
		while(indexWord >= 0){
			String name_class =namesDmpFields.get(indexWord);
			String unique_name = namesDmpFields.get(indexWord-1);
			String name_txt = namesDmpFields.get(indexWord-2);
			int tax_id =  Integer.parseInt(namesDmpFields.get(indexWord-3));

			if(name_txt.charAt(0) == '\"' && name_txt.charAt(name_txt.length()-1) == '\"')
			{
				name_txt = name_txt.replace("\"", "");
			}else if(name_txt.charAt(0) == '\'' && name_txt.charAt(name_txt.length()-1) == '\'')
			{
				name_txt = name_txt.replace("'", "");
			}

            name_txt = name_txt.trim().replace("\\s+", " ");
            
            name_txt = name_txt.toLowerCase();

			Names namesObj = new Names(tax_id,name_txt,unique_name,name_class);
			allNames.add(namesObj);
            allNamesMap.put(name_txt, namesObj);
			indexWord -= 4;
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


