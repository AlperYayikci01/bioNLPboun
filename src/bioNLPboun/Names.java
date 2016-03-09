package bioNLPboun;

public class Names {
	public int tax_id;
	public String name_txt;
	public String unique_name;
	public String name_class;
	
	Names(){
		this.tax_id = -1;
		this.name_txt = "";
		this.unique_name = "";
		this.name_class = "";
	}

	Names(int tax_id){
		this.tax_id = tax_id;
		this.name_txt = "";
		this.unique_name = "";
		this.name_class = "";
	}
	
	Names(int tax_id, String name_txt, String unique_name, String name_class){
		this.tax_id = tax_id;
		this.name_txt = name_txt;
		this.unique_name = unique_name;
		this.name_class = name_class;
	}
	
	@Override
	public String toString() {
		return "Names : {\n\t" + tax_id + ",\n\t"+ name_txt + ",\n\t"+ unique_name + ",\n\t"+ name_class + "\n}";
	}
}
