package com.iqueue.dataflow.rtprocessor.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@RequiredArgsConstructor
public class UserInfo {

    private Long userId;
    private Double longitude;
    private Double latitude;
}
