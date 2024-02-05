package org.zatag.dev.datamanagement.Controller.MySql;

// MySQLLinkController.java

import org.zatag.dev.datamanagement.Models.MySql.MySQLLink;
import org.zatag.dev.datamanagement.Repository.MySql.MySQLLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mysql-links")
public class MySQLLinkController {

    @Autowired
    private MySQLLinkRepository mySQLLinkRepository;

    @GetMapping
    public List<MySQLLink> getAllMySQLLinks() {
        return mySQLLinkRepository.findAll();
    }

    @PostMapping
    public MySQLLink addMySQLLink(@RequestBody MySQLLink mySQLLink) {
        return mySQLLinkRepository.save(mySQLLink);
    }

    @PutMapping("/{id}")
    public MySQLLink updateMySQLLink(@PathVariable String id, @RequestBody MySQLLink updatedLink) {
        updatedLink.setId(id);
        return mySQLLinkRepository.save(updatedLink);
    }

    @DeleteMapping("/{id}")
    public void deleteMySQLLink(@PathVariable String id) {
        mySQLLinkRepository.deleteById(id);
    }
}

