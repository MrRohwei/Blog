package com.voidpen.server.common.exception;

import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.response.Result;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Result<Void>> handleError(HttpServletRequest request) {
        Integer statusCode = resolveStatusCode(request);
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (statusCode == ErrorCode.NOT_FOUND.getCode()) {
            return ResponseEntity.status(httpStatus).body(Result.fail(ErrorCode.NOT_FOUND));
        }
        if (statusCode == ErrorCode.BAD_REQUEST.getCode()) {
            return ResponseEntity.status(httpStatus).body(Result.fail(ErrorCode.BAD_REQUEST));
        }
        if (statusCode == ErrorCode.UNAUTHORIZED.getCode()) {
            return ResponseEntity.status(httpStatus).body(Result.fail(ErrorCode.UNAUTHORIZED));
        }
        if (statusCode == ErrorCode.FORBIDDEN.getCode()) {
            return ResponseEntity.status(httpStatus).body(Result.fail(ErrorCode.FORBIDDEN));
        }
        return ResponseEntity.status(httpStatus).body(Result.fail(ErrorCode.SYSTEM_ERROR));
    }

    private Integer resolveStatusCode(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return Integer.parseInt(status.toString());
    }
}
