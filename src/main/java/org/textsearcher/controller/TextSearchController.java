package org.textsearcher.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.textsearcher.model.SearchRequest;
import org.textsearcher.service.TextSearchService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
public class TextSearchController {

    @Autowired
    private TextSearchService textSearchService;



    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    public ResponseEntity<String> searchText(@RequestBody SearchRequest request) {
        try {
            Map<String, Set<String>> searchResults = textSearchService.search(request.getUrl(), request.getSearchStrings(), request.getCaseSensitive());
            return ResponseEntity.ok().body(searchResults.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during search");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
