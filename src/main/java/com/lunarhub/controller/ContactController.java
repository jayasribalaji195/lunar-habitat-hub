package com.lunarhub.controller;

import com.lunarhub.model.Contact;
import com.lunarhub.repository.ContactRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping
    public Contact create(@RequestBody Contact contact) {
        return contactRepository.save(contact);
    }

    @GetMapping
    public List<Contact> list() {
        return contactRepository.findAll();
    }
}
