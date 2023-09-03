package com.pct.device.command.dto;

import java.io.Serializable;

public class MessageDTO<T extends Object> implements Serializable {

	private static final long serialVersionUID = 3374181227401992217L;
	public String message;
	public Boolean status;
	public T body;
	public Long totalKey;
	public int currentPage;
	public int total_pages;
	public String title;

	public MessageDTO() {

		super();
	}

	/**
	 * Constructor
	 *
	 * @param message
	 * @param Status
	 */
	public MessageDTO(String message, boolean status) {

		super();
		this.message = message;
		this.status = status;
	}

	public MessageDTO(String message, boolean status, String title) {

		super();
		this.message = message;
		this.status = status;
		this.title = title;
	}

	/**
	 * Constructor
	 *
	 * @param message
	 * @param body
	 * @param Status
	 */
	public MessageDTO(String message, T body, boolean status) {

		super();
		this.message = message;
		this.body = body;
		this.status = status;
	}

	/**
	 * Constructor
	 *
	 * @param message
	 * @param body
	 */
	public MessageDTO(String message, T body) {

		super();
		this.message = message;
		this.body = body;
	}

	/**
	 * Constructor
	 *
	 * @param body
	 */
	public MessageDTO(String message) {

		super();
		this.message = message;
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
	public void setMessage(String message) {

		this.message = message;
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

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {

		return currentPage;
	}

	

	public Long getTotalKey() {
		return totalKey;
	}

	public void setTotalKey(Long totalKey) {
		this.totalKey = totalKey;
	}

	/**
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(int currentPage) {

		this.currentPage = currentPage;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
