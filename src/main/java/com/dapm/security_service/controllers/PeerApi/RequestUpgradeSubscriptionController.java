package com.dapm.security_service.controllers.PeerApi;

import com.dapm.security_service.models.SubscriberOrganization;
import com.dapm.security_service.models.Voucher;
import com.dapm.security_service.models.dtos2.SubscriptionRequestDto;
import com.dapm.security_service.models.enums.SubscriptionTier;
import com.dapm.security_service.repositories.SubscriberOrganizationRepository;

import com.dapm.security_service.repositories.VoucherRepository;
import com.dapm.security_service.services.TokenVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/peer")
public class RequestUpgradeSubscriptionController {

    @Autowired private VoucherRepository voucherRepository;
    @Autowired private SubscriberOrganizationRepository subscriptionRepository;
    @Autowired
    private TokenVerificationService verificationService;
    @PostMapping("/upgrade-subscription")
    public ResponseEntity<?> upgradeSubscription(@RequestBody SubscriptionRequestDto dto) {
        // 1. Validate voucher
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(dto.getVoucher());
        if (voucherOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new UpgradeResponse(false, "Invalid voucher code",null)
            );
        }

        Voucher voucher = voucherOpt.get();
        if (voucher.isRedeemed()) {
            return ResponseEntity.badRequest().body(
                    new UpgradeResponse(false, "Voucher already redeemed",null)
            );
        }

        String callerOrg = verificationService.verifyTokenAndGetOrganization(dto.getToken());
        if (callerOrg==null) {
            return ResponseEntity.status(403).body(
                    new UpgradeResponse(false, "Token organization mismatch",null)
            );
        }

        SubscriberOrganization subscriberOrganization=subscriptionRepository.findByName(callerOrg)
                .orElseThrow(() -> new IllegalArgumentException("Partner Organization not found or handshake not completed."));


        subscriberOrganization.setTier(voucher.getTier());

        subscriptionRepository.save(subscriberOrganization);

        // 3. Mark voucher as redeemed
        voucher.setRedeemed(true);
        voucher.setRedeemedAt(Instant.now());
        voucherRepository.save(voucher);

        // 4. Return success
        return ResponseEntity.ok(
                new UpgradeResponse(true,
                        "Subscription upgraded to tier: " + voucher.getTier(),voucher.getTier())
        );
    }

    // Simple DTO for responses
    record UpgradeResponse(boolean success, String message, SubscriptionTier tier) {}
}
