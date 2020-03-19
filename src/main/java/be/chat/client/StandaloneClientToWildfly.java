package be.chat.client;

import be.chat.ChatRemote;
import be.chat.dto.MessageDTO;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

public class StandaloneClientToWildfly {

    private static final Logger logger = Logger.getLogger(StandaloneClientToWildfly.class.getName());

    public static void main(String[] argc) throws NamingException {
        ChatRemote chat = lookup();

        logger.info("I have remote object ...");

        chat.sendMessageDTO(MessageDTO.builder()
                .owner("Standalone-Client")
                .time(new Date())
                .message("Hello WildFly !!!")
                .build());
    }

    @SuppressWarnings("unchecked")
    private static ChatRemote lookup() throws NamingException {
        Properties properties = new Properties();
        //properties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
        properties.put("remote.connections", "default");
        properties.put("remote.connection.default.port", "8090");
        properties.put("remote.connection.default.host", "localhost");
        //properties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");

        //properties.put("remote.connection.default.username", "ejbuser");
        //properties.put("remote.connection.default.password", "pasword123");

        //properties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        //properties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");

        EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(properties);
        ContextSelector<EJBClientContext> contextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
        EJBClientContext.setSelector(contextSelector);

        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        Context context = new InitialContext(jndiProperties);

        String appName = "";
        String moduleName = "wildfly-chat";
        String beanName = "ChatBean";
        final String viewClassName = ChatRemote.class.getName();
        String jndiRemoteAddress = "ejb:" + appName + "/" + moduleName + "/" + beanName + "!" + viewClassName;
        logger.info("I am looking bean: " + jndiRemoteAddress);

        return (ChatRemote) context.lookup(jndiRemoteAddress);
    }

}
