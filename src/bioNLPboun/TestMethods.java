package bioNLPboun;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by berfu on 9.3.2016.
 */
public class TestMethods {

    private static ArrayList<File> trainFiles;
    private static ArrayList<String> names;

    private static final Logger LOGGER = Logger.getLogger( TestMethods.class.getName() );


    public TestMethods(ArrayList<File> trFiles, ArrayList<String> nm){

        trainFiles = trFiles;
        names = nm;
    }

    public static void TestExactMatchesTraining() throws IOException {

        BufferedReader br;
        String line;
        String[] wordsInLine;

        for(File file : trainFiles) {
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

        if(names.contains(word))
        {
            return  true;
        }
        else return false;


    }

    public static void ConstructOutputFiles()throws IOException
    {

        BufferedReader br;
        String line;
        String[] wordsInLine;
        int lineNo =2;
        int nextCharIndex = 0;
        int startCharIndexOfMatch =0; //line based char no of first character of the matching word
        PrintWriter writer;

        for(File file : trainFiles) {
            br = new BufferedReader(new FileReader(file));
            writer = new PrintWriter(file.getName() + ".a1", "UTF-8");

            nextCharIndex = writeTitleAndParagraph(file, writer);

            while ((line = br.readLine()) != null) {


                wordsInLine = line.split("\\s+");

                for(String word : wordsInLine)
                {
                    startCharIndexOfMatch += word.length();

                    if(IsBacteriaName(word))
                    {
                        lineNo ++;
                        writer.println("T" + lineNo + " Bacteria " + (startCharIndexOfMatch + nextCharIndex) + " " + (startCharIndexOfMatch + nextCharIndex + word.length()) + " " + word) ;
                        //LOGGER.log( Level.FINER, "bacteria name match" + word );
                    }
                }
              nextCharIndex += line.length();
            }

            writer.close();

            nextCharIndex =0;
            lineNo = 2;
            startCharIndexOfMatch =0;
        }

    }

    public static int writeTitleAndParagraph(File file, PrintWriter writer)throws IOException
    {

        BufferedReader br;
        String line;
        int lineNo =0;
        int nextCharIndex =0;
        String paragraph ="";
        br = new BufferedReader(new FileReader(file));


            while ((line = br.readLine()) != null) {

                lineNo ++;

             if(lineNo == 1)
             {
                 writer.println("T1 Title 0 " + (line.length()-1) + " " + line + "\n");

             }
             else if (lineNo == 2){

                 writer.print("T2 Paragraph " + (nextCharIndex));
                 paragraph += line;
             }
             else{

                 paragraph += line;
             }

                nextCharIndex += line.length();

            }

            writer.println(" " + nextCharIndex + " " + paragraph);


         return nextCharIndex;



    }

}
