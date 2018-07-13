package ntu.celt.eUreka2.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.server.UID;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.joda.time.DateTime;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SamlUtil {
    private static Logger log = Logger.getLogger(SamlUtil.class);

    public static boolean assertionEncryptionRequired = false;

    // SAML tools for assertion processing
    public static final Properties samlConfigurationProperties = new Properties();
    public static HTTPPostDecoder messageDecoder;
    public static MarshallerFactory marshallerFactory;
    public static AuthnRequestBuilder authnRequestBuilder;
    public static IssuerBuilder issuerBuilder;
    public static NameIDPolicyBuilder nameIdPolicyBuilder;
    public static KeyStore keystore;

    private static final String CONFIGURATION_FILE_NAME = "samlConfig.properties";

    // resource keys from configuration file
    public static final String SECURE_CONNECTION_REQUIRED_KEY = "secure.connection.required";
    public static final String INTEGRATED_SERVER_ID_KEY = "integrated.server.id";
    public static final String USER_REGISTRATION_ENABLED = "user.registration.enabled";
    public static final String USE_COURSE_PREFIX = "use.course.prefix";
    public static final String PARSER_MAX_POOL_SIZE_KEY = "paser.pool.max.size";
    public static final String VALID_ISSUER_AND_CERTIFICATE_MAPPING_KEY = "issuer.certificate.mapping";
    public static final String LAMS_ISSUER_NAME_KEY = "lams.issuer.name";
    public static final String LAMS_PROVIDER_NAME_KEY = "lams.provider.name";
    public static final String IDP_AUTH_METHOD_MAPPING = "idp.auth.method.mapping";
    public static final String IDP_URL_MAPPING_KEY = "idp.url.mapping";
    public static final String IDP_EXTERNAL_REGISTRATION_URL_MAPPING_KEY = "idp.external.registration.url.mapping";
    public static final String IDP_DEFAULT_RELAY_STATE = "idp.default.relay.state";
    public static final String CERTIFICATE_KEYSTORE_FILE_KEY = "certificate.keystore.file";
    public static final String CERTIFICATE_KEYSTORE_PASSWORD_KEY = "certificate.keystore.password";
    public static final String ASSERTION_DECRYPT_KEY_ALIAS_KEY = "assertion.decrypt.key.alias";
    public static final String ASSERTION_ENCRYPTION_REQUIRED_KEY = "assertion.encryption.required";
    public static final String VALIDATION_SIGNATURE_PERFORM_KEY = "validation.signature.perform";
    public static final String VALIDATION_SIGNATURE_REQUIRED_KEY = "validation.signature.required";
    public static final String VALIDATION_CONDITIONS_PERFORM_KEY = "validation.conditions.perform";
    public static final String VALIDATION_CONDITIONS_REQUIRED_KEY = "validation.conditions.required";
    public static final String JBOSS_CONFIG_URL_KEY = "jboss.server.config.url";

    // request parameters names
    public static final String PARAM_SAML_REQUEST = "SAMLRequest";
    public static final String PARAM_SAML_RESPONSE = "SAMLResponse";
    public static final String PARAM_RELAY_STATE = "RelayState";
    public static final String PARAM_TARGET = "TARGET";
    public static final String PARAM_HIDE_HEADER_IFRAME = "HideHeader";
    public static final String PARAM_CALL_BACK_IFRAME_URL = "CallBackURL";

    public static final String PARAM_LOGIN_REQUEST_PARAM_HASH = "loginRequestParamHash";
    public static final String PARAM_IDP = "idp";
   // public static final String PARAM_ACTION = "a";

    // attributes of authentication assertion
    public static final String ASSERTION_ATTRIBUTE_USER_NAME = "userName";
    public static final String ASSERTION_ATTRIBUTE_FIRST_NAME = "firstName";
    public static final String ASSERTION_ATTRIBUTE_LAST_NAME = "lastName";
    public static final String ASSERTION_ATTRIBUTE_EMAIL = "mail";
    public static final Map<String, String[]> assertionAttributeAliases = Collections
	    .synchronizedMap(new TreeMap<String, String[]>());

    static {
	// fill aliases for attributes
	
	SamlUtil.assertionAttributeAliases.put(SamlUtil.ASSERTION_ATTRIBUTE_USER_NAME,
		new String[] { "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified/cn" });
	SamlUtil.assertionAttributeAliases.put(SamlUtil.ASSERTION_ATTRIBUTE_FIRST_NAME,
		new String[] { "givenName", "urn_oid_2.5.4.42", "urn:oid:2.5.4.42",
			"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified/firstname" });
	SamlUtil.assertionAttributeAliases.put(SamlUtil.ASSERTION_ATTRIBUTE_LAST_NAME,
		new String[] { "surname", "urn_oid_2.5.4.42", "urn:oid:2.5.4.42",
			"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified/lastname",
			"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified/displayName", });
	SamlUtil.assertionAttributeAliases.put(SamlUtil.ASSERTION_ATTRIBUTE_EMAIL,
		new String[] { "mail", "urn_oid_0.9.2342.19200300.100.1.3", "urn:oid:0.9.2342.19200300.100.1.3",
			"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified/email" });
    }

    /**
     * Build a SAML AuthnRequest object
     */
    public static String buildAuthnRequest(String destination, String consumerURL, String issuer, String providerName)
	    throws MarshallingException, IOException {
		Issuer issuerObject = SamlUtil.issuerBuilder.buildObject();
		issuerObject.setValue(issuer);
	
		NameIDPolicy nameIdPolicy = SamlUtil.nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat(NameIDType.EMAIL);
		nameIdPolicy.setAllowCreate(true);
	
		AuthnRequest authnRequest = SamlUtil.authnRequestBuilder.buildObject();
		authnRequest.setID(new UID().toString());
		authnRequest.setIssueInstant(new DateTime());
		authnRequest.setDestination(destination);
		authnRequest.setAssertionConsumerServiceURL(consumerURL);
		authnRequest.setIssuer(issuerObject);
		authnRequest.setNameIDPolicy(nameIdPolicy);
		authnRequest.setProviderName(providerName);
		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
	
		Marshaller marshaller = SamlUtil.marshallerFactory.getMarshaller(authnRequest);
		Element authDOM = marshaller.marshall(authnRequest);
	
		StringWriter stringWriter = new StringWriter();
		XMLHelper.writeNode(authDOM, stringWriter);
		String messageXML = stringWriter.toString();
	
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		deflaterOutputStream.write(messageXML.getBytes());
		deflaterOutputStream.close();
		String samlRequest = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
		return URLEncoder.encode(samlRequest, "UTF-8");
    }

    /**
     * Extract parameter value from URL
     */
    public static String extractParameterValue(String url, String param) {
		if (!StringUtils.isBlank(url) && !StringUtils.isBlank(param)) {
		    int quotationMarkIndex = url.indexOf("?");
		    String queryPart = quotationMarkIndex > -1 ? url.substring(quotationMarkIndex + 1) : url;
		    String[] paramEntries = queryPart.split("&");
		    for (String paramEntry : paramEntries) {
			String[] paramEntrySplitted = paramEntry.split("=");
			if (param.equalsIgnoreCase(paramEntrySplitted[0])) {
			    return paramEntrySplitted[1];
			}
		    }
		}
		return null;
    }

    /**
     * Read authentication assertion from HTTP response sent by SAML server
     *
     * @throws DataFormatException
     * @throws ServletException
     * @throws UnmarshallingException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    @SuppressWarnings("rawtypes")
    public static Assertion getAssertion(HttpServletRequest request) throws MessageDecodingException, SecurityException,
	    DataFormatException, ServletException, UnmarshallingException, ParserConfigurationException, SAXException, IOException {
		MessageContext messageContext = new BasicSAMLMessageContext();
		messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		SamlUtil.messageDecoder.decode(messageContext);
		
		Element responseDOMElement = messageContext.getInboundMessage().getDOM();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(responseDOMElement);
		Response samlResponse = (Response) unmarshaller.unmarshall(responseDOMElement);
		
		
    	/*String responseMessage = request.getParameter("SAMLResponse");
    	
    	//log.info("DEBUG:" + responseMessage);
    	
    	byte[] base64DecodedResponse = Base64.decode(responseMessage);
    	ByteArrayInputStream is = new ByteArrayInputStream(base64DecodedResponse);

    	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    	documentBuilderFactory.setNamespaceAware(true);
    	DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

    	Document document = docBuilder.parse(is);
    	Element element = document.getDocumentElement();
    	UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
    	Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
    	XMLObject responseXmlObj = unmarshaller.unmarshall(element);
    	
    	Response samlResponse = (Response) responseXmlObj;
    	*/
	
		String status = samlResponse.getStatus().getStatusCode().getValue();
		if (!StatusCode.SUCCESS_URI.equals(status)) {
		    throw new ServletException("Response status indicates that authentication was not a success: " + status);
		}
	
		Assertion assertion = null;
		List<EncryptedAssertion> encryptedAssertions = samlResponse.getEncryptedAssertions();
		if (encryptedAssertions.size() > 0) {
		    if (encryptedAssertions.size() > 1) {
			SamlUtil.log.warn("Received message contains multiple assertions, but processing only the first one.");
		    }
		    assertion = SamlUtil.decryptAssertion(encryptedAssertions.get(0));
	
		} else if (SamlUtil.assertionEncryptionRequired) {
		    throw new SecurityException("Assertion is not encrypted and it is required");
		} else {
		    List<Assertion> assertions = samlResponse.getAssertions();
		    if (assertions.size() > 1) {
			SamlUtil.log.warn("Received message contains multiple assertions, but processing only the first one.");
		    }
		    assertion = assertions.get(0);
		}
		return assertion;
    }

    private static Assertion decryptAssertion(EncryptedAssertion encryptedAssertion) {
		try {
		    String alias = SamlUtil.getConfigurationProperty(SamlUtil.ASSERTION_DECRYPT_KEY_ALIAS_KEY);
		    String keystorePassword = SamlUtil.getConfigurationProperty(SamlUtil.CERTIFICATE_KEYSTORE_PASSWORD_KEY);
		    PrivateKey privateKey = (PrivateKey) SamlUtil.keystore.getKey(alias, keystorePassword.toCharArray());
		    if (privateKey == null) {
			SamlUtil.log.error("Could not find a key in keystore with alias: " + alias);
			return null;
		    }
	
		    // Create the credentials.
		    BasicX509Credential decryptionCredential = new BasicX509Credential();
		    decryptionCredential.setPrivateKey(privateKey);
	
		    // Create a decrypter.
		    Decrypter decrypter = new Decrypter(null, new StaticKeyInfoCredentialResolver(decryptionCredential),
			    new InlineEncryptedKeyResolver());
		    decrypter.setRootInNewDocument(true);
		    Assertion assertion = decrypter.decrypt(encryptedAssertion);
	
		    if (SamlUtil.log.isDebugEnabled()) {
			SamlUtil.log.debug("Successfully decrypted assertion");
		    }
		    return assertion;
		} catch (Exception e) {
		    SamlUtil.log.error("Error while decrypting assertion", e);
		    return null;
		}
    }

    /**
     * Prints assertion contents.
     */
    public static void logAssertion(Assertion assertion) {
		if (SamlUtil.log.isDebugEnabled()) {
		    SamlUtil.log.debug("--- START LOG ASSERTION ---");
		    try {
			Element domRoot = assertion.getDOM();
			OutputFormat format = new OutputFormat(domRoot.getOwnerDocument());
			format.setIndenting(true);
			StringWriter writer = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(writer, format);
			serializer.serialize(domRoot.getOwnerDocument());
			SamlUtil.log.debug(writer.toString());
		    } catch (Exception e) {
			SamlUtil.log.error("Error while printing out assertion.", e);
		    }
		    SamlUtil.log.debug("--- END LOG ASSERTION ---");
		}
    }

    /**
     * Parses mapping from configuration.
     */
    public static Map<String, String> parseMapping(String property) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		if (!StringUtils.isBlank(property)) {
		    String[] mapEntries = property.split(";");
		    for (String mapEntry : mapEntries) {
			// mapping value can also contain '=' so find only the first one,
			// which is the mapping assignment character
			int mappingAssignmentCharIndex = mapEntry.indexOf("=");
			if (mappingAssignmentCharIndex < 0) {
			    if (SamlUtil.log.isDebugEnabled()) {
				SamlUtil.log.error(
					"Malformed mapping, no '=' character found, skipping following entry: " + mapEntry);
			    }
			} else {
			    String key = mapEntry.substring(0, mappingAssignmentCharIndex);
			    String value = mapEntry.substring(mappingAssignmentCharIndex + 1);
			    result.put(key, value);
			}
		    }
		}
		return result;
    }

    public static String getAttributeName(Attribute attribute) {
	String name = attribute.getName();
	synchronized (SamlUtil.assertionAttributeAliases) {
	    for (Entry<String, String[]> entry : SamlUtil.assertionAttributeAliases.entrySet()) {
		for (String alias : entry.getValue()) {
		    if (alias.equals(name)) {
			return entry.getKey();
		    }
		}
	    }
	}
	return null;
    }

    public static String getUserName(Assertion assertion) {
		String userName = SamlUtil.getAttributeValue(assertion, SamlUtil.ASSERTION_ATTRIBUTE_USER_NAME);
		if (userName == null) {
		    //return null;
			userName = assertion.getSubject().getNameID().getValue();
		}
		return userName;
    }

    public static String getAttributeValue(Attribute attribute) {
	XMLObject valueObject = attribute.getAttributeValues().get(0);
	if (valueObject instanceof XSAny) {
	    return ((XSAny) valueObject).getTextContent();
	}
	if (valueObject instanceof XSStringImpl) {
	    return ((XSStringImpl) valueObject).getValue();
	}
	return null;
    }

    public static String getAttributeValue(Assertion assertion, String attributeName) {
	for (Statement statement : assertion.getStatements()) {
	    if (statement instanceof AttributeStatement) {
		for (Attribute attribute : ((AttributeStatement) statement).getAttributes()) {
		    String name = SamlUtil.getAttributeName(attribute);
		    if (attributeName.equals(name)) {
			return SamlUtil.getAttributeValue(attribute);
		    }
		}
	    }
	}
	return null;
    }

    /**
     * Check conditions set in assertion
     */
    public static boolean validateConditions(Conditions conditions, DateTime assertionReceivedTime) {
	DateTime notBefore = conditions.getNotBefore();
	if (notBefore != null) {
	    if (assertionReceivedTime.isBefore(notBefore)) {
		SamlUtil.log.error("Assertion received time: " + assertionReceivedTime
			+ " does not conform to \"notBefore\" condition " + notBefore);

		return false;
	    }
	    if (SamlUtil.log.isDebugEnabled()) {
		SamlUtil.log.debug("Assertion received time: " + assertionReceivedTime
			+ " conforms to \"notBefore\" condition: " + notBefore);
	    }
	}
	DateTime notOnOrAfter = conditions.getNotOnOrAfter();
	if (notOnOrAfter != null) {
	    if (assertionReceivedTime.isAfter(notOnOrAfter) || assertionReceivedTime.isEqual(notOnOrAfter)) {
		SamlUtil.log.error("Assertion received time: " + assertionReceivedTime
			+ " does not conform to \"notOnOrAfter\" condition: " + notOnOrAfter);
		return false;
	    }
	    SamlUtil.log.debug("Assertion received time: " + assertionReceivedTime
		    + " conforms to \"notOnOrAfter\" condition: " + notOnOrAfter);
	}

	return true;
    }

    /**
     * Read configuration file from WAR and extract given property
     */
    public static String getConfigurationProperty(String key) throws IOException {
	if (SamlUtil.samlConfigurationProperties.isEmpty()) {
		Resource r = new ClasspathResource(SamlUtil.CONFIGURATION_FILE_NAME);
		
	    InputStream propertyInputStream = r.openStream();//SamlServlet.class.getResourceAsStream(SamlUtil.CONFIGURATION_FILE_NAME);
	    if (propertyInputStream == null) {
		throw new IOException("Can not find SAML configuration properties file");
	    }
	    SamlUtil.samlConfigurationProperties.load(propertyInputStream);
	    if (SamlUtil.log.isDebugEnabled()) {
		SamlUtil.log.debug("Loaded SAML configuration properties file");
	    }
	}
	return SamlUtil.samlConfigurationProperties.getProperty(key);
    }
}