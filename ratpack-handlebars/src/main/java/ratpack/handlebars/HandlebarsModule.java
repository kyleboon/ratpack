/*
 * Copyright 2013 the original author or authors.
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

package ratpack.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ratpack.file.FileSystemBinding;
import ratpack.func.Action;
import ratpack.guice.internal.GuiceUtil;
import ratpack.handlebars.internal.FileSystemBindingTemplateLoader;
import ratpack.handlebars.internal.HandlebarsTemplateRenderer;
import ratpack.handlebars.internal.RatpackTemplateCache;
import ratpack.handlebars.internal.TemplateKey;
import ratpack.launch.LaunchConfig;

/**
 * An extension module that provides support for Handlebars.java templating engine.
 * <p>
 * To use it one has to register the module and then render {@link ratpack.handlebars.Template} instances.
 * Instances of {@code Template} can be created using one of the
 * {@link ratpack.handlebars.Template#handlebarsTemplate(java.util.Map, String, String)}
 * static methods.
 * </p>
 * <p>
 * By default templates are looked up in the {@code handlebars} directory of the application root with a {@code .hbs} suffix.
 * So {@code handlebarsTemplate("my/template/path")} maps to {@code handlebars/my/template/path.hbs} in the application root directory.
 * This can be configured using {@link #setTemplatesPath(String)} and {@link #setTemplatesSuffix(String)} as well as
 * {@code other.handlebars.templatesPath} and {@code other.handlebars.templatesSuffix} configuration properties.
 * </p>
 * <p>
 * Response content type can be manually specified, i.e. {@code handlebarsTemplate("template", model, "text/html")} or can
 * be detected based on the template extension. Mapping between file extensions and content types is performed using
 * {@link ratpack.file.MimeTypes} contextual object so content type for {@code handlebarsTemplate("template.html")}
 * would be {@code text/html} by default.
 * </p>
 * <p>Custom handlebars helpers can be registered by binding instances of {@link ratpack.handlebars.NamedHelper}.</p>
 * <p>
 * Example usage: (Java DSL)
 * </p>
 * <pre class="tested">
 * import ratpack.handling.*;
 * import ratpack.guice.*;
 * import ratpack.func.Action;
 * import ratpack.launch.*;
 * import ratpack.handlebars.HandlebarsModule;
 * import static ratpack.handlebars.Template.handlebarsTemplate;
 *
 * class MyHandler implements Handler {
 *   void handle(final Context context) {
 *     context.render(handlebarsTemplate("my/template/path", key: "it works!"));
 *   }
 * }
 *
 * class ModuleBootstrap implements Action&lt;BindingsSpec&gt; {
 *   public void execute(BindingsSpec bindings) {
 *     bindings.add(new HandlebarsModule());
 *   }
 * }
 *
 * LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("appRoot"))
 *   .build(new HandlerFactory() {
 *     public Handler create(LaunchConfig launchConfig) {
 *       return Guice.handler(launchConfig, new ModuleBootstrap(), new Action&lt;Chain&gt;() {
 *         public void execute(Chain chain) {
 *           chain.handler(chain.getRegistry().get(MyHandler.class));
 *         }
 *       });
 *     }
 *   });
 * </pre>
 * <p>
 * Example usage: (Groovy DSL)
 * </p>
 * <pre class="groovy-ratpack-dsl">
 * import ratpack.handlebars.HandlebarsModule
 * import static ratpack.handlebars.Template.handlebarsTemplate
 * import static ratpack.groovy.Groovy.ratpack
 *
 * ratpack {
 *   bindings {
 *     add new HandlebarsModule()
 *   }
 *   handlers {
 *     get {
 *       render handlebarsTemplate('my/template/path', key: 'it works!')
 *     }
 *   }
 * }
 * </pre>
 *
 * @see <a href="http://jknack.github.io/handlebars.java/" target="_blank">Handlebars.java</a>
 */
public class HandlebarsModule extends AbstractModule {

  private String templatesPath;

  private String templatesSuffix;

  private Integer cacheSize;

  private Boolean reloadable;

  public String getTemplatesPath() {
    return templatesPath;
  }

  public void setTemplatesPath(String templatesPath) {
    this.templatesPath = templatesPath;
  }

  public String getTemplatesSuffix() {
    return templatesSuffix;
  }

  public void setTemplatesSuffix(String templatesSuffix) {
    this.templatesSuffix = templatesSuffix;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public boolean isReloadable() {
    return reloadable;
  }

  public void setReloadable(boolean reloadable) {
    this.reloadable = reloadable;
  }

  @Override
  protected void configure() {
    bind(HandlebarsTemplateRenderer.class).in(Singleton.class);
  }

  @SuppressWarnings("UnusedDeclaration")
  @Provides
  @Singleton
  TemplateLoader provideTemplateLoader(LaunchConfig launchConfig) {
    String path = templatesPath == null ? launchConfig.getOther("handlebars.templatesPath", "handlebars") : templatesPath;
    String suffix = templatesSuffix == null ? launchConfig.getOther("handlebars.templatesSuffix", ".hbs") : templatesSuffix;

    FileSystemBinding templatesBinding = launchConfig.getBaseDir().binding(path);
    return new FileSystemBindingTemplateLoader(templatesBinding, suffix);
  }

  @SuppressWarnings("UnusedDeclaration")
  @Provides
  TemplateCache provideTemplateCache(LaunchConfig launchConfig) {
    boolean reloadable = this.reloadable == null ? launchConfig.isDevelopment() : this.reloadable;
    int cacheSize = this.cacheSize == null ? Integer.parseInt(launchConfig.getOther("handlebars.cacheSize", "100")) : this.cacheSize;
    return new RatpackTemplateCache(reloadable, CacheBuilder.newBuilder().maximumSize(cacheSize).<TemplateKey, com.github.jknack.handlebars.Template>build());
  }

  @SuppressWarnings("UnusedDeclaration")
  @Provides
  @Singleton
  Handlebars provideHandlebars(Injector injector, TemplateLoader templateLoader, TemplateCache templateCache) {

    final Handlebars handlebars = new Handlebars().with(templateLoader);
    handlebars.with(templateCache);

    TypeToken<NamedHelper<?>> type = new TypeToken<NamedHelper<?>>() {
      private static final long serialVersionUID = 0;
    };

    GuiceUtil.eachOfType(injector, type, new Action<NamedHelper<?>>() {
      public void execute(NamedHelper<?> helper) {
        handlebars.registerHelper(helper.getName(), helper);
      }
    });

    return handlebars;
  }
}
