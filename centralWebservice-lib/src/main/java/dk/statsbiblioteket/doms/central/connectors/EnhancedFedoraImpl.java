package dk.statsbiblioteket.doms.central.connectors;

import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import org.w3c.dom.Document;

import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.Fedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.FedoraRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.fedoraDBsearch.DBSearchRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;
import dk.statsbiblioteket.doms.central.connectors.fedora.linkpatterns.LinkPattern;
import dk.statsbiblioteket.doms.central.connectors.fedora.linkpatterns.LinkPatternsImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.Methods;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.MethodsImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PidGenerator;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PidGeneratorImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.Templates;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.TemplatesImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.tripleStore.TripleStore;
import dk.statsbiblioteket.doms.central.connectors.fedora.tripleStore.TripleStoreRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.views.Views;
import dk.statsbiblioteket.doms.central.connectors.fedora.views.ViewsImpl;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of EnhancedFedora using REST.
 */
public class EnhancedFedoraImpl implements EnhancedFedora {

    private final LinkPatternsImpl linkPatterns;
    DBSearchRest db;
    Fedora fedora;
    TripleStore ts;
    Templates templates;
    Views views;
    PidGenerator pidGenerator;
    private Methods methods;

    public EnhancedFedoraImpl(Credentials creds, String fedoraLocation, String pidGenLocation, String thisLocation)
            throws MalformedURLException, PIDGeneratorException, JAXBException {

        //1.st level
        fedora = new FedoraRest(creds, fedoraLocation);
        ts = new TripleStoreRest(creds, fedoraLocation, fedora);
        db = new DBSearchRest(creds, fedoraLocation);
        pidGenerator = new PidGeneratorImpl(pidGenLocation);


        //3. level
        templates = new TemplatesImpl(fedora, pidGenerator, ts);
        views = new ViewsImpl(ts, fedora);

        methods = new MethodsImpl(fedora, thisLocation);

        linkPatterns = new LinkPatternsImpl(fedora, fedoraLocation);
    }

    /**
     * Initialise a version of EnhancedFedora where we retry a number of times on 409 results. This is used because
     * URLConnection may
     * retry PUT or POST requests on timeout or connection errors, and this may result in spurious locks where the
     * original request still has the object locked.
     * We delay between retries, and the delay is done with exponential backoff, first waiting retryDelay, and
     * 2*retryDelay, then 4*retryDelay and so forth.
     *
     * @param creds          Credentials for communicating with Fedora.
     * @param fedoraLocation Location of Fedora.
     * @param pidGenLocation Location of PID Generator.
     * @param thisLocation   Not actually used.
     * @param maxTriesPut    Number of times to try a PUT request.
     * @param maxTriesPost   Number of times to try a POST request.
     * @param maxTriesDelete Number of times to try a DELETE request.
     * @param retryDelay     Delay between retries (with exponential backoff).
     * @throws JAXBException
     * @throws PIDGeneratorException
     * @throws MalformedURLException
     */
    public EnhancedFedoraImpl(Credentials creds, String fedoraLocation, String pidGenLocation, String thisLocation,
                              int maxTriesPut, int maxTriesPost, int maxTriesDelete, int retryDelay)
            throws JAXBException, PIDGeneratorException, MalformedURLException {

        //1.st level
        fedora = new FedoraRest(creds, fedoraLocation, maxTriesPut, maxTriesPost, maxTriesDelete, retryDelay);
        ts = new TripleStoreRest(creds, fedoraLocation, fedora);
        db = new DBSearchRest(creds, fedoraLocation);
        pidGenerator = new PidGeneratorImpl(pidGenLocation);


        //3. level
        templates = new TemplatesImpl(fedora, pidGenerator, ts);
        views = new ViewsImpl(ts, fedora);

        methods = new MethodsImpl(fedora, thisLocation);

        linkPatterns = new LinkPatternsImpl(fedora, fedoraLocation);
    }

