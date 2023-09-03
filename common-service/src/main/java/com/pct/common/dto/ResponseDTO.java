package com.pct.common.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseDTO {

    Boolean status;

    String message;

    public ResponseDTO(Boolean status, String responseMessage) {
        super();
        this.status = status;
        this.message = responseMessage;
    }

    public ResponseDTO(Boolean status) {
        super();
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String responseMessage) {
        this.message = responseMessage;
    }
}
