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
package com.navercorp.pinpoint.plugin.sample._06_Constructor_Interceptor_Scope_Limitation;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * 构造方法拦截器同样可以带范围（scope），不过有些限制：
 * <p>
 *     Java强制要求构造方法的第一个操作必须是对父构造方法或重载的构造方法的调用，
 *     任何注入的代码（包括拦截器方法）都在这之后执行。
 *     因此，构造方法拦截器的方法调用顺序与普通方法不同
 * <p>
 *     如果方法A调用方法B，拦截器调用顺序如下：
 * <ol>
 * <li><tt>A_interceptor.before();</tt></li>
 * <li><tt>B_interceptor.before();</tt></li>
 * <li><tt>B_interceptor.after();</tt></li>
 * <li><tt>A_interceptor.after();</tt></li>
 * </ol>
 * 如果A和B都是构造方法，那么拦截器调用顺序将会是下面的顺序：
 * <ol>
 * <li><tt>B_interceptor.before();</tt></li>
 * <li><tt>B_interceptor.after();</tt></li>
 * <li><tt>A_interceptor.before();</tt></li>
 * <li><tt>A_interceptor.after();</tt></li>
 * </ol>
 * 这同样会影响范围拦截器的执行策略。对于普通方法，如果A和B执行策略都是BOUNDARY并且有相同的scope，那么B的拦截器将不会被执行。
 * 然而，如果A和B都是构造方法，A和B的拦截都会被执行，当B拦截器执行的时候，它的外层并没有拦截器A
 */
public class Sample_06_Constructor_Interceptor_Scope_Limitation implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        InterceptorScope scope = instrumentor.getInterceptorScope("SAMPLE_SCOPE");

        InstrumentMethod targetConstructorA = target.getConstructor();
        targetConstructorA.addScopedInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE), scope);

        InstrumentMethod targetConstructorB = target.getConstructor("int");
        targetConstructorB.addScopedInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE), scope);

        return target.toBytecode();
    }

}
