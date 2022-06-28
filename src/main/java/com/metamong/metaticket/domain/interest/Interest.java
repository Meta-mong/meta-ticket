package com.metamong.metaticket.domain.interest;

import com.metamong.metaticket.domain.BaseEntity;
import com.metamong.metaticket.domain.concert.Concert;
import com.metamong.metaticket.domain.user.User;
import lombok.*;

import javax.persistence.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "Interests")
@Entity
public class Interest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private Concert concert;
}
