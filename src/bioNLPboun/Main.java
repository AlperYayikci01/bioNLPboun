package bioNLPboun;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {
	public static ArrayList<File> trainFiles = new ArrayList<File>(); // All File objects of training set
	public static ArrayList<Document> trainDocs = new ArrayList<Document>(); // All Document objects of training set
	public static ArrayList<Names> allNames = new ArrayList<Names>(); // All Names objects of names.dmp file.
	public static ArrayList<String> names = new ArrayList<String>(); // All Names objects of names.dmp file.
	public static TestMethods testing;
	public static final int NEXT_N_WORDS = 2;

	public static void main(String[] args) throws Exception{

		InitializeLogger();
		
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
		testing = new TestMethods();
		
		for(Document doc : trainDocs){
			TestMethods.ConstructOutputFiles(doc);
		}
		
		//testing.TestExactMatchesTraining();
		System.out.println("Done!");
		
	}
	
	private static void InitializeLogger(){
		try {
			FileHandler fh = new FileHandler("bioNLPboun.log", false);
			Logger l = Logger.getLogger("");
			fh.setFormatter(new SimpleFormatter());
			l.addHandler(fh);
			l.setLevel(Level.ALL);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

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

	}

	private static void ConstructNamesObjects(){

		ArrayList<String> namesDmpFields = new ArrayList<String>();
		namesDmpFields = ReadFields("taxdump\names.dmp");
		int indexWord = 0;
		while(indexWord < namesDmpFields.size()){
			int tax_id = Integer.parseInt(namesDmpFields.get(indexWord));
			String name_txt = namesDmpFields.get(indexWord+1);
			String unique_name = namesDmpFields.get(indexWord+2);
			String name_class = namesDmpFields.get(indexWord+3);

			Names namesObj = new Names(tax_id,name_txt,unique_name,name_class);
			allNames.add(namesObj);
			names.add(name_txt);

			indexWord += 4;
		}

	}

	private static void ProcessDocuments(ArrayList<Document> docs){
		
		for(Document doc : docs){
			int term_id = 0;
			ArrayList<Term> candidates = new ArrayList<Term>();
			String text = doc.title + doc.paragraph;
			String pattern = "(?U)\\b\\p{Lu}\\p{L}*\\b";
			String[] words = text.split("\\s");

			int tokenBeginIndex=0;
			int tokenEndIndex=0;
			int tokenLength=0;
			for(int i = 0; i < words.length; i++){
				if(words[i].matches(pattern)){ // Find words starting with capital letter.
					String token = words[i];
					tokenLength = token.length();
					tokenBeginIndex = text.indexOf(token, tokenEndIndex);
					// Add the next x words to candidates to handle phrases too.
					for(int j = 0; j < NEXT_N_WORDS ; j++){

						if(i+j >= words.length){
							break;
						}
						if(j == 0) {
							tokenEndIndex = tokenBeginIndex + tokenLength;
						}
						else
						{
							tokenEndIndex = tokenBeginIndex + tokenLength + words[i + j].length() + 1; //+1 for space
							tokenLength += words[i + j].length();
							token += " " + words[i + j];
						}

						Term term = new Term();
						term.term_id = term_id;
						term.name_txt = token;
						term.start_pos = tokenBeginIndex;
						term.end_pos = tokenEndIndex;
						candidates.add(term);
						term_id++;

					}
				}
			}
			
			doc.candidates = candidates;
		}
	}
}


