package hu.gigflow.performance.application.port.out;

import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;

import java.util.Optional;
import java.util.List;

public interface PerformanceRepository {

    Optional<Performance> findById(PerformanceId performanceId);

    List<Performance> findAll();

    void save(Performance performance);
}
