package com.library.management.exception;

public class BookHasActiveBorrowingsException extends RuntimeException {
    public BookHasActiveBorrowingsException(String message) {
        super(message);
    }
}

