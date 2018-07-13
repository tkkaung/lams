package ntu.celt.eUreka2.services;

import java.io.IOException;

import ntu.celt.eUreka2.dao.AnalyticDAO;
import ntu.celt.eUreka2.dao.AnalyticDAOImpl;
import ntu.celt.eUreka2.dao.AuditTrailDAO;
import ntu.celt.eUreka2.dao.AuditTrailDAOImpl;
import ntu.celt.eUreka2.dao.ModuleDAO;
import ntu.celt.eUreka2.dao.ModuleDAOImpl;
import ntu.celt.eUreka2.dao.PreferenceDAO;
import ntu.celt.eUreka2.dao.PreferenceDAOImpl;
import ntu.celt.eUreka2.dao.PrivilegeDAO;
import ntu.celt.eUreka2.dao.PrivilegeDAOImpl;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAO;
import ntu.celt.eUreka2.dao.ProjExtraInfoDAOImpl;
import ntu.celt.eUreka2.dao.ProjRoleDAO;
import ntu.celt.eUreka2.dao.ProjRoleDAOImpl;
import ntu.celt.eUreka2.dao.ProjStatusDAO;
import ntu.celt.eUreka2.dao.ProjStatusDAOImpl;
import ntu.celt.eUreka2.dao.ProjTypeDAO;
import ntu.celt.eUreka2.dao.ProjTypeDAOImpl;
import ntu.celt.eUreka2.dao.ProjectDAO;
import ntu.celt.eUreka2.dao.ProjectDAOImpl;
import ntu.celt.eUreka2.dao.RedirectException;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAO;
import ntu.celt.eUreka2.dao.SchlTypeAnnouncementDAOImpl;
import ntu.celt.eUreka2.dao.SchoolDAO;
import ntu.celt.eUreka2.dao.SchoolDAOImpl;
import ntu.celt.eUreka2.dao.SysRoleDAO;
import ntu.celt.eUreka2.dao.SysRoleDAOImpl;
import ntu.celt.eUreka2.dao.SysAnnouncementDAO;
import ntu.celt.eUreka2.dao.SysAnnouncementDAOImpl;
import ntu.celt.eUreka2.dao.WebSessionDAO;
import ntu.celt.eUreka2.dao.WebSessionDAOImpl;
import ntu.celt.eUreka2.dao.UserDAO;
import ntu.celt.eUreka2.dao.UserDAOImpl;
import ntu.celt.eUreka2.internal.Config;
import ntu.celt.eUreka2.internal.DatabaseInitializer;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAO;
import ntu.celt.eUreka2.modules.announcement.AnnouncementDAOImpl;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAO;
import ntu.celt.eUreka2.modules.assessment.AssessmentDAOImpl;
import ntu.celt.eUreka2.modules.backuprestore.BackupRestoreDAO;
import ntu.celt.eUreka2.modules.backuprestore.BackupRestoreDAOImpl;
import ntu.celt.eUreka2.modules.backuprestore.Migrate2DAO;
import ntu.celt.eUreka2.modules.backuprestore.Migrate2DAOImpl;
import ntu.celt.eUreka2.modules.backuprestore.MigrateDAO;
import ntu.celt.eUreka2.modules.backuprestore.MigrateDAOImpl;
import ntu.celt.eUreka2.modules.blog.BlogDAO;
import ntu.celt.eUreka2.modules.blog.BlogDAOImpl;
import ntu.celt.eUreka2.modules.budget.BudgetDAO;
import ntu.celt.eUreka2.modules.budget.BudgetDAOImpl;
import ntu.celt.eUreka2.modules.care.CAREDAO;
import ntu.celt.eUreka2.modules.care.CAREDAOImpl;
import ntu.celt.eUreka2.modules.elog.ElogDAO;
import ntu.celt.eUreka2.modules.elog.ElogDAOImpl;
import ntu.celt.eUreka2.modules.forum.ForumDAO;
import ntu.celt.eUreka2.modules.forum.ForumDAOImpl;
import ntu.celt.eUreka2.modules.group.GroupDAO;
import ntu.celt.eUreka2.modules.group.GroupDAOImpl;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAO;
import ntu.celt.eUreka2.modules.scheduling.SchedulingDAOImpl;
import ntu.celt.eUreka2.modules.search.SearchDAO;
import ntu.celt.eUreka2.modules.search.SearchDAOImp;
import ntu.celt.eUreka2.modules.usage.UsageDAO;
import ntu.celt.eUreka2.modules.usage.UsageDAOImpl;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAO;
import ntu.celt.eUreka2.modules.lcdp.LCDPDAOImpl;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAO;
import ntu.celt.eUreka2.modules.learninglog.LearningLogDAOImpl;
import ntu.celt.eUreka2.modules.message.MessageDAO;
import ntu.celt.eUreka2.modules.message.MessageDAOImpl;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAO;
import ntu.celt.eUreka2.modules.peerevaluation.EvaluationDAOImpl;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAOImpl;
import ntu.celt.eUreka2.modules.resources.ResourceDAO;
import ntu.celt.eUreka2.modules.resources.ResourceDAOImpl;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManager;
import ntu.celt.eUreka2.services.attachFiles.AttachedFileManagerImpl;
import ntu.celt.eUreka2.services.email.EmailManager;
import ntu.celt.eUreka2.services.email.EmailManagerImpl;
import ntu.celt.eUreka2.services.email.EmailTemplateDAO;
import ntu.celt.eUreka2.services.email.EmailTemplateDAOImpl;

