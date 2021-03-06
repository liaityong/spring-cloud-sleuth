/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.instrument.hystrix;

import java.util.concurrent.atomic.AtomicReference;

import brave.Span;
import brave.Tracing;
import brave.sampler.Sampler;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.sleuth.instrument.DefaultTestAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { HystrixAnnotationsIntegrationTests.TestConfig.class })
@DirtiesContext
public class HystrixAnnotationsIntegrationTests {

	@Autowired
	HystrixCommandInvocationSpanCatcher catcher;

	@Autowired
	Tracing tracer;

	@BeforeClass
	@AfterClass
	public static void reset() {
		HystrixPlugins.reset();
	}

	@Test
	public void should_create_new_span_with_thread_name_when_executed_a_hystrix_command_annotated_method() {
		whenHystrixCommandAnnotatedMethodGetsExecuted();

		thenSpanInHystrixThreadIsCreated();
	}

	private void whenHystrixCommandAnnotatedMethodGetsExecuted() {
		this.catcher.invokeLogicWrappedInHystrixCommand();
	}

	private void thenSpanInHystrixThreadIsCreated() {
		Awaitility.await().atMost(5, SECONDS).untilAsserted(() -> {
			then(HystrixAnnotationsIntegrationTests.this.catcher.getSpan()).isNotNull();
		});
	}

	@DefaultTestAutoConfiguration
	@EnableHystrix
	@Configuration
	static class TestConfig {

		@Bean
		HystrixCommandInvocationSpanCatcher spanCatcher(Tracing tracing) {
			return new HystrixCommandInvocationSpanCatcher(tracing);
		}

		@Bean
		Sampler sampler() {
			return Sampler.ALWAYS_SAMPLE;
		}

	}

	public static class HystrixCommandInvocationSpanCatcher {

		private final Tracing tracing;

		AtomicReference<Span> spanCaughtFromHystrixThread;

		public HystrixCommandInvocationSpanCatcher(Tracing tracing) {
			this.tracing = tracing;
		}

		@HystrixCommand
		public void invokeLogicWrappedInHystrixCommand() {
			System.out.println("FOOO");
			this.spanCaughtFromHystrixThread = new AtomicReference<>(
					this.tracing.tracer().currentSpan());
			System.out.println("aksdhkasd: " + this.spanCaughtFromHystrixThread);
		}

		public Long getTraceId() {
			if (this.spanCaughtFromHystrixThread == null
					|| this.spanCaughtFromHystrixThread.get() == null) {
				return null;
			}
			return this.spanCaughtFromHystrixThread.get().context().traceId();
		}

		public Span getSpan() {
			return this.spanCaughtFromHystrixThread.get();
		}

	}

}
