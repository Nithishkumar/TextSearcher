package org.textsearcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.textsearcher.service.TextSearchService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
public class TextSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextSearchApplication.class, args);

        List<String> inputStrings = Arrays.asList("James", "John", "Robert", "Michael", "William", "David",
                "Richard", "Charles", "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald",
                "George", "Kenneth", "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason",
                "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen",
                "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter",
                "Harold", "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger","Nithish");
        String url = "http://norvig.com/big.txt";
        Boolean isCaseSensitive = false;
        TextSearchService textSearchService = new TextSearchService();
        try {
            Map<String, Set<String>> result = textSearchService.search(url,inputStrings,isCaseSensitive);
            result.forEach((key,value)->System.out.println(key +"-->"+value));

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
