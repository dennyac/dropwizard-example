package com.example.helloworld.service;

import com.example.helloworld.core.RandomDataGenerator;
import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.TimeUnit;

public class RandomDataGeneratorScheduledService extends AbstractScheduledService implements Managed {

    private final RandomDataGenerator randomDataGenerator;

    public RandomDataGeneratorScheduledService(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    protected void runOneIteration() {
        randomDataGenerator.populateCatData();
        randomDataGenerator.populatePeopleData();
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(1,1, TimeUnit.SECONDS);
    }

    @Override
    public void start() throws Exception {
        this.startAsync();
    }

    @Override
    public void stop() throws Exception {
        this.stopAsync();
    }
}
