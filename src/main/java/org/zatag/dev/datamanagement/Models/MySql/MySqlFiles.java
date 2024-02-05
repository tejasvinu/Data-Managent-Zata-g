package org.zatag.dev.datamanagement.Models.MySql;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MySqlFiles {
    @Id
    private String id;
    private String FileName;
    @CreatedDate
    private LocalDateTime Timestamp;

    public MySqlFiles(String fileName) {
        FileName = fileName;
    }
}
