package hu.gigflow.performance.application.port.in;

import hu.gigflow.performance.domain.model.PerformanceSnapshot;

import java.util.List;

public interface ListPerformancesUseCase {

    List<PerformanceSnapshot> findAll();
}
