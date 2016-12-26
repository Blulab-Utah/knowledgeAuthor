package eu.utah.blulab.documentSearch;

public class FindSentence {

	public String[] getSentence(String sen){
		return sen.split("\n\n+");
		
	}
}
