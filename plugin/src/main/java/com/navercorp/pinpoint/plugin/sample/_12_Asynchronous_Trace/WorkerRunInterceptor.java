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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

/**
 * 此拦截器拦截run()方法，run()方法执行异步任务
 * 
 * Pinpoint 提供{@link AsyncContextSpanEventSimpleAroundInterceptor} ，它处理了与线程边界之外的连续跟踪有关的所有琐事
 * <br/>
 * {@link AsyncContextSpanEventSimpleAroundInterceptor} 通过via {@link AsyncContextAccessor}从目标对象获取{@link AsyncContext}
 * 因此目标类必须能够转换成<tt>AsyncContextAccessor</tt>对象，并且有<tt>AsyncContext</tt> 字段。
 * <br/>
 * 在这个例子中{@link WorkerConstructorInterceptor} 设置<tt>AsyncContext</tt> 到自身this对象，即传递给父类.
 * 
 * @author Jongho Moon
 */
public class WorkerRunInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public WorkerRunInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        // do nothing
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(SamplePluginConstants.MY_SERVICE_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
