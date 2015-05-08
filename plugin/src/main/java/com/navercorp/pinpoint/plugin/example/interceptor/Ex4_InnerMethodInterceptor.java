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
package com.navercorp.pinpoint.plugin.example.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.example.MyAttachment;
import com.navercorp.pinpoint.plugin.example.MyPlugin;

/**
 * This interceptor uses {@link InterceptorGroupInvocation} attachment to pass data to {@link Ex4_OuterMethodInterceptor}
 * 
 * @see {@link MyPlugin#example4_Interceptors_In_A_Group_Share_Value(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext)}
 * @author Jongho Moon
 */
public class Ex4_InnerMethodInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorGroup group;

    public Ex4_InnerMethodInterceptor(InterceptorGroup group) {
        this.group = group;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        
        MyAttachment attachment = (MyAttachment)group.getCurrentInvocation().getAttachment();
        
        if (attachment.isTrace()) {
            attachment.setValue(result);
        }
    }
}
