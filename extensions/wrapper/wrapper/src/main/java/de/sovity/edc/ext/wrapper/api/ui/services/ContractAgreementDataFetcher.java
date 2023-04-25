/*
 *  Copyright (c) 2022 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.edc.ext.wrapper.api.ui.services;

import lombok.RequiredArgsConstructor;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.connector.spi.contractagreement.ContractAgreementService;
import org.eclipse.edc.connector.spi.transferprocess.TransferProcessService;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.List;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class ContractAgreementDataFetcher {
    private final ContractAgreementService contractAgreementService;
    private final ContractNegotiationStore contractNegotiationStore;
    private final TransferProcessService transferProcessService;

    /**
     * Fetches all contract agreements as {@link ContractAgreementData}s.
     *
     * @return {@link ContractAgreementData}s
     */
    public List<ContractAgreementData> getContractAgreements() {
        var agreements = getAllContractAgreements();

        var negotiations = getAllContractNegotiations().stream()
                .filter(it -> it.getContractAgreement() != null)
                .collect(toMap(it -> it.getContractAgreement().getId(), identity()));

        var transfers = getAllTransferProcesses().stream()
                .collect(groupingBy(it -> it.getDataRequest().getContractId()));

        return agreements.stream()
                .filter(agreement -> negotiations.containsKey(agreement.getId()))
                .map(agreement -> {
                    var negotiation = negotiations.get(agreement.getId());
                    var asset = findAssetInNegotiations(agreement, negotiation);
                    var contractTransfers = transfers.getOrDefault(agreement.getId(), List.of());

                    return new ContractAgreementData(agreement, negotiation, asset, contractTransfers);
                })
                .toList();
    }

    private List<ContractNegotiation> getAllContractNegotiations() {
        return contractNegotiationStore.queryNegotiations(QuerySpec.max()).toList();
    }

    private List<ContractAgreement> getAllContractAgreements() {
        return contractAgreementService.query(QuerySpec.max()).getContent().toList();
    }

    private List<TransferProcess> getAllTransferProcesses() {
        return transferProcessService.query(QuerySpec.max()).getContent().toList();
    }

    private Asset findAssetInNegotiations(ContractAgreement agreement, ContractNegotiation negotiation) {
        // Agreements have a single assetId
        // The negotiation has multiple contract offers
        // Find the asset from the contract offer
        return negotiation.getContractOffers().stream()
                .map(ContractOffer::getAsset)
                .filter(it -> agreement.getAssetId().equals(it.getId()))
                .findFirst()
                .orElseGet(() -> Asset.Builder.newInstance().id(agreement.getAssetId()).build());
    }
}
