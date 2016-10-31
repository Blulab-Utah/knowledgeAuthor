package eu.utah.blulab.documentSearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.*;



public class FindDocuments {
	public String home =new File("").getAbsolutePath();
	public String indexDir =home.concat("/index");
	public String dataDir = home.concat("/docs");
	public String termPath=home.concat("/queryTerms.txt");

	StandardAnalyzer analyzer = new StandardAnalyzer();
	public static void main(String[] args) throws ParseException, IOException {
		FindDocuments findDocuments;
		ReadFiles readFiles = new ReadFiles();
		SearchDocs searchDocs = new SearchDocs();
		findDocuments = new FindDocuments();
		searchDocs.searcher(findDocuments.indexDir,findDocuments.analyzer);
		List<String> terms = readFiles.queryTerm(findDocuments.termPath);

		findDocuments.doSearch();

		/*		for(String q:terms){
			long startTime = System.currentTimeMillis();	 
			TopDocs hits =searchDocs.search(q);
			long endTime = System.currentTimeMillis();
			System.out.println("Query Term : "+q);
			for(ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searchDocs.getDocument(scoreDoc);

				System.out.println("File: "
						+ doc.get("title"));

			}
			System.out.println(hits.totalHits +
					" documents found. Time :" + (endTime - startTime));
			System.out.println("--------- \n --------------");
		}*/
	}


	public void doSearch() throws IOException, ParseException {
		// 1. Specify the analyzer for tokenizing text.  
		//    The same analyzer should be used as was used for indexing  

		FindDocuments findDocuments;
		ReadFiles readFiles = new ReadFiles();
		SearchDocs searchDocs = new SearchDocs();
		findDocuments = new FindDocuments();
		searchDocs.searcher(findDocuments.indexDir,findDocuments.analyzer);
		List<String> terms = readFiles.queryTerm(findDocuments.termPath);
		DirectoryReader ir = searchDocs.getReader();

		for(String q:terms){
			long startTime = System.currentTimeMillis();	 
			TopDocs hits =searchDocs.search(q);
			long endTime = System.currentTimeMillis();
			System.out.println("Query Term : "+q);
			for(ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = searchDocs.getDocument(scoreDoc);

				System.out.println("File: "
						+ doc.get("title"));

				Terms tv = ir.getTermVector( scoreDoc.doc , "fieldname" );
				TermsEnum terms1 = tv.iterator();
				PostingsEnum p = null;
				BytesRef text;
				while( (text = terms1.next()) != null ) {
					if(q.equalsIgnoreCase(text.utf8ToString())){
						p = terms1.postings( p, PostingsEnum.ALL );
						while( p.nextDoc() != PostingsEnum.NO_MORE_DOCS ) {
							int freq = p.freq();
							for( int i = 0; i < freq; i++ ) {
								int pos = p.nextPosition();   // Always returns -1!!!
								BytesRef data = p.getPayload();
								System.out.println("Term Position"+":"+ pos);
							}
						}
					} 
				}
			}
			System.out.println(hits.totalHits +
					" documents found. Time :" + (endTime - startTime)+" ms");
			System.out.println("--------- \n --------------");
		}


	}
}


