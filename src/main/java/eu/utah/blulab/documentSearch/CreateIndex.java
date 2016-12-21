package eu.utah.blulab.documentSearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import eu.utah.blulab.documentSearch.SearchDocs;

public class CreateIndex {

	   public String home =new File("").getAbsolutePath();
	   public String indexDir =home.concat("/senIndex");
	   
	   StandardAnalyzer analyzer = new StandardAnalyzer();
	   public static void main(String[] args) throws ParseException, IOException {
	      FindDocuments findDocuments;
	      ReadFiles readFiles = new ReadFiles();
	      SearchDocs searchDocs = new SearchDocs();
	         findDocuments = new FindDocuments();
	         readFiles.Indexer(findDocuments.indexDir,findDocuments.analyzer );
	         readFiles.readData(findDocuments.dataDir);

	   }
}
