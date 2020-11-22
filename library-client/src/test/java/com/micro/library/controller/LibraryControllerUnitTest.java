package com.micro.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.library.domain.Book;
import com.micro.library.domain.LibraryEvent;
import com.micro.library.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc
public class LibraryControllerUnitTest {
    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
        //given
        Book book = Book.builder()
                .bookId(123)
                .author("Sreejith")
                .bookName("Kafka using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(post("/v1/library")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    void postLibraryEvent_BadRequest() throws Exception {
        //given

        Book book = Book.builder()
                .bookId(null)
                .author(null)
                .bookName("Kafka using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);
        //expect
        String expectedErrorMessage = "book.author - must not be null, book.bookId - must not be null";
        mockMvc.perform(post("/v1/library")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedErrorMessage));

    }

    @Test
    void updateLibraryEvent() throws Exception {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .author("Sreejith")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/v1/library")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void updateLibraryEvent_withNullLibraryEventId() throws Exception {

        //given
        Book book = new Book().builder()
                .bookId(123)
                .author("Sreejith")
                .bookName("Kafka Using Spring Boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/v1/library")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Please pass the LibraryEventId"));

    }

}
