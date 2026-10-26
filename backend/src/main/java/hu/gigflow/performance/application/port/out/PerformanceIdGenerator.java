package hu.gigflow.performance.application.port.out;

import hu.gigflow.performance.domain.model.PerformanceId;

public interface PerformanceIdGenerator {

    PerformanceId nextId();
}
