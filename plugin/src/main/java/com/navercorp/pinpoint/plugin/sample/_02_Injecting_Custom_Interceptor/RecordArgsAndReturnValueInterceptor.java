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
package com.navercorp.pinpoint.plugin.sample._02_Injecting_Custom_Interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

/**
 * <strong>强烈建议你编写插件的时候继承 SpanEventSimpleAroundInterceptorForPlugin.</strong>
 *  它包含了实现一个基本的拦截器类所需的大部分模板代码.下面这个拦截器展示了如何记录一个带参数的方法调用以及它的返回值.
 * <p>
 *  下面这个拦截器展示了如何记录一个带参数的方法调用以及返回值.
 * <p>
 *     拦截器必须实现下列中的任一接口：
 * <ul>
 * <li>{@link AroundInterceptor}</li>
 * <li>{@link AroundInterceptor0}</li>
 * <li>{@link AroundInterceptor1}</li>
 * <li>{@link AroundInterceptor2}</li>
 * <li>{@link AroundInterceptor3}</li>
 * <li>{@link AroundInterceptor4}</li>
 * <li>{@link AroundInterceptor5}</li>
 * <li>{@link StaticAroundInterceptor}</li>
 * </ul>
 * 这些接口的不同之处是：当目标方法被拦截时，拦截器接收的参数个数不一样。
 * <p>
 *     下面这个样例中的拦截器实现了AroundInterceptor1，它在执行目标方法之前和之后进行拦截，同时接收一个参数.
 *
 * @see SpanEventSimpleAroundInterceptorForPlugin
 */
public class RecordArgsAndReturnValueInterceptor implements AroundInterceptor1 {
    // You have to use PLogger for logging because you don't know which logging library the target application uses. 
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    // 拦截器类可以将Pinpoint自身的对下作为构造方法参数，这些参数会自动注入
    public RecordArgsAndReturnValueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    /**
     * 此方法会在目标方法调用之前调用
     * @param target 目标类
     * @param arg0 参数
     * */
    @Override
    public void before(Object target, Object arg0) {
        if (isDebug) {
            logger.beforeInterceptor(target, new Object[] { arg0 } );
        }

        // 1. 获取Trace. 当不分析当前事务时(transaction)为null，这里也即当当前没有transaction或者此条transaction没被采样时返回null
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        // 2. 开始一个追踪块trace block（即SpanEvent）
        trace.traceBlockBegin();
    }

    /**
     * 此方法会在目标方法调用之后调用
     * @param target 目标类
     * @param arg0 参数
     * @param result 目标方法执行返回值
     * @param throwable 目标方法执行抛出的异常
     * */
    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, new Object[] { arg0 });
        }

        // 1. 获取Trace
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            // 2. 获取当前的Span event记录器
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            // 3. 记录Service-type
            recorder.recordServiceType(SamplePluginConstants.MY_SERVICE_TYPE);
            
            // 4. 记录方法签名和参数
            recorder.recordApi(descriptor, new Object[] { arg0 });
            
            // 5. 记录异常（如果有的话）
            recorder.recordException(throwable);
            
            // 6. Trace没有提供方法记录方法的返回值，所以需要将返回值记录成一个属性
            recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        } finally {
            // 7. 结束追踪块
            trace.traceBlockEnd();
        }
        /*
        * 看到这里是不是有点摸清插件编写的套路了，在before方法里通过traceBlockBegin开启一个SpanEvent，在after里通过traceBlockEnd关闭这个SpanEvent
        * 一个方法执行结束了，一个SpanEvent也就结束了，所以简单理解一个SpanEvent代表一次方法执行
        * */
    }
}
