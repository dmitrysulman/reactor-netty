/*
 * Copyright (c) 2017-2025 VMware, Inc. or its affiliates, All Rights Reserved.
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

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AddressUtilsTest {

	@Test
	void shouldCreateResolvedNumericIPv4Address() {
		InetSocketAddress socketAddress = AddressUtils.createResolved("127.0.0.1", 8080);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("127.0.0.1");
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("127.0.0.1");
	}

	@Test
	void shouldCreateResolvedNumericIPv6Address() {
		InetSocketAddress socketAddress = AddressUtils.createResolved("::1", 8080);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("0:0:0:0:0:0:0:1");
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("0:0:0:0:0:0:0:1");
	}

	@Test
	void shouldCreateUnresolvedAddressByHostName() {
		InetSocketAddress socketAddress = AddressUtils.createUnresolved("example.com", 80);
		assertThat(socketAddress.isUnresolved()).isTrue();
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("example.com");
	}

	@Test
	void shouldAlwaysCreateResolvedNumberIPAddress() {
		InetSocketAddress socketAddress = AddressUtils.createUnresolved("127.0.0.1", 8080);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("127.0.0.1");
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("127.0.0.1");
	}

	@Test
	void shouldReplaceNumericIPAddressWithResolvedInstance() {
		InetSocketAddress socketAddress = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
		InetSocketAddress replacedAddress = AddressUtils.replaceUnresolvedNumericIp(socketAddress);
		assertThat(replacedAddress).isNotSameAs(socketAddress);
		assertThat(socketAddress.isUnresolved()).isTrue();
		assertThat(replacedAddress.isUnresolved()).isFalse();
		assertThat(replacedAddress.getAddress().getHostAddress()).isEqualTo("127.0.0.1");
		assertThat(replacedAddress.getPort()).isEqualTo(8080);
		assertThat(replacedAddress.getHostString()).isEqualTo("127.0.0.1");
	}

	@Test
	void shouldNotReplaceIfNonNumeric() {
		InetSocketAddress socketAddress = InetSocketAddress.createUnresolved("example.com", 80);
		InetSocketAddress processedAddress = AddressUtils.replaceUnresolvedNumericIp(socketAddress);
		assertThat(processedAddress).isSameAs(socketAddress);
	}

	@Test
	void shouldNotReplaceIfAlreadyResolvedWhenCallingReplaceUnresolvedNumericIp() {
		InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 80);
		InetSocketAddress processedAddress = AddressUtils.replaceUnresolvedNumericIp(socketAddress);
		assertThat(processedAddress).isSameAs(socketAddress);
	}

	@Test
	void shouldResolveUnresolvedAddress() {
		InetSocketAddress socketAddress = InetSocketAddress.createUnresolved("example.com", 80);
		InetSocketAddress processedAddress = AddressUtils.replaceWithResolved(socketAddress);
		assertThat(processedAddress).isNotSameAs(socketAddress);
		assertThat(processedAddress.isUnresolved()).isFalse();
	}

	@Test
	void shouldNotReplaceIfAlreadyResolved() {
		InetSocketAddress socketAddress = new InetSocketAddress("example.com", 80);
		InetSocketAddress processedAddress = AddressUtils.replaceWithResolved(socketAddress);
		assertThat(processedAddress).isSameAs(socketAddress);
	}

	@Test
	void shouldParseAddressForIPv4() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("127.0.0.1", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("127.0.0.1");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("127.0.0.1");
	}

	@Test
	void shouldParseAddressForIPv4WithPort() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("127.0.0.1:8080", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("127.0.0.1");
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("127.0.0.1");
	}

	@Test
	void shouldParseAddressForIPv6() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("[1abc:2abc:3abc::5ABC:6abc]", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldParseAddressForIPv6WithPort() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("[1abc:2abc:3abc::5ABC:6abc]:8080", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldParseAddressForIPv6WithoutBrackets() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("1abc:2abc:3abc:0:0:0:5abc:6abc", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldNotParseAddressForIPv6WithoutBrackets_Strict() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> AddressUtils.parseAddress("1abc:2abc:3abc:0:0:0:5abc:6abc", 80, true))
				.withMessage("Invalid IPv4 address 1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldParseAddressForIPv6WithNotNumericPort() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("[1abc:2abc:3abc::5ABC:6abc]:abc42", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldNotParseAddressForIPv6WithNotNumericPort_Strict() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> AddressUtils.parseAddress("[1abc:2abc:3abc:0:0:0:5abc:6abc]:abc42", 80, true))
				.withMessage("Failed to parse a port from [1abc:2abc:3abc:0:0:0:5abc:6abc]:abc42");
	}

	@Test
	void shouldParseAddressForIPv6WithoutPort() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("[1abc:2abc:3abc::5ABC:6abc]:", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldParseAddressForIPv6WithoutPort_Strict() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("[1abc:2abc:3abc::5ABC:6abc]:", 80);
		assertThat(socketAddress.isUnresolved()).isFalse();
		assertThat(socketAddress.getAddress().getHostAddress()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("1abc:2abc:3abc:0:0:0:5abc:6abc");
	}

	@Test
	void shouldParseAddressForHostName() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("example.com", 80);
		assertThat(socketAddress.isUnresolved()).isTrue();
		assertThat(socketAddress.getPort()).isEqualTo(80);
		assertThat(socketAddress.getHostString()).isEqualTo("example.com");
	}

	@Test
	void shouldParseAddressForHostNameWithPort() {
		InetSocketAddress socketAddress = AddressUtils.parseAddress("example.com:8080", 80);
		assertThat(socketAddress.isUnresolved()).isTrue();
		assertThat(socketAddress.getPort()).isEqualTo(8080);
		assertThat(socketAddress.getHostString()).isEqualTo("example.com");
	}

	@Test
	@SuppressWarnings("NullAway")
	void createInetSocketAddressBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.createInetSocketAddress(null, 0, true))
				.withMessage("hostname");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> AddressUtils.createInetSocketAddress("hostname", -1, true))
				.withMessage("port out of range:-1");
	}

	@Test
	@SuppressWarnings("NullAway")
	void createResolvedBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.createResolved(null, 0))
				.withMessage("hostname");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> AddressUtils.createResolved("hostname", -1))
				.withMessage("port out of range:-1");
	}

	@Test
	@SuppressWarnings("NullAway")
	void createUnresolvedBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.createUnresolved(null, 0))
				.withMessage("hostname");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> AddressUtils.createUnresolved("hostname", -1))
				.withMessage("port out of range:-1");
	}

	@Test
	@SuppressWarnings("NullAway")
	void replaceUnresolvedNumericIpBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.replaceUnresolvedNumericIp(null))
				.withMessage("inetSocketAddress");
	}

	@Test
	@SuppressWarnings("NullAway")
	void replaceWithResolvedBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.replaceWithResolved(null))
				.withMessage("inetSocketAddress");
	}

	@Test
	@SuppressWarnings("NullAway")
	void updateHostBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.updateHost(null, null))
				.withMessage("hostname");
	}

	@Test
	void updatePortBadValues() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> AddressUtils.updatePort(null, -1))
				.withMessage("port out of range:-1");
	}

	@Test
	@SuppressWarnings("NullAway")
	void parseAddressBadValues() {
		assertThatExceptionOfType(NullPointerException.class)
				// Deliberately suppress "NullAway" for testing purposes
				.isThrownBy(() -> AddressUtils.parseAddress(null, 0))
				.withMessage("address");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> AddressUtils.parseAddress("address", -1))
				.withMessage("port out of range:-1");
	}
}