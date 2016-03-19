package bioNLPboun;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by berfu on 9.3.2016.
 */
public class TestMethods {

//    private static ArrayList<File> trainFiles;
//    private static ArrayList<String> names;

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

    public static void ConstructOutputFiles(Document doc)throws IOException
    {
    	PrintWriter writer = new PrintWriter(doc.file_name + ".a1", "UTF-8");
    	
    	writer.println("T1\tTitle 0 " + (doc.title.length()-1) + "\t" + doc.title);
    	writer.println("T2\tParagraph " + doc.title.length() + " " + 
    				(doc.paragraph.length()-1) + "\t" + doc.paragraph);
    	
    	int t_id = 3;
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
		    			isMatched = searchInNames(candidate);
		    			if(isMatched)
		    				matched_candidates.add(candidate);
		    			i--;
		    		}
				}
    		}
    	}
    	
    	// Write matched bacterias
    	for(int i = matched_candidates.size()-1; i >= 0; i--){
    		Term matched_candidate = doc.candidates.get(i);
    		writer.println("T" + t_id + "\tBacteria " + matched_candidate.start_pos + 
    				" " + matched_candidate.end_pos + "\t" + matched_candidate.name_txt);
    		
    	}
    	
    	writer.close();
//        BufferedReader br;
//        String line;
//        String[] wordsInLine;
//        
//        int lineNo =2;
//        int nextCharIndex = 0;
//        int startCharIndexOfMatch =0; //line based char no of first character of the matching word
//        PrintWriter writer;
//
//        for(File file : Main.trainFiles) {
//            br = new BufferedReader(new FileReader(file));
//            writer = new PrintWriter(file.getName() + ".a1", "UTF-8");
//
//            nextCharIndex = writeTitleAndParagraph(file, writer);
//
//            while ((line = br.readLine()) != null) {
//
//
//                wordsInLine = line.split("\\s+");
//
//                for(String word : wordsInLine)
//                {
//                    startCharIndexOfMatch += word.length();
//
//                    if(IsBacteriaName(word))
//                    {
//                        lineNo ++;
//                        writer.println("T" + lineNo + " Bacteria " + (startCharIndexOfMatch + nextCharIndex) + " " + (startCharIndexOfMatch + nextCharIndex + word.length()) + " " + word) ;
//                        //LOGGER.log( Level.FINER, "bacteria name match" + word );
//                    }
//                }
//              nextCharIndex += line.length();
//            }
//
//            writer.close();
//
//            nextCharIndex =0;
//            lineNo = 2;
//            startCharIndexOfMatch =0;
//        }

    }

    public static boolean searchInNames(Term candidate){
    	
    	boolean isMatched = false;
		for(Names namesObject : Main.allNames){
			if(namesObject.name_txt.contains(candidate.name_txt)){
				isMatched = true;
				candidate.term_id = namesObject.tax_id;
				break;
			}
		}
		return isMatched;
    	
    }

//    public static int writeTitleAndParagraph(Document doc)throws IOException
//    {
//    	
//
//    	
//        BufferedReader br;
//        String line;
//        int lineNo =0;
//        int nextCharIndex =0;
//        String paragraph ="";
//        br = new BufferedReader(new FileReader(file));
//
//
//            while ((line = br.readLine()) != null) {
//
//                lineNo ++;
//
//             if(lineNo == 1)
//             {
//                 writer.println("T1 Title 0 " + (line.length()-1) + " " + line + "\n");
//
//             }
//             else if (lineNo == 2){
//
//                 writer.print("T2 Paragraph " + (nextCharIndex));
//                 paragraph += line;
//             }
//             else{
//
//                 paragraph += line;
//             }
//
//                nextCharIndex += line.length();
//
//            }
//
//            writer.println(" " + nextCharIndex + " " + paragraph);
//
//
//         return nextCharIndex;
//
//
//
//    }
}
