/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.func;

import ratpack.util.ExceptionUtils;

/**
 * Factories for different {@link ratpack.func.Action} implementations.
 */
public abstract class Actions {

  private final static Action<Throwable> THROW_EXCEPTION = new Action<Throwable>() {
    @Override
    public void execute(Throwable throwable) throws Exception {
      throw ExceptionUtils.toException(throwable);
    }
  };
  private static final Action<Object> NOOP = new Action<Object>() {
    @Override
    public void execute(Object thing) throws Exception {
    }
  };

  private Actions() {

  }

  /**
   * Returns an action that does precisely nothing.
   *
   * @return an action that does precisely nothing
   */
  public static Action<Object> noop() {
    return NOOP;
  }

  /**
   * Returns an action that does precisely nothing.
   *
   * @return an action that does precisely nothing
   */
  public static Action<Throwable> throwException() {
    return THROW_EXCEPTION;
  }

  /**
   * Returns a new action that executes the given actions in order.
   *
   * @param actions the actions to join into one action
   * @param <T> the type of object the action accepts
   * @return the newly created aggregate action
   */
  @SafeVarargs
  public static <T> Action<T> join(final Action<? super T>... actions) {
    return new Action<T>() {
      @Override
      public void execute(T thing) throws Exception {
        for (Action<? super T> action : actions) {
          action.execute(thing);
        }
      }
    };
  }


  public static <T> Action<Action<? super T>> wrap(final T t) {
    return new Action<Action<? super T>>() {
      @Override
      public void execute(Action<? super T> action) throws Exception {
        action.execute(t);
      }
    };
  }

}
