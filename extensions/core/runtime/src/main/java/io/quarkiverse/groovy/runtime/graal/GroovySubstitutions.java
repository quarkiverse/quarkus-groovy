/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.groovy.runtime.graal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MutableCallSite;
import java.util.function.BiFunction;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.memoize.MemoizeCache;
import org.codehaus.groovy.vmplugin.v8.CacheableCallSite;
import org.codehaus.groovy.vmplugin.v8.IndyInterface;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

final class GroovySubstitutions {
}

@TargetClass(className = "org.codehaus.groovy.vmplugin.v8.MethodHandleWrapper")
final class SubstituteMethodHandleWrapper {

    @Alias
    public boolean isCanSetTarget() {
        return false;
    }

    @Alias
    public MethodHandle getCachedMethodHandle() {
        return null;
    }
}

@TargetClass(className = "org.codehaus.groovy.vmplugin.v8.IndyInterface$FallbackSupplier")
final class SubstituteIndyFallbackSupplier {

    @Alias
    SubstituteIndyFallbackSupplier(MutableCallSite callSite, Class<?> sender, String methodName, int callID,
            Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments) {

    }

    @Alias
    SubstituteMethodHandleWrapper get() {
        return null;
    }
}

@TargetClass(CacheableCallSite.class)
final class SubstituteCacheableCallSite {

    @Alias
    public SubstituteMethodHandleWrapper getAndPut(String className,
            MemoizeCache.ValueProvider<? super String, ? extends SubstituteMethodHandleWrapper> valueProvider) {
        return null;
    }

    @Alias
    public SubstituteMethodHandleWrapper put(String name, SubstituteMethodHandleWrapper mhw) {
        return null;
    }
}

@TargetClass(IndyInterface.class)
final class SubstituteIndyInterface {

    @Alias
    private static SubstituteMethodHandleWrapper NULL_METHOD_HANDLE_WRAPPER;

    @Substitute
    protected static void invalidateSwitchPoints() {
        throw new UnsupportedOperationException("invalidateSwitchPoints is not supported");
    }

    @Alias
    private static boolean bypassCache(Boolean spreadCall, Object[] arguments) {
        return false;
    }

    @Alias
    private static SubstituteMethodHandleWrapper fallback(MutableCallSite callSite, Class<?> sender, String methodName,
            int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver,
            Object[] arguments) {
        return null;
    }

    @Alias
    private static <T> T doWithCallSite(MutableCallSite callSite, Object[] arguments,
            BiFunction<? super SubstituteCacheableCallSite, ? super Object, ? extends T> f) {
        return null;
    }

    @Substitute
    public static Object selectMethod(MutableCallSite callSite, Class<?> sender, String methodName, int callID,
            Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments)
            throws Throwable {
        final SubstituteMethodHandleWrapper mhw = fallback(callSite, sender, methodName, callID, safeNavigation, thisCall,
                spreadCall, dummyReceiver, arguments);

        if (callSite instanceof CacheableCallSite) {
            CacheableCallSite cacheableCallSite = (CacheableCallSite) callSite;

            final MethodHandle defaultTarget = cacheableCallSite.getDefaultTarget();
            if (defaultTarget == cacheableCallSite.getTarget()) {
                // correct the stale methodhandle in the inline cache of callsite
                // it is important but impacts the performance somehow when cache misses frequently
                doWithCallSite(callSite, arguments, new ToCacheBiFunction(mhw));
            }
        }

        return mhw.getCachedMethodHandle().invokeExact(arguments);
    }

    @Substitute
    public static Object fromCache(MutableCallSite callSite, Class<?> sender, String methodName, int callID,
            Boolean safeNavigation, Boolean thisCall, Boolean spreadCall, Object dummyReceiver, Object[] arguments)
            throws Throwable {
        SubstituteIndyFallbackSupplier fallbackSupplier = new SubstituteIndyFallbackSupplier(callSite, sender, methodName,
                callID, safeNavigation, thisCall, spreadCall, dummyReceiver, arguments);

        SubstituteMethodHandleWrapper mhw = bypassCache(spreadCall, arguments)
                ? NULL_METHOD_HANDLE_WRAPPER
                : doWithCallSite(
                        callSite, arguments,
                        new FromCacheBiFunction(fallbackSupplier));

        if (NULL_METHOD_HANDLE_WRAPPER == mhw) {
            mhw = fallbackSupplier.get();
        }

        return mhw.getCachedMethodHandle().invokeExact(arguments);
    }

    static class ToCacheBiFunction implements BiFunction<SubstituteCacheableCallSite, Object, SubstituteMethodHandleWrapper> {

        private final SubstituteMethodHandleWrapper mhw;

        ToCacheBiFunction(SubstituteMethodHandleWrapper mhw) {
            this.mhw = mhw;
        }

        @Override
        public SubstituteMethodHandleWrapper apply(SubstituteCacheableCallSite cs, Object receiver) {
            return cs.put(receiver.getClass().getName(), mhw);
        }
    }

    static class FromCacheBiFunction implements BiFunction<SubstituteCacheableCallSite, Object, SubstituteMethodHandleWrapper> {

        private final SubstituteIndyFallbackSupplier fallbackSupplier;

        FromCacheBiFunction(SubstituteIndyFallbackSupplier fallbackSupplier) {
            this.fallbackSupplier = fallbackSupplier;
        }

        @Override
        public SubstituteMethodHandleWrapper apply(SubstituteCacheableCallSite cs, Object receiver) {
            return cs.getAndPut(
                    receiver.getClass().getName(),
                    c -> {
                        SubstituteMethodHandleWrapper fbMhw = fallbackSupplier.get();
                        return fbMhw.isCanSetTarget() ? fbMhw : NULL_METHOD_HANDLE_WRAPPER;
                    });
        }
    }
}

@TargetClass(SourceUnit.class)
final class SubstituteSourceUnit {

    @Substitute
    public void convert() {
        throw new UnsupportedOperationException("convert is not supported");
    }
}
