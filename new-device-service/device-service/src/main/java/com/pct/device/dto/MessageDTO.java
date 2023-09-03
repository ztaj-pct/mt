package com.pct.device.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;

/**
 * @param <T>
 * @author Exatip
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MessageDTO<T extends Object> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 3374181227401992217L;
    public String message;
    public Boolean status;
    public String title;
    public T body;
    public Long totalKey;
    public int currentPage;
    public int total_pages;

    /**
     * No argumen constructor
     */
    public MessageDTO() {

        super();
    }


    /**
     * Constructor
     *
     * @param message
     * @param Status
     */
    public MessageDTO(String responseMessage, boolean status) {

        super();
        this.message = responseMessage;
        this.status = status;
    }


    /**
     * Constructor
     *
     * @param message
     * @param body
     * @param Status
     */
    public MessageDTO(String responseMessage, T body, boolean status) {

        super();
        this.message = responseMessage;
        this.body = body;
        this.status = status;
    }

    /**
     * Constructor
     *
     * @param message
     * @param body
     */
    public MessageDTO(String responseMessage, T body) {

        super();
        this.message = responseMessage;
        this.body = body;
    }

    /**
     * Constructor
     *
     * @param body
     */
    public MessageDTO(String responseMessage) {

        super();
        this.message = responseMessage;
    }


    /**
     * Constructor
     *
     * @param message
     * @param Status
     * @param title
     */
    public MessageDTO(String responseMessage, boolean status, String title) {

        super();
        this.message = responseMessage;
        this.status = status;
        this.title= title;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    /**
     * @return the message
     */
    public String getMessage() {

        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String responseMessage) {

        this.message = responseMessage;
    }

    /**
     * @return the body
     */
    public T getBody() {

        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(T body) {

        this.body = body;
    }

    /**
     * @return the totalPage
     */
    public Long getTotalKey() {

        return totalKey;
    }

    /**
     * @param totalPage the totalPage to set
     */
    public void setTotalKey(Long totalPage) {

        this.totalKey = totalPage;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage() {

        return currentPage;
    }

    /**
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int currentPage) {

        this.currentPage = currentPage;
    }
    
    

}
