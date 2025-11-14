package futureStack.futureStack.checkIn;

import futureStack.futureStack.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CheckInService {

    @Autowired
    private CheckInRepository repository;

    @Autowired
    private WScoreCalculator calculator;

    @CacheEvict(value = "userCheckIns", key = "#user.idUser")
    public CheckInModel createCheckIn(User user, CheckInRequestDTO dto) {
        if (repository.findByUser_IdAndDate(user.getId(), LocalDate.now()).isPresent()) {
            throw new RuntimeException("Check-in já realizado hoje");
        }

        CheckInModel checkIn = new CheckInModel();
        checkIn.setUser(user);
        checkIn.setDate(LocalDate.now());
        checkIn.setMood(dto.mood());
        checkIn.setEnergy(dto.energy());
        checkIn.setSleep(dto.sleep());
        checkIn.setFocus(dto.focus());
        checkIn.setHoursWorked(dto.hoursWorked());

        int score = calculator.calculateScore(
                dto.mood(),
                dto.energy(),
                dto.sleep(),
                dto.focus(),
                dto.hoursWorked()
        );
        checkIn.setScore(score);

        return repository.save(checkIn);
    }

    @Cacheable(value = "userCheckIns", key = "#userId")
    public List<CheckInModel> getUserCheckIns(Long userId) {
        return repository.findByUser_IdOrderByDateDesc(userId);
    }

    public Page<CheckInModel> getUserCheckInsPaginated(Long userId, Pageable pageable) {
        return repository.findByUser_IdOrderByDateDesc(userId, pageable);
    }

    public Double getWeeklyAverage(Long userId) {
        return repository.findAverageScoreSince(userId, LocalDate.now().minusDays(7));
    }

    public CheckInModel getTodayCheckIn(Long userId) {
        return repository.findByUser_IdAndDate(userId, LocalDate.now()).orElse(null);
    }

    public List<CheckInModel> getHistory(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return repository.findByUser_IdOrderByDateDesc(userId)
                .stream()
                .filter(c -> !c.getDate().isBefore(startDate))
                .toList();
    }

    public CheckInStatisticsDTO getStatistics(Long userId) {
    var checkIns = repository.findByUser_IdOrderByDateDesc(userId);

    if (checkIns.isEmpty()) {
        return new CheckInStatisticsDTO(0, 0, 0, 0, 0);
    }

    double avgMood = checkIns.stream().mapToInt(CheckInModel::getMood).average().orElse(0);
    double avgEnergy = checkIns.stream().mapToInt(CheckInModel::getEnergy).average().orElse(0);
    double avgSleep = checkIns.stream().mapToInt(CheckInModel::getSleep).average().orElse(0);
    double avgFocus = checkIns.stream().mapToInt(CheckInModel::getFocus).average().orElse(0);
    double avgScore = checkIns.stream().mapToInt(CheckInModel::getScore).average().orElse(0);

    return new CheckInStatisticsDTO(
            Math.round(avgMood * 10.0) / 10.0,
            Math.round(avgEnergy * 10.0) / 10.0,
            Math.round(avgSleep * 10.0) / 10.0,
            Math.round(avgFocus * 10.0) / 10.0,
            Math.round(avgScore * 10.0) / 10.0
    );
}

    public Double getMonthlyAverage(Long userId) {
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    return repository.findAverageScoreSince(userId, startOfMonth);
}

}
