package bioNLPboun;

import java.util.ArrayList;
import java.util.HashMap;

public class Document {
	public int doc_id;
	public String file_name;
	public String title;
	public String paragraph; // Paragraph birden fazla olabiliyor düzeltmek lazım!!
	public ArrayList<Term> candidates;
	public ArrayList<Term> a1Terms;
	public HashMap<Term, String> originalCandidateVersions;
	public HashMap<String, String> acronyms = new HashMap<String, String>();
	
	Document(){
		this.doc_id = -1;
		this.file_name = "";
		this.title = "";
		this.paragraph = "";
		this.a1Terms = new ArrayList<Term>();
		this.candidates = new ArrayList<Term>();
		this.acronyms = new HashMap<String, String>();
	}

	Document(int doc_id){
		this.doc_id = doc_id;
		this.file_name = "";
		this.title = "";
		this.paragraph = "";
		this.candidates = new ArrayList<Term>();
		this.a1Terms = new ArrayList<Term>();
		this.acronyms = new HashMap<String, String>();
	}
	
	Document(int doc_id, String file_name, String title, String paragraph, ArrayList<Term> candidates, ArrayList<Term> a1Terms, HashMap<String, String> acronyms){
		this.doc_id = doc_id;
		this.file_name = file_name;
		this.title = title;
		this.paragraph = paragraph;
		this.candidates = candidates;
		this.a1Terms = a1Terms;
		this.acronyms = acronyms;
	}
	
	@Override
	public String toString() {
		String result = "Document : {\n\t" + doc_id + ",\n\t"+ file_name + ",\n\t"+ title + ",\n\t"+ paragraph + "Candidates : {\n";
		for(int i = 0 ; i < candidates.size(); i++){
			result += "\n" + candidates.get(i).toString() + "\n";
		}
		result += "\t}\n}";
		return result;
	}
}
