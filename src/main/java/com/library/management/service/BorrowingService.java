package com.library.management.service;

import com.library.management.dto.BorrowingRequest;
import com.library.management.dto.BorrowingResponse;
import com.library.management.exception.BookAlreadyReturnedException;
import com.library.management.exception.BookNotAvailableException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.Borrowing;
import com.library.management.model.BorrowingStatus;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final int loanDays;

    public BorrowingService(BorrowingRepository borrowingRepository,
                            BookRepository bookRepository,
                            @Value("${library.loan-days:14}") int loanDays) {
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.loanDays = loanDays;
    }

    public BorrowingResponse borrowBook(BorrowingRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("No copies available for book id " + request.getBookId());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(loanDays);

        Borrowing borrowing = new Borrowing();
        borrowing.setBookId(book.getId());
        borrowing.setStudentName(request.getStudentName());
        borrowing.setStudentEmail(request.getStudentEmail());
        borrowing.setBorrowDate(borrowDate);
        borrowing.setDueDate(dueDate);
        borrowing.setStatus(BorrowingStatus.BORROWED);

        Borrowing saved = borrowingRepository.save(borrowing);
        return mapToResponse(saved);
    }

    public BorrowingResponse returnBook(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with id: " + borrowingId));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new BookAlreadyReturnedException("Borrowing record " + borrowingId + " has already been returned");
        }

        Book book = bookRepository.findById(borrowing.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated book not found with id: " + borrowing.getBookId()));

        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnDate(LocalDate.now());

        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        bookRepository.save(book);

        Borrowing updated = borrowingRepository.save(borrowing);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<BorrowingResponse> getAllBorrowings() {
        return borrowingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BorrowingResponse getBorrowingById(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with id: " + id));
        return mapToResponse(borrowing);
    }

    @Transactional(readOnly = true)
    public List<BorrowingResponse> getActiveBorrowings() {
        return borrowingRepository.findByStatus(BorrowingStatus.BORROWED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BorrowingResponse mapToResponse(Borrowing borrowing) {
        return new BorrowingResponse(
                borrowing.getId(),
                borrowing.getBookId(),
                borrowing.getStudentName(),
                borrowing.getStudentEmail(),
                borrowing.getBorrowDate(),
                borrowing.getDueDate(),
                borrowing.getReturnDate(),
                borrowing.getStatus()
        );
    }
}

