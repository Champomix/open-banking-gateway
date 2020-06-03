package de.adorsys.opba.fintech.impl.database.repositories;

import de.adorsys.opba.fintech.impl.database.entities.ConsentEntity;
import de.adorsys.opba.fintech.impl.database.entities.UserEntity;
import de.adorsys.opba.fintech.impl.tppclients.ConsentType;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConsentRepository extends CrudRepository<ConsentEntity, Long> {
    Optional<ConsentEntity> findByAuthId(String authId);
    Optional<ConsentEntity> findByUserEntityAndBankIdAndConsentTypeAndConsentConfirmed(UserEntity userEntity, String bankId, ConsentType consentType, Boolean consentConfirmed);

}