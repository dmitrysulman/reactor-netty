/*
 * Copyright (c) 2020-2025 VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.transport;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.logging.LogLevel;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.DnsNameResolverChannelStrategy;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.RoundRobinDnsAddressResolverGroup;
import io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpResources;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_CACHE_MAX_TIME_TO_LIVE;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_CACHE_MIN_TIME_TO_LIVE;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_CACHE_NEGATIVE_TIME_TO_LIVE;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_DATAGRAM_CHANNEL_STRATEGY;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_MAX_PAYLOAD_SIZE;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_MAX_QUERIES_PER_RESOLVE;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_NDOTS;
import static reactor.netty.transport.NameResolverProvider.Build.DEFAULT_QUERY_TIMEOUT;

/**
 * This test class verifies {@link NameResolverProvider}.
 *
 * @author Violeta Georgieva
 */
class NameResolverProviderTest {
	private NameResolverProvider.Build builder;

	@BeforeEach
	void setUp() {
		builder = new NameResolverProvider.Build();
	}

	@Test
	void bindAddressSupplier() {
		assertThat(builder.build().bindAddressSupplier()).isNull();

		Supplier<@Nullable SocketAddress> addressSupplier = () -> new InetSocketAddress("localhost", 9527);
		builder.bindAddressSupplier(addressSupplier);
		assertThat(builder.build().bindAddressSupplier()).isEqualTo(addressSupplier);

		addressSupplier = () -> null;
		builder.bindAddressSupplier(addressSupplier);
		assertThat(builder.build().bindAddressSupplier()).isEqualTo(addressSupplier);
		assertThat(builder.build().bindAddressSupplier().get()).isNull();
	}

