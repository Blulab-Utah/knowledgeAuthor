package eu.utah.blulab.documentSearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;


public class FindDocuments {
	   public String home =new File("").getAbsolutePath();
	   public String indexDir =home.concat("/index");
	   public String dataDir = home.concat("/docs");
	   public String termPath=home.concat("/queryTerms.txt");
	   
	   StandardAnalyzer analyzer = new StandardAnalyzer();
	   public static void main(String[] args) throws ParseException {
	      FindDocuments findDocuments;
	      ReadFiles readFiles = new ReadFiles();
	      SearchDocs searchDocs = new SearchDocs();
	      try {
	         findDocuments = new FindDocuments();
	         readFiles.Indexer(findDocuments.indexDir,findDocuments.analyzer );
	         readFiles.read(findDocuments.dataDir);
	         searchDocs.searcher(findDocuments.indexDir,findDocuments.analyzer);
	         List<String> terms = readFiles.queryTerm(findDocuments.termPath);
	         
	         for(String q:terms){
	         TopDocs hits =searchDocs.search(q);
	         System.out.println("Query Term : "+q);
	         for(ScoreDoc scoreDoc : hits.scoreDocs) {
	             Document doc = searchDocs.getDocument(scoreDoc);
	            
	                System.out.println("File: "
	                + doc.get("title"));

	          }
             
             System.out.println("--------- \n --------------");
	         }
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	   }
}
