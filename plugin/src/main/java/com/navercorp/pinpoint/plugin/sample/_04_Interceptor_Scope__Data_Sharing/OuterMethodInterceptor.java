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
package com.navercorp.pinpoint.plugin.sample._04_Interceptor_Scope__Data_Sharing;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

/**
 * 此拦截器附加对象到当前的{@link InterceptorScopeInvocation}，能够让{@link InnerMethodInterceptor}知道当前的transaction是否被追踪同时获取返回值
 * 注意看类 {@linkplain com.navercorp.plugin.sample.target.TargetClass04}，两个拦截器的执行顺序如下
 * <p>
 *  <li>OuterMethodInterceptor.before</li>
 *  <li>InnerMethodInterceptor.before</li>
 *  <li>InnerMethodInterceptor.after</li>
 *  <li>OuterMethodInterceptor.after</li>
 * </p>
 * @see Sample_04_Interceptors_In_A_Scope_Share_Value
 * @author Jongho Moon
 */
public class OuterMethodInterceptor implements AroundInterceptor1 {
    private static final AttachmentFactory ATTACHMENT_FACTORY = new AttachmentFactory() {
        
        @Override
        public Object createAttachment() {
            return new MyAttachment();
        }
    };
    
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    // An interceptor receives an InterceptorScope through its constructor
    public OuterMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.scope = scope;
    }
    
    @Override
    public void before(Object target, Object arg0) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        boolean shouldTrace = ((String)arg0).startsWith("FOO");
        
        if (!shouldTrace) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(SamplePluginConstants.MY_SERVICE_TYPE);

        // 创建或获取附加对象 attachment
        MyAttachment attachment = (MyAttachment)scope.getCurrentInvocation().getOrCreateAttachment(ATTACHMENT_FACTORY);
        attachment.setTrace(shouldTrace);
    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        try {
            MyAttachment attachment = (MyAttachment)scope.getCurrentInvocation().getAttachment();
            
            if (!attachment.isTrace()) {
                return; 
            }
            
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            
            recorder.recordApi(descriptor, new Object[] { arg0 });
            recorder.recordException(throwable);
            
            // 记录InnerMethodInterceptor拦截器设置的值
            recorder.recordAttribute(SamplePluginConstants.ANNOTATION_KEY_MY_VALUE, attachment.getValue());
        } finally {
            trace.traceBlockEnd();
        }
    }
}
