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
package com.navercorp.pinpoint.plugin.sample._13_RPC_Client;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;
import com.navercorp.plugin.sample.target.TargetClass13_Request;

/**
 * 此拦截器展示了如何记录rpc调用同时传递追踪数据到server端
 * 
 * @author Jongho Moon
 *
 */
public class SendRequestInterceptor implements AroundInterceptor1 {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public SendRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object arg0) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        TargetClass13_Request request = (TargetClass13_Request) arg0;

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();

            // rpc调用追踪在客户端代码范围必须记录一个service code
            recorder.recordServiceType(SamplePluginConstants.MY_RPC_CLIENT_SERVICE_TYPE);

            // 你必须触发一个TraceId，请求的接收方将使用这个TraceId（这里也即server端的traceId其实是由client端创建的）
            TraceId nextId = trace.getTraceId().getNextTraceId();

            // 记录下一个span id
            recorder.recordNextSpanId(nextId.getSpanId());

            // 最后，将追踪数据传递到server
            // 如何传递取决于特定的协议
            // 这个例子假设目标协议消息支持任何元数据（比如Http headers）
            request.addMetadata(SamplePluginConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            request.addMetadata(SamplePluginConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            request.addMetadata(SamplePluginConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            request.addMetadata(SamplePluginConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            request.addMetadata(SamplePluginConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            request.addMetadata(SamplePluginConstants.META_FLAGS, Short.toString(nextId.getFlags()));
        } else {
            // 如果本次不需要采样，那么只需要传递是否采样给server端
            request.addMetadata(SamplePluginConstants.META_DO_NOT_TRACE, "1");
        }
    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            recorder.recordApi(descriptor);

            if (throwable == null) {
                // RPC 客户端必须记录end point(服务端地址)
                String serverAddress = ((ServerAddressGetter) target)._$PREFIX$_getServerAddress();
                int port = ((ServerPortGetter) target)._$PREFIX$_getServerPort();
                recorder.recordEndPoint(serverAddress + ":" + port);

                TargetClass13_Request request = (TargetClass13_Request) arg0;
                // 可选的，记录目的唯一id(比如server的逻辑名，举个例子：DB名称)
                recorder.recordDestinationId(request.getNamespace());
                recorder.recordAttribute(SamplePluginConstants.MY_RPC_PROCEDURE_ANNOTATION_KEY, request.getProcedure());
                recorder.recordAttribute(SamplePluginConstants.MY_RPC_ARGUMENT_ANNOTATION_KEY, request.getArgument());
                recorder.recordAttribute(SamplePluginConstants.MY_RPC_RESULT_ANNOTATION_KEY, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
