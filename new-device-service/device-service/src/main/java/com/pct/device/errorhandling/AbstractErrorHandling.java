package com.pct.device.errorhandling;

import com.pct.device.exception.BaseMessageException;
import com.pct.device.i18n.MessageService;
import com.pct.device.util.ErrorResponse;
import com.pct.common.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public abstract class AbstractErrorHandling {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;

    /**
     * Translate a Message into a LocalizedMessage.
     *
     * @param message a Message to translate
     * @return a LocalizedMessage translated by the MessageService bean
     */
    protected final Message.LocalizedMessage getLocalizedMessage(Message message) {
        return messageService.localize(message);
    }

    /**
     * Translate a String into a LocalizedMessage.
     *
     * @param message a String key code to translate
     * @return a LocalizedMessage translated by the MessageService bean
     */
    protected final Message.LocalizedMessage getLocalizedMessage(String message) {
        return getLocalizedMessage(new Message(message));
    }

    /**
     * Translate the Message in a ValidationMessageException into a LocalizedMessage.
     *
     * @param exception is any ValidationMessageException containing a Message
     * @return a LocalizedMessage translated by the MessageService bean
     */
    protected final Message.LocalizedMessage getLocalizedMessage(
            BaseMessageException exception) {
        Message.LocalizedMessage message = messageService.localize(exception.asMessage());
        return message;
    }

    /**
     * Logs an error message and returns an error response.
     *
     * @param message the error message
     * @param ex      the exception to log.
     *                Message from the exception is used as the error description.
     * @return the error response that should be sent to the client
     */
    protected ErrorResponse logErrorAndRespond(String message, Exception ex) {
        logger.error(message, ex);
        return new ErrorResponse(message, ex.getMessage());
    }

    protected ErrorResponse logErrorAndRespond(String message, BaseMessageException ex) {
        logger.error(message, ex);
        ErrorResponse err = new ErrorResponse(message, getLocalizedMessage(ex).getMessage());
        return err;
    }
}
