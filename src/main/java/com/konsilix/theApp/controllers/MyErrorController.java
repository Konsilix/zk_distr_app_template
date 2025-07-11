package com.konsilix.theApp.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j // this gives access to a logger called "log"
@Controller
public class MyErrorController implements ErrorController {

//    @RequestMapping("/error")
//    public String handleError() {
//        //do something like logging
//        return "error";
//    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            try {
                Integer statusCode = Integer.valueOf(status.toString());
                log.error("Error code: " + statusCode);
                if(statusCode == HttpStatus.UNAUTHORIZED.value()) {
                    return "/error/error-401.html";
                }
                if(statusCode == HttpStatus.NOT_FOUND.value()) {
                    return "/error/error-404.html";
                }
                else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    return "/error/error-500.html";
                }
            } catch (NumberFormatException e) {
                log.error(String.format("Error code = %s: ", status.toString()), e);
            }
        }
        return "/error/error.html";
    }
}