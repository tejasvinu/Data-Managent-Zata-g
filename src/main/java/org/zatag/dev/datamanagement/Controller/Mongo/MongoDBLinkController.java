package org.zatag.dev.datamanagement.Controller.Mongo;

// MongoDBLinkController.java

import org.zatag.dev.datamanagement.Models.Mongo.MongoDBLink;
import org.zatag.dev.datamanagement.Repository.Mongo.MongoDBLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mongo-links")
public class MongoDBLinkController {

    @Autowired
    private MongoDBLinkRepository mongoDBLinkRepository;

    @GetMapping
    public List<MongoDBLink> getAllMongoDBLinks() {
        return mongoDBLinkRepository.findAll();
    }

    @PostMapping
    public MongoDBLink addMongoDBLink(@RequestBody MongoDBLink mongoDBLink) {
        return mongoDBLinkRepository.save(mongoDBLink);
    }

    @PutMapping("/{id}")
    public MongoDBLink updateMongoDBLink(@PathVariable String id, @RequestBody MongoDBLink updatedLink) {
        updatedLink.setId(id);
        return mongoDBLinkRepository.save(updatedLink);
    }

    @DeleteMapping("/{id}")
    public void deleteMongoDBLink(@PathVariable String id) {
        mongoDBLinkRepository.deleteById(id);
    }
}

