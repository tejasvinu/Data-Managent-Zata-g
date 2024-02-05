package org.zatag.dev.datamanagement.Models.Mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MongoFiles {
    @Id
    private String id;
    private String FileName;
    @CreatedDate
    private LocalDateTime Timestamp;

    public MongoFiles(String fileName) {
        FileName = fileName;
    }
}