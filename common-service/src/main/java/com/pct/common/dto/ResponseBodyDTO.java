package com.pct.common.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseBodyDTO<T extends Object> {

    public T body;
    Boolean status;
    String message;

    public ResponseBodyDTO(Boolean status, String responseMessage) {
        super();
        this.status = status;
        this.message = responseMessage;
    }

    public ResponseBodyDTO(Boolean status, String responseMessage, T body) {
        super();
        this.status = status;
        this.message = responseMessage;
        this.body = body;
    }

    public ResponseBodyDTO(String responseMessage, T body) {
        super();
        this.message = responseMessage;
        this.body = body;
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

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
