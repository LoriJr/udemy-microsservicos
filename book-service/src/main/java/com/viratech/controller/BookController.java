package com.viratech.controller;

import com.viratech.model.Book;
import com.viratech.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book Endpoint")
@RestController
@RequestMapping("/book-service")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @Operation(summary = "Find a specific book by your ID")
    @GetMapping(value = "/{id}/{currency}",
    produces = MediaType.APPLICATION_JSON_VALUE)
    public Book findBook(
            @PathVariable Long id,
            @PathVariable String currency
    ){

        return service.findBook(id, currency);

    }

}
