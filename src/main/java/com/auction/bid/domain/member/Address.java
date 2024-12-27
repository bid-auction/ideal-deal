package com.auction.bid.domain.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Address {

    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @NotEmpty
    private String zipcode;
}
