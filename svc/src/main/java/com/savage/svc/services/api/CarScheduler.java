package com.savage.svc.services.api;

import org.springframework.scheduling.annotation.Scheduled;

public interface CarScheduler {
   @Scheduled(fixedDelay = 1000)
   void serviceRequests();
}
