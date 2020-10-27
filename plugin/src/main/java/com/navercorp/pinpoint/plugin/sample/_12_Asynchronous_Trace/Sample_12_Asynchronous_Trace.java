/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.sample._12_Asynchronous_Trace;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * 为了追踪异步调用，你需要：
 * <ol>
 *     <li>
 *         拦截初始化异步任务的方法同时创建或者记录一个新的{@link AsyncContext}.
 *     </li>
 *     <li>
 *         将<tt>AsyncContext</tt> 传递到处理异步任务的地方
 *     </li>
 *     <li>
 *         通过{@link AsyncContextAccessor}给处理异步任务的类添加字段
 *     </li>
 *     <li>
 *         继承{@link AsyncContextSpanEventSimpleAroundInterceptor}实现拦截器拦截处理异步任务的方法
 *     </li>
 * </ol>
 * 在这个例子中，{@link AsyncInitiator}转换 {@link com.navercorp.plugin.sample.target.TargetClass12_AsyncInitiator}，TargetClass12_AsyncInitiator初始化了异步任务
 *
 * {@link Worker} 转换 {@link com.navercorp.plugin.sample.target.TargetClass12_Worker}, TargetClass12_Worker处理TargetClass12_AsyncInitiator初始化的异步任务
 */
public class Sample_12_Asynchronous_Trace {
    private static final String SCOPE_NAME = "AsyncSample";
    
    public static class AsyncInitiator implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            // 范围interceptors 将 AsyncTraceId 作为 拦截器范围调用附件（interceptor scope invocation attachment）
            // 参见Sample04 Interceptor_Scope_Data_Sharing
            InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod targetMethod = target.getDeclaredMethod("asyncHello", "java.lang.String");
            targetMethod.addScopedInterceptor(AsyncInitiatorInterceptor.class, scope);

            return target.toBytecode();
        }
    }
    
    public static class Worker implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            // Scope interceptors to pass AsyncTraceId as interceptor scope invocation attachment.
            InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);
            
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            
            InstrumentMethod constructor = target.getConstructor("java.lang.String", "com.navercorp.plugin.sample.target.TargetClass12_Future");
            constructor.addScopedInterceptor(WorkerConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);
            
            InstrumentMethod run = target.getDeclaredMethod("run");
            run.addInterceptor(WorkerRunInterceptor.class);

            return target.toBytecode();
        }
    }
    
    public static class Future implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            
            InstrumentMethod get = target.getDeclaredMethod("get");
            get.addInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE));

            return target.toBytecode();
        }
    }
}