import org.apache.tapestry5.*;
import org.apache.tapestry5.hibernate.HibernateTransactionAdvisor;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseRenderer;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.slf4j.Logger;

/**
 * This module is automatically included as part of the Tapestry IoC Registry, it's a good place to
 * configure and extend Tapestry, or to place your own service definitions.
 */
public class AppModule
{
    public static void bind(ServiceBinder binder)
    {
        // binder.bind(MyServiceInterface.class, MyServiceImpl.class);
        
        // Make bind() calls on the binder object to define most IoC services.
        // Use service builder methods (example below) when the implementation
        // is provided inline, or requires more initialization than simply
        // invoking the constructor.
    	binder.bind(AssessmentDAO.class, AssessmentDAOImpl.class);
    	binder.bind(BlogDAO.class, BlogDAOImpl.class);
    	binder.bind(ModuleDAO.class, ModuleDAOImpl.class);
    	binder.bind(ProjectDAO.class, ProjectDAOImpl.class);
    	binder.bind(ProjRoleDAO.class, ProjRoleDAOImpl.class);
    	binder.bind(ProjStatusDAO.class, ProjStatusDAOImpl.class);
    	binder.bind(ProjTypeDAO.class, ProjTypeDAOImpl.class);
    	binder.bind(ProjExtraInfoDAO.class, ProjExtraInfoDAOImpl.class);
    	binder.bind(ResourceDAO.class, ResourceDAOImpl.class);
    	binder.bind(AnalyticDAO.class, AnalyticDAOImpl.class);
    	binder.bind(AuditTrailDAO.class, AuditTrailDAOImpl.class);
    	
    	
    	binder.bind(SchoolDAO.class, SchoolDAOImpl.class);
    	binder.bind(SysRoleDAO.class, SysRoleDAOImpl.class);
    	binder.bind(UserDAO.class, UserDAOImpl.class);
    	binder.bind(PreferenceDAO.class, PreferenceDAOImpl.class);
    	binder.bind(PrivilegeDAO.class, PrivilegeDAOImpl.class);
    	binder.bind(WebSessionDAO.class, WebSessionDAOImpl.class);
    	binder.bind(SysAnnouncementDAO.class, SysAnnouncementDAOImpl.class);
    	binder.bind(SchlTypeAnnouncementDAO.class, SchlTypeAnnouncementDAOImpl.class);
    	binder.bind(AnnouncementDAO.class, AnnouncementDAOImpl.class);
    	binder.bind(ForumDAO.class, ForumDAOImpl.class);
    	binder.bind(SchedulingDAO.class, SchedulingDAOImpl.class);
    	binder.bind(BudgetDAO.class, BudgetDAOImpl.class);
    	binder.bind(MessageDAO.class, MessageDAOImpl.class);
    	binder.bind(AttachedFileManager.class, AttachedFileManagerImpl.class);
    	binder.bind(EmailTemplateDAO.class, EmailTemplateDAOImpl.class);
    	binder.bind(EmailManager.class, EmailManagerImpl.class);
    	binder.bind(BackupRestoreDAO.class, BackupRestoreDAOImpl.class);
    	binder.bind(MigrateDAO.class, MigrateDAOImpl.class);
    	binder.bind(Migrate2DAO.class, Migrate2DAOImpl.class);
    	binder.bind(SearchDAO.class, SearchDAOImp.class);
    	binder.bind(LearningLogDAO.class, LearningLogDAOImpl.class);
    	binder.bind(ElogDAO.class, ElogDAOImpl.class);
    	binder.bind(UsageDAO.class, UsageDAOImpl.class);
    	binder.bind(LdapService.class, LdapServiceImpl.class);
    	
    	binder.bind(GroupDAO.class, GroupDAOImpl.class);
    	binder.bind(EvaluationDAO.class, EvaluationDAOImpl.class);
    	binder.bind(ProfilingDAO.class, ProfilingDAOImpl.class);
    	binder.bind(LCDPDAO.class, LCDPDAOImpl.class);
    	binder.bind(CAREDAO.class, CAREDAOImpl.class);
    	
    }
    
    
    public static void contributeApplicationDefaults(
            MappedConfiguration<String, String> configuration)
    {
        // Contributions to ApplicationDefaults will override any contributions to
        // FactoryDefaults (with the same key). Here we're restricting the supported
        // locales to just "en" (English). As you add localised message catalogs and other assets,
        // you can extend this list of locales (it's a comma separated series of locale names;
        // the first locale name is the default when there's no reasonable match).
        
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

        // The factory default is true but during the early stages of an application
        // overriding to false is a good idea. In addition, this is often overridden
        // on the command line as -Dtapestry.production-mode=false
        configuration.add(SymbolConstants.PRODUCTION_MODE, "true");

        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions.
        configuration.add(SymbolConstants.APPLICATION_VERSION, "2.1.93");
     
        //configuration.add(SymbolConstants.MIN_GZIP_SIZE, "10000");
        
        //configuration.add(SymbolConstants.CHARSET, "UTF-8");
        
        
        
        //--------Upload configuration  (optional):
        configuration.add(UploadSymbols.REPOSITORY_LOCATION, Config.getString("tapestry.upload.repository_location"));
        configuration.add(UploadSymbols.FILESIZE_MAX, Config.getString("tapestry.upload.filesize_max"));
        configuration.add(UploadSymbols.REPOSITORY_THRESHOLD, Config.getString("tapestry.upload.repository_threshold"));
        configuration.add(UploadSymbols.REQUESTSIZE_MAX, Config.getString("tapestry.upload.requestsize_max"));
    }
    
