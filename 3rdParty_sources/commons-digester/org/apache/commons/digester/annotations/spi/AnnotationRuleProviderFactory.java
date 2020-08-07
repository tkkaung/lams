/* $Id: AnnotationRuleProviderFactory.java 992719 2010-09-05 07:49:25Z simonetripodi $
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
package org.apache.commons.digester.annotations.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.apache.commons.digester.Rule;
import org.apache.commons.digester.annotations.AnnotationRuleProvider;
import org.apache.commons.digester.annotations.DigesterLoadingException;

/**
 * An object capable of providing instances of {@link AnnotationRuleProvider}.
 *
 * @since 2.1
 */
public interface AnnotationRuleProviderFactory {

    /**
     * Return an {@link AnnotationRuleProvider} instance of the specified type.
     *
     * @param <T> the {@link AnnotationRuleProvider} type.
     * @param type the class of the object to be returned.
     * @return an instance of the specified class.
     * @throws DigesterLoadingException if any error occurs while creating the
     *         {@code type} instance.
     */
    <T extends AnnotationRuleProvider<? extends Annotation, ? extends AnnotatedElement, ? extends Rule>>
        T newInstance(Class<T> type) throws DigesterLoadingException;

}
