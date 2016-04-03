package bioNLPboun;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by berfu on 9.3.2016.
 */

public class TestMethods {

    private static final Logger LOGGER = Logger.getLogger( TestMethods.class.getName() );


    public TestMethods(){
    }


    public static void ConstructOutputFiles(Document doc) throws IOException {
    	
      //// WRITE A1 AND A2 FILE ////
    	
    	PrintWriter writer_a1 = new PrintWriter(doc.file_name.substring(0,doc.file_name.length()-4) + ".a1", "UTF-8");
    	PrintWriter writer_a2 = new PrintWriter(doc.file_name.substring(0,doc.file_name.length()-4) + ".a2", "UTF-8");
    	
    	writer_a1.println("T1\tTitle 0 " + (doc.title.length()) + "\t" + doc.title);
    	
    	if(doc.paragraph.length() != 0){
    		writer_a1.println("T2\tParagraph " + (doc.title.length()+1) + " " + 
    				(doc.title.length() + doc.paragraph.length()+1) + "\t" + doc.paragraph);
    	}
    	

    	ArrayList<Term> matched_candidates = new ArrayList<Term>();
    	
    	// Find matched candidates
    	for(int i = doc.candidates.size()-1; i >= 0; i--){
    		Term candidate = doc.candidates.get(i);
    		boolean isMatched = searchInNames(candidate);
    		
			if(isMatched)
				matched_candidates.add(candidate);
			
			// Handle phrases up to next n words of the candidates
			for(int n = 1; n < Main.NEXT_N_WORDS; n++){
				if(i-n < 0)
					break;
				if(!doc.candidates.get(i).name_txt.contains((CharSequence) doc.candidates.get(i-n).name_txt)){
					// If previous candidate is not shorter version of the candidate, leave and pull next canddidate.
					break;
				}else{
					if(isMatched){
						// If candidate's longer phrase already matched, we can ignore shorter version of it
		    			i--;
		    			continue;
		    		}else{
		    			Term next_candidate = doc.candidates.get(i-n);
		    			isMatched = searchInNames(next_candidate);
		    			if(isMatched)
		    				matched_candidates.add(next_candidate);
		    			i--;
		    		}
				}
    		}
    	}
    	int T_id = 3;
    	int N_id = 1;
    	// Write matched bacteria
    	
    	for(int i = matched_candidates.size()-1; i >= 0; i--){
    		Term matched_candidate = matched_candidates.get(i);
    		writer_a1.println("T" + T_id + "\tBacteria " + matched_candidate.start_pos + 
    				" " + matched_candidate.end_pos + "\t" + matched_candidate.name_txt);
    		writer_a2.println("N" + N_id + "\tNCBI_Taxonomy Annotation:T" + T_id + 
    				" Referent:" + matched_candidate.term_id);
    		T_id++;
    		N_id++;
    	}
    	
    	writer_a1.close();
    	writer_a2.close();
    }

    public static void ConstructA2Files(Document doc) throws IOException {
    	
        File file = new File("resources/BB-cat-output-a2-files/"+ doc.file_name.substring(0,doc.file_name.length()-4) + ".a2");
        if (!file.exists()) {
        	file.createNewFile();
        }
        PrintWriter writer_a2 = new PrintWriter(file);
    	int N_id = 1;
    	for(Term term : doc.a1Terms){
    		String termID = String.valueOf(term.term_id);
    		if(term.isBacteria == true){
    			if(searchInNames(term)){
    				termID = String.valueOf(term.term_id);;
    			}
    		}
			String isBacteria = "NCBI_Taxonomy";
			if(term.isBacteria == false){
				isBacteria = "OntoBiotope";
				termID = "OBT:000000";
			}
			writer_a2.println("N" + N_id + "\t" + isBacteria + " Annotation:T" + term.T_id + 
    				" Referent:" + termID);
			N_id ++;
    	}
    	writer_a2.close();
    }

	public static boolean searchInNames(Term candidate){

		double editDistance = Double.POSITIVE_INFINITY;
		int editDisFound = -1;
		boolean isMatched = false;
		Names currentName = new Names();
		for(Names namesObject : Main.allNames){
			currentName = namesObject;
			// If original text already matches , match and return
			if(namesObject.name_txt.equalsIgnoreCase(candidate.original_name_txt)){
				isMatched = true;
				editDistance = 0;
				candidate.term_id = namesObject.tax_id;
				break;
			}
			if(namesObject.name_txt.equalsIgnoreCase(candidate.name_txt)){
				isMatched = true;
				editDistance = 0;
				candidate.term_id = namesObject.tax_id;
				break;
			}
			// Check the first 2 words and last 2 words of a candidate that has 3 words in it.
			String[] wordsInCandidate = candidate.name_txt.split(" ");
			if(wordsInCandidate.length == 3){
				String first2words = wordsInCandidate[0] + " " + wordsInCandidate[1];
				String last2words = wordsInCandidate[0] + " " + wordsInCandidate[1];
				if(namesObject.name_txt.equalsIgnoreCase(first2words)){
					isMatched = true;
					editDistance = 0;
					candidate.term_id = namesObject.tax_id;
					break;
				}
				if(namesObject.name_txt.equalsIgnoreCase(last2words)){
					isMatched = true;
					editDistance = 0;
					candidate.term_id = namesObject.tax_id;
					break;
				}
			}
			
			
//			else
//			{
//				editDisFound = computeLevenshteinDistance(candidate.name_txt, namesObject.name_txt);
//				if(editDistance > editDisFound)
//				{
//					editDistance = editDisFound;
//					candidate.term_id = namesObject.tax_id;
//				}
//
//			}
		}
//		double errorRatio = editDistance / candidate.name_txt.length();
//
//		if(editDistance < 2 && errorRatio < 0.2)
//		{
//			isMatched = true;
//		}
		
		return isMatched;

	}

	public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
		int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

		for (int i = 0; i <= lhs.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= rhs.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= lhs.length(); i++) {

			for (int j = 1; j <= rhs.length(); j++) {

				int a = distance[i - 1][j] + 1;
				int b = distance[i][j - 1] + 1;
				int c = distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1);
				int min = a > b ? b : a;
				min = min > c ? c : min;

				distance[i][j] = min;
			}
		}

		return distance[lhs.length()][rhs.length()];
	}
}
