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
package ratpack.launch.internal;

import ratpack.util.internal.PropertiesUtil;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class LaunchConfigData {
  private final ClassLoader classLoader;
  private final Path baseDir;
  private final Properties properties;
  private final Map<String, String> envVars;

  public LaunchConfigData(ClassLoader classLoader, Path baseDir, Properties properties, Map<String, String> envVars) {
    this.classLoader = classLoader;
    this.baseDir = baseDir;
    this.properties = properties;
    this.envVars = envVars;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Path getBaseDir() {
    return baseDir;
  }

  public Properties getProperties() {
    return properties;
  }

  public Map<String, String> getEnvVars() {
    return envVars;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LaunchConfigData that = (LaunchConfigData) o;
    return Objects.equals(this.baseDir, that.baseDir)
      && Objects.equals(this.classLoader, that.classLoader)
      && Objects.equals(this.envVars, that.envVars)
      && PropertiesUtil.flatEquals(this.properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseDir, classLoader, envVars, properties);
  }
}
