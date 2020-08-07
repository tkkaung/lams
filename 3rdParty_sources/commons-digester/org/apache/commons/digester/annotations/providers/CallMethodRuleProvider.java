/* $Id: CallMethodRuleProvider.java 992060 2010-09-02 19:09:47Z simonetripodi $
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.digester.annotations.providers;

import java.lang.reflect.Method;

import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.annotations.AnnotationRuleProvider;
import org.apache.commons.digester.annotations.rules.CallMethod;

/**
 * Provides instances of {@code CallMethodRule}
 *
 * @since 2.1
 */
public final class CallMethodRuleProvider implements AnnotationRuleProvider<CallMethod, Method, CallMethodRule> {

    private String methodName;

    private Class<?>[] parameterTypes;

    /**
     * {@inheritDoc}
     */
    public void init(CallMethod annotation, Method element) {
        this.methodName = element.getName();
        this.parameterTypes = element.getParameterTypes();
    }

    /**
     * {@inheritDoc}
     */
    public CallMethodRule get() {
        return new CallMethodRule(this.methodName,
                this.parameterTypes.length,
                this.parameterTypes);
    }

}
