package com.iqueue.dataflow.rtprocessor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iqueue.dataflow.rtprocessor.model.PredictionsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "rt-client", url = "${feign.client.config.rt-client.url}", path = "/model")
public interface RTClient {

    @PostMapping("/recommend_based_on_coordinates/4/invocations")
    PredictionsDTO geoRecommendationList(@RequestBody JsonNode inputNode);

    @PostMapping("/als_model/4/invocations")
    PredictionsDTO alsRecommendationList(@RequestBody JsonNode inputNode);
}
