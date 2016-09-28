package eu.utah.blulab.documentSearch;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;


public class ReadFiles {
	private IndexWriter writer;

	public void Indexer(String indexDirectoryPath, StandardAnalyzer analyzer) throws IOException{
		//this directory will contain the indexes
		Directory indexDirectory = 
				FSDirectory.open(new File(indexDirectoryPath).toPath());

		//create the indexer
		writer = new IndexWriter(indexDirectory,new IndexWriterConfig(analyzer) );
	}

	public void read(String path) throws IOException{
		File dir = new File(path);
		BufferedReader reader;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File fl : files) {
				//if(fl.getName().equalsIgnoreCase("write.lock")){
				//System.out.println("read "+fl.getName());
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));

				Document document = new Document();
				document.add(new StringField("title", fl.getName(), Store.YES));
				document.add(new TextField("fieldname", reader));
				writer.addDocument(document);
				reader.close();

			}
		} else {
			System.out.println("no files exist");
		}
		writer.close();
	}

	public List<String> queryTerm(String path) throws FileNotFoundException{
		List<String> terms = new ArrayList<String>();
		BufferedReader reader = null ;
		try {

			String line;

			reader = new BufferedReader(new FileReader(path));

			while ((line = reader.readLine()) != null) {
				terms.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return terms;
	}
}
