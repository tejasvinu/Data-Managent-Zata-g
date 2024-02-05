package org.zatag.dev.datamanagement.Models.Mongo;

// MongoDBLink.java
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "mongo_links")
public class MongoDBLink {
    @Id
    private String id;

    private String mongoUri;
    private String dbName;
    private String collectionName;

    // Getters and setters
}
