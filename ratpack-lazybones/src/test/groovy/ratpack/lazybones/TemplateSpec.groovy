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

package ratpack.lazybones

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients
import ratpack.lazybones.fixture.LazybonesTemplateRatpackApp
import ratpack.lazybones.fixture.TestConfig
import spock.lang.AutoCleanup
import spock.lang.Specification

class TemplateSpec extends Specification {

  TestConfig testConfig = new TestConfig()

  @Rule
  TemporaryFolder projectDirectoryProvider = new TemporaryFolder()

  @AutoCleanup
  LazybonesTemplateRatpackApp app = new LazybonesTemplateRatpackApp(projectDirectoryProvider, testConfig.templateDirectory, testConfig.localRepoUrl)

  @AutoCleanup
  @Delegate
  TestHttpClient client = TestHttpClients.testHttpClient(app)

  def "can run template"() {
    expect:
    text.contains("This is the main page for your Ratpack app")
  }
}
