// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.common.gpb.converter;

import ca.jeb.common.gpb.IProtoBufConverter;
import ca.jeb.common.infra.JGPBAnnotationException;

/**
 * This is a class to convert Strings to Date objects.
 */
public class NullConverter implements IProtoBufConverter
{
  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertToProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertToProtoBuf(Object sourceObject) throws JGPBAnnotationException
  {
    return sourceObject;
  }

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertFromProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertFromProtoBuf(Object sourceObject) throws JGPBAnnotationException
  {
    return sourceObject;
  }
}
