/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.axis.description;

import org.apache.axis.enum.Style;
import org.apache.axis.enum.Use;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

public interface ServiceDesc {
    /**
     * What kind of service is this?
     * @return
     */
    Style getStyle();

    void setStyle(Style style);

    /**
     * What kind of use is this?
     * @return
     */
    Use getUse();

    void setUse(Use use);

    /**
     * the wsdl file of the service.
     * When null, it means that the wsdl should be autogenerated
     * @return filename or null
     */
    String getWSDLFile();

    /**
     * set the wsdl file of the service; this causes the named
     * file to be returned on a ?wsdl, probe, not introspection
     * generated wsdl.
     * @param wsdlFileName filename or null to re-enable introspection
     */
    void setWSDLFile(String wsdlFileName);

    List getAllowedMethods();

    void setAllowedMethods(List allowedMethods);

    TypeMapping getTypeMapping();

    void setTypeMapping(TypeMapping tm);

    /**
     * the name of the service
     */
    String getName();

    /**
     * the name of the service
     * @param name
     */
    void setName(String name);

    /**
	 * get the documentation for the service
	 */
    String getDocumentation();

    /**
	 * set the documentation for the service
	 */
    void setDocumentation(String documentation);

    void addOperationDesc(OperationDesc operation);

    /**
     * get all the operations as a list of OperationDescs.
     * this method triggers an evaluation of the valid operations by
     * introspection, so use sparingly
     * @return reference to the operations array. This is not a copy
     */
    ArrayList getOperations();

    /**
     * get all overloaded operations by name
     * @param methodName
     * @return null for no match, or an array of OperationDesc objects
     */
    OperationDesc [] getOperationsByName(String methodName);

    /**
     * Return an operation matching the given method name.  Note that if we
     * have multiple overloads for this method, we will return the first one.
     * @return null for no match
     */
    OperationDesc getOperationByName(String methodName);

    /**
     * Map an XML QName to an operation.  Returns the first one it finds
     * in the case of mulitple matches.
     * @return null for no match
     */
    OperationDesc getOperationByElementQName(QName qname);

    /**
     * Return all operations which match this QName (i.e. get all the
     * overloads)
     * @return null for no match
     */
    OperationDesc [] getOperationsByQName(QName qname);

    void setNamespaceMappings(List namespaces);

    String getDefaultNamespace();

    void setDefaultNamespace(String namespace);

    void setProperty(String name, Object value);

    Object getProperty(String name);

    String getEndpointURL();

    void setEndpointURL(String endpointURL);

    TypeMappingRegistry getTypeMappingRegistry();

    void setTypeMappingRegistry(TypeMappingRegistry tmr);
    
    boolean isInitialized();

    /**
     * Determine whether or not this is a "wrapped" invocation, i.e. whether
     * the outermost XML element of the "main" body element represents a
     * method call, with the immediate children of that element representing
     * arguments to the method.
     *
     * @return true if this is wrapped (i.e. RPC or WRAPPED style),
     *         false otherwise
     */
    boolean isWrapped();

    List getDisallowedMethods();

    void setDisallowedMethods(List disallowedMethods);
}
