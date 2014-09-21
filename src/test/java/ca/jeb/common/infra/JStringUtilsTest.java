// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.common.infra;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 */
public class JStringUtilsTest
{

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#safeTrim(java.lang.String)}.
   */
  @Test
  public void testSafeTrim()
  {
    Assert.assertEquals("null is not returning empty string", JStringUtils.EMPTY, JStringUtils.safeTrim(null));
    Assert.assertEquals("null is not returning empty string", "foo", JStringUtils.safeTrim("foo   "));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#ltrim(java.lang.String)}.
   */
  @Test
  public void testLtrim()
  {
    Assert.assertEquals("ltrim is not returning correct string", "foo", JStringUtils.ltrim("    foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#rtrim(java.lang.String)}.
   */
  @Test
  public void testRtrim()
  {
    Assert.assertEquals("rtrim is not returning correct string", "foo", JStringUtils.rtrim("foo   "));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#hasDigit(java.lang.String)}.
   */
  @Test
  public void testHasDigit()
  {
    Assert.assertEquals("hasDigit is not returning true for '12foo'", true, JStringUtils.hasDigit("12foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#pad(java.lang.String, int, char)}.
   */
  @Test
  public void testPad()
  {
    Assert.assertEquals("pad is not returning 'foo222'", "foo222", JStringUtils.pad("foo", 6, '2'));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#convertInputStreamToString(java.io.InputStream, java.lang.String)}.
   */
  @Test
  public void testConvertInputStreamToString()
  {
    // fail("Not yet implemented");
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#upperCaseFirst(java.lang.String)}.
   */
  @Test
  public void testUpperCaseFirst()
  {
    Assert.assertEquals("upperCaseFirst is not returning 'Foo'", "Foo", JStringUtils.upperCaseFirst("foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#join(java.util.Collection, java.lang.String)}.
   */
  @Test
  public void testJoin()
  {
    Assert.assertEquals("upperCaseFirst is not returning 'foo:bar'", "foo:bar",
            JStringUtils.join(Arrays.asList(new String[]{ "foo", "bar" }), ":"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#split(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testSplit()
  {
    final List<String> strs = JStringUtils.split("foo:bar", ":");
    Assert.assertEquals("First string when splitting 'foo:bar' is not 'foo'", "foo", strs.get(0));
    Assert.assertEquals("First string when splitting 'foo:bar' is not 'foo'", "bar", strs.get(1));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#decode(byte[], java.nio.charset.Charset)}.
   */
  @Test
  public void testDecode()
  {
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#encode(java.lang.String, java.nio.charset.Charset)}.
   */
  @Test
  public void testEncode()
  {
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#isNullOrEmpty(java.lang.String)}.
   */
  @Test
  public void testIsNullOrEmpty()
  {
    Assert.assertEquals("isNullOrEmpty is not returning true for \"\"", true, JStringUtils.isNullOrEmpty(JStringUtils.EMPTY));
    Assert.assertEquals("isNullOrEmpty is not returning true for null", true, JStringUtils.isNullOrEmpty(null));
    Assert.assertEquals("isNullOrEmpty is not returning false for 'foo'", false, JStringUtils.isNullOrEmpty("foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#hasValue(java.lang.String)}.
   */
  @Test
  public void testHasValue()
  {
    Assert.assertEquals("hasValue is not returning false for null", true, JStringUtils.isNullOrEmpty(null));
    Assert.assertEquals("hasValue is not returning true for 'foo'", false, JStringUtils.isNullOrEmpty("foo"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#compare(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testCompare()
  {
    Assert.assertEquals("compare is not returning true for null == null", true, JStringUtils.compare(null, null));
    Assert.assertEquals("compare is not returning true for \"\" == \"\"", true,
            JStringUtils.compare(JStringUtils.EMPTY, JStringUtils.EMPTY));
    Assert.assertEquals("compare is not returning false for \"foo\" == \"bar\"", false, JStringUtils.compare("foo", "bar"));
  }

  /**
   * Test method for {@link ca.jeb.common.infra.JStringUtils#getNonNullValue(java.lang.String)}.
   */
  @Test
  public void testGetNonNullValue()
  {
  }
}