package org.zatag.dev.datamanagement.Repository.Mongo;

import org.zatag.dev.datamanagement.Models.Mongo.MongoDBLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDBLinkRepository extends MongoRepository<MongoDBLink, String> {
}