/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.sleuth.trace.intercept.web.client;

import static org.springframework.cloud.sleuth.trace.Trace.SPAN_ID_NAME;
import static org.springframework.cloud.sleuth.trace.Trace.TRACE_ID_NAME;

import java.io.IOException;

import org.springframework.cloud.sleuth.trace.SpanHolder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Interceptor that verifies whether the trance and span id has been set on the
 * request and sets them if one or both of them are missing.
 *
 * @see org.springframework.web.client.RestTemplate
 * @see org.springframework.cloud.sleuth.trace.Trace
 *
 * @author Marcin Grzejszczak, 4financeIT
 * @author Spencer Gibb
 */
public class TraceRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		if (!request.getHeaders().containsKey(SPAN_ID_NAME)) {
			request.getHeaders().add(SPAN_ID_NAME,
					SpanHolder.getCurrentSpan().getSpanId());
		}
		if (!request.getHeaders().containsKey(TRACE_ID_NAME)) {
			request.getHeaders().add(TRACE_ID_NAME,
					SpanHolder.getCurrentSpan().getSpanId());
		}
		return execution.execute(request, body);
	}

}
