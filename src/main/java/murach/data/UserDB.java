package murach.data;

import murach.business.User;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class UserDB {

    private static final EntityManagerFactory emf = createEntityManagerFactory();

    private static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        putEnv(properties, "javax.persistence.jdbc.url", "DB_URL");
        putEnv(properties, "javax.persistence.jdbc.user", "DB_USER");
        putEnv(properties, "javax.persistence.jdbc.password", "DB_PASSWORD");

        return Persistence.createEntityManagerFactory("emailListPU", properties);
    }

    private static void putEnv(Map<String, String> properties, String propertyName, String envName) {
        String value = System.getenv(envName);
        if (value != null && !value.trim().isEmpty()) {
            properties.put(propertyName, value.trim());
        }
    }

    public static int insert(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user); // JPA tự insert vào DB
            tx.commit();
            return 1;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
            return 0;
        } finally {
            em.close();
        }
    }

    public static boolean emailExists(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
