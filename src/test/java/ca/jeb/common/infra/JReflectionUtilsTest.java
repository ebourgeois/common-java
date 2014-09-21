// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.core.infra;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 */
public class JReflectionUtilsTest
{
  final static int  FOO       = 1;

  private TestClass testClass = new TestClass();

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public void setUpBeforeClass() throws Exception
  {
    testClass.setFoo(FOO);
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#runGetter(java.lang.reflect.Field, java.lang.Object)}.
   */
  @Test
  public void testRunGetter()
  {
    
    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link ca.jeb.common.infra.JReflectionUtils#runSetter(java.lang.Object, java.lang.String, java.lang.Object, java.lang.Class)}.
   */
  @Test
  public void testRunSetter()
  {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#runMethod(java.lang.Object, java.lang.String, java.lang.Object[])}.
   */
  @Test
  public void testRunMethod()
  {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getAllFields(java.util.List, java.lang.Class)}.
   */
  @Test
  public void testGetAllFields()
  {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getAllMethods(java.util.List, java.lang.Class)}.
   */
  @Test
  public void testGetAllMethods()
  {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#getMethodByName(java.lang.Class, java.lang.String)}.
   */
  @Test
  public void testGetMethodByName()
  {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JReflectionUtils#stringToType(java.lang.String, java.lang.Class, java.lang.String)}.
   */
  @Test
  public void testStringToType()
  {
    fail("Not yet implemented");
  }

  public class TestClass
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