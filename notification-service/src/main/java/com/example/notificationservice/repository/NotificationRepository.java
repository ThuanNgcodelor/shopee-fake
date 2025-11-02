package com.example.notificationservice.repository;

import com.example.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findAllByUserIdOrderByCreationTimestampDesc(String id);
    
    @Query("SELECT n FROM notifications n WHERE n.shopId = :shopId ORDER BY n.creationTimestamp DESC")
    List<Notification> findAllByShopIdOrderByCreationTimestampDesc(@Param("shopId") String shopId);
    
    // Get user notifications (isShopOwnerNotification = false)
    @Query("SELECT n FROM notifications n WHERE n.userId = :userId AND n.shopOwnerNotification = :shopOwnerNotification ORDER BY n.creationTimestamp DESC")
    List<Notification> findAllByUserIdAndShopOwnerNotificationOrderByCreationTimestampDesc(@Param("userId") String userId, @Param("shopOwnerNotification") boolean shopOwnerNotification);
    
    // Get shop owner notifications (isShopOwnerNotification = true)
    @Query("SELECT n FROM notifications n WHERE n.shopId = :shopId AND n.shopOwnerNotification = :shopOwnerNotification ORDER BY n.creationTimestamp DESC")
    List<Notification> findAllByShopIdAndShopOwnerNotificationOrderByCreationTimestampDesc(@Param("shopId") String shopId, @Param("shopOwnerNotification") boolean shopOwnerNotification);
}
