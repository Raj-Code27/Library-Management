package com.library.management.config;

import com.library.management.model.Book;
import com.library.management.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            bookRepository.saveAll(Arrays.asList(
                    new Book(null, "Clean Code", "Robert C. Martin", "9780132350884", "Programming", 5, 5, 2008),
                    new Book(null, "Design Patterns", "Erich Gamma et al.", "9780201633610", "Programming", 3, 3, 1994),
                    new Book(null, "The Phoenix Project", "Gene Kim et al.", "9780988262591", "DevOps", 4, 4, 2013),
                    new Book(null, "Effective Java", "Joshua Bloch", "9780134685991", "Programming", 6, 6, 2017),
                    new Book(null, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "9780062316097", "History", 5, 5, 2014),
                    new Book(null, "A Brief History of Time", "Stephen Hawking", "9780553380163", "Science", 4, 4, 1988),
                    new Book(null, "1984", "George Orwell", "9780451524935", "Fiction", 5, 5, 1949),
                    new Book(null, "Continuous Delivery", "Jez Humble, David Farley", "9780321601919", "DevOps", 3, 3, 2010)
            ));
        }
    }
}

