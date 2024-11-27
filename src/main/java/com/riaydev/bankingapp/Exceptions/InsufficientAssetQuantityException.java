package com.riaydev.bankingapp.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientAssetQuantityException extends RuntimeException{

    public InsufficientAssetQuantityException(String message){
        super(message);
    }

}
