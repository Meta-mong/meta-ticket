package com.metamong.metaticket.service.batch;

import com.metamong.metaticket.service.batch.draw.DetermineRankJobConfig;
import com.metamong.metaticket.service.batch.draw.EndDateConcertDTO;
import com.metamong.metaticket.service.batch.email.CancelAndSendEmailJobConfig;
import com.metamong.metaticket.service.batch.email.CancelNotPaidDrawRPW;
import com.metamong.metaticket.service.batch.email.SendEmailNextUserRPW;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class runJobScheduler {

    private final DetermineRankJobConfig determineRankJobConfig;
    private final CancelAndSendEmailJobConfig cancelAndSendEmailJobConfig;
    private final JobLauncher jobLauncher;
    private final EntityManager em;


    private List<EndDateConcertDTO> endDateConcerts;
    private List<Long> postDrawPreStartConcerts;

//    @Scheduled(cron = "0 0 1 1/1 * *")
    public void runDetermineRankJob() {
        setEndDateConcerts();

        for (EndDateConcertDTO endDateConcerts : endDateConcerts) {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("concertId", endDateConcerts.getConcertId())
                    .addLong("seatNum", (long) endDateConcerts.getSeatNum())
                    .addLong("drawCnt", endDateConcerts.getDrawCnt())
                    .toJobParameters();
            try {
                jobLauncher.run(determineRankJobConfig.determineRankJob(), jobParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    @Scheduled(cron = "0 0 2 1/1 * *")
    public void runCancelAndSendEmailJob() {
        setPostDrawPreStartConcerts();

        for (Long postDrawPreStartConcert : postDrawPreStartConcerts) {
            System.out.println("postDrawPreStartConcert = " + postDrawPreStartConcert);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("concertId", postDrawPreStartConcert)
                    .toJobParameters();
            try {
                jobLauncher.run(cancelAndSendEmailJobConfig.cancelAndSendEmailJob(), jobParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setEndDateConcerts() {
        String findEndDateConcertQuery = "select new com.metamong.metaticket.service.batch.draw.EndDateConcertDTO(c.id, c.seatNum, count(d.id)) " +
                        "from Draw d " +
                        "join d.concert c " +
                        "group by c.id, c.seatNum, c.drawEndDate " +
                        "having c.drawEndDate between :startDate and :endDate";
        this.endDateConcerts = em.createQuery(findEndDateConcertQuery, EndDateConcertDTO.class)
                .setParameter("startDate", LocalDateTime.parse(LocalDate.now() + "T00:00:00"))
                .setParameter("endDate", LocalDateTime.parse(LocalDate.now() + "T23:59:59"))
                .getResultList();
    }

    public void setPostDrawPreStartConcerts() {
        String findPostDrawPreStartConcertQuery = "select c.id " +
                "from Concert c " +
                "where :today between c.drawEndDate and c.concertDate";

        postDrawPreStartConcerts = em.createQuery(findPostDrawPreStartConcertQuery, Long.class)
                .setParameter("today", LocalDateTime.parse("2022-06-02T09:00:00"))
                //.setParameter("today", LocalDateTime.parse(LocalDate.now() + "T23:59:59"))
                .getResultList();
    }
}
