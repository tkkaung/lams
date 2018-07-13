//based on http://jumpstart.doublenegative.com.au:8080/jumpstart/examples/infrastructure/protectingpages
//All pages require users to login unless they are annotated with @PublicPage.

package ntu.celt.eUreka2.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* Specifies that the class is a "public page", which can be accessed by non-logged-in users. 
* All pages are protected by default.
* This annotation is applied to a Tapestry page class. 
* The protection is provided by {@link PageProtectionFilter}. 
*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicPage {
}

