package subway.line;

import static fixture.LineFixtureCreator.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static testhelper.LineRequestExecutor.*;
import static testhelper.StationRequestExecutor.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import subway.api.interfaces.dto.LineCreateRequest;
import subway.api.interfaces.dto.LineResponse;

/**
 * todo -> truncate 방식에 대한 자동화 필요 (.sql에서 읽어오지 않고 table을 찾아서 삭제하는 방식 고민)
 * @author : Rene Choi
 * @since : 2024/01/27
 */
@DisplayName("지하철 노선도 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineAcceptanceTest {


	/**
	 * when 지하철 노선을 생성하면
	 * then 지하철 노선 목록 조회시 생성한 노선을 찾을 수 있다
	 */
	@Test
	@Sql(scripts = {"/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

	@DisplayName("지하철노선 생성")
	void createLine(){
		// given
		executeCreateStationRequest("지하철역");
		executeCreateStationRequest("새로운지하철역");

		// when
		ExtractableResponse<Response> request = executeCreateLineRequest(createStationLineCreateDefaultRequest());
		assertThat(request.statusCode()).isEqualTo(CREATED.value());

		// then
		ExtractableResponse<Response> response = executeGetAllStationLineRequest();
		List<String> stationNames = parseLineNames(response);
		assertThat(stationNames).containsAnyOf("신분당선");
	}


	/**
	 * Given 2개의 지하철 노선을 생성하고
	 * When 지하철 노선 목록을 조회하면
	 * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
	 */
	@Test
	@Sql(scripts = {"/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

	@DisplayName("지하철노선 목록 조회")
	void fetchLines(){

		// given
		executeCreateStationRequest("지하철역");
		executeCreateStationRequest("새로운지하철역");
		executeCreateStationRequest("또다른지하철역");
		executeCreateLineRequest(createLineCreateRequest("신분당선", 1L, 2L));
		executeCreateLineRequest(createLineCreateRequest("분당선", 1L, 3L));

		// When
		ExtractableResponse<Response> response = executeGetAllStationLineRequest();

		// Then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		List<LineResponse> lines = parseLines(response);
		assertThat(lines).hasSize(2);
	}



	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 조회하면
	 * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
	 */
	@Test
	@Sql(scripts = {"/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@DisplayName("지하철노선 조회")
	void fetchLine() {
		// given
		executeCreateStationRequest("지하철역");
		executeCreateStationRequest("새로운지하철역");
		ExtractableResponse<Response> createResponse = executeCreateLineRequest(createStationLineCreateDefaultRequest());

		Long createdLineId = parseLineId(createResponse);

		// when
		ExtractableResponse<Response> response = executeGetSpecificStationLineRequest(createdLineId);

		// then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		LineResponse line = response.as(LineResponse.class);
		assertThat(line.getId()).isEqualTo(createdLineId);
		assertThat(line.getName()).isEqualTo("신분당선");
		assertThat(line.getColor()).isEqualTo("bg-red-600");
		assertThat(line.getStations()).hasSize(2);
		assertThat(line.getStations()).extracting("name").containsExactly("지하철역", "새로운지하철역");
	}




	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 수정하면
	 * Then 해당 지하철 노선 정보는 수정된다
	 */
	@Test
	@Sql(scripts = {"/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

	@DisplayName("지하철노선 수정")
	void updateLine(){
		// given
		executeCreateStationRequest("지하철역1");
		executeCreateStationRequest("지하철역2");

		LineCreateRequest firstCreateRequest = createStationLineCreateDefaultRequest();
		firstCreateRequest.setName("1호선");
		firstCreateRequest.setColor("bg-blue-500");
		ExtractableResponse<Response> createResponse = executeCreateLineRequest(firstCreateRequest);
		Long createdLineId = parseLineId(createResponse);

		// when
		executeUpdateLineRequest(createdLineId, createStationLineUpdateDefaultRequest("다른분당선", "bg-red-600"));

		// then
		ExtractableResponse<Response> response = executeGetSpecificStationLineRequestWithOk(createdLineId);

		LineResponse line = response.as(LineResponse.class);
		assertThat(line.getName()).isEqualTo("다른분당선");
		assertThat(line.getColor()).isEqualTo("bg-red-600");
	}



	/**
	 * Given 지하철 노선을 생성하고
	 * When 생성한 지하철 노선을 삭제하면
	 * Then 해당 지하철 노선 정보는 삭제된다
	 */
	@Test
	@Sql(scripts = {"/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

	@DisplayName("지하철노선 삭제")
	void deleteLine(){
		// given
		executeCreateStationRequest("지하철역1");
		executeCreateStationRequest("지하철역2");
		ExtractableResponse<Response> createResponse = executeCreateLineRequest(createStationLineCreateDefaultRequest());
		Long createdLineId = parseLineId(createResponse);

		// when
		ExtractableResponse<Response> deleteResponse = executeDeleteLineRequest(createdLineId);

		// then
		assertThat(deleteResponse.statusCode()).isEqualTo(NO_CONTENT.value());
	}




	private  long parseLineId(ExtractableResponse<Response> createResponse) {
		return createResponse.jsonPath().getLong("id");
	}

	private List<LineResponse> parseLines(ExtractableResponse<Response> response) {
		return response.jsonPath().getList("", LineResponse.class);
	}

	public List<String> parseLineNames(ExtractableResponse<Response> response){
		return response.jsonPath().getList("name", String.class);
	}



}