// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.infra;

/**
 * Common exception for jeb Java exceptions.
 */
public class JException extends Exception
{
  /**
   * @param e - Exception
   */
  public JException(Exception exception)
  {
    super(exception);
  }

  /**
   * @param string
   */
  public JException(String string)
  {
    super(string);
  }

  /**
   * @param string
   * @param e
   */
  public JException(String string, Exception exception)
  {
    super(string, exception);
  }
}