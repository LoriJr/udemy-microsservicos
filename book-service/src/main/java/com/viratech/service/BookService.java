package com.viratech.service;

import com.viratech.dto.ExchangeRequest;
import com.viratech.environment.InstanceInformationService;
import com.viratech.model.Book;
import com.viratech.proxy.ExchangeProxy;
import com.viratech.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final InstanceInformationService informationService;
    private final ExchangeProxy proxy;

    public Book findBook(Long id, String currency){

        Book book = repository.findById(id)
                .orElseThrow(()-> new RuntimeException("Book not found"));

        HashMap<String, String> params = new HashMap<>();
        params.put("amount", book.getPrice().toString());
        params.put("from", "USD");
        params.put("to", currency);

//        var request = new RestTemplate()
//                .getForEntity("http://localhost:8000/exchange-service/" +
//                        "{amount}/{from}/{to}", ExchangeRequest.class, params);

//        ExchangeRequest response = request.getBody();

        ExchangeRequest response = proxy.getExchange(book.getPrice(), "USD", currency);

        String port =  informationService.retrieveServerPort();

        book.setEnvironment("BOOK PORT: " + port + " | " +"EXCHANGE PORT: " + response.environment());
        book.setPrice(response.convertedValue());
        book.setCurrency(currency);

        return book;
    }
}
