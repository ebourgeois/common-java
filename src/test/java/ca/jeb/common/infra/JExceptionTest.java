// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.common.infra;

import org.junit.Test;

import ca.jeb.common.infra.JException;
import ca.jeb.common.infra.JReflectionUtils;

/**
 */
public class JExceptionTest
{
  @Test(expected = JException.class)
  public void test() throws JException
  {
    JReflectionUtils.runMethod(new Object(), null, null);
  }
}