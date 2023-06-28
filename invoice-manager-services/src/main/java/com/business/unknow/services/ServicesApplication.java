package com.business.unknow.services;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ServicesApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServicesApplication.class, args);
    int mb = 1024 * 1024;
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    long xmx = memoryBean.getHeapMemoryUsage().getMax() / mb;
    long xms = memoryBean.getHeapMemoryUsage().getInit() / mb;
    log.info("Initial Memory (xms) : {} mb", xms);
    log.info("Max Memory (xmx) : {} mb", xmx);
  }
}
