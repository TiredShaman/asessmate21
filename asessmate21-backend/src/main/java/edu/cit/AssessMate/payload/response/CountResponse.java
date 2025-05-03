package edu.cit.AssessMate.payload.response;

import lombok.Data;

@Data
public class CountResponse {
    private Long count;

    public CountResponse(Long count) {
        this.count = count;
    }
}