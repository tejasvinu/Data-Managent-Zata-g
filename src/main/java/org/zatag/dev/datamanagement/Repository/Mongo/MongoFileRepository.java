package org.zatag.dev.datamanagement.Repository.Mongo;

import org.zatag.dev.datamanagement.Models.Mongo.MongoFiles;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoFileRepository extends MongoRepository<MongoFiles, String> {

}
