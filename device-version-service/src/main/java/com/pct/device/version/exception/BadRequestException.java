package com.pct.device.version.exception;

import com.pct.common.util.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BadRequestException extends BaseMessageException {

    public BadRequestException(Message message) {
        super(message);
    }

    public BadRequestException(Throwable cause, Message message) {
        super(cause, message);
    }

    public BadRequestException(Throwable cause, String messageKey) {
        super(cause, new Message(messageKey));
    }

    public BadRequestException(String messageKey) {
        super(messageKey);
    }
}
