package com.example.userservice.enums;

public enum ActivityType {
    // Shop Owner Registration & Setup
    SHOP_OWNER_REGISTERED("Shop Owner Registered", "User đăng ký thành shop owner"),
    SHOP_PROFILE_CREATED("Shop Profile Created", "Tạo profile shop"),
    SHOP_PROFILE_UPDATED("Shop Profile Updated", "Cập nhật profile shop"),
    
    // Verification Process
    VERIFICATION_REQUESTED("Verification Requested", "Yêu cầu xác thực shop"),
    VERIFICATION_APPROVED("Verification Approved", "Shop được xác thực"),
    VERIFICATION_REJECTED("Verification Rejected", "Shop bị từ chối xác thực"),
    
    // Follow/Unfollow Activities
    USER_FOLLOWED("User Followed", "User follow shop"),
    USER_UNFOLLOWED("User Unfollowed", "User unfollow shop"),
    FOLLOWERS_COUNT_UPDATED("Followers Count Updated", "Cập nhật số lượng followers"),
    
    // Rating Activities
    RATING_RECEIVED("Rating Received", "Nhận rating từ user"),
    RATING_UPDATED("Rating Updated", "Cập nhật rating"),
    TOTAL_RATINGS_UPDATED("Total Ratings Updated", "Cập nhật tổng rating"),
    
    // Shop Management
    SHOP_SETTINGS_CHANGED("Shop Settings Changed", "Thay đổi cài đặt shop"),
    SHOP_STATUS_CHANGED("Shop Status Changed", "Thay đổi trạng thái shop"),
    
    // Product Management (if integrated)
    PRODUCT_ADDED("Product Added", "Thêm sản phẩm"),
    PRODUCT_UPDATED("Product Updated", "Cập nhật sản phẩm"),
    PRODUCT_DELETED("Product Deleted", "Xóa sản phẩm"),
    
    // Order Management (if integrated)
    ORDER_RECEIVED("Order Received", "Nhận đơn hàng"),
    ORDER_PROCESSED("Order Processed", "Xử lý đơn hàng"),
    ORDER_COMPLETED("Order Completed", "Hoàn thành đơn hàng"),
    
    // Analytics & Reports
    ANALYTICS_VIEWED("Analytics Viewed", "Xem báo cáo phân tích"),
    REPORT_GENERATED("Report Generated", "Tạo báo cáo"),
    
    // System Activities
    LOGIN("Login", "Đăng nhập"),
    LOGOUT("Logout", "Đăng xuất"),
    PROFILE_VIEWED("Profile Viewed", "Xem profile"),
    DASHBOARD_ACCESSED("Dashboard Accessed", "Truy cập dashboard");
    
    private final String displayName;
    private final String description;
    
    ActivityType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
