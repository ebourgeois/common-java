// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.spring;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
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

  public static void main(String[] args)
  {
    if (args == null || args.length < 1)
    {
      throw new IllegalArgumentException("Please provide a config location");
    }

    final SpringMain sm = new SpringMain();
    sm.setConfigLocations(args);

    if (System.getSecurityManager() == null)
    {
      sm.runInternal();
    }
    else
    {
      AccessController.doPrivileged(new PrivilegedAction<Void>()
      {
        public Void run()
        {
          sm.runInternal();
          return null;
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
    BeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(this.context);
    AnnotatedBeanDefinitionReader classReader = new AnnotatedBeanDefinitionReader(context);

    for (String configLocation : configLocations)
    {
      if (!registerClass(classReader, configLocation))
      {
        xmlReader.loadBeanDefinitions(configLocation);
      }
    }

    boolean started = false;
    try
    {
      this.context.registerShutdownHook();
      // refresh context and start SmartLifecycleBean
      this.context.refresh();
      LOGGER.info("SpringMain has completed startup!");
      started = true;
    }
    catch (Throwable t)
    {
      LOGGER.error("SpringMain startup failed", t);
      if (t instanceof RuntimeException)
      {
        throw (RuntimeException)t;
      }
      throw new RuntimeException("SpringMain failed startup: " + t, t);
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
          LOGGER.info("Ignoring exception during application close due to exception at startup", e);
        }
      }
    }
  }

  private boolean registerClass(AnnotatedBeanDefinitionReader classReader, String className)
  {
    if (!(className.indexOf('/') == -1 && className.indexOf(':') == -1))
    {
      return false;
    }
    try
    {
      Class<?> configClass = getClass().getClassLoader().loadClass(className);
      classReader.register(configClass);
      return true;
    }
    catch (ClassNotFoundException e)
    {
      return false;
    }
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