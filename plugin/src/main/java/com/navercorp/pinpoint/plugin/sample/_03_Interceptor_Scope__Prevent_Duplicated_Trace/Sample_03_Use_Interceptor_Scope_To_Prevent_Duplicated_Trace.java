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
package com.navercorp.pinpoint.plugin.sample._03_Interceptor_Scope__Prevent_Duplicated_Trace;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.sample.SamplePluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * {@link com.navercorp.plugin.sample.target.TargetClass03 TargetClass03} 有两个重载方法，我们想追踪这两个方法<br/>
 * 假设像之前一样只是简单的给这两个方法添加<tt>BasicMethodInterceptor</tt> 拦截器. 当我们调用
 * <tt>invoke()</tt>, 追踪的调用栈将会有11层; 第一层是<tt>invoke()</tt> 调用,
 * 后面十层递归调用<tt>invoke(int)</tt>.
 * <p>
 * 虽然这可能是你期望的, 如果您只是想知道已经调用了任何重载方法，而又不关心重载和递归调用会污染调用堆栈（即调用栈里堆满了不关心的递归调用层等），该怎么办？
 *  Scoped interceptors（范围拦截器） 对于这种场景再适合不过了。
 * <p>
 *     拦截器可以关联{@link InterceptorScope}（拦截器范围）并可以指定 {@link ExecutionPolicy}（拦截器执行策略），拦截器执行策略有以下几种：
 * <ul>
 * <li>ALWAYS: 无论同一个范围（scope）中的其他拦截器是否处于活跃状态，都执行拦截器。</li>
 * <li>BOUNDARY: 仅当同一范围内没有其他拦截器处于活跃状态时才执行拦截器，这是默认的执行策略。</li>
 * <li>INTERNAL: 仅当至少一个处于相同范围的拦截器处于活跃状态时，才执行拦截器。</li>
 * </ul>
 * 同一个范围（same scope）的Scoped interceptors 只有当满足其执行策略时才会执行 <tt>before()</tt> 和 <tt>after()</tt> 方法。
 * (当拦截器的before方法被执行并且after方法没被执行，拦截器处于活跃状态)
 * <p>
 *     对于上面的例子，两个拦截器关联同样的scope并且设置执行策略为 BOUNDARY
 *, 重载方法 <tt>invoke(int)</tt> 的调用将不会被追踪，因为它在
 * <tt>invoke()</tt>的范围内, 同样递归调用<tt>invoke(int)</tt> 也不会被追踪，因为它在自己的scope内
 */
public class Sample_03_Use_Interceptor_Scope_To_Prevent_Duplicated_Trace implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        
        // 从 Instrumentor 获取scope对象，instrumentor对象pinpoint会自动注入到参数
        InterceptorScope scope = instrumentor.getInterceptorScope("SAMPLE_SCOPE");

        // 添加一个默认执行策略（BOUNDARY）的范围拦截器
        InstrumentMethod targetMethodA = target.getDeclaredMethod("invoke");
        targetMethodA.addScopedInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE), scope);
        
        // 添加一个默认执行策略（BOUNDARY）的范围拦截器，可以看到这两个拦截器的scope被设置成一样的
        InstrumentMethod targetMethodB = target.getDeclaredMethod("invoke", "int");
        targetMethodB.addScopedInterceptor(BasicMethodInterceptor.class, va(SamplePluginConstants.MY_SERVICE_TYPE), scope);
        
        return target.toBytecode();
    }
}
