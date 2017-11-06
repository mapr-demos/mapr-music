import model.Rating;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RatingGenerator {

    /**
     * Maximum percent of documents, which can be rated by single user.
     */
    public static final int MAX_PERCENT_OF_RATED_DOCUMENTS = 20;

    private static final Random random = new Random();
    private static final Random rateRandom = new Random();

    private RatingGenerator() {
    }

    /**
     * Generates set of ratings according to the specified sets of documents' identifiers and users' identifiers.
     *
     * @param documentIds set of documents' identifiers.
     * @param userIds     set of users' identifiers.
     * @return set of ratings.
     */
    public static Set<Rating> generateRatings(Set<String> documentIds, Set<String> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User ids set can not be empty");
        }

        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Artist set can not be empty");
        }

        return userIds.stream()
                .flatMap(userId -> imitateUserBehaviour(userId, new HashSet<>(documentIds)).stream())
                .collect(Collectors.toSet());
    }

    private static Set<Rating> imitateUserBehaviour(String userId, Set<String> documentIdsModifiableSet) {

        Set<Rating> ratings = new HashSet<>();
        int bound = documentIdsModifiableSet.size() * MAX_PERCENT_OF_RATED_DOCUMENTS / 100;
        int documentsWillBeRatedByUser = random.nextInt(bound);
        for (int i = 0; i < documentsWillBeRatedByUser; i++) {

            String documentId = popRandomDocumentId(documentIdsModifiableSet);

            Rating rating = new Rating();
            rating.setId(UUID.randomUUID().toString());
            rating.setRating(getRandomRate());
            rating.setUserId(userId);
            rating.setDocumentId(documentId);

            ratings.add(rating);
        }

        return ratings;
    }

    private static String popRandomDocumentId(Set<String> documentIdsModifiableSet) {

        int index = random.nextInt(documentIdsModifiableSet.size());
        int i = 0;

        String result = null;
        for (String id : documentIdsModifiableSet) {
            if (i == index) {
                result = id;
                break;
            }
            i++;
        }

        documentIdsModifiableSet.remove(result);
        return result;
    }

    /**
     * Generates random rate in [0.5, 5] range with step of 0.5.
     *
     * @return random rate in [0.5, 5] range with step of 0.5.
     */
    private static double getRandomRate() {
        double randomValue = rateRandom.nextInt(10) + 1;
        return randomValue / 2;
    }

}
