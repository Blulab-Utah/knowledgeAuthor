package eu.utah.blulab.documentSearch;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import au.com.bytecode.opencsv.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
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
		//BufferedReader reader;
		System.out.print(path);
		File[] files = dir.listFiles();
		if (files != null) {
			for (File fl : files) {
				//if(fl.getName().equalsIgnoreCase("write.lock")){
				//System.out.println("read "+fl.getName());
				System.out.println("read "+fl.getAbsolutePath());
				//reader = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));
				//String entireFileText = new Scanner(fl)
				//	    .useDelimiter("\\A").next();

				FileInputStream fis = new FileInputStream(fl);
				byte[] data = new byte[(int) fl.length()];
				fis.read(data);
				fis.close();

				String entireFileText = new String(data, "UTF-8");

				BreakIterator border = BreakIterator.getSentenceInstance(Locale.US);
				border.setText(entireFileText );
				int start = border.first();
				int senCount =0;
				//iterate, creating sentences out of all the Strings between the given boundaries
				for (int end = border.next(); end != BreakIterator.DONE; start = end, end = border.next()) {
				    //System.out.println(text.substring(start,end));
					Document document = new Document();
					document.add(new StringField("title", fl.getName()+":"+"Sen"+senCount, Store.YES));
					//StringField field = new StringField("fieldname", entireFileText.substring(start,end),Field.Store.NO);
					
					FieldType ft = new FieldType();
				    ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
				    ft.setStoreTermVectors( true );
				    ft.setStoreTermVectorOffsets( true );
				    ft.setStoreTermVectorPayloads( true );
				    ft.setStoreTermVectorPositions( true );
				    ft.setTokenized( true );
	
					document.add(new Field("fieldname", entireFileText.substring(start,end), ft));
					document.add(new StringField("Sentence", entireFileText.substring(start,end), Store.YES));
					writer.addDocument(document);
					senCount++;
					//reader.close();
				}

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

	public void readData(String path) throws IOException{


				BufferedReader br = null;
				String line = "";
				String cvsSplitBy = ",";

				try {
					CSVReader reader = new CSVReader(new FileReader(path));
					String[] notEvent;
					int lineNumber = 0;
					while ((notEvent = reader.readNext()) != null) {

						BreakIterator border = BreakIterator.getSentenceInstance(Locale.US);
						border.setText(notEvent[10]);
						int start = border.first();
						int senCount =0;
						//iterate, creating sentences out of all the Strings between the given boundaries
						for (int end = border.next(); end != BreakIterator.DONE; start = end, end = border.next()) {
							//System.out.println(text.substring(start,end));
							Document document = new Document();
							document.add(new StringField("title", notEvent[0]+"---"+"Sen"+senCount, Store.YES));
							//StringField field = new StringField("fieldname", entireFileText.substring(start,end),Field.Store.NO);

							FieldType ft = new FieldType();
							ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
							ft.setStoreTermVectors( true );
							ft.setStoreTermVectorOffsets( true );
							ft.setStoreTermVectorPayloads( true );
							ft.setStoreTermVectorPositions( true );
							ft.setTokenized( true );

							document.add(new Field("fieldname", notEvent[10].substring(start,end), ft));
							document.add(new StringField("Sentence", notEvent[10].substring(start,end), Store.YES));
							writer.addDocument(document);
							senCount++;
							//reader.close();
						}

					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}


		writer.close();
			}


}
