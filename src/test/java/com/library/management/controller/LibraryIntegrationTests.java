package com.library.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.dto.BookRequest;
import com.library.management.dto.BorrowingRequest;
import com.library.management.model.Book;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @BeforeEach
    void cleanDatabase() {
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Actuator health check returns UP")
    void testActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Book CRUD flow and validation checks")
    void testBookCrudFlow() throws Exception {
        BookRequest request = new BookRequest("Distributed Systems", "Andrew Tanenbaum", "9780130340740", "Computer Science", 4, 2016);

        // Create book (201)
        String responseString = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.availableCopies", is(4)))
                .andReturn().getResponse().getContentAsString();

        Long bookId = objectMapper.readTree(responseString).get("id").asLong();

        // Get by ID (200)
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Distributed Systems")));

        // Duplicate ISBN rejection (409)
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Validation failure (400)
        BookRequest invalidReq = new BookRequest("", "", "123", "", 0, 900);
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()));

        // Delete book (204)
        mockMvc.perform(delete("/books/{id}", bookId))
                .andExpect(status().isNoContent());

        // Get deleted book (404)
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Borrow and return workflow with availability checks")
    void testBorrowAndReturnWorkflow() throws Exception {
        Book book = bookRepository.save(new Book(null, "Database Internals", "Alex Petrov", "9781492040347", "Technology", 1, 1, 2019));

        // Borrow success (201)
        BorrowingRequest borrowReq = new BorrowingRequest(book.getId(), "Student Demo", "student@university.edu");
        String borrowResp = mockMvc.perform(post("/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("BORROWED")))
                .andReturn().getResponse().getContentAsString();

        Long borrowingId = objectMapper.readTree(borrowResp).get("id").asLong();

        // Verify available copies decreased to 0
        mockMvc.perform(get("/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies", is(0)));

        // Borrow when zero available copies returns 409
        mockMvc.perform(post("/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        // Attempt deleting book with active borrowing returns 409
        mockMvc.perform(delete("/books/{id}", book.getId()))
                .andExpect(status().isConflict());

        // Return book (200)
        mockMvc.perform(put("/borrowings/{id}/return", borrowingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate", notNullValue()));

        // Available copies restored to 1
        mockMvc.perform(get("/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies", is(1)));

        // Double return returns 409
        mockMvc.perform(put("/borrowings/{id}/return", borrowingId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Query endpoints: category, search by title, available")
    void testQueries() throws Exception {
        bookRepository.save(new Book(null, "Kubernetes in Action", "Marko Luksa", "9781617293726", "DevOps", 2, 2, 2018));
        bookRepository.save(new Book(null, "The DevOps Handbook", "Gene Kim", "9781942788002", "DevOps", 1, 0, 2016));

        // By Category
        mockMvc.perform(get("/books/category/{category}", "devops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Search by title
        mockMvc.perform(get("/books/search").param("title", "handbook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("The DevOps Handbook")));

        // Available books (availableCopies > 0)
        mockMvc.perform(get("/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Kubernetes in Action")));
    }
}

