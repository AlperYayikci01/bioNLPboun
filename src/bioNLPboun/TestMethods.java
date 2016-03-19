package bioNLPboun;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by berfu on 9.3.2016.
 */
public class TestMethods {

    private static final Logger LOGGER = Logger.getLogger( TestMethods.class.getName() );


    public TestMethods(){
    }

    public static void TestExactMatchesTraining() throws IOException {

        BufferedReader br;
        String line;
        String[] wordsInLine;

        for(File file : Main.trainFiles) {
            br = new BufferedReader(new FileReader(file));


            while ((line = br.readLine()) != null) {

                wordsInLine = line.split("\\s+");

                for(String word : wordsInLine)
                {
                    if(IsBacteriaName(word))
                    {
                        LOGGER.log( Level.FINER, "bacteria name match" + word );
                    }
                }

            }


        }

    }

    public static boolean IsBacteriaName(String word) throws IOException {

        if(Main.names.contains(word))
        {
            return  true;
        }
        else return false;


    }

    public static void ConstructOutputFiles(Document doc) throws IOException {
    	
      //// WRITE A1.FILE ////
    	
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

    public static boolean searchInNames(Term candidate){
    	
    	boolean isMatched = false;
		for(Names namesObject : Main.allNames){
			if(namesObject.name_txt.equals(candidate.name_txt)){
				isMatched = true;
				candidate.term_id = namesObject.tax_id;
				break;
			}
		}
		return isMatched;
    	
    }
}
