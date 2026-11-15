package com.library.management.service;

import com.library.management.dto.BookRequest;
import com.library.management.dto.BookResponse;
import com.library.management.dto.BorrowingRequest;
import com.library.management.dto.BorrowingResponse;
import com.library.management.exception.BookAlreadyReturnedException;
import com.library.management.exception.BookHasActiveBorrowingsException;
import com.library.management.exception.BookNotAvailableException;
import com.library.management.exception.DuplicateIsbnException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.Borrowing;
import com.library.management.model.BorrowingStatus;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    private BookService bookService;
    private BorrowingService borrowingService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, borrowingRepository);
        borrowingService = new BorrowingService(borrowingRepository, bookRepository, 14);
    }

    @Test
    @DisplayName("Borrow book should decrement available copies and not go below zero")
    void testBorrowBookSuccess() {
        Book book = new Book(1L, "Test Title", "Author", "ISBN123", "Fiction", 2, 2, 2021);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        BorrowingRequest request = new BorrowingRequest(1L, "Alice", "alice@example.com");
        BorrowingResponse response = borrowingService.borrowBook(request);

        assertNotNull(response);
        assertEquals(1, book.getAvailableCopies());
        assertEquals(BorrowingStatus.BORROWED, response.getStatus());
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Borrow book fails with 409 when available copies == 0")
    void testBorrowBookThrowsWhenZeroAvailable() {
        Book book = new Book(1L, "Test Title", "Author", "ISBN123", "Fiction", 2, 0, 2021);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BorrowingRequest request = new BorrowingRequest(1L, "Alice", "alice@example.com");
        assertThrows(BookNotAvailableException.class, () -> borrowingService.borrowBook(request));
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Return book increments available copies and sets RETURNED")
    void testReturnBookSuccess() {
        Book book = new Book(1L, "Test Title", "Author", "ISBN123", "Fiction", 2, 1, 2021);
        Borrowing borrowing = new Borrowing(10L, 1L, "Alice", "alice@example.com", LocalDate.now(), LocalDate.now().plusDays(14), null, BorrowingStatus.BORROWED);

        when(borrowingRepository.findById(10L)).thenReturn(Optional.of(borrowing));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);

        BorrowingResponse response = borrowingService.returnBook(10L);

        assertEquals(BorrowingStatus.RETURNED, response.getStatus());
        assertEquals(2, book.getAvailableCopies());
        assertNotNull(response.getReturnDate());
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Return book twice throws 409 BookAlreadyReturnedException")
    void testReturnBookAlreadyReturned() {
        Borrowing borrowing = new Borrowing(10L, 1L, "Alice", "alice@example.com", LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now(), BorrowingStatus.RETURNED);
        when(borrowingRepository.findById(10L)).thenReturn(Optional.of(borrowing));

        assertThrows(BookAlreadyReturnedException.class, () -> borrowingService.returnBook(10L));
    }

    @Test
    @DisplayName("Delete book with active borrowings throws 409")
    void testDeleteBookWithActiveBorrowings() {
        Book book = new Book(1L, "Test Title", "Author", "ISBN123", "Fiction", 2, 1, 2021);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookIdAndStatus(1L, BorrowingStatus.BORROWED)).thenReturn(true);

        assertThrows(BookHasActiveBorrowingsException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Create book with duplicate ISBN throws DuplicateIsbnException")
    void testCreateBookDuplicateIsbn() {
        when(bookRepository.existsByIsbn("ISBN123")).thenReturn(true);

        BookRequest request = new BookRequest("Title", "Author", "ISBN123", "Fiction", 3, 2022);
        assertThrows(DuplicateIsbnException.class, () -> bookService.createBook(request));
    }
}

