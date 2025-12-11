package com.ktai.catcollector.batch;

import com.ktai.catcollector.service.CatCollectorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatCollectorJob  implements CommandLineRunner {

    private final CatCollectorService catCollectorService;

    public CatCollectorJob(CatCollectorService catCollectorService) {
        this.catCollectorService = catCollectorService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[CatCollectorJob] start");
        catCollectorService.crawlAndCollect();
        System.out.println("[CatCollectorJob] end");
    }
}