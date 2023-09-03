package com.pct.device.errorhandling;

import com.pct.device.exception.DeviceException;
import com.pct.device.util.ErrorResponse;
import com.pct.device.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Base classes for controller advices dealing with error handling.
 */
@ControllerAdvice
public class GlobalErrorHandling extends AbstractErrorHandling {

    private static final Map<String, String> SQL_STATES = new HashMap<>();

    @ExceptionHandler(DeviceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAuthenticationException(DeviceException ex) {
        return new ResponseEntity<ErrorResponse>(logErrorAndRespond("Internal server error", ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<ErrorResponse>(logErrorAndRespond("Error in request", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        return new ResponseEntity<ErrorResponse>(logErrorAndRespond("Not found", ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return new ResponseEntity<ErrorResponse>(logErrorAndRespond("Parameter missing exception", ex), HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles Jpa System Exception.
     *
     * @param ex the Jpa System Exception
     * @return the user-oriented error message.
     */
    @ExceptionHandler(JpaSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException ex) {
        logger.info(ex.getMessage(), ex);

        if (ex.getCause() instanceof PersistenceException) {
            PersistenceException persistence = (PersistenceException) ex.getCause();

            if (persistence.getCause() instanceof SQLException) {
                SQLException sql = (SQLException) persistence.getCause();
                String message = SQL_STATES.get(sql.getSQLState());

                if (null != message) {
                    return new ResponseEntity<ErrorResponse>(new ErrorResponse("Internal System Error", message), HttpStatus.BAD_REQUEST);//getLocalizedMessage(message);
                }
            }
        }
        return new ResponseEntity<ErrorResponse>(logErrorAndRespond("Internal System Error", ex), HttpStatus.BAD_REQUEST);
        //return getLocalizedMessage(ex.getMessage());
    }
}
