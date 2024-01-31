package subway.api.infrastructure.operators;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import subway.api.domain.dto.inport.SectionCreateCommand;
import subway.api.domain.model.entity.Line;
import subway.api.domain.model.entity.Section;
import subway.api.domain.model.entity.Station;
import subway.api.domain.operators.SectionFactory;
import subway.api.infrastructure.persistence.SectionRepository;
import subway.api.infrastructure.persistence.StationRepository;
import subway.api.interfaces.dto.LineCreateRequest;
import subway.api.interfaces.dto.SectionCreateRequest;

/**
 * @author : Rene Choi
 * @since : 2024/01/27
 */
@Component
@RequiredArgsConstructor
public class SimpleSectionFactory implements SectionFactory {
	private final StationRepository stationRepository;
	private final SectionRepository sectionRepository;

	@Override
	public Section createSection(LineCreateRequest request, Line line) {

		Station upStation = stationRepository.findById(request.getUpStationId()).orElseThrow();
		Station downStation = stationRepository.findById(request.getDownStationId()).orElseThrow();

		Section section = Section.of(upStation, downStation, request.getDistance(), line);

		return sectionRepository.save(section);
	}

	@Override
	public Section createSection(SectionCreateCommand command, Line line, Station upStation, Station downStation) {
		Section section = Section.of(upStation, downStation, command.getDistance(), line);
		return sectionRepository.save(section);
	}

	@Override
	public void deleteByLine(Line line) {
		sectionRepository.deleteByLine(line);
	}
}