package subway.api.infrastructure.operators;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import subway.api.domain.model.entity.Station;
import subway.api.domain.operators.StationResolver;
import subway.api.infrastructure.persistence.StationRepository;

/**
 * @author : Rene Choi
 * @since : 2024/01/31
 */
@Component
@RequiredArgsConstructor
public class SimpleStationResolver implements StationResolver {

	private final StationRepository stationRepository;

	@Override
	public List<Station> fetchAll() {
		return stationRepository.findAll();
	}

	@Override
	public Optional<Station> fetchOptional(Long id) {
		return stationRepository.findById(id);
	}
}
