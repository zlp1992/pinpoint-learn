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
package com.navercorp.pinpoint.plugin.sample._10_Adding_Field;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.plugin.sample.target.TargetClass10_Consumer;
import com.navercorp.plugin.sample.target.TargetClass10_Message;
import com.navercorp.plugin.sample.target.TargetClass10_Producer;

/**
 * 你可以给目标类添加字段来附加一些追踪数据
 * 
 * 在这个例子中，我们将使用生产者的名字来追踪 {@link TargetClass10_Consumer#consume(TargetClass10_Message)}。
 * 但是考虑到我们无法在这个方法里获取到生产者的名字，
 * 我们拦截{@link TargetClass10_Producer#produce()}来注入生产者的名字到返回对象{@link TargetClass10_Message}
 */
public class Sample_10_Adding_Field {

    public static class Producer implements TransformCallback {
        
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            target.getDeclaredMethod("produce").addInterceptor(ProducerInterceptor.class);
    
            return target.toBytecode();
        }
    }
    
    public static class Consumer implements TransformCallback {
        
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            target.getDeclaredMethod("consume", "com.navercorp.plugin.sample.target.TargetClass10_Message").addInterceptor(ConsumerInterceptor.class);
    
            return target.toBytecode();
        }
    }
    
    public static class Message implements TransformCallback {
        
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // 给类添加字段，注意到你不需要提供字段名
            target.addField(ProducerNameAccessor.class);

            return target.toBytecode();
        }
    }
}
