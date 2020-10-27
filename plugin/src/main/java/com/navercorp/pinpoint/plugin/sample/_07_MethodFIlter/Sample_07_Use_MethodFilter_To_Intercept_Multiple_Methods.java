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
package com.navercorp.pinpoint.plugin.sample._07_MethodFIlter;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.LoggingInterceptor;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * 如果你想给多个方法添加相同的拦截器，使用 {@link MethodFilter}.
 * 
 * {@link MethodFilters} 提供工厂方法，获取预定义的filters
 */
public class Sample_07_Use_MethodFilter_To_Intercept_Multiple_Methods implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        // 通过过滤方法的名称获取方法
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("recordMe"))) {
            // 给每个方法添加拦截器，注意每个方法会注入各自独立的拦截器实例（即这些方法不会共享同一个拦截器实例）
            method.addInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE));
        }
        
        // 如果你想让方法使用同一个拦截器实例，像下面一样使用addInterceptor(int) 方法，参数是添加拦截器后返回的pinpoint中的拦截器唯一标识id
        int interceptorId = -1;
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("logMe"))) {
            if (interceptorId == -1) {
                interceptorId = method.addInterceptor(LoggingInterceptor.class, va("SMAPLE_07_LOGGER"));
            } else {
                method.addInterceptor(interceptorId);
            }
        }
        
        return target.toBytecode();
    }
}
