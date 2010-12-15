/*
 * $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2010  The State and University Library
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package dk.statsbiblioteket.doms.central.connectors.bitstorage;

import dk.statsbiblioteket.doms.bitstorage.highlevel.Characterisation;
import dk.statsbiblioteket.doms.bitstorage.highlevel.HighlevelBitstorageSoapWebservice;
import dk.statsbiblioteket.doms.bitstorage.highlevel.HighlevelBitstorageSoapWebserviceService;
import dk.statsbiblioteket.doms.bitstorage.highlevel.HighlevelSoapException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.Connector;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Aug 25, 2010
 * Time: 1:50:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bitstorage extends Connector {

    private HighlevelBitstorageSoapWebservice service;
    private static final QName QNAME = new QName(
            "http://highlevel.bitstorage.doms.statsbiblioteket.dk/",
            "HighlevelBitstorageSoapWebserviceService");

    public Bitstorage(Credentials creds, String location)
            throws MalformedURLException {
        super(creds, location);
        URL wsdlLocation = new URL(location);
        service = new HighlevelBitstorageSoapWebserviceService(wsdlLocation,
                                                               QNAME).getHighlevelBitstorageSoapWebservicePort();
        ((BindingProvider)service).getRequestContext().put(BindingProvider.USERNAME_PROPERTY,creds.getUsername());
        ((BindingProvider)service).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,creds.getPassword());
    }

    public void uploadFileToObjectFromPermanentURLWithCharacterisation(String pid,
                                                                       String filename,
                                                                       String permanentURL,
                                                                       String md5String,
                                                                       Characterisation characterisation)
            throws BackendMethodFailedException,
                   BackendInvalidCredsException,
                   BackendInvalidResourceException
    {
        try {
            service.uploadFileToObjectFromPermanentURLWithCharacterisation(pid,
                                                                           filename,
                                                                           permanentURL,
                                                                           md5String,
                                                                           0,
                                                                           characterisation);
        } catch (HighlevelSoapException e) {
            throw new BackendMethodFailedException("The uploadFileToObjectFromPermanentURLWithCharacterisation method failed to execute",e);
        }
    }
}
