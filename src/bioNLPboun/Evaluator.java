package bioNLPboun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Evaluator {
	Evaluator(){}
	public static void compareA2Files(String outFolderName, String testFolderName){
		
		File outFolder = new File(outFolderName);
		File testFolder = new File(testFolderName);
		ArrayList<File> allOutFiles = new ArrayList<File>(Arrays.asList(outFolder.listFiles()));
		ArrayList<File> allTestFiles = new ArrayList<File>(Arrays.asList(testFolder.listFiles()));
		int totalTruePositive = 0;
		int totalTrueNegative = 0;
		int totalFalsePositive = 0;
		int totalFalseNegative = 0;
		
		for(File outFile : allOutFiles){
			
			if(outFile.getName().startsWith("BB-cat-") && outFile.getName().endsWith(".a2")){
				
				for(File testFile : allTestFiles){
					if(testFile.getName().startsWith("BB-cat-") && testFile.getName().endsWith(".a2")){
						
						String outName = outFile.getName().substring(0,outFile.getName().indexOf("."));
						String testName = testFile.getName().substring(0,testFile.getName().indexOf("."));
						
						if(outName.equals(testName)){
							
							try {
								BufferedReader buf = new BufferedReader(new FileReader(outFile.getAbsolutePath()));
								String line = null;
								String[] wordsInLine;
								ArrayList<Term> outFileA2Terms = new ArrayList<Term>();
								ArrayList<Term> testFileA2Terms = new ArrayList<Term>();
								int truePositive = 0;
								int trueNegative = 0;
								int falsePositive = 0;
								int falseNegative = 0;
								while(true){
									line = buf.readLine();
									if(line == null){
										break;
									}else{
										wordsInLine = line.split("\\t");
										if(wordsInLine[1].startsWith("NCBI_Taxonomy")){
											Term term = new Term();
											term.N_id = Integer.parseInt(wordsInLine[0].substring(1, wordsInLine[0].length()));
											String[] wordsInTaxonomy;
											wordsInTaxonomy = wordsInLine[1].split(" ");		
											term.T_id = Integer.parseInt(wordsInTaxonomy[1].substring(wordsInTaxonomy[1].indexOf(':') + 2, wordsInTaxonomy[1].length()));
											term.term_id = Integer.parseInt(wordsInTaxonomy[2].substring(wordsInTaxonomy[2].indexOf(':') + 1, wordsInTaxonomy[2].length()));
											outFileA2Terms.add(term);
										}
									}
								}
								buf.close();
								buf = new BufferedReader(new FileReader(testFile.getAbsolutePath()));
	
								while(true){
									line = buf.readLine();
									if(line == null){
										break;
									}else{
										wordsInLine = line.split("\\t");
										if(wordsInLine[1].startsWith("NCBI_Taxonomy")){
											Term term = new Term();
											term.N_id = Integer.parseInt(wordsInLine[0].substring(1, wordsInLine[0].length()));
											String[] wordsInTaxonomy;
											wordsInTaxonomy = wordsInLine[1].split(" ");		
											term.T_id = Integer.parseInt(wordsInTaxonomy[1].substring(wordsInTaxonomy[1].indexOf(':') + 2, wordsInTaxonomy[1].length()));
											term.term_id = Integer.parseInt(wordsInTaxonomy[2].substring(wordsInTaxonomy[2].indexOf(':') + 1, wordsInTaxonomy[2].length()));
											
											testFileA2Terms.add(term);
										}
									}
								}
								
								for(Term testTerm : testFileA2Terms){
									boolean foundInOutandTest = false;
									for(Term outTerm : outFileA2Terms){
										if(outTerm.T_id == testTerm.T_id){
											foundInOutandTest = true;
											if(outTerm.term_id == testTerm.term_id){
												truePositive++;
											} else {
												falsePositive++;
												for(Document doc : Main.trainDocs){
													if(doc.file_name.substring(0,doc.file_name.length()-4).equals(outFile.getName().substring(0,outFile.getName().length()-3))){
														for(Term term : doc.a1Terms){
															if(term.T_id == outTerm.T_id){
																System.out.println("#ERROR: Term Name:\"" + 
																		term.name_txt +"\" Predicted ID:" +
																		outTerm.term_id + " Real ID:" + testTerm.term_id);
																break;
															}
															
														}
														break;
													}
												}
											}
										}
									}
									if(foundInOutandTest == false){
										falseNegative++;
									}
								}
								
//								System.out.println("Results for document : " + outFile.getName());
//								double precision = (double) truePositive / ( truePositive + falsePositive );
//								System.out.println("\tPrecision = %" + Math.round(precision*100));
//								double recall = (double) truePositive / ( truePositive + falseNegative );
//								System.out.println("\tRecall = %" + Math.round(recall*100));
//								double f_measure = (double) 2.0 * (precision * recall ) / ( precision + recall );
//								System.out.println("\tF-measure = %" + Math.round(f_measure*100));
								
								totalTruePositive += truePositive;
								totalTrueNegative += trueNegative;
								totalFalsePositive += falsePositive;
								totalFalseNegative += falseNegative;

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
		}
		
		System.out.println("Results for ALL documents : ");
		double precision = (double) totalTruePositive / ( totalTruePositive + totalFalsePositive );
		System.out.println("\tPrecision = %" + Math.round(precision*100));
		double recall = (double) totalTruePositive / ( totalTruePositive + totalFalseNegative );
		System.out.println("\tRecall = %" + Math.round(recall*100));
		double f_measure = (double) 2.0 * (precision * recall ) / ( precision + recall );
		System.out.println("\tF-measure = %" + Math.round(f_measure*100));
	}
}

