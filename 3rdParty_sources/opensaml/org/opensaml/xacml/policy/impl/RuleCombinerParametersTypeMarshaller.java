/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.xacml.policy.impl;

import org.opensaml.xacml.impl.AbstractXACMLObjectMarshaller;
import org.opensaml.xacml.policy.RuleCombinerParametersType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.DatatypeHelper;
import org.w3c.dom.Element;

/**
 * Marshaller for {@link RuleCombinerParametersType}.
 */
public class RuleCombinerParametersTypeMarshaller extends AbstractXACMLObjectMarshaller {

    /** Constructor. */
    public RuleCombinerParametersTypeMarshaller() {
        super();
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        RuleCombinerParametersType ruleCombinerParametersType = (RuleCombinerParametersType)xmlObject;
        
        if(!DatatypeHelper.isEmpty(ruleCombinerParametersType.getRuleIdRef())){
            domElement.setAttribute(RuleCombinerParametersType.RULE_ID_REF_ATTRIB_NAME,
                    ruleCombinerParametersType.getRuleIdRef());
        }        
    }

}
