package com.scholanova.projectstore.controllers;

import com.scholanova.projectstore.exceptions.ModelNotFoundException;
import com.scholanova.projectstore.exceptions.StoreNameCannotBeEmptyException;
import com.scholanova.projectstore.models.Store;
import com.scholanova.projectstore.services.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping(path = "/stores/{id}")
    public Store getStation(@PathVariable int id) throws ModelNotFoundException {
        return storeService.getById(id);
    }

    @PostMapping(path = "/stores")
    public ResponseEntity<?> createStore(@RequestBody Store store) throws StoreNameCannotBeEmptyException {
        try{
            Store createdStore = storeService.create(store);
            return ResponseEntity.ok()
                    .body(createdStore);
        }
        catch(StoreNameCannotBeEmptyException e) {
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("msg", "name cannot be empty");
            return ResponseEntity.status(400).body(errorMsg);
        }
    }
}
