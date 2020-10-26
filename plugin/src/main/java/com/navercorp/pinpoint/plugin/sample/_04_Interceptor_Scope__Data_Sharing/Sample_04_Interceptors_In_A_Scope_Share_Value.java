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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * 有时候在追踪链路中，拦截器需要和其他拦截器共享数据，
 * 这样的一个示例便是在跟踪RPC客户端时，其中链末端的拦截器所需的目标地址只能由链中更远的其他某些拦截器获取。.
 * <p>
 * 出于这个目的，你可以在两个有相同scope的拦截器通过附加对象到{@link InterceptorScope}从而共享数据
 *
 */
public class Sample_04_Interceptors_In_A_Scope_Share_Value implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        
        // 从Instrumentor获取scope对象
        InterceptorScope scope = instrumentor.getInterceptorScope("SAMPLE_04_SCOPE");

        // 需要共享数据的拦截器必须有相同的scope
        InstrumentMethod outerMethod = target.getDeclaredMethod("outerMethod", "java.lang.String");
        outerMethod.addScopedInterceptor(OuterMethodInterceptor.class, scope);
        
        //注意，InnerMethodInterceptor的执行策略设置为INTERNAL，这样只有在同一个scope范围内已经有其他的拦截器处于活跃状态，这个拦截器才能执行
        InstrumentMethod innerMethod = target.getDeclaredMethod("innerMethod", "java.lang.String");
        innerMethod.addScopedInterceptor(InnerMethodInterceptor.class, scope, ExecutionPolicy.INTERNAL);
        
        return target.toBytecode();
    }

}
