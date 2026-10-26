package hu.gigflow.performance.application.port.in;

import hu.gigflow.performance.application.command.CreatePerformanceCommand;
import hu.gigflow.performance.domain.model.PerformanceSnapshot;

public interface CreatePerformanceUseCase {

    PerformanceSnapshot create(CreatePerformanceCommand command);
}
