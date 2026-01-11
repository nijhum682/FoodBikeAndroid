# FoodBikeAndroid

FoodBikeAndroid is a modern Android application for food delivery, designed with robust features and professional polish. Below is a comprehensive overview of all features and implementation details for this project.

## Features
Here are all the main features and functionalities of the FoodBikeAndroid project:

Functionalities include: User authentication, browsing restaurants and menus, placing and tracking orders, managing profiles, viewing order history, receiving push notifications, and rating restaurants/deliveries.

Auto-cancel system for pending orders: Automatically cancels orders older than 1 hour in the background, keeping order lists up-to-date.

Reusable loading overlays: Displays Material Design loading indicators during data fetches and operations for a smooth user experience.

Comprehensive error handling: Shows user-friendly error dialogs with retry options and consistent messaging across the app.

Beautiful empty state layouts: Presents custom illustrations and messages when lists (orders, restaurants, etc.) are empty.

Material Design confirmation dialogs: Prompts users for confirmation before logout, delete, or cancel actions to prevent accidental changes.

Real-time input validation: Validates required fields, email, phone, password, price, and more as users type, with instant error feedback.

Pull-to-refresh functionality: Allows users to refresh order lists and other data screens with a swipe gesture.

Enhanced session management: Automatically logs out users after 30 minutes of inactivity, supports “remember me” on login, and tracks user activity.

Complete Material Design theme: Consistent colors, styles, typography, and responsive layouts for a professional look and feel.

Professional splash screen: Displays app logo, tagline, and loading indicator on startup, with smooth transitions and authentication checks.

Secure session management: Protects user data with auto-logout and confirmation dialogs for sensitive actions.

Efficient background tasks: Uses WorkManager for reliable background operations like auto-cancel and session tracking.

Optimized database queries: Ensures fast data access and updates for orders, users, and restaurants.

Lazy loading and optimized layouts: Improves performance by loading resources only when needed and using efficient UI components.

Clean code architecture: Follows best practices, single responsibility principle, and uses reusable components for maintainability.

Accessibility-friendly: Designs UI for easy navigation and readability for all users.
