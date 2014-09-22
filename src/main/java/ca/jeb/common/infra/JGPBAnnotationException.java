// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.infra;

/**
 * Common exception for jeb Java exceptions.
 */
public class JGPBAnnotationException extends Exception
{
  /**
   * @param e - Exception
   */
  public JGPBAnnotationException(Exception e)
  {
    super(e);
  }
}