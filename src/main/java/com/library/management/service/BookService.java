package com.library.management.service;

import com.library.management.dto.BookRequest;
import com.library.management.dto.BookResponse;
import com.library.management.exception.BookHasActiveBorrowingsException;
import com.library.management.exception.DuplicateIsbnException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.BorrowingStatus;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;

    public BookService(BookRepository bookRepository, BorrowingRepository borrowingRepository) {
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapToResponse(book);
    }

    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getTotalCopies());
        book.setPublishedYear(request.getPublishedYear());

        Book saved = bookRepository.save(book);
        return mapToResponse(saved);
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (bookRepository.existsByIsbnAndIdNot(request.getIsbn(), id)) {
            throw new DuplicateIsbnException("Another book with ISBN " + request.getIsbn() + " already exists");
        }

        int currentlyBorrowed = book.getTotalCopies() - book.getAvailableCopies();
        int newTotal = request.getTotalCopies();
        int newAvailable = newTotal - currentlyBorrowed;

        if (newAvailable < 0) {
            throw new IllegalArgumentException("Cannot reduce total copies to " + newTotal + " because " + currentlyBorrowed + " copies are currently borrowed");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setTotalCopies(newTotal);
        book.setAvailableCopies(newAvailable);
        book.setPublishedYear(request.getPublishedYear());

        Book updated = bookRepository.save(book);
        return mapToResponse(updated);
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        boolean hasActiveBorrowings = borrowingRepository.existsByBookIdAndStatus(id, BorrowingStatus.BORROWED);
        if (hasActiveBorrowings) {
            throw new BookHasActiveBorrowingsException("Cannot delete book with id " + id + " because it has active borrowings");
        }

        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title search query parameter cannot be empty");
        }
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findByAvailableCopiesGreaterThan(0).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getPublishedYear()
        );
    }
}

