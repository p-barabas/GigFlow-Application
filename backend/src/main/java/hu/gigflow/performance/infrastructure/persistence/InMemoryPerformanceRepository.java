package hu.gigflow.performance.infrastructure.persistence;

import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.Performance;
import hu.gigflow.performance.domain.model.PerformanceId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Comparator;

public final class InMemoryPerformanceRepository implements PerformanceRepository {

    private final Map<PerformanceId, Performance> performances = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Performance> findById(PerformanceId performanceId) {
        return Optional.ofNullable(performances.get(performanceId));
    }

    @Override
    public synchronized List<Performance> findAll() {
        return performances.values().stream()
                .sorted(Comparator.comparing(performance -> performance.snapshot().startsAt()))
                .toList();
    }

    @Override
    public synchronized void save(Performance performance) {
        PerformanceId id = performance.snapshot().id();
        performances.put(id, performance);
    }
}
