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
package com.navercorp.pinpoint.plugin.sample._14_RPC_Server;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;
import com.navercorp.plugin.sample.target.TargetClass14_Request;
import com.navercorp.plugin.sample.target.TargetClass14_Server;

/**
 * 最好通过继承{@link SpanSimpleAroundInterceptor}来编写server端应用拦截器
 * 
 * @author Jongho Moon
 */
public class ProcessInterceptor extends SpanSimpleAroundInterceptor {
    public ProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, ProcessInterceptor.class);
    }
    

    /**
     * 在这个方法中，你必须检查当前请求是否包含以下信息：
     * 
     * 1. 是否有标记来标识当前请求不应该被追踪
     * 2. 继续追踪所必须的数据，transaction id、parent id等等
     * 
     * Then you have to create appropriate Trace object.
     */
    @Override
    protected Trace createTrace(Object target, Object[] args) {
        TargetClass14_Request request = (TargetClass14_Request)args[0];
        
        // 如果此transaction 不能被追踪, 标记为不被追踪.
        if (request.getMetadata(SamplePluginConstants.META_DO_NOT_TRACE) != null) {
            return traceContext.disableSampling();
        }
        
        String transactionId = request.getMetadata(SamplePluginConstants.META_TRANSACTION_ID);

        // 如果没有transaction id, 开启一个新的trasaction进行追踪
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // 否则，根据已有数据继续追踪
        long parentSpanID = NumberUtils.parseLong(request.getMetadata(SamplePluginConstants.META_PARENT_SPAN_ID), SpanId.NULL);
        long spanID = NumberUtils.parseLong(request.getMetadata(SamplePluginConstants.META_SPAN_ID), SpanId.NULL);
        short flags = NumberUtils.parseShort(request.getMetadata(SamplePluginConstants.META_FLAGS), (short) 0);
        TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

        return traceContext.continueTraceObject(traceId);
    }
    
    
    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        TargetClass14_Server server = (TargetClass14_Server)target;
        TargetClass14_Request request = (TargetClass14_Request)args[0];
        
        // 你必须在服务端范围内记录服务类型（service type）
        recorder.recordServiceType(SamplePluginConstants.MY_RPC_SERVER_SERVICE_TYPE);
        
        // 记录rpc名称，客户端地址，服务端地址
        recorder.recordRpcName(request.getProcedure());
        recorder.recordEndPoint(server.getAddress());
        recorder.recordRemoteAddress(request.getClientAddress());

        // 如果不是请求的根节点（即请求不是从当前节点发出），记录父节点（发出这个请求的client）信息
        if (!recorder.isRoot()) {
            String parentApplicationName = request.getMetadata(SamplePluginConstants.META_PARENT_APPLICATION_NAME);
            
            if (parentApplicationName != null) {
                short parentApplicationType = NumberUtils.parseShort(request.getMetadata(SamplePluginConstants.META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                
                String serverHostName = request.getServerHostName();
                
                if (serverHostName != null) {
                    recorder.recordAcceptorHost(serverHostName);
                } else {
                    recorder.recordAcceptorHost(server.getAddress());
                }
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        TargetClass14_Request request = (TargetClass14_Request)args[0];

        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(SamplePluginConstants.MY_RPC_ARGUMENT_ANNOTATION_KEY, request.getArgument());
        
        if (throwable == null) {
            recorder.recordAttribute(SamplePluginConstants.MY_RPC_RESULT_ANNOTATION_KEY, result);
        } else {
            recorder.recordException(throwable);
        }
    }
}
