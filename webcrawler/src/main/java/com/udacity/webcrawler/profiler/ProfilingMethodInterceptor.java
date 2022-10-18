package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;

  Object target;
  private final ProfilingState state;
  public <T> ProfilingMethodInterceptor(Clock clock, T delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.target = delegate;
    this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    boolean isProfiled = method.getAnnotation(Profiled.class) != null;
    Instant start = null;
    if (isProfiled) {
      start = clock.instant();
    }

    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    } finally {
      if (isProfiled) {
        state.record(target.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
  }
}
