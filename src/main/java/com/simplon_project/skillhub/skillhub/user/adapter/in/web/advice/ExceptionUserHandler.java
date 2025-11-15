package com.simplon_project.skillhub.skillhub.user.adapter.in.web.advice;

import com.simplon_project.skillhub.skillhub.user.domain.exception.DomainException;
import feign.FeignException;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.zalando.problem.*;
import org.zalando.problem.spring.web.advice.AdviceTrait;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;

@ControllerAdvice
public class ExceptionUserHandler implements ProblemHandling, AdviceTrait {
    private static final String SOURCE_KEY = "sources";
    private final BuildProperties buildProperties;

    public ExceptionUserHandler(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public ProblemBuilder prepare(Throwable throwable, StatusType status, URI type) {
        var problemBuilder = Problem.builder();
        handleThrowable(problemBuilder, throwable, status, type);
        problemBuilder.with(SOURCE_KEY, getSourcesParameter(throwable));

        return problemBuilder;
    }

    @Override
    public ResponseEntity<Problem> create(Throwable throwable, Problem problem, NativeWebRequest request, HttpHeaders headers) {
        var problemBuilder = Problem.builder()
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance())
                .withStatus(problem.getStatus())
                .withTitle(problem.getTitle())
                .withType(problem.getType());

        problem.getParameters().forEach(problemBuilder::with);
        problemBuilder.with(SOURCE_KEY, getSourcesParameter(throwable));
        addErrorMessage(throwable, problem, problemBuilder);
        return ProblemHandling.super.create(throwable, problem, request, headers);
    }

    private String getSourcesParameter(Throwable throwable) {
        return getExistingSources(throwable).map(existingSource -> buildProperties.getName() + " -> " + existingSource)
                .orElse(buildProperties.getName());
    }

    private Optional<String> getExistingSources(Throwable throwable) {
        Optional<String> optionalSources = Optional.empty();
        if (throwable instanceof ThrowableProblem problem) {
            Object sources = problem.getParameters().get(SOURCE_KEY);
            if (sources != null) {
                optionalSources = Optional.of(sources.toString());
            }
        }
        return optionalSources;
    }

    private void addErrorMessage(Throwable throwable, Problem problem, ProblemBuilder problemBuilder) {
        if (throwable instanceof HandlerMethodValidationException handlerMethodValidationException) {
            problemBuilder.with("errorCode", "CONSTRAINT_VIOLATION")
                    .with("errorMessage", handlerMethodValidationException.getDetailMessageArguments());

        } else {
            problemBuilder.with("errorCode", problem.getTitle())
                    .with("errorMessage", problem.getDetail());
        }
    }

    private void handleThrowable(ProblemBuilder problemBuilder, Throwable throwable, StatusType status, URI type) {
        problemBuilder.withType(type)
                .withTitle(status.getReasonPhrase())
                .withStatus(status);
        int statusCode = status.getStatusCode();
        if (statusCode == HttpStatus.BAD_REQUEST.value() || statusCode == HttpStatus.FORBIDDEN.value() || statusCode == HttpStatus.NOT_FOUND.value() || statusCode == HttpStatus.CONFLICT.value()) {
            problemBuilder.withDetail(throwable.getMessage());
        } else if (throwable instanceof FeignException feignException) {
            handleFeignException(feignException, problemBuilder);
        } else if (throwable instanceof CompletionException completionException) {
            handleCompletionException(completionException, problemBuilder);
        }
    }

    private static void handleFeignException(FeignException feignException, ProblemBuilder problembuilder) {
        var zalandoStatus = Status.valueOf(feignException.status());
        problembuilder.withTitle(zalandoStatus.getReasonPhrase());
        problembuilder.withStatus(zalandoStatus);
        problembuilder.withDetail(feignException.getMessage());
    }

    private void handleCompletionException(CompletionException completionException, ProblemBuilder problembuilder) {
        var cause = completionException.getCause();
        if (cause instanceof FeignException feignException) {
            handleFeignException(feignException, problembuilder);
        } else if (cause instanceof ThrowableProblem problem) {
            handleThrowable(problembuilder, problem, Objects.requireNonNull(problem.getStatus()), problem.getType());
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DomainException.class)
    public ResponseEntity<Problem> handleDomainException(
            DomainException ex,
            NativeWebRequest request) {

        Problem problem = Problem.builder()
                .withStatus(ex.getStatus())
                .withTitle(ex.getStatus().getReasonPhrase())
                .withDetail(ex.getMessage())
                .with("errorCode", ex.getClass().getSimpleName())
                .with("errorMessage", ex.getMessage())
                .with("sources", buildProperties.getName())
                .build();

        return create(ex, problem, request);
    }

}
