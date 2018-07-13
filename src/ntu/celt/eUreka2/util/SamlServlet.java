/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

package ntu.celt.eUreka2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ntu.celt.eUreka2.components.Layout;
import ntu.celt.eUreka2.data.PredefinedNames;
import ntu.celt.eUreka2.entities.School;
import ntu.celt.eUreka2.entities.SysRole;
import ntu.celt.eUreka2.entities.User;
import ntu.celt.eUreka2.entities.WebSessionData;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.SamlUtil;
import ntu.celt.eUreka2.internal.WebUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;

/**
 * Servlet for SAML integration. Access to LAMS can be initiated by GET or POST
 * method, but Assertion Consumer works only for POST.
 * 
 * @author Marcin Cieslak
 */
/*
 * Below comment is separated from class comment so it does not get
 * automatically formatted.
 * 
 * Authentication flow goes like this: 1. User enters URL or clicks a link that
 * leads to LAMS server with /saml/access context. It is processed either by
 * doGet() or doPost(), but ultimately by access() method. There are one
 * required parameter "idp" which is used to find URL where to redirect the user
 * to log in.
 * 
 * 2. User gets redirected to IdP's login page.
 * 
 * 3. After successful authentication, an authentication assertion is sent to
 * /saml/authentication context. The assertion is processed by authenticate()
 * method. If user does not exist in LAMS, registerUser() gets called. User
 * details are extracted from assertion and previously created session. If user
 * exists, authenticateInternally() gets called. Group is updated if needed.
 * 
 * 4. An internal call is done to LoginRequestServlet. It does not go through
 * user browser, it is a server-server call. LoginRequestServelt returns a
 * redirect to j_security_check, which is automatically followed. After
 * successful log in into LAMS, cookies with current HTTP and system sessions
 * IDs are copied to response, which is send back to user, along with redirect
 * to main page.
 */
