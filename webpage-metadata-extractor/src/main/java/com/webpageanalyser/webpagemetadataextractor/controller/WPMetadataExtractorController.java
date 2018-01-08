package com.webpageanalyser.webpagemetadataextractor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/metadataextractor")
public class WPMetadataExtractorController {

    /*Endpoint to process URL to analyze submitted via
    POST request as http://<domain_name>:<port_number>/api/v1/metadataextractor/url
    */
     @RequestMapping(value = "/url" , method = RequestMethod.POST)
     public ResponseEntity<?> processUrl(@RequestBody String request){
         return  new ResponseEntity<String>("Request "+request+" under processing.", HttpStatus.OK);
    }
}
