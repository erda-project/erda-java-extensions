/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.redisson.v3;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liuhaoyang
 * @date 2021/11/1 13:31
 */
public class ChannelFutureWrapper implements ChannelFuture {

    private static ILog logger = LogManager.getLogger(ChannelFutureWrapper.class);

    private final ChannelFuture delegate;
    private final TracerSnapshot tracerSnapshot;

    public ChannelFutureWrapper(ChannelFuture delegate, TracerSnapshot tracerSnapshot) {
        this.delegate = delegate;
        this.tracerSnapshot = tracerSnapshot;
    }

    @Override
    public Channel channel() {
        return delegate.channel();
    }

    @Override
    public boolean isSuccess() {
        return delegate.isSuccess();
    }

    @Override
    public boolean isCancellable() {
        return delegate.isCancellable();
    }

    @Override
    public Throwable cause() {
        return delegate.cause();
    }

    @Override
    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
        return delegate.addListener(genericFutureListener);
    }

    @Override
    public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... genericFutureListeners) {
        return delegate.addListeners(genericFutureListeners);
    }

    @Override
    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
        return delegate.removeListener(genericFutureListener);
    }

    @Override
    public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... genericFutureListeners) {
        return delegate.removeListeners(genericFutureListeners);
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        logger.info("ChannelFutureWrapper sync");
        return delegate.sync();
    }

    @Override
    public ChannelFuture syncUninterruptibly() {
        logger.info("ChannelFutureWrapper syncUninterruptibly");
        return delegate.syncUninterruptibly();
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
        logger.info("ChannelFutureWrapper syncUninterruptibly");
        return delegate.await();
    }

    @Override
    public ChannelFuture awaitUninterruptibly() {
        logger.info("ChannelFutureWrapper awaitUninterruptibly");
        return delegate.awaitUninterruptibly();
    }

    @Override
    public boolean await(long l, TimeUnit timeUnit) throws InterruptedException {
        logger.info("ChannelFutureWrapper await");
        return delegate.await(l, timeUnit);
    }

    @Override
    public boolean await(long l) throws InterruptedException {
        logger.info("ChannelFutureWrapper await");
        return delegate.await(l);
    }

    @Override
    public boolean awaitUninterruptibly(long l, TimeUnit timeUnit) {
        logger.info("ChannelFutureWrapper awaitUninterruptibly");
        return delegate.awaitUninterruptibly(l, timeUnit);
    }

    @Override
    public boolean awaitUninterruptibly(long l) {
        logger.info("ChannelFutureWrapper awaitUninterruptibly");
        return delegate.awaitUninterruptibly(l);
    }

    @Override
    public Void getNow() {
        logger.info("ChannelFutureWrapper getNow");
        return delegate.getNow();
    }

    @Override
    public boolean cancel(boolean b) {
        logger.info("ChannelFutureWrapper cancel");
        return delegate.cancel(b);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        logger.info("ChannelFutureWrapper get");
        return delegate.get();
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("ChannelFutureWrapper get");
        return delegate.get(timeout, unit);
    }

    @Override
    public boolean isVoid() {
        return delegate.isVoid();
    }
}
