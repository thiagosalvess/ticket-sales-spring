package com.thiagosalvess.ticketsales.features.partner.service;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerRegisterRequest;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerResponse;
import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import com.thiagosalvess.ticketsales.features.partner.repository.PartnerRepository;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartnerServiceTest {
    private PartnerRepository partnerRepo;
    private UserRepository userRepo;
    private PasswordEncoder encoder;
    private PartnerService service;

    @BeforeEach
    public void setUp() {
        partnerRepo = mock(PartnerRepository.class);
        userRepo = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new PartnerService(partnerRepo, userRepo, encoder);
    }

    @Test
    public void shouldCreateAPatnerSuccessfully() {
        when(encoder.encode("123456")).thenReturn("hashed");
        var savedUser = User.builder().id(10L).name("Partner")
                .email("p@x.com").password("hashed").createdAt(Instant.now()).build();
        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        var savedPartner = Partner.builder().id(5L)
                .user(savedUser).companyName("Comp").createdAt(Instant.now()).build();
        when(partnerRepo.save(any(Partner.class))).thenReturn(savedPartner);

        var dto = new PartnerRegisterRequest("Partner", "p@x.com", "123456", "Comp");
        PartnerResponse resp = service.register(dto);

        assertThat(resp.id()).isEqualTo(5L);
        assertThat(resp.userId()).isEqualTo(10L);
        verify(userRepo).save(any(User.class));
        verify(partnerRepo).save(any(Partner.class));
    }

    @Test
    public void shouldFindUserByIdSuccessfully() {
        var creatAt = Instant.now();
        var user = User.builder().id(10L).name("P").email("p@x.com").build();
        var partner = Partner.builder().id(7L).user(user).companyName("Company name").createdAt(creatAt).build();
        when(partnerRepo.findByUserId(10L)).thenReturn(Optional.of(partner));

        var userFound = service.findByUserId(10L);

        assertThat(userFound.id()).isEqualTo(partner.getId());
        assertThat(userFound.companyName()).isEqualTo(partner.getCompanyName());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenUserIsMissing() {
        when(partnerRepo.findByUserId(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByUserId(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Partner not found");
    }
}
