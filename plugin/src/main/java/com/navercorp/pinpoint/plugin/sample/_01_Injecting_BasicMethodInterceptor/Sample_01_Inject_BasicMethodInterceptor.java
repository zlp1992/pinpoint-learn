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
package com.navercorp.pinpoint.plugin.sample._01_Injecting_BasicMethodInterceptor;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * Pinpoint提供了一个基本拦截器 BasicMethodInterceptor 记录方法的执行时间和异常，
 * 以下代码展示了如何在方法中注入这个拦截器，同时也展示了如何给拦截器的构造方法传递参数
 * 这个transform注入的是类 {@linkplain com.navercorp.plugin.sample.target.TargetClass01}
 */
public class Sample_01_Inject_BasicMethodInterceptor implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        // 1. 获取目标类对应的织入类，Pinpoint中使用InstrumentClass包装需要拦截的类，方便进行代码增强
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        
        // 2. 获取目标方法对应的织入类，Pinpoint中使用InstrumentMethod包装需要拦截的方法
        InstrumentMethod targetMethod = target.getDeclaredMethod("targetMethod", "java.lang.String");

        // 3. 给方法添加拦截器. 第一个参数是拦截器类, 后面是拦截器类构造方法的参数，使用va将参数转换成Object[]数组
        targetMethod.addInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE));
        
        // 4. 返回修改后的字节码
        return target.toBytecode();
    }

}