public class SamlServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(SamlServlet.class);

	// list of allowed issuers, with optional mapping to signature validator
	// based on its public certificate
	private static final Map<String, SignatureValidator> validIssuers = new HashMap<String, SignatureValidator>(
			1);
	// mapping between Identity Provider and authentication method: either
	// Authentication Request assertion or redirect
	private static final Map<String, String> idpAuthMethodMap = new TreeMap<String, String>();
	// mapping between Identity Provider name and URL where to authenticate
	private static final Map<String, String> idpUrlMap = new TreeMap<String, String>();
	// mapping between idp and Asssertion Consumer of server where user is
	// registered externally
	private static final Map<String, String> idpDefaultRelayStateMap = new TreeMap<String, String>();
	// mapping between Identity Provider name and URL where user should be
	// redirected after authentication
	private static final Map<String, String> idpExternalRegistrationUrlMap = new TreeMap<String, String>();

	// other tools
	private static final EmailValidator emailValidator = EmailValidator
			.getInstance();

	// properties loaded from LAMS database or project config file
	private static String baseContextPath;
	// use prefix in LoginRequest calls
	private static boolean useCoursePrefix = false;
	// should HTTPS protocol be required when accessing servlet
	private static boolean secureConnectionRequired = false;
	// should signature validation be performed at all
	private static boolean validationSignaturePerform = true;
	// should error be sent to user if signature validation fails
	private static boolean validationSignatureRequired = false;
	// should conditions validation be performed at all
	private static boolean validationConditionsPerform = true;
	// should error be sent to user if conditions validation fails
	private static boolean validationConditionsRequired = true;
	// should users not existing in LAMS DB be automatically registered
	private static boolean userRegistrationEnabled = true;

	// actions within SAML servlet
	private static final String CONTEXT_SAML = "saml/";
	private static final String CONTEXT_ACCESS = "access";
	private static final String CONTEXT_AUTHENTICATE = "authenticate";
	private static final String CONTEXT_REGISTER_EXTERNALLY = "registerExternally";
	private static final String CONTEXT_REGISTER_INTERNALLY = "registerInternally";
	
	private static final String AUTHENTICATION_METHOD_ASSERTION = "assertion";
	private static final String AUTHENTICATION_METHOD_REDIRECT = "redirect";

	private static final int PARSER_MAX_POOL_SIZE_DEFAULT_VALUE = 2;

	private static SessionFactory dbSessionFactory;

	static {

		SamlServlet.dbSessionFactory = new org.hibernate.cfg.Configuration()
				.configure().buildSessionFactory();
		// get properties 
		SamlServlet.baseContextPath = Config.getString(Config.BASE_URL); //"/webapp";//

		// set security properties
		try {
			SamlServlet.secureConnectionRequired = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.SECURE_CONNECTION_REQUIRED_KEY));
			SamlUtil.assertionEncryptionRequired = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.ASSERTION_ENCRYPTION_REQUIRED_KEY));
			SamlServlet.validationSignaturePerform = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.VALIDATION_SIGNATURE_PERFORM_KEY));
			SamlServlet.validationSignatureRequired = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.VALIDATION_SIGNATURE_REQUIRED_KEY));
			SamlServlet.validationConditionsPerform = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.VALIDATION_CONDITIONS_PERFORM_KEY));
			SamlServlet.validationConditionsRequired = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.VALIDATION_CONDITIONS_REQUIRED_KEY));
			SamlServlet.userRegistrationEnabled = Boolean
					.valueOf(SamlUtil
							.getConfigurationProperty(SamlUtil.USER_REGISTRATION_ENABLED));
			SamlServlet.useCoursePrefix = Boolean.valueOf(SamlUtil
					.getConfigurationProperty(SamlUtil.USE_COURSE_PREFIX));

			// set valid issuer names only; certificates are loaded when
			// processing assertion, if needed
			String issuerCertificateMapProperty = SamlUtil
					.getConfigurationProperty(SamlUtil.VALID_ISSUER_AND_CERTIFICATE_MAPPING_KEY);
			if (!StringUtils.isBlank(issuerCertificateMapProperty)) {
				String[] issuerCertificateMapEntries = issuerCertificateMapProperty
						.split(";");
				for (String issuerCertificateMapEntry : issuerCertificateMapEntries) {
					String[] issuerAndCertificate = issuerCertificateMapEntry
							.split("=");
					SamlServlet.validIssuers.put(issuerAndCertificate[0], null);
				}
			}

			SamlServlet.idpAuthMethodMap
					.putAll(SamlUtil
							.parseMapping(SamlUtil
									.getConfigurationProperty(SamlUtil.IDP_AUTH_METHOD_MAPPING)));
			SamlServlet.idpUrlMap.putAll(SamlUtil.parseMapping(SamlUtil
					.getConfigurationProperty(SamlUtil.IDP_URL_MAPPING_KEY)));
			SamlServlet.idpDefaultRelayStateMap
					.putAll(SamlUtil
							.parseMapping(SamlUtil
									.getConfigurationProperty(SamlUtil.IDP_DEFAULT_RELAY_STATE)));
			SamlServlet.idpExternalRegistrationUrlMap
					.putAll(SamlUtil
							.parseMapping(SamlUtil
									.getConfigurationProperty(SamlUtil.IDP_EXTERNAL_REGISTRATION_URL_MAPPING_KEY)));

		} catch (IOException e) {
			SamlServlet.log.error(
					"Error while reading SAML configuration properties.", e);
		}

		try {
			// initialize SAML tools
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			SamlServlet.log.error("Error while loading SAML configuration", e);
		}

		BasicParserPool parserPool = new BasicParserPool();
		try {
			String paserMaxPoolSizeProperty = SamlUtil
					.getConfigurationProperty(SamlUtil.PARSER_MAX_POOL_SIZE_KEY);
			parserPool
					.setMaxPoolSize(Integer.valueOf(paserMaxPoolSizeProperty));
		} catch (Exception e) {
			SamlServlet.log
					.error(
							"Error while loading parser max pool size property from config file, setting to default value: "
									+ SamlServlet.PARSER_MAX_POOL_SIZE_DEFAULT_VALUE,
							e);
			parserPool
					.setMaxPoolSize(SamlServlet.PARSER_MAX_POOL_SIZE_DEFAULT_VALUE);
		}
		parserPool.setNamespaceAware(true);
		// initialise SAML tools in the helper class
		SamlUtil.messageDecoder = new HTTPPostDecoder(parserPool);

		SamlUtil.marshallerFactory = Configuration.getMarshallerFactory();
		SamlUtil.authnRequestBuilder = new AuthnRequestBuilder();
		SamlUtil.issuerBuilder = new IssuerBuilder();
		SamlUtil.nameIdPolicyBuilder = new NameIDPolicyBuilder();

		// get keystore location and password
		try {
			/*String keystoreFilePath = System.getProperty(
					SamlUtil.JBOSS_CONFIG_URL_KEY).replace("file:", "")
					+ SamlUtil
							.getConfigurationProperty(SamlUtil.CERTIFICATE_KEYSTORE_FILE_KEY);*/
			String keystoreFilePath = SamlUtil
							.getConfigurationProperty(SamlUtil.CERTIFICATE_KEYSTORE_FILE_KEY);
			
			File keystoreFile = new File(keystoreFilePath);
			if (keystoreFile.canRead()) {
				FileInputStream keystoreStream = new FileInputStream(
						keystoreFile);
				KeyStore keystore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				String keystorePassword = SamlUtil
						.getConfigurationProperty(SamlUtil.CERTIFICATE_KEYSTORE_PASSWORD_KEY);
				char[] keystorePasswordChars = keystorePassword.toCharArray();
				keystore.load(keystoreStream, keystorePasswordChars);
				keystoreStream.close();
				SamlUtil.keystore = keystore;
			} else {
				if (SamlServlet.log.isDebugEnabled()) {
					SamlServlet.log.debug("No keystore found");
				}
			}
		} catch (Exception e) {
			SamlServlet.log.error("Error while loading keystore");
		}
	}

	/**
	 * Build AuthRequest using request parameters and redirect user to IdP
	 */
	private static void sendAuthnRequest(HttpServletRequest request,
			HttpServletResponse response, String consumerURL, String relayState)
			throws IOException {
		String idp = request.getParameter(SamlUtil.PARAM_IDP);
		if (StringUtils.isBlank(idp)) {
			response
					.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - don't know where to authenticate, missing 'idp' parameter");
			return;
		}

		String idpLoginPage = SamlServlet.idpUrlMap.get(idp);
		if (idpLoginPage == null) {
			response
					.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - don't know where to authenticate, no configured URL found for IdP: "
									+ idp);
			return;
		}

		String lamsIssuerName = SamlUtil
				.getConfigurationProperty(SamlUtil.LAMS_ISSUER_NAME_KEY);
		String lamsProviderName = SamlUtil
				.getConfigurationProperty(SamlUtil.LAMS_PROVIDER_NAME_KEY);

		String authnRequest = null;
		try {
			authnRequest = SamlUtil.buildAuthnRequest(idpLoginPage,
					consumerURL, lamsIssuerName, lamsProviderName);
		} catch (MarshallingException e) {
			throw new IOException("Error while building AuthnRequest", e);
		}

		String redirectURL = WebUtil.appendParameterToURL(idpLoginPage,
				SamlUtil.PARAM_SAML_REQUEST, authnRequest);

		if (relayState != null) {
			if (!relayState.startsWith("http")) {
				relayState = SamlServlet.baseContextPath +"/" + relayState;
			}
			String originalQueryString = request.getQueryString();
			if (!StringUtils.isBlank(originalQueryString)) {
				relayState = WebUtil.appendParameterDeliminator(relayState)
						+ originalQueryString;
			}
			relayState = URLEncoder.encode(relayState, "UTF-8");
			redirectURL = WebUtil.appendParameterToURL(redirectURL,
					SamlUtil.PARAM_RELAY_STATE, relayState);
		}

		SamlServlet.log.debug("Redirecting with authentication request to IdP: "
				+ idp);
		response.sendRedirect(redirectURL);
	}

	/**
	 * Entry point for accessing LAMS through this servlet.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/* For TEST
		 * String callBackURL = request.getParameter(SamlUtil.PARAM_CALL_BACK_IFRAME_URL);
		if (!StringUtils.isBlank(callBackURL)) {
			HttpSession jsession = request.getSession(true);
			jsession.setAttribute(Layout.EUREKA2_CALL_BACK_IFRAME, callBackURL);
		}*/
		
		
		String requestURI = request.getRequestURI();
		if (requestURI.endsWith(SamlServlet.CONTEXT_ACCESS)) {
		    access(request, response);
		} else if (requestURI.endsWith(SamlServlet.CONTEXT_REGISTER_EXTERNALLY)) {
		    registerExternally(request, response);
		} else if (requestURI.endsWith(SamlServlet.CONTEXT_REGISTER_INTERNALLY)) {
		    //registerInternally(request, response);
		} else if (requestURI.endsWith(SamlServlet.CONTEXT_AUTHENTICATE)) {
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
			    "SAML servlet request failed - only POST is supported for authentication");
		} else {
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "SAML servlet request failed - unknown action");
		}
		
