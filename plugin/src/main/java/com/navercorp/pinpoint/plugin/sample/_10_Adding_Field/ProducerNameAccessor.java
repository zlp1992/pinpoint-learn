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

/**
 * 目标类实现的访问接口，最好唯一命名方法避免重复
 * 
 * @author Jongho Moon
 */
public interface ProducerNameAccessor {
    public void _$PINPOINT$_setProducerName(String name);
    public String _$PINPOINT$_getProducerName();
}
