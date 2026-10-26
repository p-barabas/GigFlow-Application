package hu.gigflow.performance.application.service;

import hu.gigflow.performance.application.port.in.ListPerformancesUseCase;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.util.List;
import java.util.Objects;

public final class ListPerformancesService implements ListPerformancesUseCase {

    private final PerformanceRepository repository;

    public ListPerformancesService(PerformanceRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<PerformanceSnapshot> findAll() {
        return repository.findAll().stream()
                .map(performance -> performance.snapshot())
                .toList();
    }
}
