package globalConstants;

public class AuthorizationConstants {
    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";
    public static final String CARD_OWNER_OR_ADMIN =
            "hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, principal.id)";
    public static final String OWNER_OF_BOTH_CARDS =
            "@cardSecurity.isCardOwner(#transferRequest.fromCardId, principal.id) " +
                    "and @cardSecurity.isCardOwner(#transferRequest.toCardId, principal.id)";
    public static final String TRANSACTION_PARTICIPANT_OR_ADMIN_BY_ID =
            "hasRole('ADMIN') or @transactionSecurity.isTransactionParticipant(#id, principal.id)";
    public static final String TRANSACTION_PARTICIPANT_OR_ADMIN_BY_TRANSACTION_ID =
            "hasRole('ADMIN') or @transactionSecurity." +
                    "isTransactionParticipantByTransactionId(#transactionId, principal.id)";
    public static final String TRANSACTION_INITIATOR_OR_ADMIN =
            "hasRole('ADMIN') or @transactionSecurity.isTransactionInitiator(#id, principal.id)";
    public static final String OWNER_OR_ADMIN =
            "hasRole('ADMIN') or (hasRole('USER') and #id == principal.id)";

}
