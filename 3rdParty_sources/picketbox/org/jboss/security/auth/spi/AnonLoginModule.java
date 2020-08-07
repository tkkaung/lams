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
package org.jboss.security.auth.spi;

import java.security.acl.Group;

import javax.security.auth.login.LoginException;

import org.jboss.security.SimpleGroup;

/**
 * A simple login module that simply allows for the specification of the
 * identity of unauthenticated users via the unauthenticatedIdentity property.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class AnonLoginModule extends UsernamePasswordLoginModule
{
   /**
    * Override to return an empty Roles set.
    * @return an array comtaning an empty 'Roles' Group.
    */
   protected Group[] getRoleSets() throws LoginException
   {
      SimpleGroup roles = new SimpleGroup("Roles");
      Group[] roleSets = {roles};
      return roleSets;
   }

   /**
    * Overriden to return null.
    * @return null always
    */
   protected String getUsersPassword() throws LoginException
   {
      return null;
   }
}
