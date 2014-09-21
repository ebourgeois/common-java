// Copyright (c) 2014 Erick Bourgeois. Incorporated, All Rights Reserved

package ca.jeb.common.infra;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 */
public class JReflectionUtilsTest
{
  final static int  TEN       = 10;
  final static int  TWENTY    = 20;

  private TestClass testClass = new TestClass();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUpBeforeClass() throws Exception
  {
    testClass.setFoo(TEN);
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#runGetter(java.lang.reflect.Field, java.lang.Object)}.
   */
  @Test
  public void testRunGetter() throws NoSuchFieldException, SecurityException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException
  {
    final Field field = testClass.getClass().getDeclaredField("foo");
    final Object value = JReflectionUtils.runGetter(testClass, field);
    Assert.assertEquals("The expected value " + TEN + " is not equal to the result " + value, (int)value, TEN);
  }

  /**
   * Test method for
   * {@link ca.jeb.common.infra.JReflectionUtils#runSetter(java.lang.Object, java.lang.String, java.lang.Object, java.lang.Class)}.
   */
  @Test
  public void testRunSetter()
  {
    try
    {
      JReflectionUtils.runSetter(testClass, "setFoo", TEN, int.class);
    }
    catch (JException e)
    {
      fail("Could not run setter 'setFoo': " + e);
    }
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#runMethod(java.lang.Object, java.lang.String, java.lang.Object[])}.
   */
  @Test
  public void testRunMethod()
  {
    try
    {
      final Object value = JReflectionUtils.runMethod(testClass, "getFoo");
      Assert.assertEquals("The expected value " + TEN + " is not equal to the result " + value, (int)value, TEN);
    }
    catch (JException e)
    {
      fail("Could not run getter 'getFoo': " + e);
    }
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getAllFields(java.util.List, java.lang.Class)}.
   */
  @Test
  public void testGetAllFields()
  {
    final List<Field> fields = JReflectionUtils.getAllFields(new ArrayList<Field>(), testClass.getClass());
    final Field field = fields.get(0);
    Assert.assertTrue("Field returned from getAllFields is not 'foo'", field.getName().equals("foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getAllMethods(java.util.List, java.lang.Class)}.
   */
  @Test
  public void testGetAllMethods()
  {
    final List<Method> methods = JReflectionUtils.getAllMethods(new ArrayList<Method>(), testClass.getClass());
    final Method method = methods.get(0);
    Assert.assertTrue("Method returned from getAllMethods is not 'setFoo'", method.getName().equals("setFoo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getMethodByName(java.lang.Class, java.lang.String)}.
   */
  @Test
  public void testGetMethodByName()
  {
    final Method method = JReflectionUtils.getMethodByName(testClass.getClass(), "getFoo");
    Assert.assertTrue("Method returned from getMethodByName is not 'getFoo'", method.getName().equals("getFoo"));
  }

  private class TestClass
  {
    private int foo;

    public void setFoo(int f)
    {
      this.foo = f;
    }

    public int getFoo()
    {
      return foo;
    }
  }
}