    public void contributeMetaDataLocator(MappedConfiguration<String,String> configuration)
    {
    	 configuration.add(MetaDataConstants.SECURE_PAGE, Config.getString("tapestry.use_secure_page"));
         
    }

    /**
     * This is a service definition, the service will be named "TimingFilter". The interface,
     * RequestFilter, is used within the RequestHandler service pipeline, which is built from the
     * RequestHandler service configuration. Tapestry IoC is responsible for passing in an
     * appropriate Logger instance. Requests for static resources are handled at a higher level, so
     * this filter will only be invoked for Tapestry related requests.
     * 
     * <p>
     * Service builder methods are useful when the implementation is inline as an inner class
     * (as here) or require some other kind of special initialization. In most cases,
     * use the static bind() method instead. 
     * 
     * <p>
     * If this method was named "build", then the service id would be taken from the 
     * service interface and would be "RequestFilter".  Since Tapestry already defines
     * a service named "RequestFilter" we use an explicit service id that we can reference
     * inside the contribution method.
     */    
    public RequestFilter buildTimingFilter(final Logger log)
    {
        return new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler)
                    throws IOException
            {
                long startTime = System.currentTimeMillis();

                try
                {
                    // The responsibility of a filter is to invoke the corresponding method
                    // in the handler. When you chain multiple filters together, each filter
                    // received a handler that is a bridge to the next filter.
                    
                    return handler.service(request, response);
                }
                finally
                {
                    long elapsed = System.currentTimeMillis() - startTime;

                    log.debug(String.format("Request time: %d ms", elapsed));
                }
            }
        };
    }

    /**
     * This is a contribution to the RequestHandler service configuration. This is how we extend
     * Tapestry using the timing filter. A common use for this kind of filter is transaction
     * management or security. The @Local annotation selects the desired service by type, but only
     * from the same module.  Without @Local, there would be an error due to the other service(s)
     * that implement RequestFilter (defined in other modules).
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,
            @Local
            RequestFilter filter)
    {
        // Each contribution to an ordered configuration has a name, When necessary, you may
        // set constraints to precisely control the invocation order of the contributed filter
        // within the pipeline.
        
        configuration.add("Timing", filter);
    }
    
    
    /**
     * This function will be call when application startup
     * @param configuration
     */
    public static void contributeRegistryStartup(OrderedConfiguration<Runnable> configuration)
    {
    	//Initialize Database to some default values, may set in config.properties whether to run this or not
    	configuration.add("InitializeDatabase", new DatabaseInitializer());
    	
    }
    
    public static void contributeComponentRequestHandler(
    		OrderedConfiguration<ComponentRequestFilter> configuration) {
    	//service to protect page. redirect user to Login.class page if user not logged in AND the accessed
    	//page is not annotated @PublicPage, it also handle annotation @RequirePrivilege
    	configuration.addInstance("PageProtection", PageProtectionFilter.class);
    	
  	}
    
    public void contributeRestfulWSDispatcher (MappedConfiguration<String, Object> config)
    {
    	//Webservice to communicate with Blackboard Buildingblock
        config.addInstance("bbsvc", BbIntegrateService.class);   
        
        config.addInstance("getfsvc", FileRetrievalService.class);
        config.addInstance("runtasksvc", RunTaskService.class);   
    }
    
    public static void contributeIgnoredPathsFilter(Configuration<String> conf) {
    	conf.add("/Handler/*");
    	conf.add("/saml/*");
    	conf.add("/saml/authenticate/*");
    	conf.add("/saml/access/*");
    	conf.add("/saml/registerExternally/*");
    	conf.add("/saml/registerInternally/*");
    	conf.add("/images/*");
    	conf.add("/wp-content/*");
    	
    }
    
   /* public static void contributeFactoryDefaults(MappedConfiguration<String,String> conf) {
    	conf.override(ChenilleKitLDAPConstants.LDAP_HOSTNAME, "staff10.staff.main.ntu.edu.sg");
    	conf.override(ChenilleKitLDAPConstants.LDAP_HOSTPORT, "389");
    	conf.override(ChenilleKitLDAPConstants.LDAP_AUTHDN, "cedcrm");
    	conf.override(ChenilleKitLDAPConstants.LDAP_AUTHPWD, "formula2");
    	conf.override(ChenilleKitLDAPConstants.LDAP_VERSION, "3");
    	
    }
    */
    
    //to use annotation inside *DAO (particular we want to use @CommitAfter )
    @Match({"*DAO", "*Manager"})
    public static void adviseTransactions(HibernateTransactionAdvisor advisor, MethodAdviceReceiver receiver)
    {
        advisor.addTransactionCommitAdvice(receiver); 
    }
    
     
    // handle RedirectException
    public static RequestExceptionHandler decorateRequestExceptionHandler(
        final Object delegate, final Response response,
        final RequestPageCache requestPageCache, final PageRenderLinkSource linkSource,
        final ComponentClassResolver resolver,
        final Logger logger,
        final ResponseRenderer renderer,
        final ComponentSource componentSource,
        @Symbol(SymbolConstants.PRODUCTION_MODE)
        final boolean productionMode,
        Object service    
    	)
    {
        return new RequestExceptionHandler()
        {
            public void handleRequestException(Throwable exception) throws IOException
            {
            	// check if wrapped
                
                Throwable cause = exception;
                if (exception.getCause() instanceof RedirectException)
                {
                    cause = exception.getCause();
                }
                

                //Better way to check if the cause is RedirectException. Sometimes it's wrapped pretty deep..
                int i = 0;
                while(true){
                    if(cause == null || cause instanceof RedirectException || i > 1000){
                        break;
                    }
                    i++;
                    cause = cause.getCause();
                }

                // check for redirect
                if (cause instanceof RedirectException)
                {
                    // check for class and string
                    RedirectException redirect = (RedirectException)cause;
                    Link pageLink = redirect.getPageLink();
                    if (pageLink == null)
                    {
                        // handle Class (see ClassResultProcessor)
                        String pageName = redirect.getMessage();
                        Class<?> pageClass = redirect.getPageClass();
                        if (pageClass != null)
                        {
                            pageName = resolver.resolvePageClassNameToPageName(pageClass.getName());
                        }

                        // handle String (see StringResultProcessor)
                      //  Page page = requestPageCache.get(pageName);
                        pageLink = linkSource.createPageRenderLink(pageName);
                    }

                    // handle Link redirect
                    if (pageLink != null)
                    {
                        response.sendRedirect(pageLink.toRedirectURI());
                        return;
                    }
                }

                // no redirect so pass on the exception
               // ((RequestExceptionHandler)delegate).handleRequestException(exception);

                //if (!productionMode) return ;
                logger.error("Unexpected runtime exception: " + exception.getMessage(), exception);

                ExceptionReporter exceptionReportPage = (ExceptionReporter) componentSource.getPage("Exception");

                exceptionReportPage.reportException(exception);

                renderer.renderPageMarkupResponse("Exception");
            }
        };
    }
    
    
    
/*    
 	//handle RequestException
 	public RequestExceptionHandler decorateRequestExceptionHandler(
            final Logger logger,
            final ResponseRenderer renderer,
            final ComponentSource componentSource,
            @Symbol(SymbolConstants.PRODUCTION_MODE)
            boolean productionMode,
            Object service)
    {
        if (!productionMode) return null;

        return new RequestExceptionHandler()
        {
            public void handleRequestException(Throwable exception) throws IOException
            {
                logger.error("Unexpected runtime exception: " + exception.getMessage(), exception);

                ExceptionReporter exceptionReportPage = (ExceptionReporter) componentSource.getPage("Exception");

                exceptionReportPage.reportException(exception);

                renderer.renderPageMarkupResponse("Exception");
            }
        };
    }
*/    
}
