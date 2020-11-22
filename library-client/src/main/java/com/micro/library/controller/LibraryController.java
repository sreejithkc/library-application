package com.micro.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.micro.library.domain.LibraryEvent;
import com.micro.library.domain.LibraryEventType;
import com.micro.library.producer.LibraryEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
@RestController
public class LibraryController {
    @Autowired
    LibraryEventProducer libraryEventProducer;
    @PostMapping("v1/library")
    public ResponseEntity<LibraryEvent> saveBook(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        //invoke kafka producer
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
