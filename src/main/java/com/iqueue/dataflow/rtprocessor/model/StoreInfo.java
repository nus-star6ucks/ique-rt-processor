package com.iqueue.dataflow.rtprocessor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreInfo {
    private String storeId;
    private Double longitude;
    private Double latitude;
}
