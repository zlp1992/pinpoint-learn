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
package com.navercorp.pinpoint.plugin.sample._11_Configuration_And_ObjectRecipe;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;

/**
 * 当Pinpoint agent初始化的时候，ProfilerPlugin和PinpointClassFileTransformer的实现类是通过插件类加载器加载的，插件类加载器的父加载器是
 * 系统类加载器（system class loader ）。然而，拦截器类是由加载实际目标类的加载器加载，这些加载目标类的加载器无法看到插件类加载器。
 * <p>
 *     因此对于插件中的类X，ProfilerPlugin以及PinpointClassFileTransformer实现类X和拦截器实现类X是不同的，
 *     这使得transformer无法传递一个定义在插件中的类作为拦截器的构造方法参数
 * <p>
 *     为了处理这个问题，你可以传递一个{@link ObjectFactory}，其描述了如何创建参数
 * To handle this problem, you can pass an {@link ObjectFactory} which describes how to create the argument.
 * <p>
 *     注意，基于同样的原因，你应该避免通过将插件中的类定义为静态变量来实现共享
 * <p>
 *     这里例子同样展示了如何通过{@link ProfilerConfig}读取 pinpoint.config文件中的配置
 */
public class Sample_11_Configuration_And_ObjectRecipe implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        InstrumentMethod targetMethod = target.getDeclaredMethod("hello", "java.lang.String");
        
        // 获取 ProfilerConfig
        ProfilerConfig config = instrumentor.getProfilerConfig();
        int maxLen = config.readInt("profiler.sample11.maxLen", 8);
        
        // 如果你像下面这样直接传递StringTrimmer对象, Pinpoint agent 将会无法创建这个拦截器实例
        // targetMethod.addInterceptor("com.navercorp.pinpoint.plugin.sample._11_Configuration_And_ObjectRecipe.HelloInterceptor", new StringTrimmer(maxLen));
        
        ObjectFactory trimmerFactory = ObjectFactory.byConstructor("com.navercorp.pinpoint.plugin.sample._11_Configuration_And_ObjectRecipe.StringTrimmer", maxLen);
        targetMethod.addInterceptor(HelloInterceptor.class, new Object[] { trimmerFactory });

        return target.toBytecode();
    }
}
