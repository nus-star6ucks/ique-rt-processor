package com.iqueue.dataflow.rtprocessor.controller;

import com.iqueue.dataflow.rtprocessor.model.RecommendationDTO;
import com.iqueue.dataflow.rtprocessor.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@CrossOrigin
@Controller
public class RTController {

    private final InteractiveQueryService interactiveQueryService;
    public RTController(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    @PostMapping("/recommendation")
    public ResponseEntity<Object> getRecommendationList(@RequestBody UserInfo userInfo) {
        ReadOnlyKeyValueStore<String, RecommendationDTO> keyValueStore =
                interactiveQueryService.getQueryableStore("recommendation", QueryableStoreTypes.keyValueStore());
        RecommendationDTO recommendationDTO = keyValueStore.get(userInfo.getUserId());
        if (recommendationDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(keyValueStore.get(userInfo.getUserId()), HttpStatus.OK);
    }
}
