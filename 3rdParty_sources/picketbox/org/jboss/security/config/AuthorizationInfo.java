/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.security.config;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.security.authorization.config.AuthorizationModuleEntry;

//$Id$

/**
 *  Holder for Authorization configuration
 *  
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 *  @since  Jun 9, 2006 
 *  @version $Revision$
 */
public class AuthorizationInfo extends BaseSecurityInfo<AuthorizationModuleEntry>
{  
   public AuthorizationInfo(String name)
   {
      super(name); 
   }  
   
   public AuthorizationModuleEntry[] getAuthorizationModuleEntry()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(GET_CONFIG_ENTRY_PERM); 
      AuthorizationModuleEntry[] entries = new AuthorizationModuleEntry[moduleEntries.size()];
      moduleEntries.toArray(entries);
      return entries;
   }

   @Override
   protected BaseSecurityInfo<AuthorizationModuleEntry> create(String name)
   {
      return new AuthorizationInfo(name);
   }
   
   /**
    * Write element content. The start element is already written.
    * 
    * @param writer
    * @throws XMLStreamException
    */
   public void writeContent(XMLStreamWriter writer) throws XMLStreamException
   {
      for (int i = 0; i < moduleEntries.size(); i++)
      {
         AuthorizationModuleEntry entry = moduleEntries.get(i);
         writer.writeStartElement(Element.POLICY_MODULE.getLocalName());
         writer.writeAttribute(Attribute.CODE.getLocalName(), entry.getPolicyModuleName());
         writer.writeAttribute(Attribute.FLAG.getLocalName(), entry.getControlFlag().toString().toLowerCase(Locale.ENGLISH));
         Map<String, ?> options = entry.getOptions();
         if (options != null && options.size() > 0)
         {
            for (Entry<String, ?> option : options.entrySet())
            {
               writer.writeStartElement(Element.MODULE_OPTION.getLocalName());
               writer.writeAttribute(Attribute.NAME.getLocalName(), option.getKey());
               writer.writeAttribute(Attribute.VALUE.getLocalName(), option.getValue().toString());
               writer.writeEndElement();
            }
         }
         writer.writeEndElement();
      }
      writer.writeEndElement();
   }
}
