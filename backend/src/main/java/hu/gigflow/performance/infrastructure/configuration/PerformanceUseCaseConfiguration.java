package hu.gigflow.performance.infrastructure.configuration;

import hu.gigflow.performance.application.port.in.CreatePerformanceUseCase;
import hu.gigflow.performance.application.port.in.ListPerformancesUseCase;
import hu.gigflow.performance.application.port.in.RespondToInvitationUseCase;
import hu.gigflow.performance.application.port.out.PerformanceIdGenerator;
import hu.gigflow.performance.application.port.out.PerformanceRepository;
import hu.gigflow.performance.application.service.CreatePerformanceService;
import hu.gigflow.performance.application.service.ListPerformancesService;
import hu.gigflow.performance.application.service.RespondToInvitationService;
import hu.gigflow.performance.domain.model.PerformanceId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class PerformanceUseCaseConfiguration {

    @Bean
    Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    PerformanceIdGenerator performanceIdGenerator() {
        return () -> PerformanceId.of(UUID.randomUUID());
    }

    @Bean
    CreatePerformanceUseCase createPerformanceUseCase(
            PerformanceRepository repository,
            PerformanceIdGenerator idGenerator,
            Clock clock,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        CreatePerformanceService service = new CreatePerformanceService(repository, idGenerator, clock);
        PlatformTransactionManager transactionManager = transactionManagerProvider.getIfAvailable();
        if (transactionManager == null) {
            return service;
        }
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.create(command));
    }

    @Bean
    RespondToInvitationUseCase respondToInvitationUseCase(
            PerformanceRepository repository,
            Clock clock,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        RespondToInvitationService service = new RespondToInvitationService(repository, clock);
        PlatformTransactionManager transactionManager = transactionManagerProvider.getIfAvailable();
        if (transactionManager == null) {
            return service;
        }
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return command -> transaction.execute(status -> service.respond(command));
    }

    @Bean
    ListPerformancesUseCase listPerformancesUseCase(PerformanceRepository repository) {
        return new ListPerformancesService(repository);
    }
}
