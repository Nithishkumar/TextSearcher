package org.textsearcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class TextSearch {
    public static void main(String[] args) throws IOException {
        List<String> firstNames = Arrays.asList("James", "John", "Robert", "Michael", "William", "David",
                "Richard", "Charles", "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald",
                "George", "Kenneth", "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason",
                "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen",
                "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter",
                "Harold", "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger");

        // Index the large text file
        Directory index = indexText("http://norvig.com/big.txt",firstNames);

        // Search for first names in the indexed text
        List<Document> results = searchFirstNames(index, firstNames);
        Map<String, Set<String>> map = new HashMap<>();

        // Print the results
        for (Document doc : results) {
            String name = Arrays.asList(doc.getValues("searchNames")).get(0);
            String location = Arrays.asList(doc.getValues("location")).get(0);

            Set<String> list = map.getOrDefault(name, new HashSet<>());
            list.add(location);
            map.put(name, list);
        }
        map.forEach((key,value)-> System.out.println(key + "=>" + value));
    }

    private static Directory indexText(String url, List<String> firstNames) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get("index"));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            Document doc = new Document();
            doc.add(new Field("content", line, getType())); // Index the content
            doc.add(new Field("lineNumber", String.valueOf(lineNumber), getType()));
            writer.addDocument(doc);
            lineNumber++;
        }

        writer.close();
        return index;
    }

    private static List<Document> searchFirstNames(Directory index, List<String> firstNames) throws IOException {
        List<Document> results = new ArrayList<>();
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(index));
        try {
            for (String firstName : firstNames) {
                Query query = new QueryParser("content", new StandardAnalyzer()).parse(firstName);
                TopDocs topDocs = searcher.search(query, 10);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    int lineNumber = Integer.parseInt(doc.get("lineNumber"));
                    String location = "[lineOffset=" + lineNumber + ", charOffset=" + doc.get("content").indexOf(firstName) + "]";
                    doc.add(new org.apache.lucene.document.StringField("location", location, Field.Store.YES));
                    doc.add(new org.apache.lucene.document.StringField("searchNames", firstName, Field.Store.YES));
                    results.add(doc);
                }
            }
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        } finally {
            searcher.getIndexReader().close(); // Close the IndexReader
        }
        return results;
    }

    private static IndexableFieldType getType() {
        org.apache.lucene.document.FieldType type = new org.apache.lucene.document.FieldType();
        type.setStored(true); // Make the field stored
        type.setIndexOptions(org.apache.lucene.index.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS); // Specify the indexing options
        return type;
    }
}
