/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.security.auth.message.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;

//$Id$

/**
 *  Privileged Blocks
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 17, 2007 
 *  @version $Revision$
 */
class SecurityActions
{
   static ClassLoader getContextClassLoader()
   {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });

   }

   static SecurityContext getSecurityContext()
   {
      return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>()
      {

         public SecurityContext run()
         {
            return SecurityContextAssociation.getSecurityContext();
         }
      });
   }

   static Class<?> loadClass(final ClassLoader cl, final String name) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>()
      {
         public Class<?> run() throws PrivilegedActionException
         {
            if (cl == null)
            {
               return loadClass(name);
            }
            try
            {
               return cl.loadClass(name);
            }
            catch (Exception ignore)
            {
               return loadClass(name);
            }
         }
      });
   }

   static Class<?> loadClass(final String name) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>()
      {
         public Class<?> run() throws PrivilegedActionException
         {
            try
            {
               return getClass().getClassLoader().loadClass(name);
            }
            catch (Exception ignore)
            {
               try
               {
                  return getContextClassLoader().loadClass(name);
               }
               catch (Exception e)
               {
                  throw new PrivilegedActionException(e);
               }
            }
         }
      });
   }
}
