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

package ratpack.test.exec

import ratpack.exec.ExecControl
import ratpack.exec.ExecutionException
import ratpack.exec.Promise
import ratpack.func.Action
import ratpack.test.UnitTest
import spock.lang.AutoCleanup
import spock.lang.Specification

class ExecHarnessSpec extends Specification {

  @AutoCleanup
  def harness = UnitTest.execHarness()
  private AsyncService service = new AsyncService(harness.control, new AsyncApi())

  static class AsyncApi {
    public <T> void  returnAsync(T thing, Action<? super T> callback) {
      Thread.start {
        callback.execute(thing)
      }
    }
  }

  static class AsyncService {

    private final ExecControl execControl
    private final AsyncApi api

    AsyncService(ExecControl execControl, AsyncApi api) {
      this.execControl = execControl
      this.api = api
    }

    public Promise<Void> fail() {
      execControl.promise { it.error(new RuntimeException("!!!")) }
    }

    public <T> Promise<T> promise(T value) {
      execControl.promise { f ->
        api.returnAsync(value) {
          f.success(it)
        }
      }
    }
  }


  def "can test async service"() {
    when:
    def value = harness.execute {
      service.promise("foo")
    }

    then:
    value == "foo"
  }

  def "exception thrown by execution is rethrown"() {
    when:
    harness.execute {
      throw new RuntimeException("!!!")
    }

    then:
    def e = thrown(RuntimeException)
    e.message == "!!!"
  }

  def "execution must return promise"() {
    /*
    This is only a problem when using dynamic Groovy, as a static compiler wouldn't let you write this
     */
    when:
    harness.execute {
      1
    }

    then:
    thrown ClassCastException
  }

  def "null promise returns null value"() {
    when:
    def value = harness.execute {
      null
    }

    then:
    value == null
  }

  def "failed promise causes exception to be thrown"() {
    when:
    harness.execute {
      service.fail()
    }

    then:
    def e = thrown RuntimeException
    e.message == "!!!"
  }

  def "fails when control is used not on a managed thread"() {
    when:
    service.promise("foo").then { println "it" }

    then:
    thrown ExecutionException
  }

}
