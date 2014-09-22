// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ca.jeb.common.gpb.converter.NullConverter;
import ca.jeb.common.infra.JStringUtils;

/**
 * ProtoBufAttribute.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProtoBufAttribute
{
  /**
   * This attribute should be used to indicate that this
   * interface should consider this field to be required
   * with ProtoBuf class.
   * 
   * @return
   */
  boolean required() default false;

  /**
   * This attribute defines what the target method should be
   * used on the {@link ProtoBufEntity}. This will default to
   * "set" + upperCaseFirst(fieldName).
   * 
   * @return
   */
  String protoBufSetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the getter method name
   * should be to retrieve the data <b>from</b> the {@link ProtoBufEntity} object.
   * This defaults to "get" + upperCaseFirst(fieldName).
   * 
   * @return
   */
  String protoBufGetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the getter method name
   * should be to retrieve the data <b>from</b> the POJO object.
   * This defaults to "get" + upperCaseFirst(fieldName).
   * 
   * @return
   */
  String pojoGetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the target setter method
   * should be used on the POJO object. This will default to
   * "set" + upperCaseFirst(fieldName).
   * 
   * @return
   */
  String pojoSetter() default JStringUtils.EMPTY;

  /**
   * This should be set to a class that implements the IProtoBufConverter.
   * This converter will be used to convert the value from the
   * POJO objects to the Google ProtoBuf object. {@link NullConverter}
   * 
   * @return
   */
  Class<? extends IProtoBufConverter> converter() default NullConverter.class;
}