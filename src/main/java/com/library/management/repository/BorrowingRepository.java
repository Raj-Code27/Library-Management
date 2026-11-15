package com.library.management.repository;

import com.library.management.model.Borrowing;
import com.library.management.model.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByBookId(Long bookId);
    boolean existsByBookIdAndStatus(Long bookId, BorrowingStatus status);
    List<Borrowing> findByStatus(BorrowingStatus status);
}

