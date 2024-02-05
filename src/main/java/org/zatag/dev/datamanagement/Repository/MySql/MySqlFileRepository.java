package org.zatag.dev.datamanagement.Repository.MySql;

import org.zatag.dev.datamanagement.Models.MySql.MySqlFiles;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MySqlFileRepository extends MongoRepository<MySqlFiles, String> {
}
