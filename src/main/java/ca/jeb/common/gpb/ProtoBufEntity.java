// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessage;

/**
 * ProtoBufEntity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProtoBufEntity
{
  /**
   * This attribute should be used to indicate that this
   * interface should consider this field to be required
   * with ProtoBuf class.
   * 
   * @return
   */
  Class<? extends GeneratedMessage> value();
}