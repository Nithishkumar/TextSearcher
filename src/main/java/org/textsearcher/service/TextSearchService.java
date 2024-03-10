package org.textsearcher.service;

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
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
@Service
public class TextSearchService {
    public Map<String, Set<String>> search(String url , List<String>firstNames, Boolean isCaseSensite) throws IOException, InterruptedException {

        // Index the large text file
        Directory index = indexText(url);

        // Search for first names in the indexed text
        List<Document> results = matcher(index, firstNames, isCaseSensite);

        // Process search results
        Map<String, Set<String>> map = aggregator(results);

        // Print the results
        return map;
    }

    public static Directory indexText(String url) throws IOException, InterruptedException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get("index"));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            int lineNumber = 0;
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            while ((line = reader.readLine()) != null) {
                int currentLineNumber = lineNumber++;
                String finalLine = line;
                executor.submit(() -> indexChunk(writer, finalLine, currentLineNumber));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        return index;
    }

    private static void indexChunk(IndexWriter writer, String chunk, int lineNumber) {
        try {
            Document doc = new Document();
            doc.add(new org.apache.lucene.document.StringField("content", chunk, Field.Store.YES));
            doc.add(new org.apache.lucene.document.StringField("lineNumber", String.valueOf(lineNumber), Field.Store.YES));
            writer.addDocument(doc);
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
    }
    private static List<Document> matcher(Directory index, List<String> firstNames, Boolean isCaseSensitive) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Document> results = new ArrayList<>();
        for (String firstName : firstNames) {
            executorService.execute(() -> {
                try {
                    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(index));
                    Query query = new QueryParser("content", new StandardAnalyzer()).parse(firstName);
                    TopDocs topDocs = searcher.search(query, searcher.getIndexReader().maxDoc());

                    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        Document doc = searcher.doc(scoreDoc.doc);
                        int lineNumber = Integer.parseInt(doc.get("lineNumber"));
                        if(!isCaseSensitive){
                            String location = "[lineOffset=" + lineNumber + ", charOffset=" + doc.get("content").toLowerCase().indexOf(firstName.toLowerCase()) + "]";
                            doc.add(new org.apache.lucene.document.StringField("location", location, Field.Store.YES));
                            doc.add(new org.apache.lucene.document.StringField("searchNames", firstName, Field.Store.YES));
                            synchronized (results) {
                                results.add(doc);
                            }
                        }else{
                            if(doc.get("content").contains(firstName)){
                                String location = "[lineOffset=" + lineNumber + ", charOffset=" + doc.get("content").indexOf(firstName) + "]";
                                doc.add(new org.apache.lucene.document.StringField("location", location, Field.Store.YES));
                                doc.add(new org.apache.lucene.document.StringField("searchNames", firstName, Field.Store.YES));
                                synchronized (results) {
                                    results.add(doc);
                                }
                            }
                        }

                    }
                    searcher.getIndexReader().close();
                } catch (IOException | org.apache.lucene.queryparser.classic.ParseException e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        return results;
    }

    private static IndexableFieldType getType() {
        org.apache.lucene.document.FieldType type = new org.apache.lucene.document.FieldType();
        type.setStored(true); // Make the field stored
        type.setIndexOptions(org.apache.lucene.index.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS); // Specify the indexing options
        return type;
    }

    private static Map<String, Set<String>> aggregator(List<Document> results) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Document doc : results) {
            String name = Arrays.asList(doc.getValues("searchNames")).get(0);
            String location = Arrays.asList(doc.getValues("location")).get(0);

            Set<String> list = map.getOrDefault(name, new HashSet<>());
            list.add(location);
            map.put(name, list);
        }
        return map;
    }
}