    /**
     * Helper overloaded constructor, that uses the same value for maxTriesPut, maxTriesPost and maxTriesDelete.
     *
     * @param creds          Credentials for communicating with Fedora.
     * @param fedoraLocation Location of Fedora.
     * @param pidGenLocation Location of PID Generator.
     * @param thisLocation   Not actually used.
     * @param maxTries    Number of times to try a PUT, POST or DELETE request.
     * @param retryDelay     Delay between retries (with exponential backoff).
     * @throws JAXBException
     * @throws PIDGeneratorException
     * @throws MalformedURLException
     */
    public EnhancedFedoraImpl(Credentials creds, String fedoraLocation, String pidGenLocation, String thisLocation,
                              int maxTries, int retryDelay)
            throws JAXBException, PIDGeneratorException, MalformedURLException {
        this(creds, fedoraLocation, pidGenLocation, thisLocation, maxTries, maxTries, maxTries, retryDelay);
    }

    public String cloneTemplate(String templatepid, List<String> oldIDs, String logMessage)
            throws BackendInvalidCredsException, BackendMethodFailedException, ObjectIsWrongTypeException,
            BackendInvalidResourceException, PIDGeneratorException {
        return templates.cloneTemplate(templatepid, oldIDs, logMessage);
    }

    @Override
    public String newEmptyObject(List<String> oldIDs, List<String> collections, String logMessage)
            throws BackendInvalidCredsException, BackendMethodFailedException, PIDGeneratorException {
        String pid = pidGenerator.generateNextAvailablePID("new_");
        return fedora.newEmptyObject(pid, oldIDs, collections, logMessage);
    }

