package eu.utah.blulab.documentSearch;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PositiveScoresOnlyCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchDocs {

	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;
	DirectoryReader ireader;
	
	public void searcher(String dirPath, StandardAnalyzer analyzer) 
			throws IOException{
		Directory indexDirectory = 
				FSDirectory.open(new File(dirPath).toPath());
		ireader = DirectoryReader.open(indexDirectory);

		indexSearcher = new IndexSearcher(ireader);
		queryParser = new QueryParser("fieldname",analyzer);
		
	}

	public TopDocs search( String searchQuery) 
			throws IOException, ParseException{
		query = queryParser.parse(searchQuery);
		TopScoreDocCollector collector = TopScoreDocCollector.create(10);
		indexSearcher.search(query, new PositiveScoresOnlyCollector(collector));
		return indexSearcher.search(query, 20, Sort.INDEXORDER);
	}

	public Document getDocument(ScoreDoc scoreDoc) 
			throws CorruptIndexException, IOException{
		return indexSearcher.doc(scoreDoc.doc);	
	}
	
	public DirectoryReader getReader() 
			throws CorruptIndexException, IOException{
		return ireader;	
	}
	
}
