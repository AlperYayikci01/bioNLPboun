package bioNLPboun;

public class Term {
	public boolean isBacteria;
	public boolean isHabitat;
	public int T_id;
	public int N_id;
	public int term_id;
	public int start_pos;
	public int end_pos;
	public String name_txt;
	public String original_name_txt;

	Term(){
		this.isBacteria = false;
		this.isHabitat = false;
		this.T_id = 0;
		this.N_id = 0;
		this.term_id = 2;
		this.start_pos = 0;
		this.end_pos = 0;
		this.name_txt = "";
		this.original_name_txt = "";
	}

	Term(int T_id,int N_id, int term_id){
		this.isBacteria = false;
		this.isHabitat = false;
		this.T_id = T_id;
		this.N_id = N_id;
		this.term_id = term_id;
		this.start_pos = 0;
		this.end_pos = 0;
		this.name_txt = "";
		this.original_name_txt = "";
	}
	
	Term(boolean isBacteria,boolean isHabitat, int T_id, int N_id, int term_id, int start_pos, int end_pos, String name_txt, String original_name_txt){
		this.isBacteria = isBacteria;
		this.isHabitat = isHabitat;
		this.T_id = T_id;
		this.N_id = N_id;
		this.term_id = term_id;
		this.start_pos = start_pos;
		this.end_pos = end_pos;
		this.name_txt = name_txt;
		this.original_name_txt = original_name_txt;
	}
	
	@Override
	public String toString() {
		return "Term : {\n\tT: " + T_id + ",\n\tN: " + N_id + ",\n\tTerm_id: " + term_id + ",\n\tStart_pos: "+ start_pos + ",\n\tEnd_pos: "+ end_pos + ",\n\tName_txt: "+ name_txt + ",\n\tOriginal_name_txt: "+ original_name_txt + "\n}";
	}
	
	@Override
	public Term clone() {
		Term term = new Term(this.isBacteria,this.isHabitat,this.T_id,this.N_id,this.term_id,this.start_pos,this.end_pos,this.name_txt,this.original_name_txt);
		return term;
	}
}
