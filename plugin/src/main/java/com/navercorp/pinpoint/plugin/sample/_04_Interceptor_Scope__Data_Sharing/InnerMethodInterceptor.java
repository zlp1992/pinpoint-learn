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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;

/**
 * 此拦截器使用{@link InterceptorScopeInvocation}获取<tt>MyAttachment</tt>对象，取里面的标识判断当前transaction是否被追踪
 * 如果被追踪，则设置返回值到附加对象里以便传递给{@link OuterMethodInterceptor}
 *
 */
public class InnerMethodInterceptor implements AroundInterceptor0 {
    private final InterceptorScope scope;

    // 拦截器，通过构造方法获取 InterceptorScope ，pinpoint会自动注入
    public InnerMethodInterceptor(InterceptorScope scope) {
        this.scope = scope;
    }

    @Override
    public void before(Object target) {

    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        //这里拿到OuterMethodInterceptor添加的附加对象
        MyAttachment attachment = (MyAttachment)scope.getCurrentInvocation().getAttachment();
        
        if (attachment.isTrace()) {
            attachment.setValue(result);
        }
    }
}
