/* $Id: DigesterLoadingException.java 992060 2010-09-02 19:09:47Z simonetripodi $
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

/**
 * The exception thrown when an error occurs while analyzing targets and
 * building rule sets.
 *
 * @since 2.1
 */
public final class DigesterLoadingException extends RuntimeException {

    /**
     * The default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new loading exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public DigesterLoadingException(String message) {
        super(message);
    }

    /**
     * Constructs a new loading exception with the specified cause.
     *
     * @param cause the specified cause.
     */
    public DigesterLoadingException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new loading exception with the specified detail message
     * and cause.
     *
     * @param message the detail message.
     * @param cause the specified cause.
     */
    public DigesterLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
