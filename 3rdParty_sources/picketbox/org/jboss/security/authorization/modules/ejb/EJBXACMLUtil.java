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
package org.jboss.security.authorization.modules.ejb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collection;

import org.jboss.security.PicketBoxLogger;
import org.jboss.security.PicketBoxMessages;
import org.jboss.security.authorization.util.JBossXACMLUtil;
import org.jboss.security.identity.Role;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.XACMLConstants;

//$Id$

/**
 *  Utility class for the XACML Integration for the EJB Layer
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jul 6, 2006 
 *  @version $Revision$
 */
public class EJBXACMLUtil extends JBossXACMLUtil
{

   public RequestContext createXACMLRequest( String ejbName, Method ejbMethod, Principal principal, RoleGroup callerRoles )
   throws Exception
   {
      String action = ejbMethod.getName();
      
      //Let us look at the number of arguments
      Class<?>[] paramTypes = ejbMethod.getParameterTypes();
      if( paramTypes.length == 0 )
         return this.createXACMLRequest(ejbName, action, principal, callerRoles );
      
      StringBuilder builder = new StringBuilder( "(" ); 
      int i = 0;
      for( Class<?> paramClass: paramTypes )
      { 
         if( i > 0 )
            builder.append( "," );
         builder.append( paramClass.getSimpleName() ); 
         i++;
      }
      
      builder.append( ")" );
      
      //Create an action type
      ActionType actionType = getActionType( action + builder.toString() );
      //actionType.

      RequestContext requestCtx = this.getRequestContext( ejbName, actionType, principal, callerRoles );
  
      if(PicketBoxLogger.LOGGER.isDebugEnabled())
      {
         ByteArrayOutputStream baos = null;
         try
         {
            baos = new ByteArrayOutputStream();

            requestCtx.marshall(baos);
            PicketBoxLogger.LOGGER.debug(new String(baos.toByteArray()));
         }
         catch(IOException e)
         {}
         finally
         {
            safeClose(baos);
         }        
      }
      return requestCtx;
   }

   /**
    * 
    * @param ejbName
    * @param methodName
    * @param principal
    * @param callerRoles
    * @return
    * @throws Exception
    */
   public RequestContext createXACMLRequest(String ejbName, String methodName,
         Principal principal, RoleGroup callerRoles) throws Exception
   {  
      String action = methodName;
      //Create an action type
      ActionType actionType = getActionType( action );

      RequestContext requestCtx = this.getRequestContext(ejbName, actionType, principal, callerRoles);

      if(PicketBoxLogger.LOGGER.isDebugEnabled())
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         requestCtx.marshall(baos);
         PicketBoxLogger.LOGGER.debug(new String(baos.toByteArray()));
      }
      return requestCtx;
  }
   
   private RequestContext getRequestContext( String ejbName, ActionType actionType,
         Principal principal, RoleGroup callerRoles ) throws IOException
   {
      if(principal == null)
         throw PicketBoxMessages.MESSAGES.invalidNullArgument("principal");

      RequestContext requestCtx = RequestResponseContextFactory.createRequestCtx();

      //Create a subject type
      SubjectType subject = this.getSubjectType( principal, callerRoles ); 

      //Create a resource type
      ResourceType resourceType = getResourceType( ejbName ); 

      //Create an Environment Type (Optional)
      EnvironmentType environmentType = getEnvironmentType();

      //Create a Request Type
      RequestType requestType = getRequestType( subject, resourceType, actionType, environmentType );

      requestCtx.setRequest( requestType );
      
      return requestCtx; 
   }

   private RequestType getRequestType(SubjectType subject, ResourceType resourceType, ActionType actionType,
         EnvironmentType environmentType)
   {
      RequestType requestType = new RequestType();
      requestType.getSubject().add(subject);
      requestType.getResource().add(resourceType);
      requestType.setAction(actionType);
      requestType.setEnvironment(environmentType);
      return requestType;
   }

   private EnvironmentType getEnvironmentType()
   {
      EnvironmentType environmentType = new EnvironmentType();
      environmentType.getAttribute().add( 
            RequestAttributeFactory.createDateTimeAttributeType(
            XACMLConstants.ATTRIBUTEID_CURRENT_TIME, null));
      return environmentType;
   }

   private ActionType getActionType(String action)
   {
      String actionID_NS = XACMLConstants.ATTRIBUTEID_ACTION_ID;
      
      AttributeType actionAttribute = RequestAttributeFactory.createStringAttributeType( actionID_NS , "jboss.org", action ); 
      ActionType actionType = new ActionType();
      actionType.getAttribute().add( actionAttribute );
      return actionType;
   }

   private ResourceType getResourceType(String ejbName)
   {
      String resourceID_NS = XACMLConstants.ATTRIBUTEID_RESOURCE_ID;
      
      ResourceType resourceType = new ResourceType();
      AttributeType resourceAttribute =  RequestAttributeFactory.createStringAttributeType( resourceID_NS, null, ejbName ); 
      resourceType.getAttribute().add( resourceAttribute );
      return resourceType;
   }    
   
  private SubjectType getSubjectType( Principal principal, RoleGroup callerRoles )
  {
     String subjectID_NS =  XACMLConstants.ATTRIBUTEID_SUBJECT_ID;
     String roleID_NS = XACMLConstants.ATTRIBUTEID_ROLE;
     String principalName = principal.getName();
     
     //Create a subject type
     SubjectType subject = new SubjectType();
     AttributeType attribute = RequestAttributeFactory.createStringAttributeType( subjectID_NS, "jboss.org", principalName );

     subject.getAttribute().add( attribute );

     Collection<Role> rolesList = callerRoles.getRoles();
     if(rolesList != null)
     {
        for(Role role:rolesList)
        {
           String roleName = role.getRoleName(); 
           AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType( roleID_NS , "jboss.org", roleName );
           subject.getAttribute().add(attSubjectID);
        }
     }  
     return subject;
  }
  private void safeClose(OutputStream os)
  {
     try
     {
        if(os != null)
        {
           os.close();
        }
     }
     catch(Exception e)
     {}
  }
}