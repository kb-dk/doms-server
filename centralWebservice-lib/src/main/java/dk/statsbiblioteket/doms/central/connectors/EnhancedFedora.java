package dk.statsbiblioteket.doms.central.connectors;

import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;
import dk.statsbiblioteket.doms.central.connectors.fedora.linkpatterns.LinkPattern;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import org.w3c.dom.Document;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: abr Date: 3/29/12 Time: 2:33 PM To change this template use File | Settings | File
 * Templates.
 */
public interface EnhancedFedora {

    String STATE_ACTIVE = "A";
    String STATE_INACTIVE = "I";
    String STATE_DELETED = "D";

    public String cloneTemplate(String templatepid,
                                List<String> oldIDs,
                                String logMessage)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            ObjectIsWrongTypeException,
            BackendInvalidResourceException, PIDGeneratorException;

    public String newEmptyObject(List<String> oldIDs,
                                 List<String> collections,
                                 String logMessage)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            PIDGeneratorException;

    public ObjectProfile getObjectProfile(String pid,
                                          Long asOfTime)
            throws
            BackendMethodFailedException,
            BackendInvalidCredsException,
            BackendInvalidResourceException;

    public ObjectProfile getLimitedObjectProfile(String pid,
                                          Long asOfTime)
            throws
            BackendMethodFailedException,
            BackendInvalidCredsException,
            BackendInvalidResourceException;


    void modifyObjectLabel(String pid,
                           String name,
                           String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    void modifyObjectState(String pid,
                           String newState,
                           String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    void deleteObject(String pid,
                      String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Date modifyDatastreamByValue(String pid,
                                 String datastream,
                                 String contents,
                                 List<String> alternativeIdentifiers,
                                 String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Date modifyDatastreamByValue(String pid,
                                 String datastream,
                                 String contents,
                                 String md5sum,
                                 List<String> alternativeIdentifiers,
                                 String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Date modifyDatastreamByValue(String pid,
                                 String datastream,
                                 String contents,
                                 String checksumType,
                                 String checksum,
                                 List<String> alternativeIdentifiers,
                                 String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    /**
       * Overloaded method for modifying a datastream which throws a ConcurrentModificationException if the
       * datastream has been modified since a given timestamp.
       * @param pid
       * @param datastream
       * @param checksumType
       * @param checksum
       * @param contents
       * @param alternativeIdentifiers
       * @param comment
       * @param lastModifiedDate the timestamp to check, in milliseconds e.g. as generated by Date().getTime().
       * @throws BackendMethodFailedException
       * @throws BackendInvalidCredsException
       * @throws BackendInvalidResourceException
       * @throws java.util.ConcurrentModificationException
       */
      Date modifyDatastreamByValue(String pid,
                                   String datastream,
                                   ChecksumType checksumType,
                                   String checksum,
                                   byte[] contents,
                                   List<String> alternativeIdentifiers,
                                   String comment,
                                   Long lastModifiedDate)
              throws
              BackendMethodFailedException,
              BackendInvalidCredsException,
              BackendInvalidResourceException,
              ConcurrentModificationException;

    Date modifyDatastreamByValue(String pid,
                                 String datastream,
                                 ChecksumType checksumType,
                                 String checksum,
                                 byte[] contents,
                                 List<String> alternativeIdentifiers,
                                 String mimeType,
                                 String comment,
                                 Long lastModifiedDate)
            throws
            BackendMethodFailedException,
            BackendInvalidCredsException,
            BackendInvalidResourceException,
            ConcurrentModificationException;

    void deleteDatastream(String pid,
                          String datastream,
                          String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    String getXMLDatastreamContents(String pid,
                                    String datastream,
                                    Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    String getXMLDatastreamContents(String pid,
                                    String datastream)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;


    Date addExternalDatastream(String pid,
                               String datastream,
                               String filename,
                               String permanentURL,
                               String formatURI,
                               String mimetype,
                               List<String> alternativeIdentifiers,
                               String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Date addExternalDatastream(String pid,
                               String datastream,
                               String filename,
                               String permanentURL,
                               String formatURI,
                               String mimetype,
                               String checksumType,
                               String checksum,
                               List<String> alternativeIdentifiers,
                               String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Date addExternalDatastream(String pid,
                               String datastream,
                               String filename,
                               String permanentURL,
                               String formatURI,
                               String mimetype,
                               String md5sum,
                               List<String> alternativeIdentifiers,
                               String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    List<String> listObjectsWithThisLabel(String label)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException;

    void addRelation(String pid,
                     String subject,
                     String predicate,
                     String object,
                     boolean literal,
                     String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    void addRelations(String pid,
                     String subject,
                     String predicate,
                     List<String> objects,
                     boolean literal,
                     String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;


    List<FedoraRelation> getNamedRelations(String pid,
                                           String predicate,
                                           Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    List<FedoraRelation> getInverseRelations(String pid,
                                             String predicate)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    void deleteRelation(String pid,
                        String subject,
                        String predicate,
                        String object,
                        boolean literal,
                        String comment)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    Document createBundle(String pid,
                          String viewAngle,
                          Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    List<String> findObjectFromDCIdentifier(String string)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException;

    List<SearchResult> fieldsearch(String query,
                                   int offset,
                                   int pageSize)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException;

    void flushTripples()
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException;

    List<String> getContentModelsInCollection(String collectionPid)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException;

    public String invokeMethod(String cmpid,
                               String methodName,
                               Map<String, List<String>> parameters,
                               Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    public List<Method> getStaticMethods(String cmpid,
                                         Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    public List<Method> getDynamicMethods(String objpid,
                                          Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    public List<LinkPattern> getLinks(String pid,
                                      Long asOfTime)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

    public Validation validate(String pid)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            BackendInvalidResourceException;

}
