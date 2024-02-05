package org.zatag.dev.datamanagement.Repository.MySql;

import org.zatag.dev.datamanagement.Models.MySql.MySQLLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MySQLLinkRepository extends MongoRepository<MySQLLink, String> {
}
