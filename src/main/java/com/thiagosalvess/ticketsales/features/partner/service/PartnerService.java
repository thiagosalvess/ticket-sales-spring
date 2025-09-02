package com.thiagosalvess.ticketsales.features.partner.service;

import com.thiagosalvess.ticketsales.common.exception.ConflictException;
import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerRegisterRequest;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerResponse;
import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import com.thiagosalvess.ticketsales.features.partner.repository.PartnerRepository;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PartnerService {
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PartnerService(PartnerRepository partnerRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PartnerResponse register(PartnerRegisterRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already in use");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .build();

        user = userRepository.save(user);

        Partner partner = Partner.builder()
                .companyName(dto.companyName())
                .user(user)
                .build();

        partner = partnerRepository.save(partner);

        return new PartnerResponse(
                partner.getId(),
                user.getName(),
                user.getId(),
                partner.getCompanyName(),
                partner.getCreatedAt()
        );
    }

    public PartnerResponse findByUserId(Long userId) {
        Partner partner = partnerRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("Partner not found for userId: " + userId));

        User user = partner.getUser();

        return new PartnerResponse(
                partner.getId(),
                user.getName(),
                user.getId(),
                partner.getCompanyName(),
                partner.getCreatedAt()
        );
    }
}
