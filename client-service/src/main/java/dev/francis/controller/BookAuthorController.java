package dev.francis.controller;

import com.google.protobuf.Descriptors;
import dev.francis.service.BookAuthorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BookAuthorController {
    final BookAuthorService bookAuthorClient;

    public BookAuthorController(BookAuthorService bookAuthorClient) {
        this.bookAuthorClient = bookAuthorClient;
    }

    @GetMapping("/author/{authorId}")
    public Map<Descriptors.FieldDescriptor, Object> getAuthor(@PathVariable String authorId){
        return bookAuthorClient.getAuthor(Integer.parseInt(authorId));
    }
    @GetMapping("/book/{authorId}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooks(@PathVariable String authorId) throws InterruptedException {
        return bookAuthorClient.getBooksByAuthor(Integer.parseInt(authorId));
    }

    @GetMapping("/book")
    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        return bookAuthorClient.getExpensiveBook();
    }

    @GetMapping("/book/author/{gender}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(@PathVariable String gender) throws InterruptedException {
        return bookAuthorClient.getBooksByAuthorGender(gender);
    }
}