import io.restassured.RestAssured;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GroupGetUserGroupsV2WithoutSessionTest {

    private static final String BASE_URL = "https://api.ok.ru/fb.do";
    private static final String APPLICATION_KEY = "";
    private static final String METHOD = "group.getUserGroupsV2";
    private static final String UID = "";
    private static final String SECRET_KEY = "";

    private Map<String, String> baseParams;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @BeforeEach
    void mapSetUp() {
        baseParams = new HashMap<>();
        baseParams.put("application_key", APPLICATION_KEY);
        baseParams.put("method", METHOD);
        baseParams.put("uid", UID);
    }

    private String generateSig(Map<String, String> params) {
        String sortedParams = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(""));

        sortedParams += SECRET_KEY;

        return DigestUtils.md5Hex(sortedParams);
    }

    @Test
    @DisplayName("Test get all user groups")
    void testGetAllUserGroups() {
        String sig = generateSig(baseParams);

        List<GroupPojo> response = given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .extract()
                .body().jsonPath().getList("groups", GroupPojo.class);

        Assertions.assertFalse(response.isEmpty(), "The response should contain a list of groups.");
    }

    @Test
    @DisplayName("Test with invalid UID")
    void testInvalidUid() {
        baseParams.put("uid", "invalid_uid");

        String sig = generateSig(baseParams);

        given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(110))
                .body("error_msg", startsWith("PARAM_USER_ID"));
    }

    @Test
    @DisplayName("Test missing required parameters")
    void testMissingRequiredParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("application_key", APPLICATION_KEY);
        params.put("method", METHOD);

        String sig = generateSig(params);

        given()
                .queryParams(params)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(100))
                .body("error_msg", startsWith("PARAM"));
    }

    @Test
    @DisplayName("Test with incorrect SIG parameter")
    void testIncorrectSig() {

        String incorrectSig = "incorrect_sig";

        given()
                .queryParams(baseParams)
                .queryParam("sig", incorrectSig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(104))
                .body("error_msg", startsWith("PARAM_SIGNATURE"));
    }

    @Test
    @DisplayName("Test with wrong anchor parameter")
    void testWithWrongAnchor() {
        baseParams.put("anchor", "some_anchor_value");

        String sig = generateSig(baseParams);

        given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(100))
                .body("error_msg", startsWith("PARAM : Invalid paging anchor"));
    }

    @Test
    @DisplayName("Test with right direction parameter")
    void testWithRightDirection() {
        baseParams.put("direction", "AROUND");

        String sig = generateSig(baseParams);

        List<GroupPojo> response = given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .extract()
                .body().jsonPath().getList("groups", GroupPojo.class);

        System.out.println(response.toString());

        Assertions.assertFalse(response.isEmpty(), "The response should contain a list of groups.");
    }

    @Test
    @DisplayName("Test with wrong direction parameter")
    void testWithWrongDirection() {
        baseParams.put("direction", "wrong");

        String sig = generateSig(baseParams);

        given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(100))
                .body("error_msg", startsWith("PARAM : Invalid parameter direction value"));
    }

    @Test
    @DisplayName("Test with incorrect count")
    void testWithIncorrectCount() {
        baseParams.put("count", "-1");

        String sig = generateSig(baseParams);

        given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("error_code", equalTo(100))
                .body("error_msg", startsWith("PARAM : Parameter 'count' should be in range : [1..100]."));
    }

    @Test
    @DisplayName("Test with correct count")
    void testWithCorrectCount() {
        baseParams.put("count", "5");

        String sig = generateSig(baseParams);

        given()
                .queryParams(baseParams)
                .queryParam("sig", sig)
                .when()
                .get()
                .then().log().all()
                .statusCode(200)
                .body("groups.size()", lessThanOrEqualTo(5));
    }
}