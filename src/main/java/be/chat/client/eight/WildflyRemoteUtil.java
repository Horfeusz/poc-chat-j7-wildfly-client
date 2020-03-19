package be.chat.client.eight;

import be.chat.ChatException;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.logging.Logger;

/*
import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.sasl.SaslMechanismSelector;
*/

//@Stateless
//@LocalBean
public class WildflyRemoteUtil {

    private static final String REMOTE_HOST = "localhost";

    private static final String REMOTE_PORT = "8090";

    private static final String SASL_MECHANISM = "DIGEST-MD5";

    private Logger logger = Logger.getLogger(WildflyRemoteUtil.class.getName());

    @Resource
    private SessionContext sessionContext;

    private String getCallerPrincipalName() {
        if (sessionContext != null) {
            return sessionContext.getCallerPrincipal().getName();
        }
        throw new IllegalStateException();
    }

    private String getCallerPrincipalPassword() {
        //TODO take the Password from ... ???
        return "password123";
    }


    /*
    private AuthenticationContext prepareAuthenticationContext() {
        AuthenticationConfiguration adminConfig =
                AuthenticationConfiguration
                        .empty()
                        .useProviders(() -> new Provider[]{new WildFlyElytronProvider()})
                        .setSaslMechanismSelector(SaslMechanismSelector.NONE.addMechanism(SASL_MECHANISM))
                        .useName(getCallerPrincipalName())
                        .usePassword(getCallerPrincipalPassword());
        AuthenticationContext context = AuthenticationContext.empty();
        return context.with(MatchRule.ALL, adminConfig);
    }


    public <T> void lookup(Class<T> remoteClass, Consumer<T> consumer) {
        prepareAuthenticationContext().run(() -> Optional.ofNullable(lookup(remoteClass))
                .ifPresent(consumer));
    }
    */

    //Java 1.7 version
    public <T> void lookup(Class<T> remoteClass, be.chat.util.Consumer<T> consumer) {
        try {
            T remoteObject = lookup(remoteClass);
            if (remoteObject != null) {
                consumer.accept(remoteObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ChatException(e);
        }

        /*
        prepareAuthenticationContext().run(new Runnable() {
            @Override
            public void run() {
                try {
                    T remoteObject = lookup(remoteClass);
                    if (remoteObject != null) {
                        consumer.accept(remoteObject);
                    }
                } catch (Exception e) {
                    logger.warning(Throwables.getStackTraceAsString(e));
                    throw new ChatException(e);
                }
            }
        });
        */
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> T lookup(Class<T> remoteClass) {
        Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");

        //TODO Host and port get from parameters
        jndiProperties.put(Context.PROVIDER_URL,
                new StringBuilder("http-remoting://")
                        .append(REMOTE_HOST)
                        .append(":")
                        .append(REMOTE_PORT)
                        .toString());

        Object remoteObject = null;
        String jndiRemoteAddress = null;
        try {
            Context context = new InitialContext(jndiProperties);

            //TODO take the JNDI name from the DB
            String appName = "";
            String moduleName = "wildfly-chat";
            String beanName = "ChatBean";
            final String viewClassName = remoteClass.getName();
            jndiRemoteAddress = "ejb:" + appName + "/" + moduleName + "/" + beanName + "!" + viewClassName;

            logger.info("I am looking bean: " + jndiRemoteAddress);
            // let's do the lookup
            remoteObject = context.lookup(jndiRemoteAddress);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        return (T) remoteObject;
    }

}
