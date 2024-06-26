package de.sovity.edc.client;

import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.spi.asset.AssetService;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class TransferProcessTestUtils {
    public static final String DATA_SINK = "http://my-data-sink/api/stuff";
    public static final String COUNTER_PARTY_ADDRESS = "http://some-other-connector/api/v1/ids/data";
    public static final String PROVIDING_CONTRACT_ID = "provider-contract:eb934d1f-6582-4bab-85e6-af19a76f7e2b";
    public static final String CONSUMING_CONTRACT_ID = "consumer-contract:f52a5d30-6356-4a55-a75a-3c45d7a88c3e";
    public static final String VALID_ASSET_ID = "urn:artifact:asset";
    public static final String UNKNOWN_ASSET_ID = "urn:junk";
    public static final String ASSET_NAME = "Test asset";
    public static final String PROVIDING_TRANSFER_PROCESS_ID = "81cdf4cf-8427-480f-9662-8a29d66ddd3b";
    public static final String CONSUMING_TRANSFER_PROCESS_ID = "be0cac12-bb43-420e-aa29-d66bb3d0e0ac";

    @NotNull
    public static void createProvidingTransferProcesses(ContractNegotiationStore store, TransferProcessStore transferProcessStore, AssetService assetStore) throws ParseException {
        DataAddress dataAddress = getDataAddress();
        createAsset(assetStore, dataAddress, VALID_ASSET_ID, ASSET_NAME);

        // preparing providing transfer process
        var providerAgreement = createContractAgreement(PROVIDING_CONTRACT_ID, VALID_ASSET_ID);
        createContractNegotiation(store, COUNTER_PARTY_ADDRESS, providerAgreement, ContractNegotiation.Type.PROVIDER);
        createTransferProcess(VALID_ASSET_ID,
                PROVIDING_CONTRACT_ID,
                dataAddress,
                TransferProcess.Type.PROVIDER,
                PROVIDING_TRANSFER_PROCESS_ID,
                "2023-07-08",
                "TransferProcessManager: attempt #8 failed to send transfer",
                transferProcessStore);
    }

    @NotNull
    public static void createConsumingTransferProcesses(ContractNegotiationStore store, TransferProcessStore transferProcessStore) throws ParseException {
        DataAddress dataAddress = getDataAddress();

        // preparing consuming transfer process
        var consumerAgreement = createContractAgreement(CONSUMING_CONTRACT_ID, UNKNOWN_ASSET_ID);
        createContractNegotiation(store, COUNTER_PARTY_ADDRESS, consumerAgreement, ContractNegotiation.Type.CONSUMER);
        createTransferProcess(UNKNOWN_ASSET_ID,
                CONSUMING_CONTRACT_ID,
                dataAddress,
                TransferProcess.Type.CONSUMER,
                CONSUMING_TRANSFER_PROCESS_ID,
                "2023-07-10",
                "",
                transferProcessStore);
    }

    private static DataAddress getDataAddress() {
        var dataAddress = DataAddress.Builder.newInstance()
                .type("HttpData")
                .property("baseUrl", DATA_SINK)
                .build();
        return dataAddress;
    }

    private static void createAsset(AssetService assetStore, DataAddress dataAddress, String assetId, String assetName) throws ParseException {
        var asset = Asset.Builder.newInstance()
                .id(assetId)
                .property("asset:prop:name", assetName)
                .createdAt(dateFormatterToLong("2023-06-01"))
                .build();

        assetStore.create(asset, dataAddress);
    }

    private static ContractAgreement createContractAgreement(
            String agreementId,
            String assetId
    ) {
        var agreement = ContractAgreement.Builder.newInstance()
                .id(agreementId)
                .providerAgentId(UUID.randomUUID().toString())
                .consumerAgentId(UUID.randomUUID().toString())
                .assetId(assetId)
                .policy(Policy.Builder.newInstance().build())
                .build();

        return agreement;
    }

    private static void createContractNegotiation(
            ContractNegotiationStore store,
            String counterPartyAddress,
            ContractAgreement agreement,
            ContractNegotiation.Type type
    ) {
        var negotiation = ContractNegotiation.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .counterPartyId(UUID.randomUUID().toString())
                .counterPartyAddress(counterPartyAddress)
                .protocol("protocol")
                .contractAgreement(agreement)
                .type(type)
                .correlationId(UUID.randomUUID().toString())
                .build();

        store.save(negotiation);
    }

    private static void createTransferProcess(String assetId, String contractId, DataAddress dataAddress, TransferProcess.Type type, String transferProcessId, String lastUpdateDateForTransferProcess, String errorMessage, TransferProcessStore transferProcessStore) throws ParseException {

        var dataRequestForTransfer = DataRequest.Builder.newInstance()
                .assetId(assetId)
                .contractId(contractId)
                .dataDestination(dataAddress)
                .build();

        var transferProcess = TransferProcess.Builder.newInstance()
                .id(transferProcessId)
                .type(type)
                .dataRequest(dataRequestForTransfer)
                .createdAt(dateFormatterToLong("2023-07-08"))
                .updatedAt(dateFormatterToLong(lastUpdateDateForTransferProcess))
                .state(800)
                .errorDetail(errorMessage)
                .build();

        transferProcessStore.save(transferProcess);
    }

    private static long dateFormatterToLong(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(date).getTime();
    }
}
