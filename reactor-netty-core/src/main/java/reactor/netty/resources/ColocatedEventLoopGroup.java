/*
 * Copyright (c) 2011-2025 VMware, Inc. or its affiliates, All Rights Reserved.
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
package reactor.netty.resources;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.ScheduledFuture;
import org.jspecify.annotations.Nullable;

/**
 * Reuse local event loop if already working inside one.
 */
final class ColocatedEventLoopGroup implements EventLoopGroup, Supplier<EventLoopGroup> {

	final EventLoopGroup eventLoopGroup;
	final FastThreadLocal<@Nullable EventLoop> localLoop = new FastThreadLocal<>();

	@SuppressWarnings("FutureReturnValueIgnored")
	ColocatedEventLoopGroup(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
		for (EventExecutor ex : eventLoopGroup) {
			if (ex instanceof EventLoop) {
				EventLoop eventLoop = (EventLoop) ex;
				if (eventLoop.inEventLoop()) {
					if (!localLoop.isSet()) {
						localLoop.set(eventLoop);
					}
				}
				else {
					//"FutureReturnValueIgnored" this is deliberate
					eventLoop.submit(() -> {
						if (!localLoop.isSet()) {
							localLoop.set(eventLoop);
						}
					});
				}
			}
		}
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return eventLoopGroup.awaitTermination(timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		next().execute(command);
	}

	@Override
	public EventLoopGroup get() {
		return eventLoopGroup;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return next().invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return next().invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return next().invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return next().invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		return eventLoopGroup.isShutdown();
	}

	@Override
	public boolean isShuttingDown() {
		return eventLoopGroup.isShuttingDown();
	}

	@Override
	public boolean isTerminated() {
		return eventLoopGroup.isTerminated();
	}

	@Override
	public Iterator<EventExecutor> iterator() {
		return eventLoopGroup.iterator();
	}

	@Override
	public EventLoop next() {
		EventLoop loop = nextInternal();
		return loop != null ? loop : eventLoopGroup.next();
	}

	@Override
	public ChannelFuture register(Channel channel) {
		return next().register(channel);
	}

	@Deprecated
	@Override
	public ChannelFuture register(Channel channel, ChannelPromise promise) {
		return next().register(channel, promise);
	}

	@Override
	public ChannelFuture register(ChannelPromise promise) {
		return next().register(promise);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return next().schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return next().schedule(command, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return next().scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return next().scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	@Deprecated
	@Override
	@SuppressWarnings({"FutureReturnValueIgnored", "InlineMeSuggester"})
	public void shutdown() {
		//"FutureReturnValueIgnored" this is deliberate
		shutdownGracefully();
	}

	@Override
	public io.netty.util.concurrent.Future<?> shutdownGracefully() {
		clean();
		return eventLoopGroup.shutdownGracefully();
	}

	@Override
	public io.netty.util.concurrent.Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
		clean();
		return eventLoopGroup.shutdownGracefully(quietPeriod, timeout, unit);
	}

	@Override
	@Deprecated
	public List<Runnable> shutdownNow() {
		clean();
		return eventLoopGroup.shutdownNow();
	}

	@Override
	public <T> io.netty.util.concurrent.Future<T> submit(Callable<T> task) {
		return next().submit(task);
	}

	@Override
	public io.netty.util.concurrent.Future<?> submit(Runnable task) {
		return next().submit(task);
	}

	@Override
	public <T> io.netty.util.concurrent.Future<T> submit(Runnable task, T result) {
		return next().submit(task, result);
	}

	@Override
	public io.netty.util.concurrent.Future<?> terminationFuture() {
		return eventLoopGroup.terminationFuture();
	}

	void clean() {
		for (EventExecutor ex : eventLoopGroup) {
			ex.execute(() -> localLoop.set(null));
		}
	}

	@Nullable EventLoop nextInternal() {
		return localLoop.isSet() ? localLoop.get() : null;
	}
}
