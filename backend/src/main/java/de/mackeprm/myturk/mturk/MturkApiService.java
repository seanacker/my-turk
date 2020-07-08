package de.mackeprm.myturk.mturk;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.mturk.externalhit.MturkExternalHITXmlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.mturk.MTurkClient;
import software.amazon.awssdk.services.mturk.model.*;
import software.amazon.awssdk.services.mturk.model.Comparator;
import software.amazon.awssdk.services.mturk.model.Locale;
import software.amazon.awssdk.services.mturk.paginators.ListHITsIterable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class MturkApiService {
    private static final Logger log = LoggerFactory.getLogger(MturkApiService.class);
    private static final int DEFAULT_FRAME_HEIGHT = 800;
    public static final String NUMBER_OF_HITS_FINISHED = "00000000000000000040";
    public static final String APPROVAL_PERCENTAGE = "000000000000000000L0";
    public static final String LOCATION = "00000000000000000071";

    private final MturkExternalHITXmlGenerator xmlGenerator;

    //TODO configure via: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config
    //TODO use proxy true/false
    private final Map<Endpoint, MTurkClient> endpointClients;
    private boolean useProxy;
    @Value("${aws.mturk.proxy.host:#{null}}")
    private String proxyHost;
    @Value("${aws.mturk.proxy.port:#{80}}")
    private int proxyPort = 80;

    public MturkApiService(MturkExternalHITXmlGenerator xmlGenerator) {
        this.xmlGenerator = xmlGenerator;
        endpointClients = new HashMap<>();
    }

    public String login(Endpoint endpoint, String accessKeyId, String secretAccessKey) throws MTurkException {
        log.info("Trying to login to " + endpoint.name());
        MTurkClient reconfiguredClient = MTurkClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .endpointOverride(endpoint.getUri())
                .build();
        endpointClients.put(endpoint, reconfiguredClient);
        final GetAccountBalanceResponse accountBalance = reconfiguredClient.getAccountBalance();
        return accountBalance.availableBalance();
    }

    //Balance
    public String getAccountBalance(Endpoint endpoint) {
        MTurkClient client = this.endpointClients.get(endpoint);
        final GetAccountBalanceResponse accountBalance = client.getAccountBalance();
        return accountBalance.availableBalance();
    }

    //HITs
    public List<HIT> listHitsByPage(Endpoint endpoint, int pagesize, String continuationToken) {
        if (endpointClients.get(endpoint) == null) {
            throw new IllegalStateException("Cannot connect to " + endpoint.name() + " because it's not configured");
        }
        ListHiTsRequest.Builder builder = ListHiTsRequest.builder().maxResults(pagesize);
        if (isNotBlank(continuationToken)) {
            builder.nextToken(continuationToken);
        }
        final ListHiTsResponse listHiTsResponse = endpointClients.get(endpoint).listHITs(builder.build());
        final String responseToken = listHiTsResponse.nextToken();
        log.info("Continuation Token is: " + responseToken);
        return listHiTsResponse.hiTs();
    }

    public List<HIT> listAllHits(Endpoint endpoint) {
        if (endpointClients.get(endpoint) == null) {
            throw new IllegalStateException("Cannot connect to " + endpoint.name() + " because it's not configured");
        }
        ListHITsIterable listHiTsResponses = endpointClients.get(endpoint).listHITsPaginator();
        List<HIT> result = new ArrayList<>();
        for (ListHiTsResponse response : listHiTsResponses) {
            result.addAll(response.hiTs());
        }
        return result;
    }


    public HIT createHitForExperiment(Experiment experiment) throws TransformerException, ParserConfigurationException {
        final Endpoint endpoint = experiment.getEndpoint();
        final MTurkClient mTurkClient = endpointClients.get(endpoint);
        if (mTurkClient == null) {
            throw new IllegalStateException("Cannot connect to " + endpoint.name() + " because it's not configured");
        }
        final CreateHitRequest.Builder builder = CreateHitRequest.builder()
                .title(experiment.getTitle())
                .description(experiment.getDescription())
                .keywords(String.join(",", experiment.getKeywords()))
                .question(xmlGenerator.generate(experiment.getEntrypoint(), DEFAULT_FRAME_HEIGHT))
                .maxAssignments(experiment.getAssignmentsPerHit())
                .lifetimeInSeconds(experiment.getHitExpiration().toSeconds())
                .assignmentDurationInSeconds(experiment.getAssignmentDuration().toSeconds())
                .reward(Double.toString(experiment.getReward()))
                //TODO metadata: seeAlso?
                .requesterAnnotation(experiment.getId())
                .qualificationRequirements(buildRequirements(experiment));
        //actually create the hit:
        final CreateHitResponse response = mTurkClient.createHIT(builder.build());
        log.info("Successfully created hit:" + response.toString());
        return response.hit();
    }

    private Collection<QualificationRequirement> buildRequirements(Experiment experiment) {
        final List<QualificationRequirement> qualificationRequirements = new ArrayList<>();

        //QUALIFICATIONS
        //for more info see: http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_QualificationRequirementDataStructureArticle.html#ApiReference_QualificationType-IDs
        //at Least 1.000 completed hits Worker_​NumberHITsApproved 00000000000000000040

        //TODO level of trust

        if (experiment.isIncludeDefaultRequirements()) {
            final QualificationRequirement moreThanThousandHITS = QualificationRequirement.builder()
                    .qualificationTypeId(NUMBER_OF_HITS_FINISHED)
                    .comparator(Comparator.GREATER_THAN_OR_EQUAL_TO)
                    .integerValues(Collections.singleton(1_000))
                    .requiredToPreview(Boolean.TRUE)
                    .build();
            qualificationRequirements.add(moreThanThousandHITS);

            //approval > 95 % Worker_​PercentAssignmentsApproved 000000000000000000L0
            final QualificationRequirement moreThanNinetyFivePercentApproval = QualificationRequirement.builder()
                    .qualificationTypeId(APPROVAL_PERCENTAGE)
                    .comparator(Comparator.GREATER_THAN_OR_EQUAL_TO)
                    .integerValues(Collections.singleton(95))
                    .requiredToPreview(Boolean.TRUE)
                    .build();
            qualificationRequirements.add(moreThanNinetyFivePercentApproval);

            //US Based: Locale is:  00000000000000000071
            final QualificationRequirement usBased = QualificationRequirement.builder()
                    .qualificationTypeId(LOCATION)
                    .comparator(Comparator.IN)
                    .localeValues(Locale.builder().country("US").build())
                    .requiredToPreview(Boolean.TRUE)
                    .build();
            qualificationRequirements.add(usBased);
        }

        //b) Award a qualification after completing the HIT:
        /*
        http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_CreateQualificationTypeOperation.html
        http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMechanicalTurkRequester/Concepts_QualificationsArticle.html
        http://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_QualificationRequirementDataStructureArticle.html
         */
        if (experiment.getCompletedExperimentQualification() != null) {
            final QualificationRequirement userHasNotParticipatedBefore = QualificationRequirement.builder()
                    .qualificationTypeId(experiment.getCompletedExperimentQualification().getQualificationTypeId())
                    .comparator(Comparator.DOES_NOT_EXIST)
                    .requiredToPreview(Boolean.TRUE)
                    .build();
            qualificationRequirements.add(userHasNotParticipatedBefore);
        }
        return qualificationRequirements;
    }

    //TODO findById
    //TODO can i save the experiment in a user-defined field @ AWS?
}
