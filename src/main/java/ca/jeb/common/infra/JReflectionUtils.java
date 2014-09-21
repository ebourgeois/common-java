// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.infra;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for running some reflection methods.
 */
public final class JReflectionUtils
{
  private static final Map<String, Map<String, Method>> METHOD_FIELD_MAP_CACHE = new ConcurrentHashMap<>();

  private JReflectionUtils()
  {
    // empty utilit class
  }

  /**
   * Execute a "getter" on the provided <i>object</i> for the given <i>field</i>.
   * 
   * @param field
   * @param object
   * @return Object
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  public static Object runGetter(Object object, Field field) throws IllegalAccessException, IllegalArgumentException,
          InvocationTargetException
  {
    final Class<?> clazz = object.getClass();
    final String fieldName = field.getName();

    Map<String, Method> fieldMethodMap = METHOD_FIELD_MAP_CACHE.get(object.getClass().getCanonicalName());
    if (fieldMethodMap != null)
    {
      final Method fieldMethod = fieldMethodMap.get(fieldName);
      if (fieldMethod != null)
      {
        return fieldMethod.invoke(object);
      }
    }
    else
    {
      fieldMethodMap = new HashMap<>();
    }

    try
    {
      final Method method = clazz.getMethod(JStringUtils.GET + JStringUtils.upperCaseFirst(fieldName), null);
      fieldMethodMap.put(fieldName, method);
      METHOD_FIELD_MAP_CACHE.put(object.getClass().getCanonicalName(), fieldMethodMap);

      return method.invoke(object);
    }
    catch (Exception e)
    {
      // Swallow exception so that we loop through the rest.
    }

    for (Method method : clazz.getMethods())
    {
      final String methodName = method.getName();
      if (((methodName.startsWith(JStringUtils.GET)) && (methodName.length() == (fieldName.length() + JStringUtils.GET.length())))
              || ((methodName.startsWith(JStringUtils.IS)) && (methodName.length() == (fieldName.length() + JStringUtils.IS.length()))))
      {
        if (methodName.toLowerCase().endsWith(fieldName.toLowerCase()))
        {
          fieldMethodMap.put(fieldName, method);
          METHOD_FIELD_MAP_CACHE.put(object.getClass().getCanonicalName(), fieldMethodMap);

          return method.invoke(object);
        }
      }
    }

    return null;
  }

  /**
   * Execute a "setter" on the provided <i>object</i> for the given <i>method</i> name.
   * 
   * @param object
   * @param method
   * @param arg
   * @param argClazz
   * @return Object
   * @throws JException
   */
  public static Object runSetter(Object object, String method, Object arg, Class<? extends Object> argClazz) throws JException
  {
    try
    {
      if (argClazz == null)
      {
        argClazz = arg.getClass();
      }
      final Method m = object.getClass().getMethod(method, argClazz);

      return m.invoke(object, arg);
    }
    catch (Exception e)
    {
      throw new JException(e);
    }
  }

  /**
   * Use reflection to run/execute the method represented by "method",
   * on the object {@code object}, given the list of {@code args}.
   * 
   * @param object - Object
   * @param method - String
   * @param args - Object...
   * @return Object
   * @throws JException
   */
  public static Object runMethod(Object object, String method, Object... args) throws JException
  {
    try
    {
      final Method m = object.getClass().getMethod(method);

      return m.invoke(object, args);
    }
    catch (Exception e)
    {
      throw new JException(e);
    }
  }

  /**
   * This static method will get ALL fields for this class, including any inherited ones.
   * 
   * @param fields List&lt;Field&gt; - a list to use to add to
   * @param clazz Class&lt;?&gt; - The Class to run the reflection on
   * @return List&lt;Field&gt;
   */
  public static List<Field> getAllFields(List<Field> fields, Class<?> clazz)
  {
    for (Field field : clazz.getDeclaredFields())
    {
      fields.add(field);
    }

    if (clazz.getSuperclass() != null)
    {
      fields = getAllFields(fields, clazz.getSuperclass());
    }

    return fields;
  }

  /**
   * This static method will get ALL methods for this class, including any inherited ones.
   * 
   * @param methods List&lt;Method&gt;
   * @param clazz Class&lt;?&gt; - The Class to run the reflection on
   * @return List&lt;Method&gt;
   */
  public static List<Method> getAllMethods(List<Method> methods, Class<?> clazz)
  {
    for (Method method : clazz.getDeclaredMethods())
    {
      methods.add(method);
    }

    if (clazz.getSuperclass() != null)
    {
      methods = getAllMethods(methods, clazz.getSuperclass());
    }

    return methods;
  }

  /**
   * Retrieve a Method object with the provided <i>name</i> on the class, <i>clazz</i>.
   * 
   * @param clazz
   * @param name
   * @return Method
   */
  public static Method getMethodByName(Class<?> clazz, String name)
  {
    final List<Method> methods = JReflectionUtils.getAllMethods(new ArrayList<Method>(), clazz);

    for (Method method : methods)
    {
      if (method.getName().equals(name))
      {
        return method;
      }
    }
    return null;
  }
}