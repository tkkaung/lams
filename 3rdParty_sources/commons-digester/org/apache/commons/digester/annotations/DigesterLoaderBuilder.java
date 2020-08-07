/* $Id: DigesterLoaderBuilder.java 992099 2010-09-02 20:09:12Z simonetripodi $
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
package org.apache.commons.digester.annotations;

import org.apache.commons.digester.annotations.internal.DefaultAnnotationRuleProviderFactory;
import org.apache.commons.digester.annotations.spi.AnnotationRuleProviderFactory;

/**
 * {@link DigesterLoader} builder implementation.
 *
 * @since 2.1
 */
public final class DigesterLoaderBuilder {

    /**
     * Builds a new {@link DigesterLoader} using the default SPI
     * implementations.
     *
     * @return a new {@link DigesterLoader} using the default SPI
     *         implementations.
     */
    public static DigesterLoader byDefaultFactories() {
        return new DigesterLoaderBuilder()
                    .useDefaultAnnotationRuleProviderFactory()
                    .useDefaultDigesterLoaderHandlerFactory();
    }

    /**
     * Builds a new {@link DigesterLoader} using the default
     * {@link AnnotationRuleProviderFactory} implementation.
     *
     * @return the next chained builder.
     * @see DefaultAnnotationRuleProviderFactory
     */
    public FromAnnotationRuleProviderFactory useDefaultAnnotationRuleProviderFactory() {
        return this.useAnnotationRuleProviderFactory(new DefaultAnnotationRuleProviderFactory());
    }

    /**
     * Builds a new {@link DigesterLoader} using the user defined
     * {@link AnnotationRuleProviderFactory} implementation.
     *
     * @param annotationRuleProviderFactory the user defined
     *        {@link AnnotationRuleProviderFactory} implementation.
     * @return the next chained builder.
     */
    public FromAnnotationRuleProviderFactory
            useAnnotationRuleProviderFactory(AnnotationRuleProviderFactory annotationRuleProviderFactory) {
        if (annotationRuleProviderFactory == null) {
            throw new IllegalArgumentException("Parameter 'annotationRuleProviderFactory' must be not null");
        }
        return new FromAnnotationRuleProviderFactory(annotationRuleProviderFactory);
    }

}
