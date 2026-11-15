package com.library.management.controller;

import com.library.management.dto.BorrowingRequest;
import com.library.management.dto.BorrowingResponse;
import com.library.management.service.BorrowingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping
    public ResponseEntity<BorrowingResponse> borrowBook(@Valid @RequestBody BorrowingRequest request) {
        return new ResponseEntity<>(borrowingService.borrowBook(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BorrowingResponse>> getAllBorrowings() {
        return ResponseEntity.ok(borrowingService.getAllBorrowings());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BorrowingResponse>> getActiveBorrowings() {
        return ResponseEntity.ok(borrowingService.getActiveBorrowings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(borrowingService.getBorrowingById(id));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(@PathVariable("id") Long id) {
        return ResponseEntity.ok(borrowingService.returnBook(id));
    }
}