    public ObjectProfile getObjectProfile(String pid, Long asOfTime)
            throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
        return fedora.getObjectProfile(pid, asOfTime);
    }

    @Override
    public ObjectProfile getLimitedObjectProfile(String pid, Long asOfTime) throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
        return fedora.getLimitedObjectProfile(pid, asOfTime);
    }

    @Override
    public void modifyObjectLabel(String pid, String name, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.modifyObjectLabel(pid, name, comment);
    }

    @Override
    public void modifyObjectState(String pid, String newState, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.modifyObjectState(pid, newState, comment);
    }

    @Override
    public void deleteObject(String pid, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.deleteObject(pid, comment);
    }

    @Override
    public Date modifyDatastreamByValue(String pid, String datastream, String contents,
                                        List<String> alternativeIdentifiers, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora
                .modifyDatastreamByValue(pid, datastream, null, null, asByteArray(contents), alternativeIdentifiers,
                                         comment);
    }

    private byte[] asByteArray(String contents) {
        try {
            return contents.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("UTF-8 not known");
        }
    }

    @Override
    public Date modifyDatastreamByValue(String pid, String datastream, String contents, String md5sum,
                                        List<String> alternativeIdentifiers, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.modifyDatastreamByValue(pid, datastream, ChecksumType.MD5, md5sum, asByteArray(contents),
                                              alternativeIdentifiers, comment);
    }

    @Override
    public Date modifyDatastreamByValue(String pid, String datastream, String contents, String checksumType,
                                        String checksum, List<String> alternativeIdentifiers, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        ChecksumType properChecksumType = null;
        if (checksumType != null) {
            try {
                properChecksumType = ChecksumType.valueOf(checksumType);
            } catch (IllegalArgumentException e) {
                properChecksumType = ChecksumType.MD5;
            }
        }

        return fedora.modifyDatastreamByValue(pid, datastream, properChecksumType, checksum, asByteArray(contents),
                                              alternativeIdentifiers, comment);
    }

    @Override
    public Date modifyDatastreamByValue(String pid, String datastream, ChecksumType checksumType, String checksum,
                                        byte[] contents, List<String> alternativeIdentifiers, String comment,
                                        Long lastModifiedDate)
            throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException,
            ConcurrentModificationException {
        return fedora.modifyDatastreamByValue(pid, datastream, checksumType, checksum, contents, alternativeIdentifiers,
                                              comment, lastModifiedDate);
    }

    @Override
    public Date modifyDatastreamByValue(String pid, String datastream, ChecksumType checksumType, String checksum,
                                        byte[] contents, List<String> alternativeIdentifiers, String mimeType,
                                        String comment, Long lastModifiedDate)
            throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException,
            ConcurrentModificationException {
        return fedora.modifyDatastreamByValue(pid, datastream, checksumType, checksum, contents, alternativeIdentifiers,
                                              mimeType, comment, lastModifiedDate);
    }

    @Override
    public void deleteDatastream(String pid, String datastream, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.deleteDatastream(pid, datastream, comment);
    }

    @Override
    public String getXMLDatastreamContents(String pid, String datastream, Long asOfDateTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.getXMLDatastreamContents(pid, datastream, asOfDateTime);
    }

    @Override
    public String getXMLDatastreamContents(String pid, String datastream)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.getXMLDatastreamContents(pid, datastream, null);
    }

    @Override
    public Date addExternalDatastream(String pid, String datastream, String filename, String permanentURL,
                                      String formatURI, String mimetype, List<String> alternativeIdentifiers,
                                      String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.addExternalDatastream(pid, datastream, filename, permanentURL, formatURI, mimetype, null, null,
                                            comment);
    }

    @Override
    public Date addExternalDatastream(String pid, String datastream, String filename, String permanentURL,
                                      String formatURI, String mimetype, String checksumType, String checksum,
                                      List<String> alternativeIdentifiers, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.addExternalDatastream(pid, datastream, filename, permanentURL, formatURI, mimetype, checksumType,
                                            checksum, comment);
    }

    @Override
    public Date addExternalDatastream(String pid, String datastream, String filename, String permanentURL,
                                      String formatURI, String mimetype, String md5sum,
                                      List<String> alternativeIdentifiers, String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.addExternalDatastream(pid, datastream, filename, permanentURL, formatURI, mimetype,
                                            ChecksumType.MD5.name(), md5sum, comment);
    }

    @Override
    public List<String> listObjectsWithThisLabel(String label)
            throws BackendInvalidCredsException, BackendMethodFailedException {
        return db.listObjectsWithThisLabel(label);
    }

    @Override
    public void addRelation(String pid, String subject, String predicate, String object, boolean literal,
                            String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.addRelation(pid, subject, predicate, object, literal, comment);
    }

    @Override
    public void addRelations(String pid, String subject, String predicate, List<String> objects, boolean literal,
                             String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.addRelations(pid, subject, predicate, objects, literal, comment);
    }

    @Override
    public List<FedoraRelation> getNamedRelations(String pid, String predicate, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.getNamedRelations(pid, predicate, asOfTime);
    }

    @Override
    public List<FedoraRelation> getInverseRelations(String pid, String name)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return ts.getInverseRelations(pid, name);
    }

    @Override
    public void deleteRelation(String pid, String subject, String predicate, String object, boolean literal,
                               String comment)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        fedora.deleteRelation(pid, subject, predicate, object, literal, comment);
    }

    @Override
    public Document createBundle(String pid, String viewAngle, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return views.getViewObjectBundleForObject(pid, viewAngle, asOfTime);
    }

    @Override
    public List<String> findObjectFromDCIdentifier(String string)
            throws BackendInvalidCredsException, BackendMethodFailedException {
        return db.findObjectFromDCIdentifier(string);
    }

    @Override
    public List<SearchResult> fieldsearch(String query, int offset, int pageSize)
            throws BackendInvalidCredsException, BackendMethodFailedException {
        return fedora.fieldsearch(query, offset, pageSize);
    }

    @Override
    public void flushTripples() throws BackendInvalidCredsException, BackendMethodFailedException {
        ts.flushTriples();
    }

    @Override
    public List<String> getContentModelsInCollection(String collectionPid)
            throws BackendInvalidCredsException, BackendMethodFailedException {
        return ts.getContentModelsInCollection(collectionPid);
    }

    @Override
    public List<Method> getStaticMethods(String cmpid, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return methods.getStaticMethods(cmpid, asOfTime);
    }

    @Override
    public List<LinkPattern> getLinks(String pid, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return linkPatterns.getLinkPatterns(pid, asOfTime);

    }

    @Override
    public Validation validate(String pid)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return fedora.validate(pid);
    }

    @Override
    public List<Method> getDynamicMethods(String objpid, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return methods.getDynamicMethods(objpid, asOfTime);
    }

    @Override
    public String invokeMethod(String cmpid, String methodName, Map<String, List<String>> parameters, Long asOfTime)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return methods.invokeMethod(cmpid, methodName, parameters, asOfTime);
    }

}