	@Test
	@SuppressWarnings("NullAway")
	void bindAddressSupplierBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.bindAddressSupplier(null));
	}

	@Test
	void cacheMaxTimeToLive() {
		assertThat(builder.build().cacheMaxTimeToLive()).isEqualTo(DEFAULT_CACHE_MAX_TIME_TO_LIVE);

		Duration cacheMaxTimeToLive = Duration.ofSeconds(5);
		builder.cacheMaxTimeToLive(cacheMaxTimeToLive);
		assertThat(builder.build().cacheMaxTimeToLive()).isEqualTo(cacheMaxTimeToLive);
	}

	@Test
	@SuppressWarnings("NullAway")
	void cacheMaxTimeToLiveBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.cacheMaxTimeToLive(null));

		builder.cacheMaxTimeToLive(Duration.ofSeconds(Long.MAX_VALUE));
		assertThatExceptionOfType(ArithmeticException.class)
				.isThrownBy(() -> builder.build().newNameResolverGroup(TcpResources.get(), LoopResources.DEFAULT_NATIVE));
	}

	@Test
	void cacheMinTimeToLive() {
		assertThat(builder.build().cacheMinTimeToLive()).isEqualTo(DEFAULT_CACHE_MIN_TIME_TO_LIVE);

		Duration cacheMinTimeToLive = Duration.ofSeconds(5);
		builder.cacheMinTimeToLive(cacheMinTimeToLive);
		assertThat(builder.build().cacheMinTimeToLive()).isEqualTo(cacheMinTimeToLive);
	}

	@Test
	@SuppressWarnings("NullAway")
	void cacheMinTimeToLiveBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.cacheMinTimeToLive(null));

		builder.cacheMinTimeToLive(Duration.ofSeconds(Long.MAX_VALUE));
		assertThatExceptionOfType(ArithmeticException.class)
				.isThrownBy(() -> builder.build().newNameResolverGroup(TcpResources.get(), LoopResources.DEFAULT_NATIVE));
	}

	@Test
	void cacheNegativeTimeToLive() {
		assertThat(builder.build().cacheNegativeTimeToLive()).isEqualTo(DEFAULT_CACHE_NEGATIVE_TIME_TO_LIVE);

		Duration cacheNegativeTimeToLive = Duration.ofSeconds(5);
		builder.cacheNegativeTimeToLive(cacheNegativeTimeToLive);
		assertThat(builder.build().cacheNegativeTimeToLive()).isEqualTo(cacheNegativeTimeToLive);
	}

	@Test
	@SuppressWarnings("NullAway")
	void cacheNegativeTimeToLiveBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.cacheNegativeTimeToLive(null));

		builder.cacheNegativeTimeToLive(Duration.ofSeconds(Long.MAX_VALUE));
		assertThatExceptionOfType(ArithmeticException.class)
				.isThrownBy(() -> builder.build().newNameResolverGroup(TcpResources.get(), LoopResources.DEFAULT_NATIVE));
	}

	@Test
	void completeOncePreferredResolved() {
		assertThat(builder.build().isCompleteOncePreferredResolved()).isTrue();

		builder.completeOncePreferredResolved(false);
		assertThat(builder.build().isCompleteOncePreferredResolved()).isFalse();
	}

	@Test
	void datagramChannelStrategy() {
		assertThat(builder.build().datagramChannelStrategy()).isEqualTo(DEFAULT_DATAGRAM_CHANNEL_STRATEGY);

		builder.datagramChannelStrategy(DnsNameResolverChannelStrategy.ChannelPerResolution);
		assertThat(builder.build().datagramChannelStrategy()).isEqualTo(DnsNameResolverChannelStrategy.ChannelPerResolution);
	}

	@Test
	@SuppressWarnings("NullAway")
	void datagramChannelStrategyBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.cacheNegativeTimeToLive(null));
	}

	@Test
	void disableOptionalRecord() {
		assertThat(builder.build().isDisableOptionalRecord()).isFalse();

		builder.disableOptionalRecord(true);
		assertThat(builder.build().isDisableOptionalRecord()).isTrue();
	}

	@Test
	void disableRecursionDesired() {
		assertThat(builder.build().isDisableRecursionDesired()).isFalse();

		builder.disableRecursionDesired(true);
		assertThat(builder.build().isDisableRecursionDesired()).isTrue();
	}

	@Test
	void dnsAddressResolverGroupProvider() {
		assertThat(builder.build().dnsAddressResolverGroupProvider()).isNull();

		Function<DnsNameResolverBuilder, DnsAddressResolverGroup> provider = RoundRobinDnsAddressResolverGroup::new;
		builder.dnsAddressResolverGroupProvider(provider);
		assertThat(builder.build().dnsAddressResolverGroupProvider()).isEqualTo(provider);
	}

	@Test
	void hostsFileEntriesResolver() {
		assertThat(builder.build().hostsFileEntriesResolver()).isNull();

		builder.hostsFileEntriesResolver((inetHost, resolvedAddressTypes) -> null);
		assertThat(builder.build().hostsFileEntriesResolver()).isNotNull();
	}

	@Test
	@SuppressWarnings("NullAway")
	void hostsFileEntriesResolverBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.hostsFileEntriesResolver(null));
	}

	@Test
	void maxPayloadSize() {
		assertThat(builder.build().maxPayloadSize()).isEqualTo(DEFAULT_MAX_PAYLOAD_SIZE);

		builder.maxPayloadSize(1024);
		assertThat(builder.build().maxPayloadSize()).isEqualTo(1024);
	}

	@Test
	void maxPayloadSizeBadValues() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> builder.maxPayloadSize(0))
				.withMessage("maxPayloadSize must be positive");
	}

	@Test
	void maxQueriesPerResolve() {
		assertThat(builder.build().maxQueriesPerResolve()).isEqualTo(DEFAULT_MAX_QUERIES_PER_RESOLVE);

		builder.maxQueriesPerResolve(4);
		assertThat(builder.build().maxQueriesPerResolve()).isEqualTo(4);
	}

	@Test
	void maxQueriesPerResolveBadValues() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> builder.maxQueriesPerResolve(0))
				.withMessage("maxQueriesPerResolve must be positive");
	}

	@Test
	void ndots() {
		assertThat(builder.build().ndots()).isEqualTo(DEFAULT_NDOTS);

		builder.ndots(4);
		assertThat(builder.build().ndots()).isEqualTo(4);
	}

	@Test
	void ndotsBadValues() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> builder.ndots(-2))
				.withMessage("ndots must be greater or equal to -1");
	}

	@Test
	void queryTimeout() {
		assertThat(builder.build().queryTimeout()).isEqualTo(DEFAULT_QUERY_TIMEOUT);

		Duration queryTimeout = Duration.ofSeconds(5);
		builder.queryTimeout(queryTimeout);
		assertThat(builder.build().queryTimeout()).isEqualTo(queryTimeout);
	}

	@Test
	@SuppressWarnings("NullAway")
	void queryTimeoutBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.queryTimeout(null));
	}

	@Test
	void resolveCache() {
		assertThat(builder.build().resolveCache()).isNull();
		TestDnsCache resolveCache = new TestDnsCache();
		builder.resolveCache(resolveCache);
		assertThat(builder.build().resolveCache()).isEqualTo(resolveCache);
	}

	@Test
	@SuppressWarnings("NullAway")
	void resolveCacheBadValues() {
		assertThat(builder.build().resolveCache()).isNull();
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.resolveCache(null));
	}

	@Test
	void resolvedAddressTypes() {
		assertThat(builder.build().resolvedAddressTypes()).isNull();

		builder.resolvedAddressTypes(ResolvedAddressTypes.IPV4_ONLY);
		assertThat(builder.build().resolvedAddressTypes()).isEqualTo(ResolvedAddressTypes.IPV4_ONLY);
	}

	@Test
	@SuppressWarnings("NullAway")
	void resolvedAddressTypesBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.resolvedAddressTypes(null));
	}

	@Test
	void retryTcpOnTimeout() {
		assertThat(builder.build().isRetryTcpOnTimeout()).isFalse();

		builder.retryTcpOnTimeout(true);
		assertThat(builder.build().isRetryTcpOnTimeout()).isTrue();
	}

	@Test
	void roundRobinSelection() {
		assertThat(builder.build().isRoundRobinSelection()).isFalse();

		builder.roundRobinSelection(true);
		assertThat(builder.build().isRoundRobinSelection()).isTrue();
	}

	@Test
	void runOn() {
		assertThat(builder.build().loopResources()).isNull();
		assertThat(builder.build().isPreferNative()).isTrue();

		LoopResources loop = LoopResources.create("runOn");
		builder.runOn(loop, false);
		assertThat(builder.build().loopResources()).isEqualTo(loop);
		assertThat(builder.build().isPreferNative()).isFalse();
	}

	@Test
	@SuppressWarnings("NullAway")
	void runOnBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.runOn(null, false));
	}

	@Test
	void searchDomains() {
		assertThat(builder.build().searchDomains()).isNull();

		List<String> searchDomains = Collections.singletonList("searchDomains");
		builder.searchDomains(searchDomains);
		assertThat(builder.build().searchDomains()).isEqualTo(searchDomains);
	}

	@Test
	@SuppressWarnings("NullAway")
	void searchDomainsBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.searchDomains(null));
	}

	@Test
	@EnabledOnOs(OS.MAC)
	void testMacOsResolver() {
		// MacOS binaries are not available for Netty SNAPSHOT version
		assumeThat(MacOSDnsServerAddressStreamProvider.isAvailable()).isTrue();
		assertThat(DnsServerAddressStreamProviders.platformDefault())
				.isInstanceOf(MacOSDnsServerAddressStreamProvider.class);
	}

	@Test
	@SuppressWarnings("NullAway")
	void traceBadValues() {
		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.trace(null, LogLevel.DEBUG));

		// Deliberately suppress "NullAway" for testing purposes
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> builder.trace("category", null));
	}

	private static class TestDnsCache implements DnsCache {

		@Override
		@SuppressWarnings("NullAway")
		public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, InetAddress address, long originalTtl, EventLoop loop) {
			// Deliberately suppress "NullAway" for testing purposes
			return null;
		}

		@Override
		@SuppressWarnings("NullAway")
		public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop) {
			// Deliberately suppress "NullAway" for testing purposes
			return null;
		}

		@Override
		public void clear() {
		}

		@Override
		public boolean clear(String hostname) {
			return false;
		}

		@Override
		@SuppressWarnings("NullAway")
		public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
			// Deliberately suppress "NullAway" for testing purposes
			return null;
		}
	}

}
