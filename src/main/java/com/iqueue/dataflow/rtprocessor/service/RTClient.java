package com.iqueue.dataflow.rtprocessor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iqueue.dataflow.rtprocessor.model.PredictionsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "rt-client", url = "${feign.client.config.rt-client.url}", path = "/model/recommend_based_on_coordinates/1/invocations")
public interface RTClient {

    @PostMapping
    PredictionsDTO recommendationList(@RequestBody JsonNode inputNode);
}
