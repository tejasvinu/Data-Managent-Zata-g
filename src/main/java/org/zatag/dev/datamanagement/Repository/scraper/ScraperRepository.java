package org.zatag.dev.datamanagement.Repository.scraper;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.zatag.dev.datamanagement.Models.scrapers.Scraper;

public interface ScraperRepository extends MongoRepository<Scraper, String> {
}
