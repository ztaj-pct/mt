package com.pct.device.i18n;


import com.pct.common.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    @Qualifier("messageSource")
    private ExposedMessageSource messageSource;

    public Message.LocalizedMessage localize(Message message) {
        return message.localMessage(messageSource, LocaleContextHolder.getLocale());
    }

}