package dev.miniredis;

import java.time.*;

public class MutableClock extends Clock {
    private Instant currentInstant;
    private ZoneId zone;

    public MutableClock(Instant currentInstant, ZoneId zone){
        this.zone = zone;
        this.currentInstant = currentInstant;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Instant instant(){
        return currentInstant;
    }

    @Override
    public Clock withZone(ZoneId zone){
        return new MutableClock(currentInstant, zone);
    }

    public void advanceSeconds(long seconds){
        Instant futureInstant = currentInstant.plusSeconds(seconds);
        this.currentInstant = futureInstant;

    }
}
