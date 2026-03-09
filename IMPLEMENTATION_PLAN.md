# Java-Mediatr Implementation Plan

## Reference baseline

This project is modeled after the public feature set documented in the official `.NET` MediatR repository:

- `ISender`, `IPublisher`, `IMediator`
- `IRequest<TResponse>` and void-style request handling
- `INotification`
- `IStreamRequest<TResponse>`
- `IPipelineBehavior<TRequest, TResponse>`
- `IStreamPipelineBehavior<TRequest, TResponse>`
- Request pre/post processors
- Request exception actions and exception handlers
- Pluggable notification publishers

## Java mapping

1. Contracts
   - Define request, notification, stream, handler, and behavior interfaces.
2. Runtime
   - Build a mediator implementation with explicit registration instead of container scanning.
3. Pipelines
   - Compose behaviors around request and stream handlers.
4. Reliability
   - Support exception observation and recovery hooks.
5. Delivery
   - Provide tests for the major execution paths.

## Current status

- Project skeleton: complete
- Core mediator contracts: complete
- Request and notification dispatch: complete
- Stream dispatch: complete
- Pipeline/processors/exceptions: complete
- Tests: complete

## Next recommended expansion

- Add Spring Boot autoconfiguration for classpath scanning and bean-based registration.
- Publish artifacts to Maven Central.
- Add benchmarks comparing sequential vs parallel notification publishing.
