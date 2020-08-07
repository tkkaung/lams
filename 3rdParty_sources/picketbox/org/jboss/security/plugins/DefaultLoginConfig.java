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
package org.jboss.security.plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.security.auth.login.Configuration;

import org.jboss.security.PicketBoxMessages;

/** An mbean that uses the default JAAS login configuration file based
 implementation. 

@author Scott.Stark@jboss.org
@version $Revision$
 */
@Deprecated
public class DefaultLoginConfig implements DynamicMBean
{
   private String authConfig = "auth.conf";
   private Configuration theConfig;

   /** Creates a new instance of DefaultLoginConfig */
   public DefaultLoginConfig()
   {
   }

   /** Get the resource path to the JAAS login configuration file to use.
    */
   public String getAuthConfig()
   {
      return authConfig;
   }

   /** Set the resource path or URL to the JAAS login configuration file to use.
    The default is "auth.conf".
    */
   public void setAuthConfig(String authConfURL) throws MalformedURLException
   {
      this.authConfig = authConfURL;
      // Set the JAAS login config file if not already set
      ClassLoader loader = SubjectActions.getContextClassLoader();
      URL loginConfig = loader.getResource(authConfig);
      if( loginConfig != null )
      {
         System.setProperty("java.security.auth.login.config", loginConfig.toExternalForm());
      }
   }

   /** Return the Configuration instance managed by this mbean. This simply
    obtains the default Configuration by calling Configuration.getConfiguration.
    Note that this means this mbean must be the first pushed onto the config
    stack if it is used.
    @see javax.security.auth.login.Configuration
    */
   public Configuration getConfiguration(Configuration currentConfig)
   {
      if( theConfig == null )
      {
         theConfig = Configuration.getConfiguration();
      }
      return theConfig;
   }

// Begin DynamicMBean interfaces
   public Object getAttribute(String name)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      if( name.equals("AuthConfig") )
         return getAuthConfig();
      throw PicketBoxMessages.MESSAGES.invalidMBeanAttribute(name);
   }

   public AttributeList getAttributes(String[] names)
   {
      AttributeList list = new AttributeList();
      for(int n = 0; n < names.length; n ++)
      {
         String name = names[n];
         try
         {
            Object value = getAttribute(name);
            Attribute attr = new Attribute(name, value);
            list.add(attr);
         }
         catch(Exception e)
         {
         }
      }
      return list;
   }

   public MBeanInfo getMBeanInfo()
   {
      Class<?> c = getClass();
      MBeanAttributeInfo[] attrInfo = {
         new MBeanAttributeInfo("AuthConfig", "java.lang.String", "", true, true, false)
      };
      Constructor<?> ctor = null;
      try
      {
         Class<?>[] sig = {};
         ctor = c.getDeclaredConstructor(sig);
      }
      catch(Exception e)
      {
      }
      MBeanConstructorInfo[] ctorInfo = {
         new MBeanConstructorInfo("Default ctor", ctor)
      };
      Method getConfiguration = null;
      try
      {
         Class<?>[] sig = {Configuration.class};
         getConfiguration = c.getDeclaredMethod("getConfiguration", sig);
      }
      catch(Exception e)
      {
      }
      MBeanOperationInfo[] opInfo = {
         new MBeanOperationInfo("Access the LoginConfiguration", getConfiguration)
      };
      MBeanInfo info = new MBeanInfo(c.getName(), "Default JAAS LoginConfig",
         attrInfo, ctorInfo, opInfo, null);
      return info;
   }

   public Object invoke(String method, Object[] args, String[] signature)
      throws MBeanException, ReflectionException
   {
      Object value = null;
      if( method.equals("getConfiguration") )
      {
         Configuration currentConfig = (Configuration) args[0];
         value = this.getConfiguration(currentConfig);
      }
      return value;
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      String name = attribute.getName();
      String value = (String) attribute.getValue();
      if( name.equals("AuthConfig") )
      {
         try
         {
            setAuthConfig(value);
         }
         catch(Exception e)
         {
            throw new MBeanException(e);
         }
      }
      else
          throw PicketBoxMessages.MESSAGES.invalidMBeanAttribute(name);
   }

   public AttributeList setAttributes(AttributeList attributeList)
   {
      AttributeList list = new AttributeList();
      for(int n = 0; n < attributeList.size(); n ++)
      {
         Attribute attr = (Attribute) attributeList.get(n);
         try
         {
            setAttribute(attr);
            list.add(attr);
         }
         catch(Exception e)
         {
         }
      }
      return list;
   }
// End DynamicMBean interfaces

}
