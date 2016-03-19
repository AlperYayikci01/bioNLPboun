package bioNLPboun;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by berfu on 9.3.2016.
 */
public class TestMethods {

    private static ArrayList<Document> trainDocs;
    private static ArrayList<String> names;

    private static final Logger LOGGER = Logger.getLogger( TestMethods.class.getName() );


    public TestMethods(ArrayList<Document> trDocs, ArrayList<String> nm){

        trainDocs = trDocs;
        names = nm;
    }

    public static void ConstructOutputFiles() throws IOException {

        BufferedReader br;
        String line;
        PrintWriter writer;

        for (Document document : trainDocs) {

            ArrayList<Term> candidates = document.candidates;
            writer = new PrintWriter(document.file_name + ".a1", "UTF-8");
            int lineNo = 2;
            String prg = document.paragraph;

            writer.println("T1 Title 0 " + (document.title.length() - 1) + " " + document.title + "\n");
            writer.println("T1 Paragraph " + document.title.length() + " " + (document.paragraph.length() - 1) + " " + document.paragraph + "\n");

            for (Term term : candidates) {
                String termName = term.name_txt.trim();

                if (IsBacteriaName(termName)) {

                    lineNo++;

                    writer.println("T" + lineNo + " Bacteria " + term.start_pos + " " + term.end_pos + " " + termName);

                }

            }
            writer.close();
        }
    }

    //MAKES EXACT MATCH ONLY. NEEDS TO BE IMPROVED AND FASTENED

    public static boolean IsBacteriaName(String termName) throws IOException {

        if(names.contains(termName))
        {
            return  true;
        }
        else return false;


    }


}
