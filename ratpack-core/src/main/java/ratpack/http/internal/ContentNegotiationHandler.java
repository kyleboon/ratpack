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

package ratpack.http.internal;

import io.netty.handler.codec.http.HttpHeaders;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContentNegotiationHandler implements Handler {

  private final Map<String, Handler> handlers;

  public ContentNegotiationHandler(Map<String, Handler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public void handle(Context context) throws Exception {
    if (handlers.isEmpty()) {
      context.clientError(406);
      return;
    }

    List<String> types = new ArrayList<>(handlers.keySet());
    String first = types.get(0);
    Collections.reverse(types);
    String winner = first;

    String acceptHeader = context.getRequest().getHeaders().get(HttpHeaders.Names.ACCEPT);
    if (acceptHeader != null && !acceptHeader.isEmpty()) {
      winner = MimeParse.bestMatch(types, acceptHeader);
    }

    if (winner == null || winner.isEmpty()) {
      context.clientError(406);
    } else {
      context.getResponse().contentType(winner);
      Handler handler = handlers.get(winner);
      handler.handle(context);
    }
  }
}
