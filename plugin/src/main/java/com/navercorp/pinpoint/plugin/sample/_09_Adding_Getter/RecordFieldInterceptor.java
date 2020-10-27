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
package com.navercorp.pinpoint.plugin.sample._09_Adding_Getter;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

/**
 * 这个拦截器需要记录目标类中未公开的字段<tt>hiddenField</tt>的值。
 * 幸运的是，通过添加一个getter方法{@link HiddenFieldGetter}，我们可以在运行时访问这个字段
 */
public class RecordFieldInterceptor implements AroundInterceptor {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public RecordFieldInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(SamplePluginConstants.MY_SERVICE_TYPE);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            
            // 将织入的目标类转换成getter类型以便获取字段的值
            String fieldValue = ((HiddenFieldGetter)target)._$PREFIX$_getValue();
            recorder.recordAttribute(SamplePluginConstants.ANNOTATION_KEY_MY_VALUE, fieldValue);
        } finally {
            trace.traceBlockEnd();
        }
    }
}
