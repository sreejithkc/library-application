package com.micro.library.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.library.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.print.attribute.IntegerSyntax;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Author : Sreejith K
 */
@Component
@Slf4j
public class LibraryEventProducer {
    private String topic = "library_events";
    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;

    /**
     * Send to Default Topic
     * @param libraryEvent
     * @throws JsonProcessingException
     */
    public void sendLibraryEventDefault(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key  = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
       ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>(){

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSucess( key,  value,  result);
            }

            @Override
            public void onFailure(Throwable ex) {
                handleFailure( key,  value,  ex);
            }
        });

    }

    /**
     * Synchronous Events
     * @param libraryEvent
     * @return
     * @throws JsonProcessingException
     */
    public SendResult<Integer, String> sendLibraryEventSynch(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key  = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (ExecutionException| InterruptedException e) {
            e.printStackTrace();
        }

        return sendResult;

    }

    /**
     *  Send Events to a given Topic
     * @param libraryEvent
     * @throws JsonProcessingException
     */
    public  ListenableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key  = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord<Integer, String> producerRecord = createProducerRecord(key, value);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>(){

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSucess( key,  value,  result);
            }

            @Override
            public void onFailure(Throwable ex) {
                handleFailure( key,  value,  ex);
            }
        });

        return listenableFuture;

    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    private ProducerRecord<Integer, String> createProducerRecord(Integer key, String value){
        List<Header> headerList = List.of(new RecordHeader("event-source","scanner".getBytes()));
        return new ProducerRecord<>(topic,null,key,value,headerList);
    }

    /**
     *
     * @param key
     * @param value
     * @param ex
     */
    private void handleFailure(Integer key, String value, Throwable ex) {
        log.info("Message Sent Failed for the key: {}, value {}, exceptin {}", key, value, ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Failure in handleFailure");
        }
    }

    /**
     *
     * @param key
     * @param value
     * @param result
     */
    private void handleSucess(Integer key, String value, SendResult<Integer, String> result){
        log.info("Message Sent sucessfully for the key: {}, value {}, partition {}", key, value, result.getRecordMetadata().partition());
    }

}
