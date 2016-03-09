package bioNLPboun;

public class Term {
	public int term_id;
	public int start_pos;
	public int end_pos;
	public String name_txt;

	Term(){
		this.term_id = -1;
		this.start_pos = 0;
		this.end_pos = 0;
		this.name_txt = "";
	}

	Term(int term_id){
		this.term_id = term_id;
		this.start_pos = 0;
		this.end_pos = 0;
		this.name_txt = "";
	}
	
	Term(int term_id, int start_pos, int end_pos, String name_txt){
		this.term_id = term_id;
		this.start_pos = start_pos;
		this.end_pos = end_pos;
		this.name_txt = name_txt;
	}
	
	@Override
	public String toString() {
		return "Term : {\n\t" + term_id + ",\n\t"+ start_pos + ",\n\t"+ end_pos + ",\n\t"+ name_txt + "\n}";
	}
}
