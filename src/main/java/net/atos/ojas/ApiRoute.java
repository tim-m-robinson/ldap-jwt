package net.atos.ojas;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.impl.DefaultCamelContext;;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;
import javax.ws.rs.core.MediaType;

@Component
public class ApiRoute extends RouteBuilder {

  @Autowired
  CamelContext ctx;

  @Value("${ssl.hostname.verify}")
  String isSslHostnameValidation;

  @Override
  public void configure() throws Exception {
    // DEBUG
    //ctx.setTracing(true);
    // JNDI setup for LDAP server
    configureJndiForLdap();

    // @formatter:off
    restConfiguration()
      .component("servlet")
      .contextPath("/")
      .endpointProperty("matchOnUriPrefix", "true")
      /*.dataFormatProperty("prettyPrint", "true")*/
      .bindingMode(RestBindingMode.off)
      .apiContextPath("/api-doc")
        .apiProperty("api.title", "User API").apiProperty("api.version", "1.0.0")
        .apiProperty("cors", "true");


    rest("/")
      .get("/")
      .consumes(MediaType.MEDIA_TYPE_WILDCARD)
      .produces(MediaType.TEXT_PLAIN)
        .to("direct:in")

      .get("/jwt")
      .consumes(MediaType.MEDIA_TYPE_WILDCARD)
      .produces(MediaType.TEXT_HTML)
        .to("direct:jwt");

    from("direct:in").routeId("route-1")
      .log(LoggingLevel.INFO, "Message Trace Id: ${header.breadcrumbId}")
      .setBody().constant("(cn=*)")
      .to("ldap:myldap?base=dc=example,dc=org&returnedAttributes=cn,userPassword")
      .to("mock:out");

    from("direct:jwt").routeId("route-2")
      .log(LoggingLevel.INFO, "Message Trace Id: ${header.breadcrumbId}")
      .process(new JwtProcessor())
      .to("mock:out");
    // @formatter:on

  }

  private void configureJndiForLdap() throws Exception {

    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    props.setProperty(Context.PROVIDER_URL, "ldap://10.0.2.15:1389");
    props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.jndi.url");
    props.setProperty(Context.REFERRAL, "ignore");
    //props.setProperty("com.sun.jndi.ldap.connect.pool", "true");

    SimpleRegistry reg = new SimpleRegistry();
    reg.put("myldap", new InitialLdapContext(props, null));
    ((DefaultCamelContext) this.getContext()).setRegistry(reg);
  }

}

