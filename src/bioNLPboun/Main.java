package bioNLPboun;

import sun.rmi.runtime.Log;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	public static ArrayList<File> trainFiles = new ArrayList<File>(); // All File objects of training set
	public static ArrayList<Names> allNames = new ArrayList<Names>(); // All Names objects of names.dmp file.
	public static ArrayList<String> names = new ArrayList<String>(); // All Names objects of names.dmp file.
	public static TestMethods testing;


	private static final Logger LOGGER = Logger.getLogger( Main.class.getName() );
	public static void main(String[] args) throws Exception{

		ConstructTrainFiles();
		System.out.print("Reading names.dmp file...");
		ConstructNamesObjects();
		System.out.println("Done!");
		testing = new TestMethods(trainFiles, names);



		//testing.TestExactMatchesTraining();
		testing.ConstructOutputFiles();

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

	private static void ConstructTrainFiles() {

		File trainFolder = new File("BioNLP-ST-2016_BB-cat_train");
		ArrayList<File> allFiles = new ArrayList<File>(Arrays.asList(trainFolder.listFiles()));

		for(File file : allFiles){
			if(file.getName().startsWith("BB-cat-") && file.getName().endsWith(".txt"))
				trainFiles.add(file);
		}

	}

	private static void ConstructNamesObjects(){

		ArrayList<String> namesDmpFields = new ArrayList<String>();
		namesDmpFields = ReadFields("names.dmp");
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







}


