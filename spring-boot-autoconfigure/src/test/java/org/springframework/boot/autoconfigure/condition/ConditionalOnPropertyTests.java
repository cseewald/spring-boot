/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.autoconfigure.condition;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

/**
 * Tests for {@link ConditionalOnProperty}.
 *
 * @author Maciej Walkowiak
 * @author Stephane Nicoll
 * @author Phillip Webb
 */
public class ConditionalOnPropertyTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private AnnotationConfigApplicationContext context;

	@After
	public void tearDown() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void allPropertiesAreDefined() {
		load(MultiplePropertiesRequiredConfiguration.class, "property1=value1",
				"property2=value2");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void notAllPropertiesAreDefined() {
		load(MultiplePropertiesRequiredConfiguration.class, "property1=value1");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void propertyValueEqualsFalse() {
		load(MultiplePropertiesRequiredConfiguration.class, "property1=false",
				"property2=value2");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void propertyValueEqualsFALSE() {
		load(MultiplePropertiesRequiredConfiguration.class, "property1=FALSE",
				"property2=value2");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void relaxedName() {
		load(RelaxedPropertiesRequiredConfiguration.class,
				"spring.theRelaxedProperty=value1");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void prefixWithoutPeriod() throws Exception {
		load(RelaxedPropertiesRequiredConfigurationWithShortPrefix.class,
				"spring.property=value1");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void nonRelaxedName() throws Exception {
		load(NonRelaxedPropertiesRequiredConfiguration.class, "theRelaxedProperty=value1");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	// Enabled by default
	public void enabledIfNotConfiguredOtherwise() {
		load(EnabledIfNotConfiguredOtherwiseConfig.class);
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void enabledIfNotConfiguredOtherwiseWithConfig() {
		load(EnabledIfNotConfiguredOtherwiseConfig.class, "simple.myProperty:false");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void enabledIfNotConfiguredOtherwiseWithConfigDifferentCase() {
		load(EnabledIfNotConfiguredOtherwiseConfig.class, "simple.my-property:FALSE");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	// Disabled by default
	public void disableIfNotConfiguredOtherwise() {
		load(DisabledIfNotConfiguredOtherwiseConfig.class);
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void disableIfNotConfiguredOtherwiseWithConfig() {
		load(DisabledIfNotConfiguredOtherwiseConfig.class, "simple.myProperty:true");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void disableIfNotConfiguredOtherwiseWithConfigDifferentCase() {
		load(DisabledIfNotConfiguredOtherwiseConfig.class, "simple.myproperty:TrUe");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void simpleValueIsSet() {
		load(SimpleValueConfig.class, "simple.myProperty:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void caseInsensitive() {
		load(SimpleValueConfig.class, "simple.myProperty:BaR");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void defaultValueIsSet() {
		load(DefaultValueConfig.class, "simple.myProperty:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void defaultValueIsNotSet() {
		load(DefaultValueConfig.class);
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void defaultValueIsSetDifferentValue() {
		load(DefaultValueConfig.class, "simple.myProperty:another");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void prefix() {
		load(PrefixValueConfig.class, "simple.myProperty:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void relaxedEnabledByDefault() {
		load(PrefixValueConfig.class, "simple.myProperty:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void strictNameMatch() {
		load(StrictNameConfig.class, "simple.my-property:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void strictNameNoMatch() {
		load(StrictNameConfig.class, "simple.myProperty:bar");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void multiValuesAllSet() {
		load(MultiValuesConfig.class, "simple.my-property:bar",
				"simple.my-another-property:bar");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void multiValuesOnlyOneSet() {
		load(MultiValuesConfig.class, "simple.my-property:bar");
		assertFalse(this.context.containsBean("foo"));
	}

	@Test
	public void usingValueAttribute() throws Exception {
		load(ValueAttribute.class, "some.property");
		assertTrue(this.context.containsBean("foo"));
	}

	@Test
	public void nameOrValueMustBeSpecified() throws Exception {
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectCause(hasMessage(containsString("The name or "
				+ "value attribute of @ConditionalOnProperty must be specified")));
		load(NoNameOrValueAttribute.class, "some.property");
	}

	@Test
	public void nameAndValueMustNotBeSpecified() throws Exception {
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectCause(hasMessage(containsString("The name and "
				+ "value attributes of @ConditionalOnProperty are exclusive")));
		load(NameAndValueAttribute.class, "some.property");
	}

	private void load(Class<?> config, String... environment) {
		this.context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(this.context, environment);
		this.context.register(config);
		this.context.refresh();
	}

	@Configuration
	@ConditionalOnProperty(name = { "property1", "property2" })
	protected static class MultiplePropertiesRequiredConfiguration {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring.", name = "the-relaxed-property")
	protected static class RelaxedPropertiesRequiredConfiguration {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring", name = "property")
	protected static class RelaxedPropertiesRequiredConfigurationWithShortPrefix {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(name = "the-relaxed-property", relaxedNames = false)
	protected static class NonRelaxedPropertiesRequiredConfiguration {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	// i.e ${simple.myProperty:true}
	@ConditionalOnProperty(prefix = "simple", name = "my-property", havingValue = "true", matchIfMissing = true)
	static class EnabledIfNotConfiguredOtherwiseConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	// i.e ${simple.myProperty:false}
	@ConditionalOnProperty(prefix = "simple", name = "my-property", havingValue = "true", matchIfMissing = false)
	static class DisabledIfNotConfiguredOtherwiseConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "simple", name = "my-property", havingValue = "bar")
	static class SimpleValueConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(name = "simple.myProperty", havingValue = "bar", matchIfMissing = true)
	static class DefaultValueConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "simple", name = "my-property", havingValue = "bar")
	static class PrefixValueConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "simple", name = "my-property", havingValue = "bar", relaxedNames = false)
	static class StrictNameConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(prefix = "simple", name = { "my-property",
			"my-another-property" }, havingValue = "bar")
	static class MultiValuesConfig {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty("some.property")
	protected static class ValueAttribute {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty
	protected static class NoNameOrValueAttribute {

		@Bean
		public String foo() {
			return "foo";
		}

	}

	@Configuration
	@ConditionalOnProperty(value = "x", name = "y")
	protected static class NameAndValueAttribute {

		@Bean
		public String foo() {
			return "foo";
		}

	}
}
