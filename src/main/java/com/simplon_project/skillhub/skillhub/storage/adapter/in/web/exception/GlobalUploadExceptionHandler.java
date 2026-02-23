package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.exception;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class GlobalUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class) // SPRING's
    public ProblemDetail handleSpringMaxUpload(MaxUploadSizeExceededException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        pd.setTitle("Upload size exceeded");
        pd.setDetail("The uploaded file exceeds the maximum allowed size.");
        return pd;
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleGenericMultipart(MultipartException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid multipart request");
        pd.setDetail("Malformed multipart request or missing parts.");
        return pd;
    }
}