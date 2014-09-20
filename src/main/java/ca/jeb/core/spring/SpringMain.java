
package ca.jeb.core.spring;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @see SmartLifecycle
 * @see PropertyPlaceholderConfigurer
 */
public final class SpringMain
{

  private static final Logger       LOGGER = LoggerFactory.getLogger(SpringMain.class);

  private GenericApplicationContext context;

  private String[]                  configLocations;

  private BeanDefinitionReader createBeanDefinitionReader(BeanDefinitionRegistry registry)
  {
    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
    return reader;
  }

  public void run(String... args)
  {
    if (args == null || args.length < 1)
    {
      throw new IllegalArgumentException("Must pass config location");
    }

    this.configLocations = args;
    run();
  }

  /**
   * Creates a new {@link ApplicationContext} if none exists, configures it with the given configuration files and
   * refreshes the {@link ApplicationContext}.
   */
  public void run()
  {
    /* Application is really starting here, ignore previous AccessControlContext context */
    if (System.getSecurityManager() == null)
    {
      runInternal();
    }
    else
    {
      AccessController.doPrivileged(new PrivilegedAction<Void>()
      {
        public Void run()
        {
          runInternal();
          return null; // nothing to return
        }
      });
    }
  }

  private void runInternal()
  {

    if (this.context == null)
    {
      this.context = new GenericApplicationContext();
    }

    // Finally load app config and start...
    BeanDefinitionReader xmlReader = createBeanDefinitionReader(this.context);
    AnnotatedBeanDefinitionReader classReader = new AnnotatedBeanDefinitionReader(context);

    for (String location : configLocations)
    {
      if (!tryRegisterClass(classReader, location))
        xmlReader.loadBeanDefinitions(location);
    }

    boolean started = false;
    try
    {
      this.context.registerShutdownHook();
      this.context.refresh(); // refresh context and start SmartLifecycleBean
      LOGGER.info("SpringMain startup complete");
      started = true;
    }
    catch (Throwable t)
    {
      LOGGER.error("SpringMain startup failed", t);
      if (t instanceof RuntimeException)
      {
        throw (RuntimeException)t;
      }
      throw new RuntimeException("SpringMain start up failed", t);
    }
    finally
    {
      if (!started)
      {
        try
        {
          this.context.close();
        }
        catch (Throwable e)
        {
          // An exception is pending
          LOGGER.info("Ignoring exception during application close due to exception at startup", e);
        }
      }
    }
  }

  private boolean tryRegisterClass(AnnotatedBeanDefinitionReader classReader, String classname)
  {
    if (!checkName(classname))
      return false;
    try
    {
      Class<?> configClass = getClass().getClassLoader().loadClass(classname);
      classReader.register(configClass);
      return true;
    }
    catch (ClassNotFoundException e)
    {
      return false;
    }
  }

  /** simple check to detect common cases */
  private boolean checkName(String className)
  {
    return className.indexOf('/') == -1 && className.indexOf(':') == -1;
  }

  public static void main(String[] args)
  {
    new SpringMain().run(args);
  }

  /**
   * @return the context
   */
  public GenericApplicationContext getContext()
  {
    return this.context;
  }

  /**
   * @param context
   *          the context to set
   */
  public void setContext(GenericApplicationContext context)
  {
    this.context = context;
  }

  /**
   * @return the array of config locations
   */
  public String[] getConfigLocations()
  {
    return this.configLocations;
  }

  /**
   * @param configLocations
   *          the config locations to be given to Spring
   */
  public void setConfigLocations(String... configLocations)
  {
    this.configLocations = configLocations;
  }
}