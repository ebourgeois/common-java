// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import ca.jeb.common.infra.JGPBAnnotationException;

/**
 * An interface class that any classes attribute needs to be
 * converted for a Google ProtoBuf Message.
 */
public interface IProtoBufConverter
{
  /**
   * @param sourceObject
   * @return Object
   * @throws CMSCoreAnnotationException
   */
  Object convertToProtoBuf(Object sourceObject) throws JGPBAnnotationException;

  /**
   * @param sourceObject
   * @return Object
   * @throws CMSCoreAnnotationException
   */
  Object convertFromProtoBuf(Object sourceObject) throws JGPBAnnotationException;
}