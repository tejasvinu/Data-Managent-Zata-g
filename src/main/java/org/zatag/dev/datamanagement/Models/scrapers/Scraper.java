package org.zatag.dev.datamanagement.Models.scrapers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scraper {

    @Id
    private String id;

    private String FileName;

    public Scraper(String fileName) {
        FileName = fileName;
    }
}
