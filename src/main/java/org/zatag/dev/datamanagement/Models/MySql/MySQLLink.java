package org.zatag.dev.datamanagement.Models.MySql;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@ToString
public class MySQLLink {
    @Id
    private String id;
    private String mysqlUrl;
    private String username;
    private String password;
    private String dbName;
    private String tableName;

}
