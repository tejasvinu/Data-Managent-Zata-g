package org.zatag.dev.datamanagement.Models.Mongo;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class MongoRequest {
    private String mongoUri;
    private String dbName;
    private String collectionName;
}
