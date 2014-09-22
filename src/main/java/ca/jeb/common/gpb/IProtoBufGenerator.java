// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import ca.jeb.common.infra.JException;

import com.google.protobuf.GeneratedMessage;

/**
 * Interface for generating ProtoBuf messages, where generic G
 * is the Protobuf class and P is the Pojo class.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public interface IProtoBufGenerator<G extends GeneratedMessage, P extends Object>
{
  /**
   * Implement the toProtoBuf method that will convert this
   * object to a GeneratedMessage G.
   * 
   * @param pojo - Pojo object of type P
   * @return
   * @throws JException
   */
  G toProtoBuf(P pojo) throws JException;

  /**
   * Implement the fromProtoBuf method to set all ProtoBufAttributes
   * on <i>this</i> POJO class.
   * 
   * @param protoBuf - GeneratedMessage G
   * @throws CMSCoreException
   */
  P fromProtoBuf(G protoBuf) throws JException;
}