/*		String action = request.getParameter(SamlUtil.PARAM_ACTION);

		if (action.equalsIgnoreCase(SamlServlet.CONTEXT_ACCESS)) {
			access(request, response);
		} else if (action
				.equalsIgnoreCase(SamlServlet.CONTEXT_REGISTER_EXTERNALLY)) {
			registerExternally(request, response);
			// } else if
			// (action.equalsIgnoreCase(SamlServlet.CONTEXT_REGISTER_INTERNALLY))
			// {
			// registerInternally(request, response);
		} else if (action.equalsIgnoreCase(SamlServlet.CONTEXT_AUTHENTICATE)) {
			response
					.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - only POST is supported for authentication");
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"SAML servlet request failed - unknow action");
		}*/
	}

	/**
	 * Entry point for accessing LAMS through this servlet and only method to
	 * consume incoming authentication assertion
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		if (requestURI.endsWith(SamlServlet.CONTEXT_ACCESS)) {
		    access(request, response);
		} else if (requestURI.endsWith(SamlServlet.CONTEXT_AUTHENTICATE)) {
		    authenticate(request, response);
		
		} else {
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "SAML servlet request failed - unknown action");
		}
/*		String action = request.getParameter(SamlUtil.PARAM_ACTION);

		if (action.equalsIgnoreCase(SamlServlet.CONTEXT_ACCESS)) {
			access(request, response);
		} else if (action.equalsIgnoreCase(SamlServlet.CONTEXT_AUTHENTICATE)) {
			authenticate(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"SAML servlet request failed - unknow action");
		}*/
	}

	/**
	 * Access LAMS or authenticate against given IdP if not done before
	 */
	private void access(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String ssid = (String) request.getSession(true).getAttribute(
				WebSessionData.EUREKA2_SESSION_ID_NAME);
		WebSessionData ws = getWebSessionData(ssid);

		if (ws != null) {
			HttpSession jsession = request.getSession(true);
			String hideHeaderIframe = request.getParameter(SamlUtil.PARAM_HIDE_HEADER_IFRAME);
			if (!StringUtils.isBlank(hideHeaderIframe)) {
				jsession.setAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME, hideHeaderIframe);
			}else{
				jsession.setAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME, "0");
			}
			String callBackURL = request.getParameter(SamlUtil.PARAM_CALL_BACK_IFRAME_URL);
			if (!StringUtils.isBlank(callBackURL)) {
				jsession.setAttribute(Layout.EUREKA2_CALL_BACK_IFRAME, callBackURL);
			}
			String targetURL = request.getParameter(SamlUtil.PARAM_TARGET);
			if (!StringUtils.isBlank(targetURL)) {
				loginAndRedirect(request, response, ws.getUsername(), targetURL);
			}
			else{
				loginAndRedirect(request, response, ws.getUsername());
			}
			
			
			return;
		}

		String idp = request.getParameter(SamlUtil.PARAM_IDP);
		if (StringUtils.isBlank(idp)) {
			response
					.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - do not know where to authenticate, missing 'idp' parameter");
			return;
		}

		String authMethod = SamlServlet.idpAuthMethodMap.get(idp);
		if (StringUtils.isBlank(authMethod)) {
			response
					.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - do not know how to authenticate, no idp -> auth method configured");
			return;
		}
		// get relay state or redirect URL for user to go to
		String relayStateParamName = SamlUtil.PARAM_RELAY_STATE;
		String relayState = request.getParameter(relayStateParamName);
		if (StringUtils.isBlank(relayState)) {
			relayStateParamName = SamlUtil.PARAM_TARGET;
			relayState = request.getParameter(relayStateParamName);
			if (StringUtils.isBlank(relayState)) {
				relayState = SamlServlet.idpDefaultRelayStateMap.get(idp);
			}
			
		}
		String hideHeaderIframe = request.getParameter(SamlUtil.PARAM_HIDE_HEADER_IFRAME);
		if (!StringUtils.isBlank(hideHeaderIframe)) {
			HttpSession jsession = request.getSession(true);
			jsession.setAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME, hideHeaderIframe);
		}else{
			HttpSession jsession = request.getSession(true);
			jsession.setAttribute(Layout.EUREKA2_HIDE_HEADER_IFRAME, "0");
		}
		String callBackURL = request.getParameter(SamlUtil.PARAM_CALL_BACK_IFRAME_URL);
		if (!StringUtils.isBlank(callBackURL)) {
			HttpSession jsession = request.getSession(true);
			jsession.setAttribute(Layout.EUREKA2_CALL_BACK_IFRAME, callBackURL);
		}
		
		
		if (authMethod.equals(SamlServlet.AUTHENTICATION_METHOD_REDIRECT)) {
			relayState = URLEncoder.encode(relayState, "UTF-8");
			String url = WebUtil.appendParameterToURL(SamlServlet.idpUrlMap
					.get(idp), relayStateParamName, relayState);
			response.sendRedirect(url);
			return;
		}

		if (authMethod.equals(SamlServlet.AUTHENTICATION_METHOD_ASSERTION)) {
			// authenticate user externally; send only over secure connection
			String secureLamsServerUrl = (SamlServlet.baseContextPath ).replace(
					"http:", "https:");
			String consumerURL = new StringBuilder(secureLamsServerUrl).append(
					SamlServlet.CONTEXT_SAML).append(
					SamlServlet.CONTEXT_AUTHENTICATE).toString();
			SamlServlet.sendAuthnRequest(request, response, consumerURL,
					relayState);
		}
	}

	
	public static String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
	
	/**
	 * Authentication Assertion Consumer
	 */
	private void authenticate(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DateTime assertionReceivedTime = new DateTime(System
				.currentTimeMillis());
		// check if HTTPS has been used and if it is required
		SamlServlet.log.debug("Connection is secure: " + request.isSecure());
		if (SamlServlet.secureConnectionRequired && !request.isSecure()) {
			response
					.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - secure connection (HTTPS) is required");
			return;
		}
	//	SamlServlet.log.info("...........");
	//	String postBody = getBody(request);
	//	SamlServlet.log.info(postBody );
	//	String responseMessage = request.getParameter("SAMLResponse");
    //	log.info("DEBUG:" + responseMessage);
    	
		
		
		// read SAML response and extract assertion
		Assertion assertion = null;
		try {
			assertion = SamlUtil.getAssertion(request);
			if (assertion == null) {
				throw new Exception("Assertion could not be read");
			}
		} catch (Exception e) {
			SamlServlet.log.error(
					"Error while reading authentication assertion", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"SAML servlet request failed - error while reading authentication assertion: "
							+ e.getMessage());
			return;
		}

		if (SamlServlet.log.isDebugEnabled()) {
			SamlUtil.logAssertion(assertion);
		}

		String issuer = assertion.getIssuer().getValue();

		boolean issuerValidated = SamlServlet.validIssuers.containsKey(issuer);
		if (issuerValidated) {
			SamlServlet.log.debug("Issuer \"" + issuer
					+ "\" is in valid issuer list");
		} else {
			response
					.sendError(HttpServletResponse.SC_UNAUTHORIZED,
							"SAML servlet request failed - issuer is not in valid issuer list");
			return;
		}

		if (SamlServlet.validationSignaturePerform) {
			boolean signatureValidated = validateSignature(issuer, assertion
					.getSignature());
			if (!signatureValidated && SamlServlet.validationSignatureRequired) {
				response
						.sendError(HttpServletResponse.SC_UNAUTHORIZED,
								"SAML servlet request failed - could not validate the issuer signature");
				return;
			}
		} else {
			SamlServlet.log.debug("Skipping signature validation");
		}

		if (SamlServlet.validationConditionsPerform) {
			boolean conditionsValidated = SamlUtil.validateConditions(assertion
					.getConditions(), assertionReceivedTime);
			if (!conditionsValidated
					&& SamlServlet.validationConditionsRequired) {
				response
						.sendError(HttpServletResponse.SC_UNAUTHORIZED,
								"SAML servlet request failed - conditions validation error");
				return;
			}
		} else {
			SamlServlet.log.debug("Skipping conditions validation");
		}

		String userName = SamlUtil.getUserName(assertion);

		boolean userExists = userExists(userName);

		if (userExists) {
			SamlServlet.log.debug("Logging in existing SAML user " + userName);
			loginAndRedirect(request, response, userName);
		} else if (SamlServlet.userRegistrationEnabled) {
			SamlServlet.log.debug("Registering new SAML user " + userName);
			registerInternally(assertion, request, response);
		} else {
			SamlServlet.log.error("User \"" + userName
					+ "\" does not exist and automatic registration is off");
			response
					.sendError(
							HttpServletResponse.SC_UNAUTHORIZED,
							"SAML servlet request failed - user \""
									+ userName
									+ "\" does not exist and automatic registration is off");
		}
	}

	
	/**
	 * Registers user on external system, authenticating against IdP first
	 */
	private void registerExternally(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String idp = request.getParameter(SamlUtil.PARAM_IDP);
		if (StringUtils.isBlank(idp)) {
			response
					.sendError(
							HttpServletResponse.SC_BAD_REQUEST,
							"SAML servlet request failed - don't know where to authenticate, missing 'idp' parameter");
			return;
		}

		String consumerURL = SamlServlet.idpExternalRegistrationUrlMap.get(idp);
		String internalRegistrationURL = SamlServlet.CONTEXT_SAML
				+ SamlServlet.CONTEXT_REGISTER_INTERNALLY;
		// this is where the user will be redirected after coming back from
		// registration on external system
		String finalRedirectURL = SamlServlet.idpDefaultRelayStateMap.get(idp);
		if (finalRedirectURL != null) {
			if (!finalRedirectURL.startsWith("http")) {
				finalRedirectURL = SamlServlet.baseContextPath +"/"  + finalRedirectURL;
			}
			finalRedirectURL = URLEncoder.encode(finalRedirectURL, "UTF-8");
			internalRegistrationURL = WebUtil.appendParameterToURL(
					internalRegistrationURL, SamlUtil.PARAM_RELAY_STATE,
					finalRedirectURL);
		}

		SamlServlet.sendAuthnRequest(request, response, consumerURL,
				internalRegistrationURL);
	}

	/**
	 * Extract user details from assertion, internally call LoginServlet and
	 * register user.
	 */
	private void registerInternally(Assertion assertion,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// extract required info from assertion
		String userName = SamlUtil.getUserName(assertion);
		String firstName = SamlUtil.getAttributeValue(assertion,
				SamlUtil.ASSERTION_ATTRIBUTE_FIRST_NAME);
		String lastName = SamlUtil.getAttributeValue(assertion,
				SamlUtil.ASSERTION_ATTRIBUTE_LAST_NAME);
		String email = SamlUtil.getAttributeValue(assertion,
				SamlUtil.ASSERTION_ATTRIBUTE_EMAIL);

		if (StringUtils.isBlank(email)) {
			email = userName;
			SamlServlet.log.debug("Using user name as email: " + userName);
		}
		if (!SamlServlet.emailValidator.isValid(email)) {
			SamlServlet.log
					.error("Email is not in a valid format, registration failed.");
			response
					.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"SAML servlet request failed - no valid email found, unable to register new user");
			return;
		}
		if (StringUtils.isBlank(firstName)) {
			firstName = ".";
			SamlServlet.log.debug("Using \".\" as first name for user: "
					+ userName);
		}
		if (StringUtils.isBlank(lastName)) {
			lastName = ".";
			SamlServlet.log.debug("Using \".\" as last name for user: "
					+ userName);
		}

		saveUserToDB(request, userName, firstName, lastName, email);

		loginAndRedirect(request, response, userName);

	}

	/**
	 * Validate issuer signature against stored certificate
	 */
	private boolean validateSignature(String issuer, Signature signature) {
		if (SamlUtil.keystore == null) {
			SamlServlet.log
					.warn("Keystore was not initialised, can not validate signature");
			return false;
		}

		try {
			SignatureValidator signatureValidator = SamlServlet.validIssuers
					.get(issuer);
			// no mapping issuer -> certificate has been found, try to add it
			if (signatureValidator == null) {
				// find certificate alias according to mapping provided in
				// config.properties file
				String certificateAlias = null;
				String issuerCertificateMapping = SamlUtil
						.getConfigurationProperty(SamlUtil.VALID_ISSUER_AND_CERTIFICATE_MAPPING_KEY);
				if (!StringUtils.isBlank(issuerCertificateMapping)) {
					String[] issuerCertificateMappingEntries = issuerCertificateMapping
							.split(";");
					for (String issuerCertificateMappingEntry : issuerCertificateMappingEntries) {
						String[] issuerAndCertificate = issuerCertificateMappingEntry
								.split("=");
						if (issuer.equalsIgnoreCase(issuerAndCertificate[0])) {
							certificateAlias = issuerAndCertificate[1];
							break;
						}
					}
				}
				if (certificateAlias == null) {
					SamlServlet.log
							.warn("Mapping between the assertion issuer and certificate alias not found.");
					return false;
				}

				KeyStore.TrustedCertificateEntry tcEntry = (KeyStore.TrustedCertificateEntry) SamlUtil.keystore
						.getEntry(certificateAlias, null);
				if (tcEntry == null) {
					SamlServlet.log
							.error("Could not find a certificate in keystore with alias: "
									+ certificateAlias);
					return false;
				}
				X509Certificate certificate = (X509Certificate) tcEntry
						.getTrustedCertificate();
				BasicX509Credential credential = new BasicX509Credential();
				credential.setEntityCertificate(certificate);
				signatureValidator = new SignatureValidator(credential);
				SamlServlet.validIssuers.put(issuer, signatureValidator);
			}

			signatureValidator.validate(signature);
			SamlServlet.log.debug("Signature from issuer \" " + issuer
					+ "\" was validated successfully");
			return true;
		} catch (Exception e) {
			SamlServlet.log.error(
					"Error occured while validating signature of assertion", e);
		}
		return false;
	}

	private School getFirstSchoolByName(String schName) {
		Session ss = dbSessionFactory.getCurrentSession();
		ss.beginTransaction();

		Query q = ss.createQuery(
				"SELECT s FROM School AS s " + " WHERE s.des = :des")
				.setString("des", schName).setMaxResults(1);

		School s = (School) q.uniqueResult();
		ss.getTransaction().commit();

		if (s != null) {
			return s;
		} else {
			return null;
		}
	}

	private SysRole getSysRoleByName(String sysRoleName) {
		Session ss = dbSessionFactory.getCurrentSession();
		ss.beginTransaction();

		Query q = ss.createQuery(
				"SELECT s FROM SysRole AS s " + " WHERE s.name = :name")
				.setString("name", sysRoleName).setMaxResults(1);

		SysRole s = (SysRole) q.uniqueResult();
		ss.getTransaction().commit();

		if (s != null) {
			return s;
		} else {
			return null;
		}
	}

	private void saveUserToDB(HttpServletRequest request, String userName,
			String firstName, String lastName, String email) {
		log.debug("saving user to DB username=" + userName);
		User u = new User();
		u.setCreateDate(new Date());
		u.setModifyDate(u.getCreateDate());
		u.setEnabled(true);
		u.setEmail(email);

		u.setFirstName(firstName);
		u.setIp(request.getRemoteAddr());

		u.setLastName(lastName);
		u.setUsername(userName);
		u.setRemarks("SSO created");

		u.setSchool(getFirstSchoolByName(PredefinedNames.SCHOOL_OTHERS));
		u.setSysRole(getSysRoleByName(PredefinedNames.SYSROLE_USER));

		Session ss = dbSessionFactory.getCurrentSession();
		ss.beginTransaction();
		ss.saveOrUpdate(u);
		ss.getTransaction().commit();
	}

	private WebSessionData getWebSessionData(String sessionId) {
		log.debug("getting sessionId=" + sessionId);
		WebSessionData ws = null;
		Session session = dbSessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session.createQuery(
					"SELECT ws FROM WebSessionData AS ws "
							+ " WHERE ws.id = :sessionId").setString(
					"sessionId", sessionId);
			ws = (WebSessionData) q.uniqueResult();

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return ws;
	}
	
	private boolean deleteWebSessionData(String sessionId) {
		boolean result = false;
		log.debug("deleting sessionId=" + sessionId);
		WebSessionData ws = null;
		Session session = dbSessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query q = session.createQuery(
					"SELECT ws FROM WebSessionData AS ws "
							+ " WHERE ws.id = :sessionId").setString(
					"sessionId", sessionId);
			ws = (WebSessionData) q.uniqueResult();
			if(ws!=null){
				session.delete(ws);
				result = true;
			}
			
			tx.commit();
			
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	private void addWebSessionData(String username, String jsessionID,
			String remoteIP) {
		log.debug("Add username=" + username + ", jsessionID=" + jsessionID + ", remoteIP="+remoteIP);
		Session session = dbSessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			WebSessionData wsData = new WebSessionData();
			wsData.setId(jsessionID);
			wsData.setUsername(username);
			wsData.setLoginTime(new Date());
			wsData.setLastActiveTime(new Date());
			wsData.setIp(remoteIP);

			session.save(wsData);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	private void updateWebSessionData(String wsDataID, Date time) {
		log.debug("Update wsData=" + wsDataID);
		Session session = dbSessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			WebSessionData wsData = (WebSessionData) session.get(
					WebSessionData.class, wsDataID);
			wsData.setLastActiveTime(time);
			session.update(wsData);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	/**
	 * Check if an external user exists in LAMS database
	 */
	private boolean userExists(String username) {
		SamlServlet.log.debug("Checking if SAML user exists in Eureka: "
				+ username);
		Session ss = dbSessionFactory.getCurrentSession();
		ss.beginTransaction();

		Query q = ss.createQuery(
				"SELECT u FROM User AS u" + " WHERE u.username = :username")
				.setString("username", username);

		User u = (User) q.uniqueResult();
		ss.getTransaction().commit();

		if (u != null) {
			return true;
		} else {
			return false;
		}
	}

	
	
	private void loginAndRedirect(HttpServletRequest request,
			HttpServletResponse response, String username) throws IOException {

		// find existing session
/*		HttpSession jsession = request.getSession(true);
		
		String ssid = (String) jsession.getAttribute(
				WebSessionData.EUREKA2_SESSION_ID_NAME);
		if(ssid==null){
			ssid = jsession.getId();
			jsession.setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ssid);
		}
		WebSessionData wsData = getWebSessionData(ssid);

		if (wsData == null) {
			addWebSessionData(username, ssid, request.getRemoteAddr());
		} else {
			updateWebSessionData(wsData.getId(), new Date());
		}
*/
		//get session
		HttpSession jsession = request.getSession(true);
		String ssid = jsession.getId();
		//check session exist, if yes, clear and create new one (in case login as new username)
		deleteWebSessionData(ssid);
		
		addWebSessionData(username, ssid, request.getRemoteAddr());
		jsession.setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ssid);
		
		
		String redirectURL = request.getParameter("RelayState");
		if (redirectURL == null || redirectURL.isEmpty()) {
			response.sendRedirect(baseContextPath );
		} else {
			if (!redirectURL.startsWith("http")) {
				redirectURL = SamlServlet.baseContextPath +"/" + redirectURL;
			}
			response.sendRedirect(redirectURL);
		}

	}
	
	private void loginAndRedirect(HttpServletRequest request,
			HttpServletResponse response, String username, String targetURL) throws IOException {

		//get session
		HttpSession jsession = request.getSession(true);
		String ssid = jsession.getId();
		//check session exist, if yes, clear and create new one (in case login as new username)
		deleteWebSessionData(ssid);
		
		addWebSessionData(username, ssid, request.getRemoteAddr());
		jsession.setAttribute(WebSessionData.EUREKA2_SESSION_ID_NAME, ssid);
		
		
		
		if (targetURL == null || targetURL.isEmpty()) {
			response.sendRedirect(baseContextPath );
		} else {
			if (!targetURL.startsWith("http")) {
				targetURL = SamlServlet.baseContextPath +"/" + targetURL;
			}
			response.sendRedirect(targetURL);
		}

	}

}